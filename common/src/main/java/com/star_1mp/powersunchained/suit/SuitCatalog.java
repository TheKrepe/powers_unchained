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

    private static final Map<Suit, SuitInfo> INFO = new LinkedHashMap<>();

    /** Non-fabric materials usable in the recipe strings (declared before the static block that uses it). */
    private static final Map<String, Supplier<? extends Item>> OTHER_ITEMS = Map.of(
            "string", () -> Items.STRING,
            "tinted_glass", () -> Items.TINTED_GLASS,
            "iron_ingot", () -> Items.IRON_INGOT);


    static {
        // materials per piece (mask | chestplate | pants | boots): "<item> <count>, ..." where the item is a dye colour
        // (= that fabric), "string", "tinted_glass" or "iron_ingot". Totals for the owner's recipes in the comments.
        // Classic: 16 red, 10 blue, 4 black, 2 white fabric, 1 tinted glass, 5 string
        spider(Suits.SPIDER_MAN,
                "red 4, black 1, white 2, tinted_glass 1, string 1 | red 8, blue 4, black 2, string 2 | blue 6, black 1, string 1 | red 4, string 1",
                new int[]{3, 7, 4, 4}, SuitTag.MARVEL, SuitTag.COMICS);
        spider(Suits.SPIDER_MAN_FIRST_APPEARANCE,
                "red 4, black 1, string 1 | red 3, blue 3, black 1, string 2 | blue 4, string 1 | red 2, black 1, string 1",
                new int[]{3, 6, 4, 3}, SuitTag.MARVEL, SuitTag.COMICS, SuitTag.FIRST_APPEARANCE);
        // Advanced 2.0: 14 red, 8 blue, 2 black, 8 white fabric, 1 tinted glass, 5 string
        spider(Suits.SPIDER_MAN_ADVANCED_2,
                "red 4, white 2, black 1, tinted_glass 1, string 1 | red 6, white 4, blue 2, black 1, string 2 | blue 6, string 1 | red 4, white 2, string 1",
                new int[]{5, 7, 4, 6}, SuitTag.MARVEL, SuitTag.GAMES);
        // Brand New Day: 16 red, 10 blue, 4 black fabric, 2 tinted glass, 5 string
        spider(Suits.SPIDER_MAN_BRAND_NEW_DAY,
                "red 4, black 1, tinted_glass 2, string 1 | red 8, blue 4, black 2, string 2 | blue 6, black 1, string 1 | red 4, string 1",
                new int[]{3, 7, 5, 4}, SuitTag.MARVEL, SuitTag.MOVIE, SuitTag.MCU);
        // Raimi: 16 red, 8 blue, 4 black, 2 white fabric, 1 tinted glass, 6 string, 1 iron ingot
        spider(Suits.SPIDER_MAN_RAIMI,
                "red 4, black 1, white 2, tinted_glass 1, iron_ingot 1, string 1 | red 8, blue 3, black 2, string 2 | blue 5, black 1, string 2 | red 4, string 1",
                new int[]{4, 7, 3, 5}, SuitTag.MARVEL, SuitTag.MOVIE);
        spider(Suits.SPIDER_MAN_MARVEL_MIDNIGHT,
                "red 4, black 1, string 1 | red 4, black 2, purple 1, string 2 | black 3, purple 1, string 1 | red 2, black 1, string 1",
                new int[]{4, 7, 6, 5}, SuitTag.MARVEL, SuitTag.COMICS, SuitTag.MARVEL_MIDNIGHT);
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
     * A cloth Spider-Man suit. {@code recipe}: materials per piece, "item count, ..." separated by "|" in the order
     * mask | chestplate | pants | boots; the item is a dye colour name (that fabric) or one of {@link #OTHER_ITEMS}.
     * No abilities of its own (those come from gadgets).
     */
    private static void spider(Suit suit, String recipe, int[] stats, SuitTag... tags) {
        String[] pieces = recipe.split("\\|");
        List<List<SuitInfo.Material>> materials = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<SuitInfo.Material> list = new ArrayList<>();
            for (String entry : pieces[i].split(",")) {
                String[] parts = entry.trim().split(" ");
                list.add(m(item(parts[0], suit), Integer.parseInt(parts[1])));
            }
            materials.add(List.copyOf(list));
        }
        INFO.put(suit, new SuitInfo("spider_man", List.of(tags), stats, List.of(), List.copyOf(materials), DEFAULT_XP_COST));
    }

    private static Supplier<? extends Item> item(String name, Suit suit) {
        Supplier<? extends Item> other = OTHER_ITEMS.get(name);
        if (other != null) {
            return other;
        }
        DyeColor color = java.util.Arrays.stream(DyeColor.values()).filter(c -> c.getSerializedName().equals(name))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown material " + name + " in " + suit.id()));
        return PUItems.FABRICS.get(color);
    }

    private static SuitInfo.Material m(Supplier<? extends Item> item, int count) {
        return new SuitInfo.Material(item, count);
    }
}
