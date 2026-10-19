package com.thekrepe.powersunchained.suit;

import com.thekrepe.powersunchained.Constants;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

/** All suits of the mod. */
public final class Suits {

    /**
     * Spider-Man (classic). A light cloth suit: a bit better than chainmail, worse than iron.
     * Defense: mask 2, suit 5, pants 4, boots 2 (13 total). Leather equip sound, repaired with string.
     */
    public static final Suit SPIDER_MAN = cloth("spider_man", 18, 2, 5, 4, 2, 0.0F);

    /** Spider-Man (First Appearance, 1962): homemade first suit, weaker. Defense 2/4/3/1 (10). */
    public static final Suit SPIDER_MAN_FIRST_APPEARANCE = cloth("spider_man_first_appearance", 14, 2, 4, 3, 1, 0.0F);

    /** Spider-Man (Advanced 2.0): high-tech suit. Defense 3/6/5/2 (16), toughness 1 per piece. */
    public static final Suit SPIDER_MAN_ADVANCED_2 = cloth("spider_man_advanced_2", 25, 3, 6, 5, 2, 1.0F);

    /** Spider-Man (Brand New Day): same stats as the classic suit. */
    public static final Suit SPIDER_MAN_BRAND_NEW_DAY = cloth("spider_man_brand_new_day", 18, 2, 5, 4, 2, 0.0F);

    /** Spider-Man (Raimi): same stats as the classic suit. */
    public static final Suit SPIDER_MAN_RAIMI = cloth("spider_man_raimi", 18, 2, 5, 4, 2, 0.0F);

    /**
     * Groups of suit variants (different items, same hero). In the Suits creative tab only the first suit of
     * a group is shown; scrolling the mouse wheel over one of its pieces cycles through the group.
     */
    public static final List<List<Suit>> VARIANT_GROUPS = List.of(
            List.of(SPIDER_MAN, SPIDER_MAN_FIRST_APPEARANCE, SPIDER_MAN_ADVANCED_2, SPIDER_MAN_BRAND_NEW_DAY, SPIDER_MAN_RAIMI));

    /** Every suit, in creative-tab order. */
    public static final List<Suit> ALL = VARIANT_GROUPS.stream().flatMap(List::stream).toList();

    private Suits() {
    }

    /** A cloth suit: leather equip sound, repaired with string. */
    private static Suit cloth(String name, int durability, int head, int chest, int legs, int feet, float toughness) {
        return new Suit(
                Constants.id(name),
                texture(name + "/" + name),
                texture(name + "/" + name + "_slim"),
                texture(name + "/" + name + "_boots"),
                new ArmorMaterial(
                        durability,
                        Map.of(ArmorType.HELMET, head, ArmorType.CHESTPLATE, chest, ArmorType.LEGGINGS, legs, ArmorType.BOOTS, feet),
                        15,
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        toughness,
                        0.0F,
                        ItemTags.REPAIRS_LEATHER_ARMOR,
                        ResourceKey.create(EquipmentAssets.ROOT_ID, Constants.id(name))),
                Items.STRING);
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

    private static Identifier texture(String path) {
        return Constants.id("textures/suit/" + path + ".png");
    }
}
