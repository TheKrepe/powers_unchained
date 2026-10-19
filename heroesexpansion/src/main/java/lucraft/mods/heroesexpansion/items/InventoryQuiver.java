package lucraft.mods.heroesexpansion.items;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class InventoryQuiver extends InventoryBasic {

    public ItemStack stack;

    public InventoryQuiver(ItemStack stack) {
        super(stack.getDisplayName(), true, 5);
        this.stack = stack;

        if (stack.hasTagCompound()) {
            NonNullList<ItemStack> items = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(stack.getTagCompound().getCompoundTag("Items"), items);
            for (int i = 0; i < items.size(); i++)
                this.setInventorySlotContents(i, items.get(i));
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemArrow;
    }

    @Override
    public void markDirty() {
        super.markDirty();

        NonNullList<ItemStack> items = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < this.getSizeInventory(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                items.set(i, getStackInSlot(i));
            }
        }
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        nbt.setTag("Items", ItemStackHelper.saveAllItems(nbt.getCompoundTag("Items"), items));
        stack.setTagCompound(nbt);
    }

}
