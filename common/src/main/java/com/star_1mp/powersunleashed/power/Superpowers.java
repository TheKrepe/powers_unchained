package com.star_1mp.powersunleashed.power;

import com.star_1mp.powersunleashed.platform.Services;
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

    /** Any owned power can grant access, with its own skill requirement. */
    public static boolean has(Player player, PowerAbility ability) {
        return has(player, data(player), ability);
    }

    private static boolean has(Player player, PowerData data, PowerAbility ability) {
        for (String id : data.owned()) {
            Superpower power = Superpower.byId(id);
            Superpower.AbilityGrant grant = power != null ? power.grant(ability) : null;
            if (grant != null && (grant.requiredSkill() == null || PowerSkills.progress(player).has(power, grant.requiredSkill()))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isActive(Player player, PowerAbility ability) {
        PowerData data = data(player);
        return data.isEnabled(ability) && has(player, data, ability);
    }

    /** Spider-Man with the Soft Landing skill takes no fall damage at all (like Fisk's Spider-Man). */
    public static boolean fallImmune(Player player) {
        return PowerSkills.spider(player, com.star_1mp.powersunleashed.power.skill.SkillTrees.SOFT_LANDING);
    }

    public static void add(ServerPlayer player, Superpower power) {
        Services.GAMEPLAY.setPowerData(player, data(player).add(power));
        PowerSkills.refreshAttributes(player);
    }

    public static void remove(ServerPlayer player, Superpower power) {
        Services.GAMEPLAY.setPowerData(player, data(player).remove(power));
        PowerSkills.refreshAttributes(player);
        if (!isActive(player, PowerAbility.NIGHT_VISION)) {
            clearNightVision(player);
        }
        if (!isActive(player, PowerAbility.WALL_CLIMBING)) {
            WallClimbing.stop(player);
        }
    }

    /** From the ability wheel. */
    public static void toggle(ServerPlayer player, PowerAbility ability) {
        PowerData data = data(player);
        if (!ability.toggleable() || !has(player, data, ability)) {
            return;
        }
        PowerData updated = data.toggle(ability);
        Services.GAMEPLAY.setPowerData(player, updated);
        if (!updated.isEnabled(ability)) {
            if (ability == PowerAbility.NIGHT_VISION) {
                clearNightVision(player);
            } else if (ability == PowerAbility.WALL_CLIMBING) {
                WallClimbing.stop(player);
            }
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
        } else if (player.tickCount % 20 == 0) {
            clearNightVision(player);
        }
        WallClimbing.serverTick(player);
        PowerSkills.serverTick(player);
    }

    private static void clearNightVision(ServerPlayer player) {
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current != null && current.isAmbient() && !current.isVisible()) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
