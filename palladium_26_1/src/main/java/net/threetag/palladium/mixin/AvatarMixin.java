package net.threetag.palladium.mixin;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Avatar.class)
public class AvatarMixin {

    @Inject(method = "getDefaultDimensions", at = @At("HEAD"), cancellable = true)
    public void getDefaultDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        Avatar entity = (Avatar) (Object) this;
        var controller = EntityFlightHandler.get(entity).getController();

        if (controller != null && controller.hasSmallHitbox(entity)) {
            cir.setReturnValue(Avatar.POSES.get(Pose.FALL_FLYING));
        }
    }

}