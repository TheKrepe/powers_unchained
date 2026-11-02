package com.star_1mp.powersunchained.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunchained.client.web.WebZipAnimation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Whole body rotations of the web zip animations (lean, flips, dive), right after the body yaw rotation like Fisk's
 * rotateCorpse hook. No loader event sits at that point of the render, hence the mixin.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState> {

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"))
    private void powers_unchained$webHandsOrigin(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera,
            CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatar) {
            com.star_1mp.powersunchained.client.power.SpiderSenseLayer.cameraOrientation.set(camera.orientation);
            com.star_1mp.powersunchained.client.web.WebHandTracker.begin(avatar.id, poseStack);
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;scale(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void powers_unchained$webZipBody(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera,
            CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatar) {
            // undo the model flip, rotate like the old rotateCorpse did, flip again
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            float pt = state.ageInTicks - Mth.floor(state.ageInTicks);
            WebZipAnimation.applyBody(poseStack, avatar.id, pt, 1.0F, state.bodyRot);
            com.star_1mp.powersunchained.client.web.WebRappel.applyBody(poseStack, avatar.id, pt, 1.0F);
            com.star_1mp.powersunchained.client.power.RollAnimation.applyBody(poseStack, avatar.id, pt, 1.0F, state.bodyRot);
            com.star_1mp.powersunchained.client.power.ClimbState.applyBody(poseStack, avatar.id, pt, 1.0F, state.bodyRot);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
        }
    }

    /** Irradiated creatures shake like a curing zombie villager (owner): the vanilla "fully frozen" shake. */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void powers_unchained$irradiationShake(net.minecraft.world.entity.LivingEntity entity, S state, float partialTicks,
            CallbackInfo ci) {
        if (com.star_1mp.powersunchained.irradiation.IrradiatorShake.isShaking(entity.getId(), entity.level().getGameTime())) {
            state.isFullyFrozen = true;
        }
    }
}
