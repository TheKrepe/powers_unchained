package com.star_1mp.powersunchained.power;

import com.star_1mp.powersunchained.network.RollPayload;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.web.WebShooting;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server side of the roll: the local client moves the roller (dash / flip impulse); the server checks the power and
 * tells everyone else watching, for the animation.
 */
public final class Rolling {

    public static final int KIND_ROLL = 0;
    public static final int KIND_FLIP = 1;
    /** Back flip off a wall (facing it). */
    public static final int KIND_BACKFLIP = 2;

    private Rolling() {
    }

    public static void roll(ServerPlayer player, int kind, float yaw) {
        if (!Superpowers.isActive(player, com.star_1mp.powersunchained.power.PowerAbility.ROLL) || (kind < KIND_ROLL || kind > KIND_BACKFLIP)) {
            return;
        }
        if (kind != KIND_ROLL && !PowerSkills.spider(player, com.star_1mp.powersunchained.power.skill.SkillTrees.FLIPS)) {
            return;  // flips are a skill
        }
        if (kind != KIND_ROLL) {
            // a flip off a jump or a web lands softly
            WebShooting.markFallSafe(player);
        }
        PowerSkills.spiderXp(player, PowerSkills.XP_ROLL);
        Services.GAMEPLAY.sendToTrackingAndSelf(player, new com.star_1mp.powersunchained.network.RollAnimPayload(player.getId(), kind, yaw));
    }
}
