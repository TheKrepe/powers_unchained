package net.threetag.palladium.client.renderer.entity.layer.pack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.model.LeggedModel;
import net.threetag.palladium.client.model.ModelPartExtension;
import net.threetag.palladium.client.model.PalladiumModelLayers;
import net.threetag.palladium.client.model.ThrusterModel;
import net.threetag.palladium.client.renderer.PalladiumRenderTypes;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.client.util.ModelUtil;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PalladiumCodecs;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;

public class ThrusterPackRenderLayer extends PackRenderLayer<ThrusterPackRenderLayer.LayerState> {

    public static final List<String> DEFAULT_ANCHORS = List.of("right_arm", "left_arm", "right_leg", "left_leg");
    public static final Identifier TEXTURE = Palladium.id("textures/entity/thruster.png");
    public static final Identifier BLOOM_TEXTURE = Palladium.id("textures/entity/thruster_bloom.png");
    private static final Color DEFAULT_COLOR = new Color(234, 182, 43);

    public static final MapCodec<ThrusterPackRenderLayer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextureReference.CODEC.optionalFieldOf("texture", TextureReference.normal(TEXTURE)).forGetter(l -> l.texture),
            PalladiumCodecs.SIMPLE_VEC3_CODEC.optionalFieldOf("scale", new Vec3(1, 1, 1)).forGetter(l -> l.scale),
            ExtraCodecs.compactListCodec(Codec.STRING).optionalFieldOf("anchors", DEFAULT_ANCHORS).forGetter(l -> l.anchors),
            PalladiumCodecs.COLOR_CODEC.optionalFieldOf("color", DEFAULT_COLOR).forGetter(l -> l.color),
            TextureReference.CODEC.optionalFieldOf("bloom_texture", TextureReference.normal(BLOOM_TEXTURE)).forGetter(l -> l.bloomTexture),
            PalladiumCodecs.COLOR_CODEC.optionalFieldOf("bloom_color", DEFAULT_COLOR).forGetter(l -> l.bloomColor),
            Codec.DOUBLE.optionalFieldOf("bloom_scale", 5D).forGetter(l -> l.bloomScale),
            propertiesCodec()
    ).apply(instance, ThrusterPackRenderLayer::new));

    private ThrusterModel model;
    private final TextureReference texture;
    private final Vec3 scale;
    private final List<String> anchors;
    private final Color color;
    private final TextureReference bloomTexture;
    private final Color bloomColor;
    private final double bloomScale;

    public ThrusterPackRenderLayer(TextureReference texture, Vec3 scale, List<String> anchors, Color color, TextureReference bloomTexture, Color bloomColor, double bloomScale, PackRenderLayerProperties properties) {
        super(properties);
        this.texture = texture;
        this.scale = scale;
        this.anchors = anchors;
        this.color = color;
        this.bloomTexture = bloomTexture;
        this.bloomColor = bloomColor;
        this.bloomScale = bloomScale;
    }

    private void buildModel() {
        this.model = new ThrusterModel(Minecraft.getInstance().getEntityModels().bakeLayer(PalladiumModelLayers.THRUSTER));
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, EntityModel<LivingEntityRenderState> parentModel, LivingEntityRenderState entityState, LayerState layerState, int packedLight, float partialTick, float xRot, float yRot) {
        if (this.model == null) {
            this.buildModel();
        }

        var prState = entityState.getRenderData(PalladiumRenderStateKeys.POWER_RENDERER);
        Object anchor = prState != null && prState.model() != null && prState.showModel() ? prState.model() : parentModel;
        if (!(anchor instanceof ArmedModel) || !(anchor instanceof LeggedModel)) {
            return;
        }

        if (layerState.texture == null) {
            return;
        }

        float age = entityState.ageInTicks;
        float propulsion = Math.max(entityState.getRenderDataOrDefault(PalladiumRenderStateKeys.FLIGHT_PROPULSION_SCALE, 0F), 0F) / 2F;
        float transition = layerState.getTransition(partialTick);

        for (int i = 0; i < this.anchors.size(); i++) {
            String partName = this.anchors.get(i);
            ModelPart part = ModelUtil.getPartFromModel(parentModel, partName);

            if (part != null) {
                poseStack.pushPose();
                ModelUtil.translateRotateFullPath(parentModel, partName, poseStack);
                ModelPart thruster = this.model.thruster;
                thruster.resetPose();
                PartPose pose = thruster.getInitialPose();
                poseStack.translate(pose.x() / 16.0F, pose.y() / 16.0F - 21F / 16F, pose.z() / 16.0F);

                if (((Object) part) instanceof ModelPartExtension ext) {
                    var bb = ext.palladium$getBoundingBox();
                    poseStack.translate(bb.minX / 16F + bb.getXsize() / 32F, bb.minY / 16F +  bb.getYsize() / 16F, bb.minZ / 16F + bb.getZsize() / 32F);
                }

                thruster.x = 0.0F;
                thruster.y = 0.0F;
                thruster.z = 0.0F;
                Vector3f scale = ThrusterModel.pulseScale(i, age, propulsion);

                if (this.bloomScale > 0 && layerState.bloomTexture != null) {
                    this.submitBloom(poseStack, submitNodeCollector, layerState.bloomTexture, scale, transition, propulsion);
                }

                poseStack.scale((float) (scale.x * transition * this.scale.x), (float) (scale.y * transition * this.scale.y), (float) (scale.z * transition * this.scale.z));

                submitNodeCollector.submitModelPart(thruster, poseStack, PalladiumRenderTypes.entityAdditive(layerState.texture), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, this.color.getRGB(), null);

                poseStack.popPose();
            }
        }
    }

    private void submitBloom(PoseStack poseStack, SubmitNodeCollector collector, Identifier texture, Vector3f pulse, float transition, float propulsion) {
        AABB bb = ((ModelPartExtension) (Object) this.model.thruster).palladium$getBoundingBox();

        poseStack.pushPose();
        var center = bb.getCenter();
        poseStack.translate(center.x / 16F, center.y / 16F, center.z / 16F);

        float partHalf = (float) Math.max(bb.getXsize(), bb.getZsize()) / 32F;
        float pulseAvg = (pulse.x + pulse.z) / 2F;
        float userAvg = (float) (this.scale.x + this.scale.z) / 2F;
        float half = (float) (partHalf * this.bloomScale * transition * pulseAvg * userAvg * (1F + propulsion));
        if (half <= 0F) {
            poseStack.popPose();
            return;
        }

        Vector3f camLocal = new Matrix4f(poseStack.last().pose()).invert().transformPosition(new Vector3f());
        if (camLocal.lengthSquared() > 1.0E-6F) {
            poseStack.mulPose(new Quaternionf().rotateTo(new Vector3f(0F, 0F, 1F), camLocal.normalize()));
        }

        int argb = applyAlpha(this.bloomColor.getRGB(), transition);
        float s = half;
        collector.submitCustomGeometry(poseStack, PalladiumRenderTypes.entityAdditive(texture),
                (pose, vc) -> bloomQuad(pose, vc, s, argb));

        poseStack.popPose();
    }

    private static void bloomQuad(PoseStack.Pose pose, VertexConsumer vc, float s, int argb) {
        vc.addVertex(pose, -s, -s, 0F).setUv(0F, 1F).setColor(argb).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        vc.addVertex(pose, s, -s, 0F).setUv(1F, 1F).setColor(argb).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        vc.addVertex(pose, s, s, 0F).setUv(1F, 0F).setColor(argb).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        vc.addVertex(pose, -s, s, 0F).setUv(0F, 0F).setColor(argb).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
    }

    private static int applyAlpha(int argb, float factor) {
        int a = Math.round(((argb >>> 24) & 0xFF) * Mth.clamp(factor, 0F, 1F));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    @Override
    public void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, HumanoidArm arm, ModelPart armPart, AvatarRenderer<?> playerRenderer, LayerState layerState, int packedLight) {
        // Thrusters are not rendered on the first-person arm.
    }

    @Override
    public PackRenderLayerSerializer<?> getSerializer() {
        return PackRenderLayerSerializers.THRUSTERS;
    }

    @Override
    public LayerState createState(DataContext context) {
        return new LayerState(this, context);
    }

    public static class LayerState extends PackRenderLayer.State {

        private static final int TRANSITION_DURATION = 4;

        public Identifier texture = null;
        public Identifier bloomTexture = null;

        private int transition = 0;
        private int prevTransition = 0;

        public LayerState(ThrusterPackRenderLayer layer, DataContext context) {
            super(layer, context);
        }

        @Override
        public void extractRenderState(PackRenderLayer<?> layer, DataContext context) {
            super.extractRenderState(layer, context);
            if (!(layer instanceof ThrusterPackRenderLayer thrusterLayer)) return;
            this.texture = thrusterLayer.texture.getTexture(context);
            this.bloomTexture = thrusterLayer.bloomTexture.getTexture(context);
        }

        @Override
        public void tick(LivingEntity entity) {
            super.tick(entity);
            this.prevTransition = this.transition;

            if (super.isMarkedForRemoval()) {
                this.transition = Math.max(this.transition - 1, 0);
            } else {
                this.transition = Math.min(this.transition + 1, TRANSITION_DURATION);
            }
        }

        public float getTransition(float partialTick) {
            float progress = Mth.clamp(Mth.lerp(partialTick, this.prevTransition, this.transition) / TRANSITION_DURATION, 0F, 1F);
            return 1F - (1F - progress) * (1F - progress);
        }

        @Override
        public boolean isMarkedForRemoval() {
            return super.isMarkedForRemoval() && this.transition <= 0;
        }
    }

    public static class Serializer extends PackRenderLayerSerializer<ThrusterPackRenderLayer> {

        @Override
        public MapCodec<ThrusterPackRenderLayer> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<PackRenderLayer<? extends State>, ThrusterPackRenderLayer> builder, HolderLookup.Provider provider) {
            builder.setName("Thrusters")
                    .setDescription("Renders thrusters on the end of each limb of the player.")
                    .addOptional("texture", TYPE_ANY_TEXTURE, "The texture to render the thrusters with.", TEXTURE.toString())
                    .addOptional("scale", TYPE_VECTOR3, "Scale for the thruster", new int[] {1, 1, 1})
                    .addOptional("anchors", TYPE_STRING_ARRAY, "Bones of the player model where the thruster will be attached to", DEFAULT_ANCHORS.toArray())
                    .add("color", TYPE_COLOR, "The color of the thrusters.")
                    .addOptional("bloom_texture", TYPE_ANY_TEXTURE, "The texture of the camera-facing bloom rendered at each thruster.", BLOOM_TEXTURE.toString())
                    .addOptional("bloom_color", TYPE_COLOR, "The color of the bloom.", String.format("#%06X", DEFAULT_COLOR.getRGB() & 0xFFFFFF))
                    .addOptional("bloom_scale", TYPE_DOUBLE, "Size multiplier for the bloom, relative to the thruster part. Set to 0 to disable.", 1.5)
                    .addExampleObject(new ThrusterPackRenderLayer(
                            TextureReference.normal(TEXTURE),
                            new Vec3(1, 1, 1),
                            DEFAULT_ANCHORS,
                            Color.WHITE,
                            TextureReference.normal(BLOOM_TEXTURE),
                            Color.WHITE,
                            1.5,
                            PackRenderLayerProperties.DEFAULT
                    ))
                    .addExampleObject(new ThrusterPackRenderLayer(
                            TextureReference.normal(Identifier.fromNamespaceAndPath("mypack", "thruster_texture_path")),
                            new Vec3(1, 2, 1),
                            List.of("head", "right_leg"),
                            Color.RED,
                            TextureReference.normal(BLOOM_TEXTURE),
                            Color.RED,
                            2.0,
                            PackRenderLayerProperties.DEFAULT
                    ));
        }
    }
}
