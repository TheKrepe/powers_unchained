package lucraft.mods.lucraftcore.extendedinventory;

import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class InventoryExtendedInventory implements IInventory {

    protected NonNullList<ItemStack> inventory;
    public EntityPlayer player;

    public static final int INV_SIZE = 5;
    public static final int SLOT_NECKLACE = 0;
    public static final int SLOT_MANTLE = 1;
    public static final int SLOT_WRIST = 2;
    public static final int SLOT_LEFT_WEAPON = 3;
    public static final int SLOT_RIGHT_WEAPON = 4;

    public InventoryExtendedInventory(EntityPlayer player) {
        this.player = player;
        this.inventory = NonNullList.<ItemStack>withSize(INV_SIZE, ItemStack.EMPTY);
    }

    public InventoryExtendedInventory(InventoryExtendedInventory inv, EntityPlayer player) {
        this.inventory = inv.inventory;
        this.player = player;
    }

    @Override
    public String getName() {
        return "extended_inventory";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("lucraftcore.extended_inventory.name");
    }

    @Override
    public int getSizeInventory() {
        return inventory.size();
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
        return index >= this.getSizeInventory() ? ItemStack.EMPTY : this.inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = this.inventory.get(index);
        if (!stack.isEmpty()) {
            if (stack.getCount() > count) {
                stack = stack.splitStack(count);
                markDirty();
                return stack;
            } else {
                setInventorySlotContents(index, ItemStack.EMPTY);
                markDirty();
                return stack;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (!this.inventory.get(index).isEmpty()) {
            ItemStack itemstack = this.inventory.get(index);
            setInventorySlotContents(index, ItemStack.EMPTY);
            markDirty();
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory.set(index, stack);
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (!player.world.isRemote) {
            player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).syncToAll();
        }
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
        return true;
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
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, ItemStack.EMPTY);
        }
        markDirty();
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
        this.inventory = NonNullList.<ItemStack>withSize(INV_SIZE, ItemStack.EMPTY);
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
