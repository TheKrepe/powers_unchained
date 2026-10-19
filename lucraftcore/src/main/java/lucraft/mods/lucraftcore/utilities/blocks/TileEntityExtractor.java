package lucraft.mods.lucraftcore.utilities.blocks;

import lucraft.mods.lucraftcore.util.energy.EnergyStorageExt;
import lucraft.mods.lucraftcore.util.fluids.FluidTankExt;
import lucraft.mods.lucraftcore.util.fluids.LCFluidUtil;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.utilities.recipes.ExtractorRecipeHandler;
import lucraft.mods.lucraftcore.utilities.recipes.IExtractorRecipe;
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
import java.util.Random;

public class TileEntityExtractor extends TileEntity implements ITickable {

    public static final int TANK_CAPACITY = 5000;

    public ItemStackHandler inputSlots = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            detectRecipe();
        }
    };
    public ItemStackHandler outputSlots = new ItemStackHandler(2);
    public ItemStackHandler fluidContainerSlots = new ItemStackHandler(2) {
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
            if (slot == 0) {
                ItemStack result = LCFluidUtil.transferFluidFromItemToTank(stack, fluidTank, fluidContainerSlots);
                if (!result.isEmpty()) {
                    fluidTank.update = true;
                    this.setStackInSlot(slot, result);
                    PlayerHelper.playSoundToAll(world, getPos().getX(), getPos().getY(), getPos().getZ(), 50, fluidTank.getFluid().getFluid().getEmptySound(fluidTank.getFluid()), SoundCategory.BLOCKS);
                }
            } else {
                ItemStack result = LCFluidUtil.transferFluidFromTankToItem(stack, fluidTank, fluidContainerSlots);
                if (!result.isEmpty()) {
                    fluidTank.update = true;
                    this.setStackInSlot(slot, result);
                    FluidStack fluidStack = FluidUtil.getFluidContained(result);
                    if (fluidStack != null)
                        PlayerHelper.playSoundToAll(world, getPos().getX(), getPos().getY(), getPos().getZ(), 50, fluidStack.getFluid().getFillSound(fluidStack), SoundCategory.BLOCKS);
                }
            }
        }
    };
    protected CombinedInvWrapper combinedSlots = new CombinedInvWrapper(fluidContainerSlots, inputSlots, outputSlots);
    public FluidTankExt fluidTank = new FluidTankExt(TANK_CAPACITY);
    public EnergyStorageExt energyStorage = new EnergyStorageExt(100000, 100000, 100000);
    private String customName;
    private IExtractorRecipe recipe;
    public int progress;

    @Override
    public void update() {
        boolean update = false;

        if (energyStorage.update) {
            update = true;
            energyStorage.update = false;
        }

        if (fluidTank.update) {
            update = true;
            detectRecipe();
            fluidTank.update = false;
        }

        if (this.recipe != null && canWork()) {
            progress++;
            if (progress >= 100) {
                makeResult(this.recipe);
                progress = 0;
            }
        } else {
            progress = 0;
        }

        if (update)
            this.markDirty();
    }

    public void makeResult(IExtractorRecipe recipe) {
        if (recipe == null || this.world.isRemote)
            return;
        this.energyStorage.extractEnergy(recipe.getRequiredEnergy(), false);
        if (recipe.getInputFluid() != null)
            this.fluidTank.drain(recipe.getInputFluid(), true);
        this.inputSlots.getStackInSlot(0).shrink(recipe.getInputAmount());
        if (recipe.getInputContainer() != null)
            this.inputSlots.getStackInSlot(1).shrink(1);
        Random random = new Random();
        float primary = random.nextFloat();
        float secondary = random.nextFloat();
        if (!recipe.getPrimaryResult().isEmpty() && primary < recipe.getPrimaryChance()) {
            this.outputSlots.insertItem(0, recipe.getPrimaryResult().copy(), false);
        }
        if (!recipe.getSecondaryResult().isEmpty() && secondary < recipe.getSecondaryChance()) {
            this.outputSlots.insertItem(1, recipe.getSecondaryResult().copy(), false);
        }
    }

    public boolean canWork() {
        if (this.recipe == null)
            return false;

        if (!this.recipe.getPrimaryResult().isEmpty() && !this.outputSlots.getStackInSlot(0).isEmpty()) {
            ItemStack stack = this.outputSlots.getStackInSlot(0);
            if (stack.getItem() != this.recipe.getPrimaryResult().getItem())
                return false;
            int max = stack.getMaxStackSize();
            if (stack.getCount() + this.recipe.getPrimaryResult().getCount() > max)
                return false;
            int i = this.recipe.getPrimaryResult().getMetadata();
            if (i != 32767 && i != stack.getMetadata())
                return false;
        }

        if (!this.recipe.getSecondaryResult().isEmpty() && !this.outputSlots.getStackInSlot(1).isEmpty()) {
            ItemStack stack = this.outputSlots.getStackInSlot(1);
            if (stack.getItem() != this.recipe.getSecondaryResult().getItem())
                return false;
            int max = stack.getMaxStackSize();
            if (stack.getCount() + this.recipe.getSecondaryResult().getCount() > max)
                return false;
            int i = this.recipe.getSecondaryResult().getMetadata();

            if (i != 32767 && i != stack.getMetadata())
                return false;
        }

        return ExtractorRecipeHandler.matches(this.recipe, this.inputSlots.getStackInSlot(0), this.inputSlots.getStackInSlot(1), this.fluidTank.getFluid(), this.energyStorage.getEnergyStored());
    }

    public void detectRecipe() {
        if (this.world.isRemote)
            return;
        this.recipe = ExtractorRecipeHandler.findExtractorRecipe(this.inputSlots.getStackInSlot(0), this.inputSlots.getStackInSlot(1), this.fluidTank.getFluid(), this.energyStorage.getEnergyStored());
    }

    public void setCustomInventoryName(String name) {
        this.customName = name;
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

    @Override
    public ITextComponent getDisplayName() {
        return this.customName != null && !this.customName.isEmpty() ? new TextComponentString(customName) : new TextComponentTranslation("tile.extractor.name", new Object[0]);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return facing == null ? (T) this.combinedSlots : (T) (facing == EnumFacing.DOWN ? this.outputSlots : this.inputSlots);
        else if (capability == CapabilityEnergy.ENERGY)
            return (T) this.energyStorage;
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) this.fluidTank;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.fluidContainerSlots.deserializeNBT(compound.getCompoundTag("FluidContainerItems"));
        this.inputSlots.deserializeNBT(compound.getCompoundTag("InputItems"));
        this.outputSlots.deserializeNBT(compound.getCompoundTag("OutputItems"));
        this.energyStorage.deserializeNBT(compound);
        this.fluidTank.readFromNBT(compound.getCompoundTag("FluidTank"));
        this.progress = compound.getInteger("Progress");
        if (compound.hasKey("CachedRecipe"))
            this.recipe = ExtractorRecipeHandler.getRecipe(new ResourceLocation(compound.getString("CachedRecipe")));
        if (compound.hasKey("CustomName", 8))
            this.customName = compound.getString("CustomName");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("FluidContainerItems", fluidContainerSlots.serializeNBT());
        compound.setTag("InputItems", inputSlots.serializeNBT());
        compound.setTag("OutputItems", outputSlots.serializeNBT());
        compound.setInteger("Energy", this.energyStorage.getEnergyStored());
        compound.setTag("FluidTank", this.fluidTank.writeToNBT(new NBTTagCompound()));
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

}
