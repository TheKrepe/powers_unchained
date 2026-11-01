package com.star_1mp.powersunchained.power.skill;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** The three branches of a power's skill tree (Spider-Man: agility / sense / combat), left to right. */
public enum SkillBranch {
    AGILITY(0xFF7CFC6A),
    SENSE(0xFFC08CFF),
    COMBAT(0xFFFF6A55);

    private final int color;

    SkillBranch(int color) {
        this.color = color;
    }

    public int color() {
        return this.color;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("skill_branch.powers_unchained." + this.id());
    }
}
