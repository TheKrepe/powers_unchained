package com.star_1mp.powersunchained.platform.services;

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

    /**
     * Changes the worn accessory stack of {@code item} and stores the result back in its slot (accessory slots may
     * hand out copies). Does nothing if it isn't worn.
     *
     * @return whether it is worn
     */
    boolean modifyAccessory(Player player, Item item, java.util.function.Consumer<ItemStack> change);

    /** Server side: sends a payload to every player tracking the entity, and the entity itself if it is a player. */
    void sendToTrackingAndSelf(net.minecraft.world.entity.Entity entity, CustomPacketPayload payload);

    /** Client side: sends a payload to the server. */
    void sendToServer(CustomPacketPayload payload);

    /** The player's Suit Creator progress (both sides; the client only knows its own). */
    com.star_1mp.powersunchained.suit.SuitProgress suitProgress(Player player);

    /** Server side: stores the player's Suit Creator progress and syncs it to them. */
    void setSuitProgress(Player player, com.star_1mp.powersunchained.suit.SuitProgress progress);

    /** The player's Tech Table progress (both sides; the client only knows its own). */
    com.star_1mp.powersunchained.tech.TechProgress techProgress(Player player);

    /** Levelling of the player's superpowers (experience, bought skills; synced to that player). */
    com.star_1mp.powersunchained.power.PowerProgress powerProgress(Player player);

    void setPowerProgress(Player player, com.star_1mp.powersunchained.power.PowerProgress progress);

    /** Stores the progress on the server WITHOUT sending it (see {@link #syncPowerProgress}). */
    void setPowerProgressQuiet(Player player, com.star_1mp.powersunchained.power.PowerProgress progress);

    /** Sends the stored progress to the player. */
    void syncPowerProgress(Player player);

    /** Server side: stores the player's Tech Table progress and syncs it to them. */
    void setTechProgress(Player player, com.star_1mp.powersunchained.tech.TechProgress progress);

    /** The player's superpowers (both sides; the client only knows its own). */
    com.star_1mp.powersunchained.power.PowerData powerData(Player player);

    /** Server side: stores the player's superpowers and syncs them to them. */
    void setPowerData(Player player, com.star_1mp.powersunchained.power.PowerData data);
}
