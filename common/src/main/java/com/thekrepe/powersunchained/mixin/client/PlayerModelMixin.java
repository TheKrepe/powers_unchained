package com.thekrepe.powersunchained.mixin.client;

import com.thekrepe.powersunchained.client.web.WebArmAnimation;
import com.thekrepe.powersunchained.client.web.WebZipAnimation;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Web-shooter animations on player-shaped models (the player and everything drawn with a PlayerModel on top of
 * it: suits, web-shooters). There is no loader event after the vanilla pose, hence the mixin.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("RETURN"))
    private void powers_unchained$webShooters(AvatarRenderState state, CallbackInfo ci) {
        float partialTick = state.ageInTicks - Mth.floor(state.ageInTicks);
        WebArmAnimation.apply((PlayerModel) (Object) this, state.id, partialTick);
        // after the web shot arm poses: the zip poses take over the arms while zipping
        WebZipAnimation.applyParts((PlayerModel) (Object) this, state.id, partialTick);
    }
}
