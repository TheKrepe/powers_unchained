package lucraft.mods.lucraftcore.utilities.container;

import lucraft.mods.lucraftcore.utilities.blocks.TileEntityBoiler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerBoiler extends Container {

    public ContainerBoiler(EntityPlayer player, TileEntityBoiler tileEntity) {
        IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        this.addSlotToContainer(new SlotItemHandler(itemHandler, 0, 8, 17));
        this.addSlotToContainer(new SlotItemHandler(itemHandler, 1, 8, 103));
        this.addSlotToContainer(new SlotItemHandler(itemHandler, 2, 152, 17));
        this.addSlotToContainer(new SlotItemHandler(itemHandler, 3, 152, 103));

        int o = 4;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(new SlotItemHandler(itemHandler, o, 46 + x * 18, 29 + y * 18));
                o++;
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 134 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(player.inventory, k, 8 + k * 18, 192));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index <= 12) {
                if (!this.mergeItemStack(itemstack1, 13, 49, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else {
                if (!this.mergeItemStack(itemstack1, 0, 13, false)) {
                    return ItemStack.EMPTY;
                } else if (index >= 13 && index < 40) {
                    if (!this.mergeItemStack(itemstack1, 40, 49, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 40 && index < 49 && !this.mergeItemStack(itemstack1, 13, 40, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}
