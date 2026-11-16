package com.star_1mp.powersunleashed.irradiation;

import com.star_1mp.powersunleashed.entity.spider.RadioactiveSpider;
import com.star_1mp.powersunleashed.power.Superpower;
import com.star_1mp.powersunleashed.power.Superpowers;
import com.star_1mp.powersunleashed.registry.PUAdvancements;
import com.star_1mp.powersunleashed.registry.PUEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * The Radioactive Spider's bite (owner): nausea + slowness I + the Radioactive Fever for 30 s; when the fever runs out
 * ({@link RadioactiveFeverEffect}) the player gets the Spider-Man power from scratch. The client shows the bite flash,
 * the fever vignette and, at the end, the first spider-sense + toast ({@code client/irradiation/FeverClient}).
 */
public final class RadioactiveBite {

    public static final int FEVER_TICKS = 600;

    private RadioactiveBite() {
    }

    public static void bite(ServerPlayer player, RadioactiveSpider spider) {
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, FEVER_TICKS + 40, 0), spider);
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, FEVER_TICKS, 0), spider);
        player.addEffect(new MobEffectInstance(PUEffects.RADIOACTIVE_FEVER.get(), FEVER_TICKS, 0, false, false, true), spider);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPIDER_HURT, SoundSource.HOSTILE, 1.0F, 1.7F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,
                SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    /** The fever ran out: the powers awaken. */
    public static void awaken(ServerPlayer player) {
        if (!Superpowers.data(player).has(Superpower.SPIDERMAN)) {
            Superpowers.add(player, Superpower.SPIDERMAN);
        }
        PUAdvancements.award(player, PUAdvancements.GREAT_POWER);
    }
}
