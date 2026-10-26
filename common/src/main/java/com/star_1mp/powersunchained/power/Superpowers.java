package com.star_1mp.powersunchained.power;

import com.star_1mp.powersunchained.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/** Owning / switching superpowers and their server side effects. */
public final class Superpowers {

    private Superpowers() {
    }

    public static PowerData data(Player player) {
        return Services.GAMEPLAY.powerData(player);
    }

    public static boolean has(Player player, PowerAbility ability) {
        return data(player).has(ability);
    }

    public static boolean isActive(Player player, PowerAbility ability) {
        return data(player).isActive(ability);
    }

    public static void add(ServerPlayer player, Superpower power) {
        Services.GAMEPLAY.setPowerData(player, data(player).add(power));
    }

    public static void remove(ServerPlayer player, Superpower power) {
        Services.GAMEPLAY.setPowerData(player, data(player).remove(power));
        if (!isActive(player, PowerAbility.NIGHT_VISION)) {
            clearNightVision(player);
        }
        if (!isActive(player, PowerAbility.WALL_CLIMBING)) {
            WallClimbing.stop(player);
        }
    }

    /** From the ability wheel. */
    public static void toggle(ServerPlayer player, PowerAbility power) {
        PowerData data = data(player);
        if (!data.has(power) || !power.toggleable()) {
            return;
        }
        Services.GAMEPLAY.setPowerData(player, data.toggle(power));
        if (power == PowerAbility.NIGHT_VISION && !data(player).isActive(power)) {
            clearNightVision(player);
        }
        if (power == PowerAbility.WALL_CLIMBING && !data(player).isActive(power)) {
            WallClimbing.stop(player);
        }
    }

    /** Server, every player tick. */
    public static void serverTick(ServerPlayer player) {
        if (isActive(player, PowerAbility.NIGHT_VISION)) {
            MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
            // refreshed before it gets near 10 seconds left, where vanilla starts to flicker the screen
            if (current == null || current.getDuration() < 260) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, false));
            }
        }
        WallClimbing.serverTick(player);
    }

    private static void clearNightVision(ServerPlayer player) {
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current != null && current.isAmbient() && !current.isVisible()) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
