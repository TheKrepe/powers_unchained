package com.thekrepe.powersunchained.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Loader-specific gameplay hooks: accessory slots, networking. */
public interface IGameplayHelper {

    /**
     * Whether the player wears {@code item} as an accessory (NeoForge: Curios slot). Without an accessory mod
     * installed, having the item anywhere in the inventory counts.
     */
    boolean isAccessoryEquipped(Player player, Item item);

    /**
     * The worn accessory stack of {@code item} (the live stack: changes to it are saved and synced), or an empty
     * stack. Without an accessory mod, the first such stack in the inventory.
     */
    ItemStack findAccessory(Player player, Item item);

    /** Client side: sends a payload to the server. */
    void sendToServer(CustomPacketPayload payload);
}
