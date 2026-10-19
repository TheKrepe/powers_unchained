package net.threetag.palladium.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.threetag.palladium.client.AbilityClientEventHandler;
import net.threetag.palladium.client.animation.BuiltinAnimations;
import net.threetag.palladium.client.renderer.entity.layer.pack.ClientEntityRenderLayers;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.client.renderer.entity.layer.pack.VibrationPackRenderLayer;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.client.renderer.power.ClientPowerRendererHandler;
import net.threetag.palladium.client.util.ModelUtil;
import net.threetag.palladium.client.util.PerspectiveAwareConditions;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PlayerUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DataFlowIssue")
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {

    @Inject(at = @At("HEAD"), method = "renderHand", cancellable = true)
    private void renderHandPre(
            PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, Identifier skinTexture, ModelPart armPart, boolean renderSleeve, CallbackInfo ci
    ) {
        AvatarRenderer<?> playerRenderer = (AvatarRenderer<?>) (Object) this;
        HumanoidArm capturedArm = armPart == playerRenderer.getModel().rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        ModelUtil.captureFirstPersonArmAnchor(capturedArm, poseStack.last().pose());
        this.palladium$applyFirstPersonShrink(armPart);

        if (this.palladium$isFirstPersonPartHidden(armPart) || this.palladium$shouldSuppressVanillaArm(armPart)) {
            this.palladium$resetArmPose(armPart);
            ModelPart powerArm = this.palladium$renderPowerRendererArm(poseStack, nodeCollector, packedLight, armPart);
            this.palladium$renderHandLayers(poseStack, nodeCollector, packedLight, skinTexture, armPart, powerArm != null ? powerArm : armPart, true);
            ci.cancel();
        }
    }

    @Unique
    private void palladium$resetArmPose(ModelPart armPart) {
        var model = ((AvatarRenderer<?>) (Object) this).getModel();

        armPart.resetPose();
        armPart.visible = true;
        model.leftArm.zRot = -0.1F;
        model.rightArm.zRot = 0.1F;
    }

    @Inject(at = @At("RETURN"), method = "renderHand")
    private void renderHand(
            PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, Identifier skinTexture, ModelPart armPart, boolean renderSleeve, CallbackInfo ci
    ) {
        this.palladium$hideFirstPersonSleeve(armPart);
        ModelPart powerArm = this.palladium$renderPowerRendererArm(poseStack, nodeCollector, packedLight, armPart);
        this.palladium$renderHandLayers(poseStack, nodeCollector, packedLight, skinTexture, armPart, powerArm != null ? powerArm : armPart, true);
    }

    @Unique
    private boolean palladium$isFirstPersonPartHidden(ModelPart part) {
        var player = Minecraft.getInstance().player;

        if (player == null) {
            return false;
        }

        var model = ((AvatarRenderer<?>) (Object) this).getModel();

        for (String partName : ModelUtil.getHiddenModelPartNames(player, PerspectiveAwareConditions.Perspective.FIRST_PERSON)) {
            if (ModelUtil.isWholeModel(partName) || ModelUtil.findPart(model, partName) == part) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private void palladium$hideFirstPersonSleeve(ModelPart armPart) {
        var model = ((AvatarRenderer<?>) (Object) this).getModel();
        var sleeve = armPart == model.rightArm ? model.rightSleeve : model.leftSleeve;

        if (this.palladium$isFirstPersonPartHidden(sleeve)) {
            sleeve.visible = false;
        }
    }

    @Unique
    private void palladium$applyFirstPersonShrink(ModelPart armPart) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (!(PalladiumEntityData.get(player, PalladiumEntityDataTypes.RENDER_LAYERS.get()) instanceof ClientEntityRenderLayers layers)) {
            return;
        }

        AvatarRenderer<?> playerRenderer = (AvatarRenderer<?>) (Object) this;
        var model = playerRenderer.getModel();
        boolean rightArm = armPart == model.rightArm;
        var part = rightArm ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE;
        var sleeve = rightArm ? model.rightSleeve : model.leftSleeve;
        BuiltinAnimations.applyFirstPersonSleeveShrink(sleeve, layers.getShrinkProgress().getOrDefault(part, 0F));
    }

    @Unique
    private boolean palladium$shouldSuppressVanillaArm(ModelPart armPart) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        var prHandler = ClientPowerRendererHandler.get(player);
        if (prHandler == null) {
            return false;
        }

        if (prHandler.getProgress(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks()) < 1.0f) {
            return false;
        }

        var activeRenderer = prHandler.getActiveRenderer();
        if (activeRenderer == null || activeRenderer.entityRenderer() == null || activeRenderer.entityRenderer().firstPersonSettings() == null) {
            return false;
        }

        AvatarRenderer<?> playerRenderer = (AvatarRenderer<?>) (Object) this;
        boolean isRightArm = armPart == playerRenderer.getModel().rightArm;
        return activeRenderer.entityRenderer().firstPersonSettings().get(isRightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT) != null;
    }

    @Unique
    @SuppressWarnings({"DataFlowIssue", "rawtypes"})
    private void palladium$renderHandLayers(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, Identifier skinTexture, ModelPart armPart, ModelPart referenceArm, boolean root) {
        var player = Minecraft.getInstance().player;
        var entityLayers = PalladiumEntityData.get(player, PalladiumEntityDataTypes.RENDER_LAYERS.get());
        AvatarRenderer<?> playerRenderer = (AvatarRenderer<?>) (Object) this;

        if (entityLayers instanceof ClientEntityRenderLayers layers) {
            boolean rightArm = armPart == playerRenderer.getModel().rightArm;
            var arm = rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT;

            layers.getLayerStates().forEach((layer, state) -> {
                state.extractRenderState(layer, state.getContext());
                if (layer.shouldRender(state, PerspectiveAwareConditions.Perspective.FIRST_PERSON)) {
                    PackRenderLayer l = layer;

                    if (l instanceof VibrationPackRenderLayer vibration && state instanceof VibrationPackRenderLayer.State vibrationState) {
                        if (root) {
                            AbilityClientEventHandler.OVERRIDDEN_OPACITY = 0.15F;
                            var progress = vibrationState.getProgress(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks());
                            var rand = RandomSource.create();

                            for (int i = 0; i < vibration.intensity; i++) {
                                poseStack.pushPose();
                                poseStack.translate(((rand.nextFloat() - 0.5F) / 1500) * progress, 0, ((rand.nextFloat() - 0.5F) / 1500) * progress);
                                nodeCollector.submitModelPart(armPart, poseStack, RenderTypes.entityTranslucent(skinTexture), packedLight, OverlayTexture.NO_OVERLAY, null);
                                this.palladium$renderHandLayers(poseStack, nodeCollector, packedLight, skinTexture, armPart, referenceArm, false);
                                poseStack.popPose();
                            }
                        }
                    } else {
                        poseStack.scale(1.001F, 1.001F, 1.001F);
                        layer.submitArm(poseStack, nodeCollector, arm, referenceArm, playerRenderer, state, packedLight);
                    }
                }
            });
        }
    }

    @Unique
    @Nullable
    private ModelPart palladium$renderPowerRendererArm(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, ModelPart originalArmPart) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }

        var prHandler = ClientPowerRendererHandler.get(player);
        if (prHandler == null || !prHandler.isModelVisible()) {
            return null;
        }

        var activeRenderer = prHandler.getActiveRenderer();
        if (activeRenderer == null || activeRenderer.entityRenderer() == null || activeRenderer.entityRenderer().firstPersonSettings() == null) {
            return null;
        }

        AvatarRenderer<?> playerRenderer = (AvatarRenderer<?>) (Object) this;
        boolean isRightArm = originalArmPart == playerRenderer.getModel().rightArm;
        var arm = isRightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        var firstPeronSettings = activeRenderer.entityRenderer().firstPersonSettings();
        String boneName = firstPeronSettings.get(arm);
        if (boneName == null) {
            return null;
        }

        boolean isSlim = PlayerUtil.hasSmallArms(player);
        var model = prHandler.getOrBuildModel(isSlim);
        if (model == null || !model.root().hasChild(boneName)) {
            return null;
        }
        var armPart = model.root().getChild(boneName);
        armPart.resetPose();
        armPart.visible = true;
        armPart.zRot = isRightArm ? 0.1F : -0.1F;
        armPart.xScale *= firstPeronSettings.scale();
        armPart.yScale *= firstPeronSettings.scale();
        armPart.zScale *= firstPeronSettings.scale();

        var texture = activeRenderer.entityRenderer().texture().getTexture(DataContext.forEntity(player));
        poseStack.scale(1.001F, 1.001F, 1.001F);
        nodeCollector.submitModelPart(armPart, poseStack,
                RenderTypes.entityTranslucent(texture),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                null);
        return armPart;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("RETURN"))
    private void extractRenderState(AvatarlikeEntity entity, AvatarRenderState reusedState, float partialTick, CallbackInfo ci) {
        PalladiumRenderStateKeys.manipulatePlayerState(entity, reusedState, partialTick);
    }

}
