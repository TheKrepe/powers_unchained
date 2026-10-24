package com.thekrepe.powersunchained.platform;

import com.thekrepe.powersunchained.compat.curios.CuriosCompat;
import com.thekrepe.powersunchained.platform.services.IGameplayHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoForgeGameplayHelper implements IGameplayHelper {

    private static final boolean CURIOS = ModList.get().isLoaded("curios");

    @Override
    public boolean isAccessoryEquipped(Player player, Item item) {
        if (CURIOS) {
            return CuriosCompat.isEquipped(player, item);
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack findAccessory(Player player, Item item) {
        if (CURIOS) {
            return CuriosCompat.find(player, item);
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                return inventory.getItem(i);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean modifyAccessory(Player player, Item item, java.util.function.Consumer<ItemStack> change) {
        if (CURIOS) {
            return CuriosCompat.modify(player, item, change);
        }
        ItemStack stack = this.findAccessory(player, item);
        if (stack.isEmpty()) {
            return false;
        }
        change.accept(stack);
        return true;
    }

    @Override
    public void sendToTrackingAndSelf(net.minecraft.world.entity.Entity entity, CustomPacketPayload payload) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public com.thekrepe.powersunchained.suit.SuitProgress suitProgress(Player player) {
        return player.getData(com.thekrepe.powersunchained.PUAttachments.SUIT_PROGRESS);
    }

    @Override
    public void setSuitProgress(Player player, com.thekrepe.powersunchained.suit.SuitProgress progress) {
        player.setData(com.thekrepe.powersunchained.PUAttachments.SUIT_PROGRESS, progress);
        player.syncData(com.thekrepe.powersunchained.PUAttachments.SUIT_PROGRESS);
    }
}
