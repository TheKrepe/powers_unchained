package com.star_1mp.powersunleashed.suit;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;

/**
 * Suit Creator data of a suit: catalog group, tags, dossier stats and abilities, blueprint materials and how it is
 * unlocked.
 *
 * @param group      hero group (lang {@code suit_group.powers_unleashed.<group>})
 * @param tags       catalog tags
 * @param stats      dossier stats 1-8: protection, agility, stealth, durability
 * @param abilities  lang keys of the abilities the suit itself gives (empty: none — e.g. Spider-Man's come from
 *                   gadgets, not the suit)
 * @param materials  blueprint per piece: mask, chestplate, pants, boots
 * @param xpCost     experience levels to unlock it without scanning a piece of it
 */
public record SuitInfo(
        String group,
        List<SuitTag> tags,
        int[] stats,
        List<String> abilities,
        List<List<Material>> materials,
        int xpCost) {

    /** {@code count} of an item, resolved lazily (items are registered later). */
    public record Material(Supplier<? extends Item> item, int count) {
    }
}
