package com.thekrepe.powersunchained.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** Loader-specific gameplay hooks: accessory slots, synced entity data, networking. */
public interface IGameplayHelper {

    /**
     * Whether the player wears {@code item} as an accessory (NeoForge: Curios slot). Without an accessory mod
     * installed, having the item anywhere in the inventory counts.
     */
    boolean isAccessoryEquipped(Player player, Item item);

    /** Game time until which the entity is covered in sticky web (synced to clients); 0 if never. */
    long getWebbedUntil(Entity entity);

    /** Server side: covers the entity in sticky web until the given game time (synced to tracking clients). */
    void setWebbedUntil(Entity entity, long gameTime);

    /** Client side: sends a payload to the server. */
    void sendToServer(CustomPacketPayload payload);
}
