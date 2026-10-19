package net.threetag.palladium.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @ModifyVariable(method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("STORE"), name = "naturalRegen")
    private boolean palladium$preventNaturalRegen(boolean naturalRegen, ServerPlayer player) {
        return naturalRegen && !AbilityUtil.isTypeEnabled(player, AbilitySerializers.PREVENT_HEALING.get());
    }

}
