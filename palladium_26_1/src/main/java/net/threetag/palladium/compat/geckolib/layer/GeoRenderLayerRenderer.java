package net.threetag.palladium.compat.geckolib.layer;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayersContainer;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.threetag.palladium.logic.context.DataContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public class GeoRenderLayerRenderer<R extends HumanoidRenderState & GeoRenderState> implements GeoRenderer<GeoRenderLayerState, DataContext, R> {

    public static final DataTicket<Identifier> LAYER_TEXTURE = DataTicket.create("palladium_layer_texture", Identifier.class);
    public static final DataTicket<Identifier> LAYER_MODEL = DataTicket.create("palladium_layer_model", Identifier.class);
    public static final DataTicket<Identifier> LAYER_ANIMATIONS = DataTicket.create("palladium_layer_animations", Identifier.class);
    public static final DataTicket<Map<net.threetag.palladium.compat.geckolib.layer.GeoRenderLayer.Bone, PartPose>> LAYER_BASE_POSE = DataTicket.create("palladium_layer_base_pose", new TypeToken<>() {});

    protected final GeoRenderLayersContainer<GeoRenderLayerState, DataContext, R> renderLayers = new GeoRenderLayersContainer<>(this);
    protected final GeoModel<GeoRenderLayerState> model;

    protected float scaleWidth = 1;
    protected float scaleHeight = 1;

    protected final Map<net.threetag.palladium.compat.geckolib.layer.GeoRenderLayer.Bone, String> bones;

    public GeoRenderLayerRenderer(Map<net.threetag.palladium.compat.geckolib.layer.GeoRenderLayer.Bone, String> bones, GeoModel<GeoRenderLayerState> model) {
        this.model = model;
        this.bones = bones;
    }

    /**
     * Gets the model instance for this renderer
     */
    @Override
    public GeoModel<GeoRenderLayerState> getGeoModel() {
        return this.model;
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public GeoRenderLayerRenderer<R> withScale(float scale) {
        return withScale(scale, scale);
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public GeoRenderLayerRenderer<R> withScale(float scaleWidth, float scaleHeight) {
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;

        return this;
    }

    @Override
    public long getInstanceId(GeoRenderLayerState animatable, DataContext relatedObject) {
        return GeoRenderer.super.getInstanceId(animatable, relatedObject);
    }

    @Nullable
    @Override
    public RenderType getRenderType(R renderState, Identifier texture) {
        return RenderTypes.armorCutoutNoCull(texture);
    }

    @Override
    public void addRenderData(GeoRenderLayerState animatable, @org.jspecify.annotations.Nullable DataContext relatedObject, R renderState, float partialTick) {
        if (animatable.texture != null)
            renderState.addGeckolibData(LAYER_TEXTURE, animatable.texture);
        if (animatable.model != null)
            renderState.addGeckolibData(LAYER_MODEL, animatable.model);
        if (animatable.animations != null)
            renderState.addGeckolibData(LAYER_ANIMATIONS, animatable.animations);
    }

    @Override
    public void captureDefaultRenderState(GeoRenderLayerState animatable, @org.jspecify.annotations.Nullable DataContext relatedObject, R renderState, float partialTick) {
        GeoRenderer.super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);
        var slot = relatedObject.getSlot();
        var item = relatedObject.getItem();

        renderState.addGeckolibData(DataTickets.TICK, (double) renderState.ageInTicks);
        renderState.addGeckolibData(DataTickets.POSITION, relatedObject.getEntity().position());
        renderState.addGeckolibData(DataTickets.HAS_GLINT, item.hasFoil());
        renderState.addGeckolibData(DataTickets.INVISIBLE_TO_PLAYER, renderState.isInvisibleToPlayer);
    }

    @Override
    public R createRenderState(GeoRenderLayerState animatable, DataContext relatedObject) {
        return (R) new HumanoidRenderState();
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        GeoRenderer.super.scaleModelForRender(renderPassInfo, this.scaleWidth * widthScale, this.scaleHeight * heightScale);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<R> renderPassInfo) {
        renderPassInfo.poseStack().translate(0, 24 / 16f, 0);
        renderPassInfo.poseStack().scale(-1, -1, 1);
    }

    @Override
    public void fireCompileRenderLayersEvent() {

    }

    @Override
    public void fireCompileRenderStateEvent(GeoRenderLayerState animatable, DataContext relatedObject, R renderState, float partialTick) {

    }

    @Override
    public boolean firePreRenderEvent(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        return true;
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<R> renderPassInfo, OrderedSubmitNodeCollector renderTasks, @org.jspecify.annotations.Nullable RenderType renderType) {
        if (renderType == null)
            return;

        if (renderPassInfo.model().isMissingno()) {
            submitMissingModelRender(renderPassInfo, renderTasks);
            return;
        }

        renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            final PoseStack poseStack = renderPassInfo.poseStack();
            final R renderState = renderPassInfo.renderState();
            final int packedLight = renderPassInfo.packedLight();
            final int packedOverlay = renderPassInfo.packedOverlay();
            final int renderColor = renderPassInfo.renderColor();

            poseStack.pushPose();
            poseStack.last().set(pose);
            renderPassInfo.renderPosed(() -> {
                for (ArmorSegment segment : getSegmentsForSlot(renderState, null)) {
                    renderPassInfo.model().getBone(getBoneNameForSegment(renderState, segment))
                            .ifPresent(bone -> bone.positionAndRender(renderPassInfo, vertexConsumer, packedLight, packedOverlay, renderColor));
                }
            });
            poseStack.popPose();
        });
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        final R renderState = renderPassInfo.renderState();
        final Map<GeoRenderLayer.Bone, PartPose> poses = renderState.getGeckolibData(LAYER_BASE_POSE);

        if (poses == null) {
            return;
        }

        for (ArmorSegment segment : getSegmentsForSlot(renderState, null)) {
            PartPose pose = poses.get(getBoneForSegment(segment));

            if (pose == null) {
                continue;
            }

            snapshots.get(getBoneNameForSegment(renderState, segment)).ifPresent(snapshot -> {
                final Vector3f bonePos = segment.modelPartMatcher.apply(new Vector3f(pose.x(), pose.y(), pose.z()));

                snapshot.setRotX(snapshot.getRotX() - pose.xRot())
                        .setRotY(snapshot.getRotY() - pose.yRot())
                        .setRotZ(snapshot.getRotZ() + pose.zRot())
                        .setTranslateX(snapshot.getTranslateX() + bonePos.x)
                        .setTranslateY(snapshot.getTranslateY() + bonePos.y)
                        .setTranslateZ(snapshot.getTranslateZ() + bonePos.z);
            });
        }
    }

    public GeoRenderLayer.Bone getBoneForSegment(ArmorSegment segment) {
        return switch (segment) {
            case HEAD -> GeoRenderLayer.Bone.HEAD;
            case CHEST -> GeoRenderLayer.Bone.BODY;
            case LEFT_ARM -> GeoRenderLayer.Bone.LEFT_ARM;
            case RIGHT_ARM -> GeoRenderLayer.Bone.RIGHT_ARM;
            case LEFT_LEG -> GeoRenderLayer.Bone.LEFT_LEG;
            case RIGHT_LEG -> GeoRenderLayer.Bone.RIGHT_LEG;
        };
    }

    public String getBoneNameForSegment(R renderState, ArmorSegment segment) {
        GeoRenderLayer.Bone bone = getBoneForSegment(segment);
        return this.bones.getOrDefault(bone, bone.getSerializedName());
    }

    public List<ArmorSegment> getSegmentsForSlot(R renderState, @Nullable EquipmentSlot slot) {
        if (slot == null) return List.of(ArmorSegment.values());
        return switch (slot) {
            case HEAD -> List.of(ArmorSegment.HEAD);
            case CHEST -> List.of(ArmorSegment.CHEST, ArmorSegment.LEFT_ARM, ArmorSegment.RIGHT_ARM);
            case LEGS, FEET -> List.of(ArmorSegment.LEFT_LEG, ArmorSegment.RIGHT_LEG);
            default -> List.of();
        };
    }

    public enum ArmorSegment {
        HEAD(EquipmentSlot.HEAD, model -> model.head, pos -> pos.mul(1, -1, 1)),
        CHEST(EquipmentSlot.CHEST, model -> model.body, pos -> pos.mul(1, -1, 1)),
        LEFT_ARM(EquipmentSlot.CHEST, model -> model.leftArm, pos -> pos.set(pos.x - 5, 2 - pos.y, pos.z)),
        RIGHT_ARM(EquipmentSlot.CHEST, model -> model.rightArm, pos -> pos.set(pos.x + 5, 2 - pos.y, pos.z)),
        LEFT_LEG(EquipmentSlot.LEGS, model -> model.leftLeg, pos -> pos.set(pos.x - 2, 12 - pos.y, pos.z)),
        RIGHT_LEG(EquipmentSlot.LEGS, model -> model.rightLeg, pos -> pos.set(pos.x + 2, 12 - pos.y, pos.z));

        public final EquipmentSlot equipmentSlot;
        public final Function<HumanoidModel<?>, ModelPart> modelPartGetter;
        public final UnaryOperator<Vector3f> modelPartMatcher;

        ArmorSegment(EquipmentSlot slot, Function<HumanoidModel<?>, ModelPart> modelPartGetter, UnaryOperator<Vector3f> modelPartMatcher) {
            this.equipmentSlot = slot;
            this.modelPartGetter = modelPartGetter;
            this.modelPartMatcher = modelPartMatcher;
        }
    }


}
