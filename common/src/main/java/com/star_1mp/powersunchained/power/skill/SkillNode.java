package com.star_1mp.powersunchained.power.skill;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * One skill of a power's tree: bought once with that power's skill points ({@link #cost}) after every node in
 * {@link #requires} is owned. {@link #col} 0..2 inside the branch (1 = middle), {@link #row} from the top.
 * {@link #icon} = an 18x18 GUI sprite ({@code textures/gui/sprites/ability/*} or the owner's {@code power/*}).
 * Names / descriptions: {@code skill.powers_unchained.<power>.<id>} (+ {@code .desc}).
 */
public record SkillNode(String power, String id, SkillBranch branch, int col, int row, int cost, List<String> requires,
        Identifier icon) {

    public Component title() {
        return Component.translatable("skill.powers_unchained." + this.power + "." + this.id);
    }

    public Component description() {
        return Component.translatable("skill.powers_unchained." + this.power + "." + this.id + ".desc");
    }
}
