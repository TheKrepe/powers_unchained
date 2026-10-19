package lucraft.mods.lucraftcore.extendedinventory.abilitybar;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.util.abilitybar.IAbilityBarEntry;
import lucraft.mods.lucraftcore.util.abilitybar.IAbilityBarProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ExtendedInventoryBarProvider implements IAbilityBarProvider {

    @Override
    public List<IAbilityBarEntry> getEntries() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        List<IAbilityBarEntry> entries = new ArrayList<>();
        InventoryExtendedInventory inv = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();

        for (int i = 0; i <= InventoryExtendedInventory.SLOT_WRIST; i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty() && stack.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) stack.getItem()).useButton(stack, player)) {
                entries.add(new ExtendedInventoryBarEntry(stack, ((IItemExtendedInventory) stack.getItem()).getEIItemType(stack)));
            }
        }

        return entries;
    }

}
