package com.star_1mp.powersunleashed.irradiation;

import com.star_1mp.powersunleashed.registry.PUItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/** What the Irradiator does to ITEMS in its sample slot (owner: spider eye -> radioactive spider eye, 70 %). */
public final class IrradiationRecipes {

    public record Recipe(Supplier<Item> input, Supplier<? extends Item> result, float chance) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && stack.is(this.input.get());
        }
    }

    private static @Nullable List<Recipe> all;

    private IrradiationRecipes() {
    }

    public static List<Recipe> all() {
        if (all == null) {
            all = List.of(new Recipe(() -> Items.SPIDER_EYE, PUItems.RADIOACTIVE_SPIDER_EYE, Irradiation.ITEM_CHANCE));
        }
        return all;
    }

    public static @Nullable Recipe find(ItemStack stack) {
        for (Recipe r : all()) {
            if (r.matches(stack)) {
                return r;
            }
        }
        return null;
    }
}
