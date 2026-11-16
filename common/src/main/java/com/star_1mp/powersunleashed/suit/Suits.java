package com.star_1mp.powersunleashed.suit;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import com.star_1mp.powersunleashed.Constants;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

/** All suits of the mod. */
public final class Suits {

    /*
     * All suits share the same stats: a full set gives a full armor bar (20 points: mask 3, suit 8, pants 6,
     * boots 3), no toughness, durability multiplier 18, leather equip sound.
     * Spider-Man suits are repaired with red fabric (owner): item tag powers_unleashed:repairs_spider_man_suits.
     */

    /** Spider-Man (classic). */
    public static final Suit SPIDER_MAN = cloth("spider_man");

    /** Spider-Man (First Appearance, 1962). */
    public static final Suit SPIDER_MAN_FIRST_APPEARANCE = cloth("spider_man_first_appearance");

    /** Spider-Man (Advanced 2.0). */
    public static final Suit SPIDER_MAN_ADVANCED_2 = cloth("spider_man_advanced_2");

    /** Spider-Man (Brand New Day). */
    public static final Suit SPIDER_MAN_BRAND_NEW_DAY = cloth("spider_man_brand_new_day");

    /** Spider-Man (Raimi). */
    public static final Suit SPIDER_MAN_RAIMI = cloth("spider_man_raimi");

    /** Spider-Man (Marvel's Midnight Suns). */
    public static final Suit SPIDER_MAN_MARVEL_MIDNIGHT = cloth("spider_man_marvel_midnight");

    /**
     * Groups of suit variants (different items, same hero). In the Suits creative tab only the first suit of
     * a group is shown; scrolling the mouse wheel over one of its pieces cycles through the group.
     */
    public static final List<List<Suit>> VARIANT_GROUPS = List.of(
            List.of(SPIDER_MAN, SPIDER_MAN_FIRST_APPEARANCE, SPIDER_MAN_ADVANCED_2, SPIDER_MAN_BRAND_NEW_DAY, SPIDER_MAN_RAIMI,
                    SPIDER_MAN_MARVEL_MIDNIGHT));

    /** Every suit, in creative-tab order. */
    public static final List<Suit> ALL = VARIANT_GROUPS.stream().flatMap(List::stream).toList();

    /** Repairs every Spider-Man suit in an anvil (data: red fabric). */
    public static final TagKey<Item> REPAIRS_SPIDER_MAN_SUITS = TagKey.create(Registries.ITEM, Constants.id("repairs_spider_man_suits"));

    private Suits() {
    }

    /** A cloth suit with the shared suit stats. */
    private static Suit cloth(String name) {
        return new Suit(
                Constants.id(name),
                texture(name + "/" + name + "_mask"),
                texture(name + "/" + name + "_chestplate"),
                texture(name + "/" + name + "_chestplate_slim"),
                texture(name + "/" + name + "_pants"),
                texture(name + "/" + name + "_boots"),
                new ArmorMaterial(
                        18,
                        Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3),
                        15,
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        0.0F,
                        0.0F,
                        REPAIRS_SPIDER_MAN_SUITS,
                        ResourceKey.create(EquipmentAssets.ROOT_ID, Constants.id(name))),
                REPAIRS_SPIDER_MAN_SUITS);
    }

    /** Returns the variant {@code offset} steps away from {@code suit} in its group (wrapping), or itself. */
    public static Suit variant(Suit suit, int offset) {
        for (List<Suit> group : VARIANT_GROUPS) {
            int index = group.indexOf(suit);
            if (index >= 0) {
                return group.get(Math.floorMod(index + offset, group.size()));
            }
        }
        return suit;
    }

    /** Position of {@code suit} in its variant group (0-based) and the group size; {@code null} if not grouped. */
    public static int[] variantPosition(Suit suit) {
        for (List<Suit> group : VARIANT_GROUPS) {
            int index = group.indexOf(suit);
            if (index >= 0) {
                return group.size() > 1 ? new int[]{index, group.size()} : null;
            }
        }
        return null;
    }

    /** Whether {@code suit} is shown directly in the Suits tab (first of its group, or not grouped). */
    public static boolean isPrimaryVariant(Suit suit) {
        int[] pos = variantPosition(suit);
        return pos == null || pos[0] == 0;
    }

    /** Finds a suit by id path (e.g. {@code spider_man_raimi}). */
    public static @org.jspecify.annotations.Nullable Suit byName(String name) {
        return ALL.stream().filter(s -> s.id().getPath().equals(name)).findFirst().orElse(null);
    }

    /** Lang key of the suit's variant name, e.g. "Advanced 2.0". */
    public static String variantNameKey(Suit suit) {
        return "suit." + suit.id().getNamespace() + "." + suit.id().getPath();
    }

    private static Identifier texture(String path) {
        return Constants.id("textures/suit/" + path + ".png");
    }
}
