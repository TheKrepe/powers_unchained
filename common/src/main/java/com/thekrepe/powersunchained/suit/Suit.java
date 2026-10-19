package com.thekrepe.powersunchained.suit;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import org.jspecify.annotations.Nullable;

/**
 * A wearable superhero suit made of up to four pieces (head, chest, legs, feet).
 *
 * @param id           suit id, e.g. {@code powers_unchained:spider_man}
 * @param texture      suit texture in player-skin layout (64x64) for the wide (Steve) model
 * @param slimTexture  same texture for the slim (Alex) model, with 3px wide arms
 * @param bootsTexture optional separate texture for the boots, drawn slightly above the leggings;
 *                     when {@code null} the boots use the main texture
 * @param material     armor stats (defense, durability, enchantability, equip sound)
 * @param repairItem   item used to repair suit pieces in an anvil
 */
public record Suit(
        Identifier id,
        Identifier texture,
        Identifier slimTexture,
        @Nullable Identifier bootsTexture,
        ArmorMaterial material,
        Item repairItem) {

    public Identifier texture(boolean slim) {
        return slim ? this.slimTexture : this.texture;
    }
}
