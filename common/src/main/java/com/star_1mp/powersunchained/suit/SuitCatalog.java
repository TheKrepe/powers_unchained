package com.star_1mp.powersunchained.suit;

import com.star_1mp.powersunchained.registry.PUItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Suit Creator data of every suit (see {@link SuitInfo}). */
public final class SuitCatalog {

    /** Experience levels to unlock a suit without the scanner. */
    public static final int DEFAULT_XP_COST = 10;
    /** Piece index of "the whole suit" in the blueprint. */
    public static final int ALL = 4;

    /** String per piece (mask, chestplate, pants, boots); declared before the static block that uses it. */
    private static final int[] STRING_PER_PIECE = {1, 2, 1, 1};

    private static final Map<Suit, SuitInfo> INFO = new LinkedHashMap<>();

    static {
        // fabrics per piece (mask | chestplate | pants | boots), picked from the colours of each suit's textures;
        // string is added on top (1 / 2 / 1 / 1)
        spider(Suits.SPIDER_MAN, "red 4, black 1 | red 4, blue 2, black 2 | light_blue 3, blue 2, black 1 | red 2, black 1",
                new int[]{3, 7, 4, 4}, SuitTag.MARVEL, SuitTag.COMICS);
        spider(Suits.SPIDER_MAN_FIRST_APPEARANCE, "red 4, black 1 | red 3, blue 3, black 1 | blue 4 | red 2, black 1",
                new int[]{3, 6, 4, 3}, SuitTag.MARVEL, SuitTag.COMICS, SuitTag.FIRST_APPEARANCE);
        spider(Suits.SPIDER_MAN_ADVANCED_2, "red 2, black 2, white 1 | red 2, black 2, white 2, blue 1 | blue 3, light_blue 1, black 1 | red 1, white 1, black 1",
                new int[]{5, 7, 4, 6}, SuitTag.MARVEL, SuitTag.GAMES);
        spider(Suits.SPIDER_MAN_BRAND_NEW_DAY, "red 4, black 1 | red 4, black 2, gray 1 | black 3, gray 1, blue 1 | black 1, red 1",
                new int[]{3, 7, 5, 4}, SuitTag.MARVEL, SuitTag.MOVIE, SuitTag.MCU);
        spider(Suits.SPIDER_MAN_RAIMI, "red 4, black 1 | red 3, blue 2, black 2 | blue 3, black 2 | red 2, black 1",
                new int[]{4, 7, 3, 5}, SuitTag.MARVEL, SuitTag.MOVIE);
        spider(Suits.SPIDER_MAN_MARVEL_MIDNIGHT, "red 4, black 1 | red 4, black 2, purple 1 | black 3, purple 1 | red 2, black 1",
                new int[]{4, 7, 6, 5}, SuitTag.MARVEL, SuitTag.GAMES, SuitTag.MARVEL_MIDNIGHT);
    }

    private SuitCatalog() {
    }

    public static SuitInfo info(Suit suit) {
        return INFO.get(suit);
    }

    /** Materials of one blueprint piece (0-3) or the whole suit ({@link #ALL}), merged by item, in order. */
    public static List<SuitInfo.Material> cost(Suit suit, int piece) {
        SuitInfo info = info(suit);
        if (piece != ALL) {
            return info.materials().get(piece);
        }
        Map<Supplier<? extends Item>, Integer> sum = new LinkedHashMap<>();
        for (List<SuitInfo.Material> list : info.materials()) {
            for (SuitInfo.Material m : list) {
                sum.merge(m.item(), m.count(), Integer::sum);
            }
        }
        List<SuitInfo.Material> out = new ArrayList<>();
        sum.forEach((item, count) -> out.add(new SuitInfo.Material(item, count)));
        return out;
    }

    /**
     * A cloth Spider-Man suit. {@code recipe}: fabrics per piece, "colour count, ..." separated by "|" in the order
     * mask | chestplate | pants | boots (dye colour names). No abilities of its own (those come from gadgets).
     */
    private static void spider(Suit suit, String recipe, int[] stats, SuitTag... tags) {
        String[] pieces = recipe.split("\\|");
        List<List<SuitInfo.Material>> materials = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<SuitInfo.Material> list = new ArrayList<>();
            for (String entry : pieces[i].split(",")) {
                String[] parts = entry.trim().split(" ");
                DyeColor color = java.util.Arrays.stream(DyeColor.values()).filter(c -> c.getSerializedName().equals(parts[0]))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown dye colour " + parts[0] + " in " + suit.id()));
                list.add(m(PUItems.FABRICS.get(color), Integer.parseInt(parts[1])));
            }
            list.add(m(() -> Items.STRING, STRING_PER_PIECE[i]));
            materials.add(List.copyOf(list));
        }
        INFO.put(suit, new SuitInfo("spider_man", List.of(tags), stats, List.of(), List.copyOf(materials), DEFAULT_XP_COST));
    }

    private static SuitInfo.Material m(Supplier<? extends Item> item, int count) {
        return new SuitInfo.Material(item, count);
    }
}
