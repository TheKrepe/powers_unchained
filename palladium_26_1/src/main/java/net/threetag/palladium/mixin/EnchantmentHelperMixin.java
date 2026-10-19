package net.threetag.palladium.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.threetag.palladium.item.PalladiumMaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "doPostAttackEffectsWithItemSource", at = @At("HEAD"), cancellable = true)
    private static void palladium$overrideWindBurst(ServerLevel level, Entity target, DamageSource source,
                                                    ItemStack stack, CallbackInfo callbackInfo) {
        if (PalladiumMaceItem.overridesWindBurst(stack)) {
            callbackInfo.cancel();
        }
    }
}
