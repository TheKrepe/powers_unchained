package com.star_1mp.powersunleashed.power;

import com.star_1mp.powersunleashed.power.skill.SkillNode;
import com.star_1mp.powersunleashed.power.skill.SkillTrees;

/**
 * Power levels (owner: experience from using the power -> levels -> 1 skill point per level, like Alien Evolution).
 * Level L -> L + 1 needs {@code 30 + 15 * L} experience; the cap is the cost of the whole tree.
 */
public final class PowerLevels {

    private PowerLevels() {
    }

    public static int maxLevel(Superpower power) {
        return Math.max(1, SkillTrees.totalCost(power));
    }

    /** Experience from level L to L + 1. */
    public static int xpForNext(int level) {
        return 30 + 15 * level;
    }

    public static int level(Superpower power, int xp) {
        int level = 0;
        int max = maxLevel(power);
        while (level < max && xp >= xpForNext(level)) {
            xp -= xpForNext(level);
            level++;
        }
        return level;
    }

    /** Experience gathered inside the current level (for the bar). */
    public static int xpInLevel(Superpower power, int xp) {
        int level = 0;
        int max = maxLevel(power);
        while (level < max && xp >= xpForNext(level)) {
            xp -= xpForNext(level);
            level++;
        }
        return level >= max ? xpForNext(level - 1) : xp;
    }

    public static int spent(Superpower power, PowerProgress.Entry entry) {
        int sum = 0;
        for (String id : entry.nodes()) {
            SkillNode n = SkillTrees.node(power, id);
            if (n != null) {
                sum += n.cost();
            }
        }
        return sum;
    }

    /** Free skill points. */
    public static int points(Superpower power, PowerProgress.Entry entry) {
        return Math.max(0, level(power, entry.xp()) - spent(power, entry));
    }
}
