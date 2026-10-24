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
    public static final int DEFAULT_XP_COST = 20;
    /** Piece index of "the whole suit" in the blueprint. */
    public static final int ALL = 4;

    private static final Map<Suit, SuitInfo> INFO = new LinkedHashMap<>();

    static {
        spider(Suits.SPIDER_MAN, DyeColor.RED, DyeColor.BLUE, DyeColor.BLACK, new int[]{3, 7, 4, 4},
                SuitTag.CLASSIC, SuitTag.COMICS, SuitTag.FABRIC, SuitTag.SPIDER, SuitTag.BRIGHT, SuitTag.STREET);
        spider(Suits.SPIDER_MAN_FIRST_APPEARANCE, DyeColor.RED, DyeColor.BLUE, DyeColor.BLACK, new int[]{3, 6, 4, 3},
                SuitTag.RETRO, SuitTag.CLASSIC, SuitTag.COMICS, SuitTag.FABRIC, SuitTag.SPIDER);
        spider(Suits.SPIDER_MAN_ADVANCED_2, DyeColor.RED, DyeColor.WHITE, DyeColor.BLACK, new int[]{5, 7, 4, 6},
                SuitTag.GAMES, SuitTag.TECH, SuitTag.MODERN, SuitTag.ARMORED, SuitTag.SPIDER);
        spider(Suits.SPIDER_MAN_BRAND_NEW_DAY, DyeColor.RED, DyeColor.BLUE, DyeColor.BLACK, new int[]{3, 7, 5, 4},
                SuitTag.COMICS, SuitTag.MODERN, SuitTag.FABRIC, SuitTag.SPIDER, SuitTag.STREET, SuitTag.DARK);
        spider(Suits.SPIDER_MAN_RAIMI, DyeColor.RED, DyeColor.BLUE, DyeColor.BLACK, new int[]{4, 7, 3, 5},
                SuitTag.MOVIE, SuitTag.CLASSIC, SuitTag.FABRIC, SuitTag.SPIDER, SuitTag.BRIGHT);
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
     * A cloth Spider-Man suit: {@code primary} fabric for most of it, {@code secondary} for the legs / sides,
     * {@code accent} for the eyes and web lines, and string. No abilities of its own (those come from gadgets).
     */
    private static void spider(Suit suit, DyeColor primary, DyeColor secondary, DyeColor accent, int[] stats, SuitTag... tags) {
        Supplier<? extends Item> p = PUItems.FABRICS.get(primary);
        Supplier<? extends Item> s = PUItems.FABRICS.get(secondary);
        Supplier<? extends Item> a = PUItems.FABRICS.get(accent);
        Supplier<? extends Item> string = () -> Items.STRING;
        List<List<SuitInfo.Material>> materials = List.of(
                List.of(m(p, 4), m(a, 1), m(string, 1)),
                List.of(m(p, 5), m(s, 3), m(a, 1), m(string, 2)),
                List.of(m(s, 4), m(p, 2), m(string, 1)),
                List.of(m(p, 2), m(s, 1), m(string, 1)));
        INFO.put(suit, new SuitInfo("spider_man", List.of(tags), stats, List.of(), materials, DEFAULT_XP_COST));
    }

    private static SuitInfo.Material m(Supplier<? extends Item> item, int count) {
        return new SuitInfo.Material(item, count);
    }
}
