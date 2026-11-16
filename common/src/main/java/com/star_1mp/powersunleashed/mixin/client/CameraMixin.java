package com.star_1mp.powersunleashed.mixin.client;

import com.star_1mp.powersunleashed.client.power.ClimbState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Third person under a ceiling: the eyes are right under it, so the camera would collide with the ceiling and sit
 * inside the climber; it starts lower instead (no loader event moves the camera's position).
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private Entity entity;

    @Shadow
    private Vec3 position;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
    private void powers_unleashed$ceilingCamera(float partialTicks, CallbackInfo ci) {
        if (this.entity == null) {
            return;
        }
        // upside down on the web: the eyes are near the bottom of the turned-over body
        float r = com.star_1mp.powersunleashed.client.web.WebRappel.weight(this.entity.getId(), partialTicks);
        if (r > 0.0F) {
            this.setPosition(this.position.add(0.0, -(this.entity.getEyeHeight() - 0.22) * r, 0.0));
        }
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }
        float w = ClimbState.ceilingWeight(this.entity.getId(), partialTicks);
        if (w > 0.0F) {
            this.setPosition(this.position.add(0.0, -0.9 * w, 0.0));
        }
    }
}
