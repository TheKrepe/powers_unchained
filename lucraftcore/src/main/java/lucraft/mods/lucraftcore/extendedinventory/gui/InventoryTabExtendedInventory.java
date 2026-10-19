package lucraft.mods.lucraftcore.extendedinventory.gui;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.extendedinventory.network.MessageOpenExtendedInventory;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.proxies.LCClientProxy;
import lucraft.mods.lucraftcore.tabs.AbstractTab;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class InventoryTabExtendedInventory extends AbstractTab {

    public InventoryTabExtendedInventory() {
        super(0, 0, 0, new ItemStack(Items.IRON_CHESTPLATE));
    }

    @Override
    public void onTabClicked() {
        if (LCClientProxy.canClickTab())
            LCPacketDispatcher.sendToServer(new MessageOpenExtendedInventory());
    }

    @Override
    public boolean shouldAddToList() {
        return LCConfig.modules.extended_inventory;
    }

}
