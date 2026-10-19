package net.threetag.palladium.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.threetag.palladium.entity.CustomProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    @Inject(method = "computeMargin", at = @At("RETURN"), cancellable = true)
    private static void palladium$projectileSizeMargin(Entity source, CallbackInfoReturnable<Float> cir) {
        if (source instanceof CustomProjectile projectile) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), projectile.getBbWidth() / 2F));
        }
    }
}
