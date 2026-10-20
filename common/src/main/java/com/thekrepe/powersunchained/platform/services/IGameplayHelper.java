package com.thekrepe.powersunchained.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** Loader-specific gameplay hooks: accessory slots, networking. */
public interface IGameplayHelper {

    /**
     * Whether the player wears {@code item} as an accessory (NeoForge: Curios slot). Without an accessory mod
     * installed, having the item anywhere in the inventory counts.
     */
    boolean isAccessoryEquipped(Player player, Item item);

    /** Client side: sends a payload to the server. */
    void sendToServer(CustomPacketPayload payload);
}
