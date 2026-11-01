package com.star_1mp.powersunchained.power.skill;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * One skill of a power's tree: bought once with that power's skill points ({@link #cost}) after every node in
 * {@link #requires} is owned. {@link #col} 0..2 inside the branch (1 = middle), {@link #row} from the top.
 * Names / descriptions: {@code skill.powers_unchained.<power>.<id>} (+ {@code .desc}).
 */
public record SkillNode(String power, String id, SkillBranch branch, int col, int row, int cost, List<String> requires,
        Supplier<ItemStack> icon) {

    public Component title() {
        return Component.translatable("skill.powers_unchained." + this.power + "." + this.id);
    }

    public Component description() {
        return Component.translatable("skill.powers_unchained." + this.power + "." + this.id + ".desc");
    }
}
