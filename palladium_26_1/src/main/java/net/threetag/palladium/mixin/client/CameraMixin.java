package net.threetag.palladium.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.threetag.palladium.config.PalladiumClientConfig;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private @Nullable Entity entity;

    @Shadow
    private float eyeHeight;

    @ModifyConstant(method = "update", constant = @Constant(floatValue = 0.05F))
    private float update(float original) {
        if (!PalladiumClientConfig.SCALE_CAMERA_FIX.getAsBoolean() || !(this.entity instanceof LivingEntity living)) {
            return original;
        }

        return Math.clamp(original * living.getScale(), 0.005F, 0.05F);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (this.entity instanceof Avatar avatar) {
            var handler = EntityFlightHandler.get(avatar);
            var animation = handler.getAnimationHandler();

            if (handler.isFlying() && animation != null) {
                float scale = avatar.getScale();
                float standing = Avatar.POSES.get(Pose.STANDING).eyeHeight() * scale;
                float small = Avatar.POSES.get(Pose.FALL_FLYING).eyeHeight() * scale;
                this.eyeHeight = Mth.lerp(animation.getPropulsionScale(1F), standing, small);
            }
        }
    }
}
