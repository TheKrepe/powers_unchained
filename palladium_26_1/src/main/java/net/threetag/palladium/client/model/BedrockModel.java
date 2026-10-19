package net.threetag.palladium.client.model;

import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public record BedrockModel(List<Geometry> geometries) {

    public static final Codec<UVPair> UV_PAIR_CODEC = Codec.FLOAT.listOf(2, 2).xmap(floats -> new UVPair(floats.getFirst(), floats.getLast()), uvPair -> List.of(uvPair.u(), uvPair.v()));

    public static final Codec<BedrockModel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Geometry.CODEC.listOf().fieldOf("minecraft:geometry").forGetter(BedrockModel::geometries)
    ).apply(instance, BedrockModel::new));

    public LayerDefinition buildLayerDefinition() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        var geometry = this.geometries.getFirst();
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

            // If .put() returned something, it was the previous part assigned to that key (meaning a duplicate)
            if (cache.put(bone.name, new PartCache(bone.name, bone.parent, bone.pivot, bone.rotation, bone.scale, plainCubes)) != null) {
                throw new JsonParseException("Duplicate bone name '" + bone.name + "'. Bone names must be unique.");
            }

            for (int i = 0; i < rotatedCubes.size(); i++) {
                Cube cube = rotatedCubes.get(i);
                String childName = bone.name + "_cube_" + i;

                // Similar check for duplicates here. Without it, cubes are getting overwritten
                if (cache.put(childName, new PartCache(childName, bone.name, new Vector3f(cube.pivot), new Vector3f(cube.rotation), 1F, List.of(cube))) != null) {
                    throw new JsonParseException("Duplicate bone name '" + childName + "'. Bone names must be unique.");
                }

            }
        }

        // Validate parents
        for (Map.Entry<String, PartCache> e : cache.entrySet()) {
            if (e.getValue().parentName != null) {
                if (e.getValue().parentName.equals(e.getKey())) {
                    throw new JsonParseException("Bone '" + e.getKey() + "' cannot be its own parent.");
                }
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
            boolean modified = false;
            var copy = new HashMap<>(cache);

            for (Map.Entry<String, PartCache> e : copy.entrySet()) {
                if (e.getValue().parentName == null) {
                    convertHierarchy(e.getValue(), Collections.newSetFromMap(new IdentityHashMap<>()));
                    modelParts.put(e.getKey(), e.getValue().add(root));
                    cache.remove(e.getKey());
                    modified = true;
                } else {
                    var parent = modelParts.get(e.getValue().parentName);

                    if (parent != null) {
                        modelParts.put(e.getKey(), e.getValue().add(parent));
                        cache.remove(e.getKey());
                        modified = true;
                    }
                }
            }

            /*
             * If no changes were detected, it means there is a cyclic dependency in the remaining children.
             * This prints out the list of remaining children, so a developer can identify the loop.
             */
            if (!modified) {
                StringBuilder remaining = new StringBuilder();
                for (Map.Entry<String, PartCache> stuck : cache.entrySet()) {
                    if (!remaining.isEmpty()) {
                        remaining.append(", ");
                    }

                    remaining.append(stuck.getKey()).append(" -> ").append(stuck.getValue().parentName);
                }

                throw new JsonParseException("Cyclic or unresolvable bone parents detected: " + remaining);
            }
        }

        return LayerDefinition.create(meshDefinition, textureWidth, textureHeight);
    }

    // Now uses a visiting set to track what parts have been evaluated.
    // Mainly for extreme edge cases
    private static void convertHierarchy(PartCache part, Set<PartCache> visiting) {
        if (!visiting.add(part)) {
            throw new JsonParseException("Cyclic bone hierarchy detected at '" + part.name + "'");
        }

        part.convert();
        for (PartCache child : part.children) {
            convertHierarchy(child, visiting);
        }
        visiting.remove(part);
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
        private final float scale;
        private List<Cube> cubes;

        public PartCache(String name, @Nullable String parentName, Vector3f pivot, Vector3f rotation, float scale, List<Cube> cubes) {
            this.name = name;
            this.parentName = parentName;
            this.unconvertedPivot = pivot;
            this.pivot = pivot;
            this.rotation = rotation;
            this.scale = scale;
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
                                    (float) Math.toRadians(this.rotation.z))
                            .withScale(this.scale));
        }

        public PartCache convert() {
            this.cubes = this.cubes.stream().map(cube -> cube.convert(this.pivot, this.name)).toList();
            this.pivot.mul(1, -1, 1).add(0, 24, 0);
            var parent = this.parent;

            Set<PartCache> seen = Collections.newSetFromMap(new IdentityHashMap<>());

            while (parent != null) {
                if (!seen.add(parent)) {
                    throw new JsonParseException("Cyclic bone parent chain at '" + this.name + "'");
                }
                this.pivot.sub(parent.unconvertedPivot);
                parent = parent.parent;
            }

            return this;
        }
    }

    private record Geometry(Description description, List<Bone> bones) {

        public static final Codec<Geometry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Description.CODEC.fieldOf("description").forGetter(Geometry::description),
                Bone.CODEC.listOf().optionalFieldOf("bones", Collections.emptyList()).forGetter(Geometry::bones)
        ).apply(instance, Geometry::new));

    }

    private record Description(int textureWidth, int textureHeight) {

        public static final Codec<Description> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("texture_width").forGetter(Description::textureWidth),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("texture_height").forGetter(Description::textureHeight)
        ).apply(instance, Description::new));

    }

    private record Bone(String name, @Nullable String parent, Vector3f pivot, Vector3f rotation, float scale,
                        List<Cube> cubes) {

        public static final Codec<Bone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Bone::name),
                Codec.STRING.optionalFieldOf("parent").forGetter(b -> Optional.ofNullable(b.parent())),
                PalladiumCodecs.VECTOR_3F_CODEC.optionalFieldOf("pivot", new Vector3f(0, 0, 0)).forGetter(Bone::pivot),
                PalladiumCodecs.VECTOR_3F_CODEC.optionalFieldOf("rotation", new Vector3f(0, 0, 0)).forGetter(Bone::rotation),
                Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(Bone::scale),
                Cube.CODEC.listOf().optionalFieldOf("cubes", Collections.emptyList()).forGetter(Bone::cubes)
        ).apply(instance, (name, parent, pivot, rot, scale, cubes) -> new Bone(name, parent.orElse(null), pivot, rot, scale, cubes)));

    }

    private record Cube(Vector3f origin, Vector3f size, Vector3f pivot, Vector3f rotation, float inflate,
                        boolean mirror, UV uv) {

        public static final Codec<UV> UV_CODEC = Codec.either(BoxUV.CODEC, PerFaceUV.CODEC).xmap(either -> either.map(uv -> uv, uv -> uv), uv -> uv instanceof BoxUV ? Either.left((BoxUV) uv) : Either.right((PerFaceUV) uv));

        public static final Codec<Cube> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PalladiumCodecs.VECTOR_3F_CODEC.fieldOf("origin").forGetter(Cube::origin),
                PalladiumCodecs.VECTOR_3F_CODEC.fieldOf("size").forGetter(Cube::size),
                PalladiumCodecs.VECTOR_3F_CODEC.optionalFieldOf("pivot", new Vector3f(0, 0, 0)).forGetter(Cube::pivot),
                PalladiumCodecs.VECTOR_3F_CODEC.optionalFieldOf("rotation", new Vector3f(0, 0, 0)).forGetter(Cube::rotation),
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

    private static class BoxUV extends UV {

        public static final Codec<BoxUV> CODEC = UV_PAIR_CODEC.xmap(BoxUV::new, boxUV -> boxUV.uvPair);

        private final UVPair uvPair;

        private BoxUV(UVPair uvPair) {
            this.uvPair = uvPair;
        }
    }

    private static class PerFaceUV extends UV {

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

    private static abstract class UV {

    }

}
