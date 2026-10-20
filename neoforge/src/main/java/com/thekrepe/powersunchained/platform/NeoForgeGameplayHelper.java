package com.thekrepe.powersunchained.platform;

import com.thekrepe.powersunchained.compat.curios.CuriosCompat;
import com.thekrepe.powersunchained.platform.services.IGameplayHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
