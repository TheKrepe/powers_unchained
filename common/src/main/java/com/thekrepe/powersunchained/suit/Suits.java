package com.thekrepe.powersunchained.suit;

import com.thekrepe.powersunchained.Constants;
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
    public static final Suit SPIDER_MAN = new Suit(
            Constants.id("spider_man"),
            texture("spider_man/spider_man"),
            texture("spider_man/spider_man_slim"),
            texture("spider_man/spider_man_boots"),
            new ArmorMaterial(
                    18,
                    Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 5, ArmorType.LEGGINGS, 4, ArmorType.BOOTS, 2),
                    15,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    0.0F,
                    0.0F,
                    ItemTags.REPAIRS_LEATHER_ARMOR,
                    ResourceKey.create(EquipmentAssets.ROOT_ID, Constants.id("spider_man"))),
            Items.STRING);

    private Suits() {
    }

    private static Identifier texture(String path) {
        return Constants.id("textures/suit/" + path + ".png");
    }
}
