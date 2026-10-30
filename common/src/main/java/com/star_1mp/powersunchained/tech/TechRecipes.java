package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.registry.PUItems;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/** Every Tech Table recipe (Creation tab), in list order. */
public final class TechRecipes {

    public static final String SPIDER_MAN = "spider_man";

    private static @Nullable List<TechRecipe> all;

    private TechRecipes() {
    }

    private static Supplier<Item> v(Item item) {
        return () -> item;
    }

    private static TechIngredient i(Item item) {
        return TechIngredient.of(v(item));
    }

    public static List<TechRecipe> all() {
        if (all == null) {
            TechIngredient empty = TechIngredient.of(PUItems.EMPTY_WEB_CARTRIDGE);
            all = List.of(
                    // owner's recipe
                    TechRecipe.shaped("web_shooters", SPIDER_MAN, new String[]{"IDI", "EPE", "ILI"},
                            Map.of('I', i(Items.IRON_INGOT), 'D', i(Items.DISPENSER), 'E', empty, 'P', i(Items.STICKY_PISTON),
                                    'L', i(Items.LEVER)),
                            PUItems.WEB_SHOOTERS, 1),
                    TechRecipe.shaped("empty_web_cartridge", SPIDER_MAN, new String[]{"I", "G", "I"},
                            Map.of('I', i(Items.IRON_INGOT), 'G', i(Items.GLASS)),
                            PUItems.EMPTY_WEB_CARTRIDGE, 1),
                    // owner: slime ball + cobweb + empty cartridge (the oozing potion / honey recipes are gone)
                    TechRecipe.shapeless("web_cartridge", SPIDER_MAN, List.of(empty, i(Items.COBWEB), i(Items.SLIME_BALL)),
                            PUItems.WEB_CARTRIDGE, 1));
        }
        return all;
    }

    public static @Nullable TechRecipe byId(String id) {
        for (TechRecipe r : all()) {
            if (r.id().equals(id)) {
                return r;
            }
        }
        return null;
    }

    /** The recipe the 9 grid stacks make, or null. */
    public static @Nullable TechRecipe find(List<ItemStack> grid) {
        for (TechRecipe r : all()) {
            if (r.matches(grid)) {
                return r;
            }
        }
        return null;
    }
}
