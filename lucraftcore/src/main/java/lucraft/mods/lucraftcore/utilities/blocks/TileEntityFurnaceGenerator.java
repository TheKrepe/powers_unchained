package lucraft.mods.lucraftcore.utilities.blocks;

import lucraft.mods.lucraftcore.util.energy.EnergyStorageExt;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityFurnaceGenerator extends TileEntity implements ITickable {

    public ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 ? TileEntityFurnace.isItemFuel(stack) : stack.hasCapability(CapabilityEnergy.ENERGY, null);
        }
    };
    private String customName;
    public EnergyStorageExt energyStorage = new EnergyStorageExt(100000, 1000);
    public int fuel;
    public int totalFuel;

    @Override
    public void update() {
        boolean update = false;

        ItemStack fuel = this.inventory.getStackInSlot(0);
        ItemStack energyItem = this.inventory.getStackInSlot(1);

        if (this.fuel <= 0 && energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored() && !fuel.isEmpty() && TileEntityFurnace.isItemFuel(fuel)) {
            this.fuel = TileEntityFurnace.getItemBurnTime(fuel);
            this.totalFuel = this.fuel;
            if (fuel.getItem() == Items.LAVA_BUCKET)
                this.inventory.setStackInSlot(0, new ItemStack(Items.BUCKET));
            else
                fuel.shrink(1);
        }

        if (this.fuel > 0) {
            this.fuel--;
            energyStorage.receiveEnergy(10, false);
        }

        if (!energyItem.isEmpty() && energyItem.hasCapability(CapabilityEnergy.ENERGY, null) && this.energyStorage.getEnergyStored() > 0) {
            IEnergyStorage energyStorage = energyItem.getCapability(CapabilityEnergy.ENERGY, null);

            if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                int i = 1000;
                int maxOutput = this.energyStorage.extractEnergy(i, true);
                int maxInput = energyStorage.receiveEnergy(i, true);
                int energy = Math.min(maxInput, maxOutput);
                int energy2 = Math.min(maxInput, maxOutput);

                if (energy > 0) {
                    energyStorage.receiveEnergy(energy, false);
                    this.energyStorage.extractEnergy(energy2, false);
                }
            }
        }

        if (this.energyStorage.getEnergyStored() > 0) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity tileEntity = this.getWorld().getTileEntity(this.getPos().add(facing.getDirectionVec()));

                if (tileEntity != null && !(tileEntity instanceof TileEntityFurnaceGenerator) && tileEntity.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                    IEnergyStorage energyStorage = tileEntity.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());

                    if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                        int i = 1000;
                        int maxOutput = this.energyStorage.extractEnergy(i, true);
                        int maxInput = energyStorage.receiveEnergy(i, true);
                        int energy = Math.min(maxInput, maxOutput);
                        int energy2 = Math.min(maxInput, maxOutput);

                        if (energy > 0) {
                            energyStorage.receiveEnergy(energy, false);
                            this.energyStorage.extractEnergy(energy2, false);
                        }
                    }
                }
            }
        }

        if (this.energyStorage.update) {
            this.energyStorage.update = false;
            update = true;
        }

        if (update)
            this.markDirty();
    }

    public void dropAllItems(World worldIn, BlockPos pos) {
        IItemHandlerModifiable handler = (IItemHandlerModifiable) this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, (EnumFacing) null);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                handler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public void setCustomInventoryName(String name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.customName != null && !this.customName.isEmpty() ? new TextComponentString(customName) : new TextComponentTranslation("tile.furnace_generator.name", new Object[0]);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == CapabilityEnergy.ENERGY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) this.inventory;
        if (capability == CapabilityEnergy.ENERGY)
            return CapabilityEnergy.ENERGY.cast(this.energyStorage);
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.inventory.deserializeNBT(compound.getCompoundTag("Inventory"));
        this.energyStorage.deserializeNBT(compound);
        this.fuel = compound.getInteger("Fuel");
        this.totalFuel = compound.getInteger("TotalFuel");

        if (compound.hasKey("CustomName", 8))
            this.customName = compound.getString("CustomName");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setTag("Inventory", this.inventory.serializeNBT());
        compound.setInteger("Energy", this.energyStorage.getEnergyStored());
        compound.setInteger("Fuel", this.fuel);
        compound.setInteger("TotalFuel", this.totalFuel);

        if (this.customName != null && !this.customName.isEmpty())
            compound.setString("CustomName", this.customName);

        return compound;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }
}
