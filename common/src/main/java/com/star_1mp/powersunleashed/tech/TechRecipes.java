package com.star_1mp.powersunleashed.tech;

import com.star_1mp.powersunleashed.registry.PUItems;
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
    /** Isotope capsule from a radioactive spider eye (owner: hidden until the player has had such an eye). */
    public static final String CHEAP_CAPSULE = "isotope_capsule_from_eye";

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
                            PUItems.WEB_CARTRIDGE, 1),
                    // owner: the Irradiator and its fuel are made only on the Tech Table
                    TechRecipe.shaped("irradiator", SPIDER_MAN, new String[]{"ITI", "GLG", "BRB"},
                            Map.of('I', i(Items.IRON_INGOT), 'T', i(Items.TINTED_GLASS), 'G', i(Items.GLASS), 'L', i(Items.GLOWSTONE),
                                    'B', i(Items.IRON_BLOCK), 'R', i(Items.REDSTONE_BLOCK)),
                            PUItems.IRRADIATOR, 1),
                    TechRecipe.shapeless("isotope_capsule", SPIDER_MAN, List.of(empty, i(Items.GLOWSTONE_DUST), i(Items.GLOWSTONE_DUST),
                                    i(Items.GLOWSTONE_DUST), i(Items.GLOWSTONE_DUST), i(Items.REDSTONE_BLOCK), i(Items.AMETHYST_SHARD)),
                            PUItems.ISOTOPE_CAPSULE, 1),
                    // the cheaper capsule: only shown once the player has had a radioactive spider eye
                    TechRecipe.shapeless(CHEAP_CAPSULE, SPIDER_MAN, List.of(empty, TechIngredient.of(PUItems.RADIOACTIVE_SPIDER_EYE),
                                    i(Items.GLOWSTONE_DUST), i(Items.GLOWSTONE_DUST), i(Items.REDSTONE)),
                            PUItems.ISOTOPE_CAPSULE, 1));
        }
        return all;
    }

    /** Whether the player may see the recipe in the list (hidden ones need a "seen" feat). */
    public static boolean isVisible(TechRecipe recipe, net.minecraft.world.entity.player.Player player) {
        return !recipe.id().equals(CHEAP_CAPSULE)
                || com.star_1mp.powersunleashed.platform.Services.GAMEPLAY.techProgress(player).hasFeat(TechFeats.SEEN_RADIOACTIVE_EYE);
    }

    /** Recipes shown in JEI / REI (the hidden ones are not: the player discovers them). */
    public static List<TechRecipe> viewerRecipes() {
        return all().stream().filter(r -> !r.id().equals(CHEAP_CAPSULE)).toList();
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
