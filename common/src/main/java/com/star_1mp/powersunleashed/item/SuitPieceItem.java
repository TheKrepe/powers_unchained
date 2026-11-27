package com.star_1mp.powersunleashed.item;

import com.star_1mp.powersunleashed.suit.Suit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import org.jspecify.annotations.Nullable;

/**
 * One piece of a {@link Suit}. Behaves like armor (defense, durability, enchanting, equip sound),
 * but has no vanilla equipment asset. Powers Unleashed draws it using the model of the wearer.
 */
public class SuitPieceItem extends Item {

    private final Suit suit;
    private final ArmorType type;

    public SuitPieceItem(Properties properties, Suit suit, ArmorType type) {
        super(properties);
        this.suit = suit;
        this.type = type;
    }

    public Suit getSuit() {
        return this.suit;
    }

    public ArmorType getArmorType() {
        return this.type;
    }

    public EquipmentSlot getSlot() {
        return this.type.getSlot();
    }

    /** Armor-like item properties for a suit piece. */
    public static Properties properties(Properties properties, Suit suit, ArmorType type) {
        ArmorMaterial material = suit.material();
        return properties
                .durability(type.getDurability(material.durability()))
                .attributes(material.createAttributes(type))
                .enchantable(material.enchantmentValue())
                // No asset id on purpose: vanilla HumanoidArmorLayer skips items without one.
                .component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot())
                        .setEquipSound(material.equipSound())
                        .build())
                .repairable(suit.repairItems());
    }

    /** Returns the suit piece if {@code stack} is a suit piece meant for {@code slot}. */
    public static @Nullable SuitPieceItem get(ItemStack stack, EquipmentSlot slot) {
        if (stack.getItem() instanceof SuitPieceItem piece && piece.getSlot() == slot) {
            return piece;
        }
        return null;
    }
}
