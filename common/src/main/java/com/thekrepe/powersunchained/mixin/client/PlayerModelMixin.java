package com.thekrepe.powersunchained.mixin.client;

import com.thekrepe.powersunchained.entity.web.AbstractWebProjectile;
import com.thekrepe.powersunchained.web.WebShootAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Web-shooter shot animation on player-shaped models (the player and everything drawn with a PlayerModel on top of
 * it: suits, web-shooters): the shooting arm points where the player looks and jabs forward. There is no loader
 * event after the vanilla pose, hence the mixin.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("RETURN"))
    private void powers_unchained$webShot(AvatarRenderState state, CallbackInfo ci) {
        WebShootAnimation.Entry entry = WebShootAnimation.get(state.id);
        Minecraft mc = Minecraft.getInstance();
        if (entry == null || mc.level == null) {
            return;
        }
        float partial = state.ageInTicks - Mth.floor(state.ageInTicks);
        float w = WebShootAnimation.weight(mc.level.getGameTime() - entry.gameTime() + partial);
        if (w <= 0.0F) {
            return;
        }
        PlayerModel model = (PlayerModel) (Object) this;
        boolean right = entry.side() == AbstractWebProjectile.RIGHT;
        ModelPart arm = right ? model.rightArm : model.leftArm;
        arm.xRot = Mth.lerp(w, arm.xRot, model.head.xRot - Mth.HALF_PI);
        arm.yRot = Mth.lerp(w, arm.yRot, model.head.yRot + (right ? -0.1F : 0.1F));
        arm.zRot = Mth.lerp(w, arm.zRot, 0.0F);
        arm.z -= 1.5F * w;
    }
}
