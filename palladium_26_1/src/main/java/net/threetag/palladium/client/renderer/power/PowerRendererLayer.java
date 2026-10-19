package net.threetag.palladium.client.renderer.power;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.threetag.palladium.client.model.ModelPartExtension;
import net.threetag.palladium.client.model.PowerRendererModel;
import net.threetag.palladium.client.renderer.entity.layer.pack.DefaultPackRenderLayer;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.client.util.ModelUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PowerRendererLayer<S extends EntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

    private static @Nullable PlayerSkinRenderCache skinRenderCache;
    private static @Nullable Function<SkullBlock.Type, SkullModelBase> skullModels;

    public PowerRendererLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    public static void setupHeadRendering(EntityModelSet modelSet, PlayerSkinRenderCache skinRenderCache) {
        PowerRendererLayer.skinRenderCache = skinRenderCache;
        PowerRendererLayer.skullModels = Util.memoize(type -> Objects.requireNonNull(SkullBlockRenderer.createModel(modelSet, type)));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S renderState, float yRot, float xRot) {
        var prState = renderState.getRenderData(PalladiumRenderStateKeys.POWER_RENDERER);
        if (prState == null || prState.progress() <= 0f || prState.model() == null || !prState.showModel()) {
            return;
        }

        renderPowerModel(prState, poseStack, nodeCollector,
                getRenderType(prState, !renderState.isInvisible, false, renderState.appearsGlowing()),
                packedLight, OverlayTexture.NO_OVERLAY, -1, renderState);
    }

    public static void renderPowerModel(PowerRendererRenderState prState, PoseStack poseStack, SubmitNodeCollector nodeCollector, RenderType renderType, int packedLight, int overlayTexture, int tintedColor, EntityRenderState renderState) {
        PowerRendererModel model = prState.model();

        for (ModelPart part : model.allParts()) {
            part.resetPose();
            part.visible = true;
            part.skipDraw = false;
        }

        var entityRenderer = prState.renderer().entityRenderer();
        if (entityRenderer != null && entityRenderer.applyVanillaMovement()) {
            VanillaMovementAnimator.apply(model, renderState, entityRenderer.boneMapping());
        }

        if (renderState instanceof IAvatarAnimationState animState) {
            AvatarAnimManager emote = animState.playerAnimLib$getAnimManager();
            if (emote != null) {
                emote.handleAnimations(emote.getTickDelta(), false, animState.playerAnimLib$isFirstPersonPass());
                if (emote.isActive()) {
                    String chestPart = entityRenderer != null ? entityRenderer.boneMapping().get("body") : null;
                    if (chestPart == null && !model.root().hasChild("torso")) {
                        chestPart = "body";
                    }
                    for (String name : model.root().children.keySet()) {
                        String boneName = name.equals(chestPart) ? "torso" : name;
                        DefaultPackRenderLayer.animate(model.root().getChild(name), boneName, emote);
                    }
                }
            }
        }

        List<ModelPartPose> snapshotPoses = new ArrayList<>();
        for (ModelPart part : model.allParts()) {
            snapshotPoses.add(new ModelPartPose(part));
        }
        SnapshotModelWrapper wrapper = new SnapshotModelWrapper(model, snapshotPoses);

        nodeCollector.submitModel(
                wrapper,
                Unit.INSTANCE,
                poseStack,
                renderType,
                LightCoordsUtil.lightCoordsWithEmission(packedLight, 0),
                overlayTexture,
                tintedColor,
                null,
                renderState.outlineColor,
                null);
    }

    public static void applyBodyBone(PoseStack poseStack, EntityRenderState renderState) {
        if (!(renderState instanceof IAvatarAnimationState animState)) {
            return;
        }
        AvatarAnimManager emote = animState.playerAnimLib$getAnimManager();
        if (emote == null) {
            return;
        }
        emote.handleAnimations(emote.getTickDelta(), false, animState.playerAnimLib$isFirstPersonPass());
        if (!emote.isActive()) {
            return;
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        PlayerAnimBone body = emote.get3DTransform("body");
        poseStack.translate(-body.position.x / 16F, body.position.y / 16F + 0.75F, body.position.z / 16F);
        body.rotation.x *= -1F;
        body.rotation.y *= -1F;
        RenderUtil.rotateMatrixAroundBone(poseStack, body);
        poseStack.scale(body.scale.x, body.scale.y, body.scale.z);
        poseStack.translate(0F, -0.75F, 0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
    }

    public static void renderHeldItems(PowerRendererRenderState prState, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, EntityRenderState renderState) {
        var entityRenderer = prState.renderer().entityRenderer();
        if (prState.model() == null || entityRenderer == null || !entityRenderer.enableHandItems() || !(renderState instanceof ArmedEntityRenderState armed)) {
            return;
        }
        AvatarAnimManager emote = renderState instanceof IAvatarAnimationState animState ? animState.playerAnimLib$getAnimManager() : null;
        submitHeldItem(prState.model(), armed, emote, armed.rightHandItemState, armed.rightHandItemStack, HumanoidArm.RIGHT, poseStack, nodeCollector, packedLight);
        submitHeldItem(prState.model(), armed, emote, armed.leftHandItemState, armed.leftHandItemStack, HumanoidArm.LEFT, poseStack, nodeCollector, packedLight);
    }

    @SuppressWarnings("ConstantValue")
    private static void submitHeldItem(PowerRendererModel model, ArmedEntityRenderState state, @Nullable AvatarAnimManager emote, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight) {
        if (item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        model.translateToHand(state, arm, poseStack);

        PlayerAnimBone itemBone = null;
        if (emote != null && emote.isActive()) {
            itemBone = new PlayerAnimBone(arm == HumanoidArm.LEFT ? "left_item" : "right_item");
            itemBone.setToInitialPose();
            emote.get3DTransform(itemBone);
            poseStack.translate(itemBone.position.x / 16F, -itemBone.position.y / 16F, itemBone.position.z / 16F);
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean baby = state.isBaby && state.entityType != EntityType.ARMOR_STAND;
        boolean left = arm == HumanoidArm.LEFT;
        float offsetX = baby ? 0.0F : 1.0F;
        float offsetY = baby ? 1.0F : 2.0F;
        float offsetZ = baby ? -4.5F : -10.0F;

        if (((Object) ModelUtil.getPartFromModel(model, model.armBoneName(arm))) instanceof ModelPartExtension ext) {
            var bb = ext.palladium$getBoundingBox();
            offsetX = (float) (bb.getXsize() / 2F);
            offsetY = (float) (bb.getZsize() / 2F);
            offsetZ = (float) (-bb.getYsize());
        }

        poseStack.translate((left ? -1 : 1) * offsetX / 16.0F, offsetY / 16.0F, offsetZ / 16.0F);

        if (state.attackTime > 0.0F && state.attackArm == arm && state.swingAnimationType == SwingAnimationType.STAB) {
            SpearAnimations.thirdPersonAttackItem(state, poseStack);
        }
        float ticksUsingItem = state.ticksUsingItem(arm);
        if (ticksUsingItem != 0.0F) {
            (arm == HumanoidArm.RIGHT ? state.rightArmPose : state.leftArmPose).animateUseItem(state, poseStack, ticksUsingItem, arm, itemStack);
        }

        if (itemBone != null) {
            if (itemBone.rotation.z != 0F) {
                poseStack.mulPose(Axis.ZP.rotation(-itemBone.rotation.y));
            }
            if (itemBone.rotation.y != 0F) {
                poseStack.mulPose(Axis.YP.rotation(-itemBone.rotation.z));
            }
            if (itemBone.rotation.x != 0F) {
                poseStack.mulPose(Axis.XP.rotation(-itemBone.rotation.x));
            }
            poseStack.scale(itemBone.scale.x, itemBone.scale.y, itemBone.scale.z);
        }

        item.submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    public static void renderHeadItem(PowerRendererRenderState prState, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, LivingEntityRenderState state) {
        PowerRendererModel model = prState.model();
        var entityRenderer = prState.renderer().entityRenderer();
        if (model == null || entityRenderer == null || !entityRenderer.enableHeadItem() || (state.headItem.isEmpty() && state.wornHeadType == null)) {
            return;
        }

        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        model.translateToHead(poseStack);

        if (state.wornHeadType != null) {
            poseStack.scale(1.1875F, 1.1875F, 1.1875F);
            SkullModelBase skullModel = skullModels != null ? skullModels.apply(state.wornHeadType) : null;
            if (skullModel != null) {
                SkullBlockRenderer.submitSkull(state.wornHeadAnimationPos, poseStack, nodeCollector, packedLight,
                        skullModel, resolveSkullRenderType(state, state.wornHeadType), state.outlineColor, null);
            }
        } else {
            // Matches CustomHeadLayer#translateToHead with default transforms.
            poseStack.translate(0.0F, -0.25F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.625F, -0.625F, -0.625F);
            state.headItem.submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }

        poseStack.popPose();
    }

    private static RenderType resolveSkullRenderType(LivingEntityRenderState state, SkullBlock.Type type) {
        if (type == SkullBlock.Types.PLAYER && skinRenderCache != null) {
            ResolvableProfile profile = state.wornHeadProfile;
            if (profile != null) {
                return skinRenderCache.getOrDefault(profile).renderType();
            }
        }
        return SkullBlockRenderer.getSkullRenderType(type, null);
    }

    public static @Nullable RenderType getRenderType(PowerRendererRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        if (forceTransparent) {
            return RenderTypes.entityTranslucentCullItemTarget(state.texture());
        } else if (isBodyVisible) {
            return RenderTypes.entityTranslucent(state.texture());
        } else {
            return appearGlowing ? RenderTypes.outline(state.texture()) : null;
        }
    }

    private record ModelPartPose(float x, float y, float z, float xRot, float yRot, float zRot,
                                 float xScale, float yScale, float zScale, boolean visible, boolean skipDraw) {
        ModelPartPose(ModelPart part) {
            this(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot,
                    part.xScale, part.yScale, part.zScale, part.visible, part.skipDraw);
        }

        void apply(ModelPart part) {
            part.x = this.x;
            part.y = this.y;
            part.z = this.z;
            part.xRot = this.xRot;
            part.yRot = this.yRot;
            part.zRot = this.zRot;
            part.xScale = this.xScale;
            part.yScale = this.yScale;
            part.zScale = this.zScale;
            part.visible = this.visible;
            part.skipDraw = this.skipDraw;
        }
    }

    private static class SnapshotModelWrapper extends Model<Unit> {
        private final PowerRendererModel original;
        private final List<ModelPartPose> poses;

        SnapshotModelWrapper(PowerRendererModel original, List<ModelPartPose> poses) {
            super(original.root(), RenderTypes::entityTranslucent);
            this.original = original;
            this.poses = poses;
        }

        @Override
        public void setupAnim(Unit state) {
            super.setupAnim(state);
            List<ModelPart> parts = this.original.allParts();
            for (int i = 0; i < parts.size(); i++) {
                this.poses.get(i).apply(parts.get(i));
            }
        }
    }
}
