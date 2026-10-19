package net.threetag.palladium.mixin;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "getDesiredPose", at = @At("RETURN"), cancellable = true)
    private void getDesiredPose(CallbackInfoReturnable<Pose> cir) {
        var player = (Player) (Object) this;

        if (cir.getReturnValue() == Pose.CROUCHING && EntityFlightHandler.get(player).isFlying()) {
            cir.setReturnValue(Pose.STANDING);
        }
    }

    @Inject(method = "getSpeed", at = @At("RETURN"), cancellable = true)
    private void getSpeed(CallbackInfoReturnable<Float> cir) {
        var sizeChangeData = PalladiumEntityData.opt((Player) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        sizeChangeData.ifPresent(changeData -> cir.setReturnValue(cir.getReturnValue() * changeData.getSpeedMultiplier()));
    }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void tryToStartFallFlying(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        if (AbilityUtil.isTypeEnabled(player, AbilitySerializers.PREVENT_GLIDING.get())) {
            cir.setReturnValue(false);
        }
    }

}
