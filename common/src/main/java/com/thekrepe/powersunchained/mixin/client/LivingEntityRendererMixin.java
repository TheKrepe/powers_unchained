package com.thekrepe.powersunchained.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thekrepe.powersunchained.client.web.WebZipAnimation;
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;scale(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void powers_unchained$webZipBody(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera,
            CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatar) {
            // undo the model flip, rotate like the old rotateCorpse did, flip again
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            WebZipAnimation.applyBody(poseStack, avatar.id, state.ageInTicks - Mth.floor(state.ageInTicks), 1.0F,
                    state.bodyRot);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
        }
    }
}
