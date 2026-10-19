package net.threetag.palladium.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.config.PalladiumClientConfig;
import net.threetag.palladium.power.ability.ShaderEffectAbility;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void setPostEffect(Identifier id);

    @Shadow
    private @Nullable Identifier postEffectId;

    @Inject(method = "checkEntityPostEffect", at = @At("RETURN"))
    private void checkEntityPostEffect(Entity cameraEntity, CallbackInfo ci) {
        if (this.postEffectId == null && this.minecraft.player != null) {
            var shader = ShaderEffectAbility.get(this.minecraft.player);
            if (shader != null) {
                this.setPostEffect(shader);
            }
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (PalladiumClientConfig.SCALE_CAMERA_FIX.getAsBoolean() && this.minecraft.getCameraEntity() instanceof AbstractClientPlayer player) {
            float scale = player.getScale();
            float scaleMultiplier = Math.min(scale * 2F, 1);
            float walkDistance = cameraState.entityRenderState.backwardsInterpolatedWalkDistance / Math.max(0.0625F, scale);
            float bob = cameraState.entityRenderState.bob;

            poseStack.translate(Mth.sin(walkDistance * (float) Math.PI) * scaleMultiplier * bob * 0.5F,
                    -Math.abs(Mth.cos(walkDistance * (float) Math.PI) * scaleMultiplier * bob), 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walkDistance * (float) Math.PI) * bob * 3.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(walkDistance * (float) Math.PI - 0.2F) * bob) * 5.0F));

            ci.cancel();
        }
    }

}
