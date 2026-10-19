package lucraft.mods.lucraftcore.extendedinventory.slots;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory.ExtendedInventoryItemType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotEIItem extends Slot {

    public ExtendedInventoryItemType type;
    private EntityPlayer player;

    public SlotEIItem(EntityPlayer player, IInventory inventoryIn, int index, int xPosition, int yPosition, IItemExtendedInventory.ExtendedInventoryItemType type) {
        super(inventoryIn, index, xPosition, yPosition);
        this.player = player;
        this.type = type;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) stack.getItem()).getEIItemType(stack) == this.type && ((IItemExtendedInventory) stack.getItem()).canEquip(stack, player);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return !getStack().isEmpty() && ((IItemExtendedInventory) getStack().getItem()).canUnequip(getStack(), player);
    }

    @Override
    public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
        if (!getHasStack() && stack.getItem() instanceof IItemExtendedInventory)
            ((IItemExtendedInventory) stack.getItem()).onUnequipped(stack, playerIn);
        return super.onTake(playerIn, stack);
    }

    @Override
    public void putStack(ItemStack stack) {
        if (getHasStack())
            ((IItemExtendedInventory) getStack().getItem()).onUnequipped(getStack(), player);

        super.putStack(stack);

        if (getHasStack())
            ((IItemExtendedInventory) getStack().getItem()).onEquipped(getStack(), player);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
