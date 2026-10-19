package lucraft.mods.heroes.speedsterheroes.tileentities;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

public class TileEntityPhilosophersStoneChest extends TileEntity implements IInventory, ITickable {

	private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
	public boolean isOpen = false;
	public int prevTimer = 0;
	public int timer = 0;

	@Override
	public void update() {
		prevTimer = timer;
		if(isOpen) {
			if(timer < 20)
				timer++;
		} else {
			if(timer > 0)
				timer--;
		}
	}
	
	public void onRightClick(EntityPlayer player) {
		if(player.isSneaking() && isOpen) {
			if(getStackInSlot(0).isEmpty()) {
				if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == SHItems.philosophersStone) {
					setInventorySlotContents(0, player.getHeldItemMainhand());
					player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
					this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
					this.markDirty();
					return;
				}
			} else {
				if(player.getHeldItemMainhand().isEmpty()) {
					if(!getWorld().isRemote)
						getWorld().spawnEntity(new EntityItem(getWorld(), getPos().getX() + 0.5F, getPos().getY() + 0.5F, getPos().getZ() + 0.5F, getStackInSlot(0)));
					setInventorySlotContents(0, ItemStack.EMPTY);
					this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
					this.markDirty();
					return;
				}
			}
		}
		else if(timer == 0 || timer == 20) {
			this.isOpen = !isOpen;
			this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
			this.markDirty();
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		writeCustomToNBT(compound);
		return compound;
	}

	public void writeCustomToNBT(NBTTagCompound compound) {
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.stacks.size(); ++i) {
			if (!this.stacks.get(i).isEmpty()) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				this.stacks.get(i).writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		compound.setTag("Items", nbttaglist);
		compound.setBoolean("IsOpen", isOpen);
		compound.setInteger("Timer", timer);
		compound.setInteger("PrevTimer", prevTimer);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readCustomFromNBT(compound);
	}

	public void readCustomFromNBT(NBTTagCompound compound) {
		NBTTagList nbttaglist = compound.getTagList("Items", 10);
		this.stacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;

			if (j >= 0 && j < this.stacks.size()) {
				this.stacks.set(j, new ItemStack(nbttagcompound));
			}
		}
		
		isOpen = compound.getBoolean("IsOpen");
		timer = compound.getInteger("Timer");
		prevTimer = compound.getInteger("PrevTimer");
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readCustomFromNBT(pkt.getNbtCompound());
		world.markBlockRangeForRenderUpdate(getPos(), getPos());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeCustomToNBT(tag);
		return new SPacketUpdateTileEntity(getPos(), 1, tag);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		writeCustomToNBT(nbt);
		return nbt;
	}
	
	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return stacks.size();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return stacks.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (!this.stacks.get(index).isEmpty()) {
			if (this.stacks.get(index).getCount() <= count) {
				ItemStack itemstack1 = this.stacks.get(index);
				this.stacks.set(index, ItemStack.EMPTY);
				this.markDirty();
				return itemstack1;
			} else {
				ItemStack itemstack = this.stacks.get(index).splitStack(count);

				if (this.stacks.get(index).getCount() == 0) {
					this.stacks.set(index, ItemStack.EMPTY);
				}

				this.markDirty();
				return itemstack;
			}
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (!this.stacks.get(index).isEmpty()) {
			ItemStack itemstack = this.stacks.get(index);
			this.stacks.set(index, ItemStack.EMPTY);
			return itemstack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.stacks.set(index, stack);

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
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
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
		for (int i = 0; i < this.stacks.size(); ++i) {
			this.stacks.set(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean isEmpty() {
		for(int i = 0; i < getSizeInventory(); i++) {
			if(!getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return false;
	}

}
