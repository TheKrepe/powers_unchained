package net.threetag.palladium.client.trail;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranimcore.animation.layered.AnimationSnapshot;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.bones.ToggleablePlayerAnimBone;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.client.AbilityClientEventHandler;
import net.threetag.palladium.client.renderer.entity.layer.pack.DefaultPackRenderLayer;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.util.PalladiumCodecs;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AfterImageTrailRenderer extends TrailRenderer<AfterImageTrailRenderer.Data> {

    private static final List<String> DEFAULT_BONES = List.of(
            "head", "body", "torso", "right_arm", "left_arm", "right_leg", "left_leg", "right_item", "left_item"
    );

    private static final float PROBE_SENTINEL = 987654.321F;

    public static MapCodec<AfterImageTrailRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PalladiumCodecs.COLOR_CODEC.optionalFieldOf("color", Color.WHITE).forGetter(r -> r.color),
            PalladiumCodecs.FLOAT_0_TO_1.optionalFieldOf("opacity", 0.5F).forGetter(r -> r.opacity),
            Codec.BOOL.optionalFieldOf("flat_color", false).forGetter(r -> r.flatColor),
            Codec.BOOL.optionalFieldOf("edges", false).forGetter(r -> r.edges),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("blur_layers", 1).forGetter(r -> r.blurLayers),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("blur_scale", 0.05F).forGetter(r -> r.blurScale)
    ).apply(instance, AfterImageTrailRenderer::new));

    private final Color color;
    private final float opacity;
    private final boolean flatColor;
    private final boolean edges;
    private final int blurLayers;
    private final float blurScale;

    public AfterImageTrailRenderer(Color color, float opacity, boolean flatColor, boolean edges, int blurLayers, float blurScale) {
        this.color = color;
        this.opacity = opacity;
        this.flatColor = flatColor;
        this.edges = edges;
        this.blurLayers = blurLayers;
        this.blurScale = blurScale;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LivingEntityRenderState ownerRenderState, TrailSegment<AfterImageTrailRenderer.Data> trailSegment, int packedLight, float partialTick) {
        AfterImageTrailRenderer.Data trailData = trailSegment.getTrailData();
        float baseOpacity = trailSegment.getOpacity() * this.opacity;
        var camera = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;

        AbilityClientEventHandler.AFTER_IMAGE = true;
        AbilityClientEventHandler.AFTER_IMAGE_GHOST = this.flatColor;
        AbilityClientEventHandler.AFTER_IMAGE_GHOST_EDGES = this.edges;
        try {
            for (int i = 0; i < this.blurLayers; i++) {
                trailData.renderState.setRenderData(PalladiumRenderStateKeys.OPACITY, baseOpacity / (i + 1));

                poseStack.pushPose();

                if (i > 0) {
                    float scale = 1F + i * this.blurScale;
                    float centerY = trailData.renderState.boundingBoxHeight / 2F;
                    poseStack.translate(0F, centerY, 0F);
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0F, -centerY, 0F);
                }

                trailData.renderer.submit(trailData.renderState, poseStack, submitNodeCollector, camera);
                poseStack.popPose();
            }
        } finally {
            AbilityClientEventHandler.AFTER_IMAGE = false;
            AbilityClientEventHandler.AFTER_IMAGE_GHOST = false;
            AbilityClientEventHandler.AFTER_IMAGE_GHOST_EDGES = false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Data createData(Entity entity) {
        var data = new Data();
        data.renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        data.renderState = data.renderer.createRenderState(entity, 1F);
        data.renderState.setRenderData(PalladiumRenderStateKeys.TINT, this.color.getRGB());
        data.renderState.setRenderData(PalladiumRenderStateKeys.TRAILS, Collections.emptyMap());
        data.renderState.nameTag = null;
        data.renderState.scoreText = null;
        freezePlayerAnimation(entity, data.renderer, data.renderState);
        return data;
    }

    private static void freezePlayerAnimation(Entity entity, EntityRenderer<?, ?> renderer, EntityRenderState renderState) {
        if (!(renderState instanceof IAvatarAnimationState animState) || !(entity instanceof Avatar avatar)) {
            return;
        }

        AvatarAnimManager live = animState.playerAnimLib$getAnimManager();
        if (live == null || !live.isActive()) {
            animState.playerAnimLib$setAnimManager(null);
            return;
        }

        Set<String> candidates = new HashSet<>(DEFAULT_BONES);
        collectModelBoneNames(entity, renderer, renderState, candidates);

        Map<String, ToggleablePlayerAnimBone> snapshot = new HashMap<>();
        for (String name : candidates) {
            ToggleablePlayerAnimBone bone = captureBone(live, name);
            if (bone != null) {
                snapshot.put(name, bone);
            }
        }
        animState.playerAnimLib$setAnimManager(new FrozenAnimManager(avatar, snapshot));
    }

    private static void collectModelBoneNames(Entity entity, EntityRenderer<?, ?> renderer, EntityRenderState renderState, Set<String> out) {
        if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer && livingRenderer.getModel() != null) {
            collectParts(livingRenderer.getModel().root(), out);
        }

        var powerState = renderState.getRenderData(PalladiumRenderStateKeys.POWER_RENDERER);
        if (powerState != null && powerState.model() != null) {
            collectParts(powerState.model().root(), out);
        }

        for (Object layer : renderState.getRenderDataOrDefault(PalladiumRenderStateKeys.RENDER_LAYERS, Collections.emptyMap()).keySet()) {
            if (layer instanceof DefaultPackRenderLayer defaultLayer) {
                for (String name : defaultLayer.getPartNames(entity)) {
                    out.add(UniversalAnimLoader.getCorrectPlayerBoneName(name));
                }
            }
        }
    }

    private static void collectParts(ModelPart part, Set<String> out) {
        for (Map.Entry<String, ModelPart> child : part.children.entrySet()) {
            out.add(UniversalAnimLoader.getCorrectPlayerBoneName(child.getKey()));
            collectParts(child.getValue(), out);
        }
    }

    private static ToggleablePlayerAnimBone captureBone(AvatarAnimManager live, String name) {
        PlayerAnimBone base = new PlayerAnimBone(name);
        live.get3DTransform(base);

        PlayerAnimBone probe = new PlayerAnimBone(name);
        probe.position.set(PROBE_SENTINEL, PROBE_SENTINEL, PROBE_SENTINEL);
        probe.rotation.set(PROBE_SENTINEL, PROBE_SENTINEL, PROBE_SENTINEL);
        probe.scale.set(PROBE_SENTINEL, PROBE_SENTINEL, PROBE_SENTINEL);
        live.get3DTransform(probe);

        ToggleablePlayerAnimBone snap = new ToggleablePlayerAnimBone(name);
        snap.position.set(base.position);
        snap.rotation.set(base.rotation);
        snap.scale.set(base.scale);

        snap.positionXEnabled = base.position.x == probe.position.x;
        snap.positionYEnabled = base.position.y == probe.position.y;
        snap.positionZEnabled = base.position.z == probe.position.z;
        snap.rotXEnabled = base.rotation.x == probe.rotation.x;
        snap.rotYEnabled = base.rotation.y == probe.rotation.y;
        snap.rotZEnabled = base.rotation.z == probe.rotation.z;
        snap.scaleXEnabled = base.scale.x == probe.scale.x;
        snap.scaleYEnabled = base.scale.y == probe.scale.y;
        snap.scaleZEnabled = base.scale.z == probe.scale.z;

        boolean driven = snap.positionXEnabled || snap.positionYEnabled || snap.positionZEnabled
                || snap.rotXEnabled || snap.rotYEnabled || snap.rotZEnabled
                || snap.scaleXEnabled || snap.scaleYEnabled || snap.scaleZEnabled;
        return driven ? snap : null;
    }

    @Override
    public TrailRendererSerializer<?> getSerializer() {
        return TrailRendererSerializers.AFTER_IMAGE;
    }

    private static class FrozenAnimManager extends AvatarAnimManager {

        FrozenAnimManager(Avatar avatar, Map<String, ToggleablePlayerAnimBone> snapshot) {
            super(avatar);
            addAnimLayer(0, new AnimationSnapshot(snapshot));
        }

        @Override
        public void handleAnimations(float partialTick, boolean fullTick, boolean isFirstPersonPass) {
            // nothing
        }
    }

    @SuppressWarnings("rawtypes")
    public static class Data {

        private EntityRenderer renderer;
        private EntityRenderState renderState;

    }

    public static class Serializer extends TrailRendererSerializer<AfterImageTrailRenderer> {

        @Override
        public MapCodec<AfterImageTrailRenderer> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<TrailRenderer<?>, AfterImageTrailRenderer> builder, HolderLookup.Provider provider) {
            builder.setName("After Image Trail").setDescription("Renders copies of the entity behind it.")
                    .addOptional("color", TYPE_COLOR, "Defines the color/tint of the after image", "#ffffff")
                    .addOptional("opacity", TYPE_FLOAT, "Starting opacity of the after image", 0.5F)
                    .addOptional("flat_color", TYPE_BOOLEAN, "If enabled, the after image is rendered as a flat, fullbright silhouette in the given color instead of a tinted copy of the entity's textures", false)
                    .addOptional("edges", TYPE_BOOLEAN, "If enabled, only high-contrast edges of the entity's textures are rendered as fullbright lines in the given color, the rest is mostly transparent", false)
                    .addOptional("blur_layers", TYPE_INT, "Amount of times each after image is rendered on top of itself, each layer slightly scaled up, to create a blurry outline", 1)
                    .addOptional("blur_scale", TYPE_FLOAT, "How much each additional blur layer is scaled up", 0.05F)
                    .addExampleObject(new AfterImageTrailRenderer(Color.RED, 0.2F, false, false, 1, 0.05F))
                    .addExampleObject(new AfterImageTrailRenderer(Color.WHITE, 0.3F, true, true, 3, 0.05F));
        }
    }
}
