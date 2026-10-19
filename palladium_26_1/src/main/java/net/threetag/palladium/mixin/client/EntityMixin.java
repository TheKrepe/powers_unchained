package net.threetag.palladium.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.power.ability.EntityGlowAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void getTeamColor(CallbackInfoReturnable<Integer> cir) {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        Entity renderedEntity = (Entity) (Object) this;

        if (cameraEntity != null) {
            Integer color = EntityGlowAbility.getGlowColor(renderedEntity, cameraEntity);

            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }
}
