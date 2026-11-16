package com.star_1mp.powersunleashed.irradiation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * "Радиоактивная лихорадка": 30 s after the Radioactive Spider's bite; on its last tick the Spider-Man power awakens
 * ({@link RadioactiveBite#awaken}). Milk / clearing can't remove it (loader hook cancels the removal), so the powers
 * always come.
 */
public class RadioactiveFeverEffect extends MobEffect {

    public RadioactiveFeverEffect() {
        super(MobEffectCategory.NEUTRAL, 0x58F05A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int remainingDuration, int amplification) {
        return remainingDuration == 1;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplification) {
        if (entity instanceof ServerPlayer player) {
            RadioactiveBite.awaken(player);
        }
        return true;
    }
}
