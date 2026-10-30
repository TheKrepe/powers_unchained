package com.star_1mp.powersunchained.suit;

import com.star_1mp.powersunchained.registry.PUItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * One Suit Creator blueprint as a plain recipe (for the recipe viewers JEI / REI): the materials of a piece (or the
 * whole suit) and what it sews.
 *
 * @param piece   0-3 = mask, chestplate, pants, boots; {@link SuitCatalog#ALL} = the whole suit
 * @param inputs  materials (stack counts = amounts)
 * @param outputs the piece, or all four pieces
 */
public record SuitRecipe(Suit suit, int piece, List<ItemStack> inputs, List<ItemStack> outputs) {

    public static final ArmorType[] PIECES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};

    /** Every blueprint: per suit the whole suit first, then its four pieces. */
    public static List<SuitRecipe> all() {
        List<SuitRecipe> list = new ArrayList<>();
        for (Suit suit : Suits.ALL) {
            if (SuitCatalog.info(suit) == null) {
                continue;
            }
            list.add(of(suit, SuitCatalog.ALL));
            for (int piece = 0; piece < 4; piece++) {
                list.add(of(suit, piece));
            }
        }
        return list;
    }

    public static SuitRecipe of(Suit suit, int piece) {
        List<ItemStack> inputs = new ArrayList<>();
        for (SuitInfo.Material m : SuitCatalog.cost(suit, piece)) {
            inputs.add(new ItemStack(m.item().get(), m.count()));
        }
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (piece == SuitCatalog.ALL || piece == i) {
                outputs.add(PUItems.piece(suit, PIECES[i]).get().getDefaultInstance());
            }
        }
        return new SuitRecipe(suit, piece, List.copyOf(inputs), List.copyOf(outputs));
    }

    public boolean wholeSuit() {
        return this.piece == SuitCatalog.ALL;
    }
}
