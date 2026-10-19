package lucraft.mods.lucraftcore.utilities.blocks;

import lucraft.mods.lucraftcore.util.energy.EnergyStorageExt;
import lucraft.mods.lucraftcore.util.fluids.FluidTankExt;
import lucraft.mods.lucraftcore.util.fluids.LCFluidUtil;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.utilities.recipes.BoilerRecipeHandler;
import lucraft.mods.lucraftcore.utilities.recipes.IBoilerRecipe;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;

public class TileEntityBoiler extends TileEntity implements ITickable {

    public static final int TANK_CAPACITY = 5000;
    public ItemStackHandler fluidContainerSlots = new ItemStackHandler(4) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return FluidUtil.getFluidHandler(stack) != null;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            ItemStack stack = this.getStackInSlot(slot);
            if (stack.isEmpty())
                return;
            FluidTankExt tank = slot <= 1 ? fluidTankInput : fluidTankOutput;
            if (slot == 0 || slot == 2) {
                ItemStack result = LCFluidUtil.transferFluidFromItemToTank(stack, tank, fluidContainerSlots);
                if (!result.isEmpty()) {
                    tank.update = true;
                    this.setStackInSlot(slot, result);
                    PlayerHelper.playSoundToAll(world, getPos().getX(), getPos().getY(), getPos().getZ(), 50, tank.getFluid().getFluid().getEmptySound(tank.getFluid()), SoundCategory.BLOCKS);
                }
            } else {
                ItemStack result = LCFluidUtil.transferFluidFromTankToItem(stack, tank, fluidContainerSlots);
                if (!result.isEmpty()) {
                    tank.update = true;
                    this.setStackInSlot(slot, result);
                    FluidStack fluidStack = FluidUtil.getFluidContained(result);
                    if (fluidStack != null)
                        PlayerHelper.playSoundToAll(world, getPos().getX(), getPos().getY(), getPos().getZ(), 50, fluidStack.getFluid().getFillSound(fluidStack), SoundCategory.BLOCKS);
                }
            }
        }
    };
    public ItemStackHandler inputSlots = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            detectRecipe();
        }
    };
    protected CombinedInvWrapper combinedSlots = new CombinedInvWrapper(fluidContainerSlots, inputSlots);
    public EnergyStorageExt energyStorage = new EnergyStorageExt(100000, 100000, 100000);
    public FluidTankExt fluidTankInput = new FluidTankExt(TANK_CAPACITY);
    public FluidTankExt fluidTankOutput = new FluidTankExt(TANK_CAPACITY);
    private String customName;
    private IBoilerRecipe recipe;
    public int progress;

    @Override
    public void update() {
        boolean update = false;

        if (energyStorage.update) {
            update = true;
            energyStorage.update = false;
        }

        if (fluidTankInput.update) {
            update = true;
            detectRecipe();
            fluidTankInput.update = false;
        }

        if (fluidTankOutput.update) {
            update = true;
            fluidTankOutput.update = false;
        }

        if (this.recipe != null && canWork()) {
            progress++;
            if (progress >= 100) {
                for (ItemStack s : ItemHelper.getItems(this.inputSlots))
                    s.shrink(1);
                this.energyStorage.extractEnergy(this.recipe.getRequiredEnergy(), false);
                this.fluidTankOutput.fill(this.recipe.getResult(), true);
                if (this.recipe.getInputFluid() != null)
                    this.fluidTankInput.drain(this.recipe.getInputFluid(), true);
                progress = 0;
            }
        } else {
            progress = 0;
        }

        if (update)
            this.markDirty();
    }

    public void detectRecipe() {
        if (this.world.isRemote)
            return;
        this.recipe = BoilerRecipeHandler.findBoilerRecipe(ItemHelper.getItems(this.inputSlots), this.fluidTankInput.getFluid(), this.energyStorage.getEnergyStored());
    }

    public boolean canWork() {
        if (this.recipe == null)
            return false;
        if (this.fluidTankOutput.getFluid() != null && this.recipe.getResult().getFluid() != this.fluidTankOutput.getFluid().getFluid())
            return false;
        if (this.fluidTankOutput.getFluid() != null && this.fluidTankOutput.getCapacity() - this.fluidTankOutput.getFluidAmount() < this.recipe.getResult().amount)
            return false;

        return BoilerRecipeHandler.matches(this.recipe, ItemHelper.getItems(this.inputSlots), this.fluidTankInput.getFluid(), this.energyStorage.getEnergyStored());
    }

    public void setCustomInventoryName(String name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.customName != null && !this.customName.isEmpty() ? new TextComponentString(customName) : new TextComponentTranslation("tile.boiler.name", new Object[0]);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) combinedSlots;
        else if (capability == CapabilityEnergy.ENERGY)
            return (T) this.energyStorage;
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) ((T) facing == EnumFacing.UP ? this.fluidTankInput : this.fluidTankOutput);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.fluidContainerSlots.deserializeNBT(compound.getCompoundTag("FluidContainerItems"));
        this.inputSlots.deserializeNBT(compound.getCompoundTag("InputItems"));
        this.energyStorage.deserializeNBT(compound);
        this.fluidTankInput.readFromNBT(compound.getCompoundTag("FluidTankInput"));
        this.fluidTankOutput.readFromNBT(compound.getCompoundTag("FluidTankOutput"));
        this.progress = compound.getInteger("Progress");
        if (compound.hasKey("CachedRecipe"))
            this.recipe = BoilerRecipeHandler.getRecipe(new ResourceLocation(compound.getString("CachedRecipe")));
        if (compound.hasKey("CustomName", 8))
            this.customName = compound.getString("CustomName");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("FluidContainerItems", fluidContainerSlots.serializeNBT());
        compound.setTag("InputItems", inputSlots.serializeNBT());
        compound.setInteger("Energy", this.energyStorage.getEnergyStored());
        compound.setTag("FluidTankInput", this.fluidTankInput.writeToNBT(new NBTTagCompound()));
        compound.setTag("FluidTankOutput", this.fluidTankOutput.writeToNBT(new NBTTagCompound()));
        compound.setInteger("Progress", this.progress);
        if (this.recipe != null)
            compound.setString("CachedRecipe", this.recipe.getRegistryName().toString());
        if (this.customName != null && !this.customName.isEmpty())
            compound.setString("CustomName", this.customName);
        return super.writeToNBT(compound);
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

    @Override
    public void markDirty() {
        super.markDirty();
        this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
    }

    public void dropAllItems(World worldIn, BlockPos pos) {
        CombinedInvWrapper handler = (CombinedInvWrapper) this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, (EnumFacing) null);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                handler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

}
