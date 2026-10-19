package net.threetag.palladium.client.beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.renderer.LaserRenderer;
import net.threetag.palladium.client.renderer.RenderTypeRegistry;
import net.threetag.palladium.client.renderer.RenderTypeFunction;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class FlatTexturedBeamRenderer extends BeamRenderer {

    public static final MapCodec<FlatTexturedBeamRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextureReference.CODEC.fieldOf("texture").forGetter(r -> r.texture),
            LaserRenderer.SIZE_CODEC.optionalFieldOf("texture_size", new Vector2f(1 / 16F, 1 / 16F)).forGetter(r -> r.textureSize),
            RenderTypeRegistry.CODEC.optionalFieldOf("render_type", RenderTypeRegistry.ENTITY_TRANSLUCENT).forGetter(r -> r.renderType),
            ExtraCodecs.intRange(0, 15).optionalFieldOf("light_emission", 0).forGetter(r -> r.lightEmission)
    ).apply(instance, FlatTexturedBeamRenderer::new));

    private final TextureReference texture;
    private final Vector2f textureSize;
    private final RenderTypeFunction renderType;
    private final int lightEmission;

    public FlatTexturedBeamRenderer(TextureReference texture, Vector2f textureSize, RenderTypeFunction renderType, int lightEmission) {
        this.texture = texture;
        this.textureSize = textureSize;
        this.renderType = renderType;
        this.lightEmission = lightEmission;
    }

    @Override
    public void submit(DataContext context, Vec3 origin, Vec3 target, Vec2 sizeMultiplier, float lengthMultiplier, float opacityMultiplier, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLightIn, int ageInTicks, float partialTick) {
        var texture = this.texture.getTexture(context);

        if (texture == null) {
            return;
        }

        float width = this.textureSize.x * sizeMultiplier.x;
        float tileLength = this.textureSize.y * sizeMultiplier.y;
        float length = (float) (origin.distanceTo(target) * lengthMultiplier);

        if (width <= 0F || tileLength <= 0F || length <= 0F) {
            return;
        }

        poseStack.pushPose();
        RenderUtil.faceVec(poseStack, origin, target);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        Vector3f cameraPos = new Matrix4f(poseStack.last().pose()).invert().transformPosition(new Vector3f());
        poseStack.mulPose(Axis.YP.rotation((float) Mth.atan2(cameraPos.x, cameraPos.z)));

        var renderType = this.renderType.getRenderType(texture);
        var light = LightCoordsUtil.lightCoordsWithEmission(packedLightIn, this.lightEmission);
        var color = ARGB.white(opacityMultiplier);
        int segments = Mth.ceil(length / tileLength);

        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            float halfWidth = width / 2F;

            for (int i = 0; i < segments; i++) {
                float minY = i * tileLength;
                float fraction = Math.min((length - minY) / tileLength, 1F);
                float maxY = minY + tileLength * fraction;
                float minV = 1 - fraction;

                vertexConsumer.addVertex(pose, -halfWidth, minY, 0).setUv(0, 1).setColor(color).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
                vertexConsumer.addVertex(pose, halfWidth, minY, 0).setUv(1, 1).setColor(color).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
                vertexConsumer.addVertex(pose, halfWidth, maxY, 0).setUv(1, minV).setColor(color).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
                vertexConsumer.addVertex(pose, -halfWidth, maxY, 0).setUv(0, minV).setColor(color).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            }
        });

        poseStack.popPose();
    }

    @Override
    public BeamRendererSerializer<?> getSerializer() {
        return BeamRendererSerializers.FLAT_TEXTURED;
    }

    public static class Serializer extends BeamRendererSerializer<FlatTexturedBeamRenderer> {

        @Override
        public MapCodec<FlatTexturedBeamRenderer> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BeamRenderer, FlatTexturedBeamRenderer> builder, HolderLookup.Provider provider) {
            builder.setName("Flat Textured")
                    .setDescription("Renders a beam between two points as a flat texture that always faces the camera. The texture is repeated along the beam and never stretched; the last repetition is cut off if the remaining length is shorter than one tile.")
                    .add("texture", TYPE_TEXTURE_REFERENCE, "The texture to use for the beam.")
                    .addOptional("texture_size", TYPE_FLOAT, "The size of one texture tile: width of the beam and length of one repetition along it.", 1F)
                    .addOptional("render_type", SettingType.enumList(RenderTypeRegistry.types().stream().map(Identifier::toString).toList()), "The render type to render the beam with.", RenderTypeRegistry.getKey(RenderTypeRegistry.ENTITY_TRANSLUCENT))
                    .addOptional("light_emission", TYPE_INT, "The light emission of the beam. Must be within 0 - 15", 0)
                    .addExampleObject(new FlatTexturedBeamRenderer(
                            TextureReference.normal(Identifier.fromNamespaceAndPath("namespace", "textures/beams/example_beam.png")),
                            new Vector2f(2 / 16F, 2 / 16F),
                            RenderTypeRegistry.ENTITY_TRANSLUCENT,
                            0
                    ));
        }
    }
}
