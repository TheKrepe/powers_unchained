package net.threetag.palladium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.threetag.palladium.item.PalladiumMaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public class MaceItemMixin {

    @ModifyConstant(method = "canSmashAttack", constant = @Constant(doubleValue = 1.5D))
    private static double palladium$smashFallThreshold(double value, @Local(argsOnly = true, name = "attacker") LivingEntity entity) {
        return PalladiumMaceItem.smashThreshold(PalladiumMaceItem.of(entity), value);
    }

    @ModifyConstant(method = "hurtEnemy", constant = @Constant(doubleValue = 5.0D))
    private double palladium$heavySmashThresholdSound(double value) {
        return PalladiumMaceItem.heavyThreshold((Object) this instanceof PalladiumMaceItem mace ? mace : null, value);
    }

    @ModifyConstant(method = "getKnockbackPower", constant = @Constant(doubleValue = 5.0D))
    private static double palladium$heavySmashThresholdKnockback(double value, @Local(argsOnly = true, name = "attacker") Entity attacker) {
        return PalladiumMaceItem.heavyThreshold(PalladiumMaceItem.of(attacker), value);
    }

    @ModifyConstant(method = {"knockback", "getKnockbackPower"}, constant = @Constant(doubleValue = 3.5D))
    private static double palladium$knockbackRadius(double value, @Local(argsOnly = true, name = "attacker") Entity attacker) {
        return PalladiumMaceItem.knockbackRadius(PalladiumMaceItem.of(attacker), value);
    }

    @ModifyConstant(method = "getKnockbackPower", constant = @Constant(doubleValue = 0.699999988079071D))
    private static double palladium$knockbackMultiplier(double value, @Local(argsOnly = true, name = "attacker") Entity attacker) {
        return PalladiumMaceItem.knockbackFactor(PalladiumMaceItem.of(attacker), value);
    }

    @ModifyConstant(method = "getKnockbackPower", constant = @Constant(intValue = 2))
    private static int palladium$heavyKnockbackMultiplier(int value, @Local(argsOnly = true, name = "attacker") Entity attacker) {
        return PalladiumMaceItem.heavyKnockbackFactor(PalladiumMaceItem.of(attacker), value);
    }

    @Redirect(method = "canSmashAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private static boolean palladium$allowFallFlyingSmash(LivingEntity entity) {
        return !PalladiumMaceItem.allowFallFlyingSmash(PalladiumMaceItem.of(entity)) && entity.isFallFlying();
    }

    @Redirect(method = "postHurtEnemy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;resetFallDistance()V"))
    private void palladium$resetFallDistance(LivingEntity entity) {
        PalladiumMaceItem mace = (Object) this instanceof PalladiumMaceItem m ? m : null;
        if (PalladiumMaceItem.resetFallDistanceEnabled(mace)) {
            entity.resetFallDistance();
        }
    }

    @Inject(method = "postHurtEnemy", at = @At("HEAD"))
    private void palladium$windBurst(ItemStack itemStack, LivingEntity mob, LivingEntity attacker, CallbackInfo callbackInfo) {
        if (itemStack.getItem() instanceof PalladiumMaceItem mace && mace.windBurstLevel() > 0 && MaceItem.canSmashAttack(attacker)) {
            attacker.push(0.0D, 0.4D + mace.windBurstLevel() * 0.1D, 0.0D);
            attacker.hurtMarked = true;
        }
    }

    @Inject(method = "getAttackDamageBonus", at = @At("RETURN"), cancellable = true)
    private void palladium$smashDamageMultiplier(Entity victim, float ignoredDamage, DamageSource damageSource,
                                                 CallbackInfoReturnable<Float> callbackInfo) {
        PalladiumMaceItem mace = (Object) this instanceof PalladiumMaceItem m ? m : null;
        float damageBonus = callbackInfo.getReturnValue() + PalladiumMaceItem.densityBonus(mace, damageSource);
        callbackInfo.setReturnValue(damageBonus * PalladiumMaceItem.damageMultiplier(mace));
    }

    @Redirect(method = "getAttackDamageBonus", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyFallBasedDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float palladium$overrideDensity(ServerLevel serverLevel, ItemStack itemStack, Entity victim,
                                            DamageSource damageSource, float damage) {
        PalladiumMaceItem mace = (Object) this instanceof PalladiumMaceItem m ? m : null;
        return PalladiumMaceItem.overridesDensity(mace)
                ? 0.0F
                : EnchantmentHelper.modifyFallBasedDamage(serverLevel, itemStack, victim, damageSource, damage);
    }
}
