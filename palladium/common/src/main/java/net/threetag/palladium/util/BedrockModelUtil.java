package net.threetag.palladium.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.client.model.ExtendedCubeListBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public class BedrockModelUtil {

    public static LayerDefinition parseAsLayerDefinition(JsonObject json) {
        return BedrockModel.CODEC.parse(JsonOps.INSTANCE, json)
                .getOrThrow(false, err -> {
                    throw new JsonParseException(err);
                })
                .buildLayerDefinition();
    }

    public static JsonObject toJsonModel(LayerDefinition layerDefinition, @Nullable String identifier) {
        JsonObject json = new JsonObject();
        json.addProperty("format_version", "1.12.0");
        var geometryArray = new JsonArray();
        var geometry = new JsonObject();

        // Description
        var description = new JsonObject();
        description.addProperty("identifier", identifier != null ? identifier : "geometry.unknown");
        description.addProperty("texture_width", layerDefinition.material.xTexSize);
        description.addProperty("texture_height", layerDefinition.material.yTexSize);
        geometry.add("description", description);

        // Bones
        var bones = new JsonArray();
        layerDefinition.mesh.getRoot().children.forEach((s, part) -> addBoneToArray(bones, s, part, new ArrayList<>(), null));
        geometry.add("bones", bones);

        geometryArray.add(geometry);
        json.add("minecraft:geometry", geometryArray);
        return json;
    }

    private static void addBoneToArray(JsonArray array, String name, PartDefinition part, List<PartDefinition> parents, @Nullable String parent) {
        var json = new JsonObject();

        json.addProperty("name", name);
        if (parent != null) {
            json.addProperty("parent", parent);
        }
        var fixedPivot = new Vector3f(part.partPose.x, part.partPose.y, part.partPose.z);
        for (PartDefinition parentPart : parents) {
            fixedPivot.add(parentPart.partPose.x, parentPart.partPose.y, parentPart.partPose.z);
        }
        fixedPivot.sub(0, 24, 0).mul(1, -1, 1);
        json.add("pivot", vec3ToJsonArray(fixedPivot));
        json.add("rotation", vec3ToJsonArray(Math.toDegrees(part.partPose.xRot), Math.toDegrees(part.partPose.yRot), Math.toDegrees(part.partPose.zRot)));

        var cubes = new JsonArray();

        for (CubeDefinition c : part.cubes) {
            var cube = new JsonObject();
            var fixedOrigin = new Vector3f().set(c.origin).add(fixedPivot.x, 0, fixedPivot.z);
            fixedOrigin.y = (fixedOrigin.y + c.dimensions.y - fixedPivot.y) * -1;
            cube.add("origin", vec3ToJsonArray(fixedOrigin));
            cube.add("size", vec3ToJsonArray(c.dimensions));
            cube.addProperty("inflate", (c.grow.growX + c.grow.growY + c.grow.growZ) / 3D);

            if (c instanceof ExtendedCubeListBuilder.PerFaceCubeDefinition perFace) {
                var uv = new JsonObject();
                for (Map.Entry<Direction, ExtendedCubeListBuilder.PerFaceUV> e : perFace.uvMap.entrySet()) {
                    var face = new JsonObject();
                    var direction = e.getKey();
                    face.add("uv", uvPairToJsonArray(e.getValue().uv()));
                    face.add("uv_size", uvPairToJsonArray(e.getValue().size()));
                    uv.add(direction.getAxis() == Direction.Axis.Z ? direction.getName() : direction.getOpposite().getName(), face);
                }
                cube.add("uv", uv);
            } else {
                cube.add("uv", uvPairToJsonArray(c.texCoord));
            }

            cube.addProperty("mirror", c.mirror);
            cubes.add(cube);
        }

        json.add("cubes", cubes);
        array.add(json);
        List<PartDefinition> newParents = new ArrayList<>(parents);
        newParents.add(part);
        part.children.forEach((n, p) -> addBoneToArray(array, n, p, newParents, name));
    }

    private static JsonArray vec3ToJsonArray(Vector3f vec) {
        var array = new JsonArray();
        array.add(vec.x);
        array.add(vec.y);
        array.add(vec.z);
        return array;
    }

    private static JsonArray vec3ToJsonArray(double x, double y, double z) {
        var array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private static JsonArray uvPairToJsonArray(UVPair uv) {
        var array = new JsonArray();
        array.add(uv.u());
        array.add(uv.v());
        return array;
    }

    public record BedrockModel(List<Geometry> geometries) {

        private static final Codec<Vector3f> VECTOR_3F_CODEC = Codec.FLOAT.listOf().comapFlatMap(
                list -> Util.fixedSize(list, 3).map(l -> new Vector3f(l.get(0), l.get(1), l.get(2))),
                v -> List.of(v.x(), v.y(), v.z()));

        public static final Codec<UVPair> UV_PAIR_CODEC = Codec.FLOAT.listOf().comapFlatMap(
                list -> Util.fixedSize(list, 2).map(l -> new UVPair(l.get(0), l.get(1))),
                uvPair -> List.of(uvPair.u(), uvPair.v()));

        public static final Codec<BedrockModel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Geometry.CODEC.listOf().fieldOf("minecraft:geometry").forGetter(BedrockModel::geometries)
        ).apply(instance, BedrockModel::new));

        public LayerDefinition buildLayerDefinition() {
            MeshDefinition meshDefinition = new MeshDefinition();
            PartDefinition root = meshDefinition.getRoot();
            var geometry = this.geometries.get(0);
            int textureWidth = geometry.description.textureWidth;
            int textureHeight = geometry.description.textureHeight;

            Map<String, PartCache> cache = new HashMap<>();
            Map<String, PartDefinition> modelParts = new HashMap<>();

            for (Bone bone : geometry.bones) {
                List<Cube> plainCubes = new ArrayList<>();
                List<Cube> rotatedCubes = new ArrayList<>();
                for (Cube cube : bone.cubes) {
                    (cube.hasRotation() ? rotatedCubes : plainCubes).add(cube);
                }

                cache.put(bone.name, new PartCache(bone.name, bone.parent, bone.pivot, bone.rotation, plainCubes));

                for (int i = 0; i < rotatedCubes.size(); i++) {
                    Cube cube = rotatedCubes.get(i);
                    String childName = bone.name + "_cube_" + i;
                    cache.put(childName, new PartCache(childName, bone.name, new Vector3f(cube.pivot), new Vector3f(cube.rotation), List.of(cube)));
                }
            }

            // Validate parents
            for (Map.Entry<String, PartCache> e : cache.entrySet()) {
                if (e.getValue().parentName != null) {
                    if (!cache.containsKey(e.getValue().parentName)) {
                        throw new JsonParseException("Unknown parent '" + e.getValue().parentName + "'");
                    } else {
                        e.getValue().parent = cache.get(e.getValue().parentName);
                        cache.get(e.getValue().parentName).children.add(e.getValue());
                    }
                }
            }

            // Organize hierarchy
            while (!cache.isEmpty()) {
                var copy = new HashMap<>(cache);

                for (Map.Entry<String, PartCache> e : copy.entrySet()) {
                    if (e.getValue().parentName == null) {
                        convertHierarchy(e.getValue());
                        modelParts.put(e.getKey(), e.getValue().add(root));
                        cache.remove(e.getKey());
                    } else {
                        var parent = modelParts.get(e.getValue().parentName);

                        if (parent != null) {
                            modelParts.put(e.getKey(), e.getValue().add(parent));
                            cache.remove(e.getKey());
                        }
                    }
                }
            }

            return LayerDefinition.create(meshDefinition, textureWidth, textureHeight);
        }

        private static void convertHierarchy(PartCache part) {
            part.convert();
            for (PartCache child : part.children) {
                convertHierarchy(child);
            }
        }

        public static class PartCache {

            private final String name;
            @Nullable
            public final String parentName;
            public PartCache parent;
            public List<PartCache> children = new ArrayList<>();
            private final Vector3f unconvertedPivot;
            private final Vector3f pivot;
            private final Vector3f rotation;
            private List<Cube> cubes;

            public PartCache(String name, @Nullable String parentName, Vector3f pivot, Vector3f rotation, List<Cube> cubes) {
                this.name = name;
                this.parentName = parentName;
                this.unconvertedPivot = pivot;
                this.pivot = pivot;
                this.rotation = rotation;
                this.cubes = cubes;
            }

            public PartDefinition add(PartDefinition parent) {
                var builder = ExtendedCubeListBuilder.create();

                for (Cube cube : this.cubes) {
                    cube.addToBuilder(builder);
                }

                return parent.addOrReplaceChild(this.name, builder,
                        PartPose.offsetAndRotation(
                                this.pivot.x, this.pivot.y, this.pivot.z,
                                (float) Math.toRadians(this.rotation.x),
                                (float) Math.toRadians(this.rotation.y),
                                (float) Math.toRadians(this.rotation.z)));
            }

            public PartCache convert() {
                this.cubes = this.cubes.stream().map(cube -> cube.convert(this.pivot, this.name)).toList();
                this.pivot.mul(1, -1, 1).add(0, 24, 0);
                var parent = this.parent;

                while (parent != null) {
                    this.pivot.sub(parent.unconvertedPivot);
                    parent = parent.parent;
                }

                return this;
            }
        }

        public record Geometry(Description description, List<Bone> bones) {

            public static final Codec<Geometry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Description.CODEC.fieldOf("description").forGetter(Geometry::description),
                    Bone.CODEC.listOf().optionalFieldOf("bones", Collections.emptyList()).forGetter(Geometry::bones)
            ).apply(instance, Geometry::new));

        }

        public record Description(int textureWidth, int textureHeight) {

            public static final Codec<Description> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("texture_width").forGetter(Description::textureWidth),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("texture_height").forGetter(Description::textureHeight)
            ).apply(instance, Description::new));

        }

        public record Bone(String name, @Nullable String parent, Vector3f pivot, Vector3f rotation,
                           List<Cube> cubes) {

            public static final Codec<Bone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(Bone::name),
                    Codec.STRING.optionalFieldOf("parent").forGetter(b -> Optional.ofNullable(b.parent())),
                    VECTOR_3F_CODEC.optionalFieldOf("pivot", new Vector3f(0, 0, 0)).forGetter(Bone::pivot),
                    VECTOR_3F_CODEC.optionalFieldOf("rotation", new Vector3f(0, 0, 0)).forGetter(Bone::rotation),
                    Cube.CODEC.listOf().optionalFieldOf("cubes", Collections.emptyList()).forGetter(Bone::cubes)
            ).apply(instance, (name, parent, pivot, rot, cubes) -> new Bone(name, parent.orElse(null), pivot, rot, cubes)));

        }

        public record Cube(Vector3f origin, Vector3f size, Vector3f pivot, Vector3f rotation, float inflate,
                           boolean mirror, UV uv) {

            public static final Codec<UV> UV_CODEC = Codec.either(BoxUV.CODEC, PerFaceUV.CODEC).xmap(either -> either.map(uv -> uv, uv -> uv), uv -> uv instanceof BoxUV ? Either.left((BoxUV) uv) : Either.right((PerFaceUV) uv));

            public static final Codec<Cube> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    VECTOR_3F_CODEC.fieldOf("origin").forGetter(Cube::origin),
                    VECTOR_3F_CODEC.fieldOf("size").forGetter(Cube::size),
                    VECTOR_3F_CODEC.optionalFieldOf("pivot", new Vector3f(0, 0, 0)).forGetter(Cube::pivot),
                    VECTOR_3F_CODEC.optionalFieldOf("rotation", new Vector3f(0, 0, 0)).forGetter(Cube::rotation),
                    Codec.FLOAT.optionalFieldOf("inflate", 0.0F).forGetter(Cube::inflate),
                    Codec.BOOL.optionalFieldOf("mirror", false).forGetter(Cube::mirror),
                    UV_CODEC.fieldOf("uv").forGetter(Cube::uv)
            ).apply(instance, Cube::new));

            public boolean hasRotation() {
                return this.rotation.x != 0 || this.rotation.y != 0 || this.rotation.z != 0;
            }

            public void addToBuilder(ExtendedCubeListBuilder builder) {
                if (this.uv instanceof BoxUV boxUV) {
                    builder.mirror(this.mirror).texOffs((int) boxUV.uvPair.u(), (int) boxUV.uvPair.v()).addBox(
                            this.origin.x, this.origin.y, this.origin.z,
                            this.size.x, this.size.y, this.size.z,
                            new CubeDeformation(this.inflate));
                } else if (this.uv instanceof PerFaceUV perFaceUV) {
                    var cubeBuilder = builder.addPerFaceUVCube().origin(this.origin).dimensions(this.size).grow(this.inflate);
                    for (Map.Entry<Direction, PerFaceUV.PerFaceUVObject> e : Objects.requireNonNull(perFaceUV.uvMap).entrySet()) {
                        var direction = e.getKey().getAxis() == Direction.Axis.Z ? e.getKey() : e.getKey().getOpposite();
                        cubeBuilder.addFace(direction, e.getValue().uv(), e.getValue().uvSize());
                    }
                    cubeBuilder.build();
                }
            }

            public Cube convert(Vector3f pivot, String name) {
                var copy = new Vector3f(this.origin);
                copy.sub(pivot.x, 0, pivot.z);
                copy.y = pivot.y - copy.y - this.size.y;
                return new Cube(copy, this.size, this.pivot, this.rotation, this.inflate, this.mirror, this.uv);
            }

        }

        public static class BoxUV extends UV {

            public static final Codec<BoxUV> CODEC = UV_PAIR_CODEC.xmap(BoxUV::new, boxUV -> boxUV.uvPair);

            private final UVPair uvPair;

            private BoxUV(UVPair uvPair) {
                this.uvPair = uvPair;
            }
        }

        public static class PerFaceUV extends UV {

            private record PerFaceUVObject(UVPair uv, UVPair uvSize) {
                public static final Codec<PerFaceUVObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        UV_PAIR_CODEC.optionalFieldOf("uv", new UVPair(0, 0)).forGetter(PerFaceUVObject::uv),
                        UV_PAIR_CODEC.optionalFieldOf("uv_size", new UVPair(0, 0)).forGetter(PerFaceUVObject::uvSize)
                ).apply(instance, PerFaceUVObject::new));
            }

            public static final Codec<PerFaceUV> CODEC = Codec.unboundedMap(Direction.CODEC, PerFaceUVObject.CODEC).xmap(PerFaceUV::new, perFaceUV -> perFaceUV.uvMap);

            private final Map<Direction, PerFaceUVObject> uvMap;

            private PerFaceUV(Map<Direction, PerFaceUVObject> uvMap) {
                this.uvMap = uvMap;
            }

        }

        public static abstract class UV {

        }

    }

}
