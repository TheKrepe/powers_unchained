package com.thekrepe.powersunchained.suit;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;

/**
 * A wearable superhero suit made of up to four pieces (head, chest, legs, feet). Every piece has its own
 * texture in player-skin layout (64x64), only the regions of that piece are used.
 *
 * @param id                  suit id, e.g. {@code powers_unchained:spider_man}
 * @param maskTexture         head + hat layer
 * @param chestplateTexture   body + arms (+ jacket / sleeves) for the wide (Steve) model
 * @param chestplateSlimTexture same for the slim (Alex) model, with 3px wide arms
 * @param pantsTexture        legs (+ pants layer)
 * @param bootsTexture        legs, drawn slightly above the pants so both can be worn
 * @param material            armor stats (defense, durability, enchantability, equip sound)
 * @param repairItem          item used to repair suit pieces in an anvil
 */
public record Suit(
        Identifier id,
        Identifier maskTexture,
        Identifier chestplateTexture,
        Identifier chestplateSlimTexture,
        Identifier pantsTexture,
        Identifier bootsTexture,
        ArmorMaterial material,
        Item repairItem) {
}
