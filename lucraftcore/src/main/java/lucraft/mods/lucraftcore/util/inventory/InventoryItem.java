package lucraft.mods.lucraftcore.util.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class InventoryItem implements IInventory {

    protected NonNullList<ItemStack> items;
    protected ItemStack stack;

    public InventoryItem(int size, ItemStack stack) {
        this.stack = stack;
        this.items = NonNullList.<ItemStack>withSize(size, ItemStack.EMPTY);
        readNBT();
    }

    private void writeNBT() {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        ItemStackHelper.saveAllItems(stack.getTagCompound(), this.items);
    }

    private void readNBT() {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Items")) {
            ItemStackHelper.loadAllItems(stack.getTagCompound(), this.items);
        }
    }

    @Override
    public int getSizeInventory() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stacks : this.items) {
            if (!stacks.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < this.items.size() ? (ItemStack) this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.items, slot, amount);

        if (!itemstack.isEmpty()) {
            this.markDirty();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        ItemStack itemstack = this.items.get(slot);

        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(slot, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.items.set(slot, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        writeNBT();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public void clear() {
        this.items.clear();
        markDirty();
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return true;
    }

    @Override
    public String getName() {
        return stack.getTranslationKey();
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.stack.getTextComponent();
    }

}