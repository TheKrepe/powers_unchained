package lucraft.mods.lucraftcore.utilities.blocks;

import lucraft.mods.lucraftcore.utilities.items.ItemInstruction;
import lucraft.mods.lucraftcore.utilities.recipes.InstructionRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileEntityConstructionTable extends TileEntity implements ITickable, IInventory {

    private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(2, ItemStack.EMPTY);
    private String customName;
    public int selectedRecipe = -1;

    @Override
    public void update() {
        if ((getStackInSlot(0).isEmpty() && selectedRecipe >= 0) || selectedRecipe >= getInstructionRecipes().size())
            setSelectedRecipe(-1);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void setSelectedRecipe(int i) {
        this.selectedRecipe = i;
        this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
        this.markDirty();
    }

    public List<InstructionRecipe> getInstructionRecipes() {
        if (getStackInSlot(0).isEmpty())
            return new ArrayList<>();
        else {
            InstructionRecipe[] recipes = ItemInstruction.getInstructionRecipes(getStackInSlot(0));

            return recipes == null ? new ArrayList<>() : Arrays.asList(recipes);
        }
    }

    public boolean hasPlayerThisItem(InventoryPlayer inv, ItemStack stack) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack s = inv.getStackInSlot(i);

            if (areItemsEqual(stack, s))
                return true;
        }

        return false;
    }

    public boolean hasPlayerAllRequiredItems(InventoryPlayer inv, InstructionRecipe recipe) {
        boolean flag = true;
        for (ItemStack stack : recipe.getRequirements()) {
            if (!hasPlayerThisItem(inv, stack)) {
                flag = false;
            }
        }
        return flag;
    }

    public boolean areItemsEqual(ItemStack needed, ItemStack stack) {
        return needed.getItem() == stack.getItem() && stack.getCount() >= needed.getCount() && stack.getItemDamage() == needed.getItemDamage();
    }

    public void craftItem(InventoryPlayer inv) {
        if (selectedRecipe >= 0 && selectedRecipe < getInstructionRecipes().size()) {
            InstructionRecipe recipe = getInstructionRecipes().get(selectedRecipe);

            if (hasPlayerAllRequiredItems(inv, recipe)) {
                for (ItemStack stack : recipe.getRequirements()) {
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack s = inv.getStackInSlot(i);

                        if (!s.isEmpty() && areItemsEqual(stack, s)) {
                            s.shrink(stack.getCount());
                            if (s.getCount() <= 0)
                                s = ItemStack.EMPTY;
                            inv.setInventorySlotContents(i, s);
                            inv.markDirty();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.inventory);
        if (compound.hasKey("CustomName", 8))
            this.customName = compound.getString("CustomName");
        this.selectedRecipe = compound.getInteger("SelectedRecipe");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackHelper.saveAllItems(compound, inventory);
        if (this.hasCustomName())
            compound.setString("CustomName", this.customName);
        compound.setInteger("SelectedRecipe", this.selectedRecipe);

        return super.writeToNBT(compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
        world.markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new SPacketUpdateTileEntity(getPos(), 1, tag);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        writeToNBT(nbt);
        return nbt;
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
        return this.inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        } else {
            return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0 ? stack.getItem() instanceof ItemInstruction : false;
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
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "tile.construction_table.name";
    }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? new TextComponentString(customName) : new TextComponentTranslation("tile.construction_table.name", new Object[0]);
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setCustomInventoryName(String name) {
        this.customName = name;
    }

}
