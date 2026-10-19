package lucraft.mods.lucraftcore.infinity.items;

import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class InventoryInfinityGauntlet implements IInventory {

    public static final EnumInfinityStone[] SLOTS = new EnumInfinityStone[]{EnumInfinityStone.SOUL, EnumInfinityStone.REALITY, EnumInfinityStone.SPACE, EnumInfinityStone.POWER, EnumInfinityStone.MIND, EnumInfinityStone.TIME};

    public final ItemStack stack;
    private final NonNullList<ItemStack> inventory;

    public InventoryInfinityGauntlet(ItemStack stack) {
        this.stack = stack;
        this.inventory = NonNullList.<ItemStack>withSize(6, ItemStack.EMPTY);
        this.readFromNBT(stack.getTagCompound());
    }

    @Override
    public int getSizeInventory() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.inventory.size() ? (ItemStack) this.inventory.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, index, count);

        if (!itemstack.isEmpty())
            this.markDirty();

        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = this.inventory.get(index);

        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.inventory.set(index, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
            stack.setCount(this.getInventoryStackLimit());

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
        this.writeToNBT(stack.getTagCompound());
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
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemInfinityStone))
            return false;

        ItemInfinityStone item = (ItemInfinityStone) stack.getItem();
        return item.isContainer() ? false : SLOTS[index] == item.getType();
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
    public void clear() {
        this.inventory.clear();
        this.markDirty();
    }

    @Override
    public String getName() {
        return this.stack.getDisplayName();
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.stack.getTextComponent();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList items = new NBTTagList();
        for (int i = 0; i < getSizeInventory(); ++i) {
            if (!getStackInSlot(i).isEmpty()) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                getStackInSlot(i).writeToNBT(item);
                items.appendTag(item);
            }
        }
        compound.setTag("Items", items);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList("Items", compound.getId());
        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");
            if (slot >= 0 && slot < getSizeInventory()) {
                this.inventory.set(slot, new ItemStack(item));
            }
        }
    }

}
