package com.star_1mp.powersunchained.tech;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** One cell of a Tech Table recipe: what fits there, and what to show for it (ghost items, recipe viewers). */
public record TechIngredient(Predicate<ItemStack> test, Supplier<List<ItemStack>> display) {

    public static TechIngredient of(Supplier<? extends Item> item) {
        return new TechIngredient(stack -> stack.is(item.get()), () -> List.of(new ItemStack(item.get())));
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && this.test.test(stack);
    }

    /** The first display stack (what the ghost recipe shows). */
    public ItemStack example() {
        List<ItemStack> list = this.display.get();
        return list.isEmpty() ? ItemStack.EMPTY : list.getFirst();
    }
}
