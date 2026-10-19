package net.threetag.pymtech.tileentity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.block.PTBlocks;
import net.threetag.pymtech.config.PTServerConfig;
import net.threetag.pymtech.container.StructureShrinkerContainer;
import net.threetag.pymtech.entity.ShrunkenStructureEntity;
import net.threetag.pymtech.fluid.PTFluids;
import net.threetag.pymtech.item.ShrunkenStructureItem;
import net.threetag.pymtech.sizechangetype.PTSizeChangeTypes;
import net.threetag.pymtech.storage.ShrunkenStructureData;
import net.threetag.pymtech.storage.ShrunkenStructureIdTracker;
import net.threetag.pymtech.util.BlockUtil;
import net.threetag.threecore.capability.CapabilitySizeChanging;
import net.threetag.threecore.fluid.FluidTankExt;
import net.threetag.threecore.item.ItemStackHandlerExt;
import net.threetag.threecore.tileentity.MachineTileEntity;
import net.threetag.threecore.util.PlayerUtil;
import net.threetag.threecore.util.TCFluidUtil;
import net.threetag.threecore.util.energy.IEnergyConfig;

import javax.annotation.Nullable;

public class StructureShrinkerTileEntity extends MachineTileEntity {

    public static final int TANK_CAPACITY = 5000;
    private BlockPos selectionStart = this.getPos();
    private BlockPos selectionEnd = this.getPos();

    protected final IIntArray intArray = new IIntArray() {
        @Override
        public int get(int i) {
            switch (i) {
                case 0:
                    return energyStorage.getEnergyStored();
                case 1:
                    return energyStorage.getMaxEnergyStored();
                case 2:
                    return selectionStart.getX();
                case 3:
                    return selectionStart.getY();
                case 4:
                    return selectionStart.getZ();
                case 5:
                    return selectionEnd.getX();
                case 6:
                    return selectionEnd.getY();
                case 7:
                    return selectionEnd.getZ();
                default:
                    return 0;
            }
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0:
                    energyStorage.setEnergyStored(value);
                case 1:
                    energyStorage.setMaxEnergyStored(value);
                case 2:
                    selectionStart = new BlockPos(value, selectionStart.getY(), selectionStart.getZ());
                case 3:
                    selectionStart = new BlockPos(selectionStart.getX(), value, selectionStart.getZ());
                case 4:
                    selectionStart = new BlockPos(selectionStart.getX(), selectionStart.getY(), value);
                case 5:
                    selectionEnd = new BlockPos(value, selectionEnd.getY(), selectionEnd.getZ());
                case 6:
                    selectionEnd = new BlockPos(selectionEnd.getX(), value, selectionEnd.getZ());
                case 7:
                    selectionEnd = new BlockPos(selectionEnd.getX(), selectionEnd.getY(), value);
            }
        }

        @Override
        public int size() {
            return 8;
        }
    };

    public FluidTankExt fluidTank = new FluidTankExt(TANK_CAPACITY, fluidStack -> fluidStack.getFluid().isEquivalentTo(PTFluids.SHRINK_PYM_PARTICLES)) {
    }.setCallback(f -> {
        if (getWorld() != null)
            getWorld().notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 3);
    }).setSoundHandler(sound -> {
        if (sound != null)
            PlayerUtil.playSoundToAll(world, getPos().getX(), getPos().getY(), getPos().getZ(), 50, sound, SoundCategory.BLOCKS);
    });

    private ItemStackHandler energySlot = new ItemStackHandlerExt(1)
            .setValidator((handler, slot, stack) -> stack.getCapability(CapabilityEnergy.ENERGY).isPresent())
            .setChangedCallback((handler, slot) -> StructureShrinkerTileEntity.this.markDirty());

    private ItemStackHandlerExt fluidSlots = new ItemStackHandlerExt(4) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }.setChangedCallback((handler, slot) -> {
        if (slot == 0) {
            FluidActionResult res = TCFluidUtil.transferFluidFromItemToTank(handler.getStackInSlot(slot), fluidTank, handler, null);
            if (res.isSuccess()) {
                handler.setStackInSlot(slot, res.getResult());
            }
        } else {
            FluidActionResult res = TCFluidUtil.transferFluidFromTankToItem(handler.getStackInSlot(slot), fluidTank, handler, null);
            if (res.isSuccess()) {
                handler.setStackInSlot(slot, res.getResult());
            }
        }
        StructureShrinkerTileEntity.this.markDirty();
    }).setValidator((handler, slot, stack) -> FluidUtil.getFluidHandler(stack).isPresent());

    private CombinedInvWrapper combinedHandler = new CombinedInvWrapper(energySlot, fluidSlots);

    public StructureShrinkerTileEntity() {
        super(PTBlocks.STRUCTURE_SHRINKER_TILE_ENTITY.get());
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent(Util.makeTranslationKey("container", PTBlocks.STRUCTURE_SHRINKER_TILE_ENTITY.get().getRegistryName()));
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new StructureShrinkerContainer(id, player, this, this.intArray);
    }

    @Override
    public IItemHandler getEnergyInputSlots() {
        return this.energySlot;
    }

    @Override
    public IEnergyConfig getEnergyConfig() {
        return PTServerConfig.STRUCTURE_SHRINKER_ENERGY;
    }

    public AxisAlignedBB getSelectionRelative() {
        return new AxisAlignedBB(this.selectionStart, this.selectionEnd);
    }

    public AxisAlignedBB getSelection() {
        BlockPos p1 = this.selectionStart.add(this.getPos());
        BlockPos p2 = this.selectionEnd.add(this.getPos());
        return new AxisAlignedBB(p1, p2);
    }

    public void setSelectionStart(BlockPos selectionStart) {
        this.selectionStart = selectionStart.subtract(this.getPos());
        if (getWorld() != null)
            getWorld().notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 3);
    }

    public void setSelectionEnd(BlockPos selectionEnd) {
        this.selectionEnd = selectionEnd.subtract(this.getPos());
        if (getWorld() != null)
            getWorld().notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 3);
    }

    public void shrink(@Nullable PlayerEntity activator) {
        if (this.getWorld() == null || this.getWorld().isRemote)
            return;

        final PlayerEntity player = activator != null ? activator : new FakePlayer((ServerWorld) this.getWorld(), new GameProfile(null, PymTech.MODID));
        AxisAlignedBB box = getSelection();
        box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY + 1, box.maxZ + 1);

        if (!box.grow(1).intersects(new AxisAlignedBB(this.getPos()))) {
            player.sendStatusMessage(new TranslationTextComponent("pymtech.structure_shrinker.not_connected").mergeStyle(TextFormatting.RED), true);
            return;
        }

        int count = BlockUtil.countBlocks(this.getWorld(), box);
        int energy = count * PTServerConfig.STRUCTURE_SHRINKER_ENERGY_PER_BLOCK.get();
        int particles = PTServerConfig.STRUCTURE_SHRINKER_PARTICLE_AMOUNT.get();

        if (count == 0) {
            player.sendStatusMessage(new TranslationTextComponent("pymtech.structure_shrinker.empty_structure").mergeStyle(TextFormatting.RED), true);
            return;
        }

        // TODO dont use this, make util method that calls extract multiple times
        if (this.energyStorage.getEnergyStored() < energy) {
            player.sendStatusMessage(new TranslationTextComponent("pymtech.structure_shrinker.not_enough_energy", energy).mergeStyle(TextFormatting.RED), true);
            return;
        }

        if (this.fluidTank.drain(particles, IFluidHandler.FluidAction.SIMULATE).getAmount() < particles) {
            player.sendStatusMessage(new TranslationTextComponent("pymtech.structure_shrinker.not_enough_particles", particles).mergeStyle(TextFormatting.RED), true);
            return;
        }

        // TODO safe check if empty

        this.energyStorage.setEnergyStored(this.energyStorage.getEnergyStored() - energy);
        this.fluidTank.drain(particles, IFluidHandler.FluidAction.EXECUTE);

        ShrunkenStructureData data = ShrunkenStructureData.create(ShrunkenStructureIdTracker.getNextId(this.getWorld()), this.getWorld(), box, (p) -> (!MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, p, world.getBlockState(p), player)) && world.getBlockState(p).getBlockHardness(world, p) != -1F), true);
        ShrunkenStructureData.register(world, data);
        ItemStack stack = ShrunkenStructureItem.create(data);
        ShrunkenStructureEntity entity = new ShrunkenStructureEntity(this.world, box.getCenter().x, box.minY, box.getCenter().z, stack);
        entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent(sizeChanging -> {
            sizeChanging.startSizeChange(PTSizeChangeTypes.PYM_PARTICLES, 0.1F);
            sizeChanging.updateBoundingBox();
        });
        this.world.addEntity(entity);
        // TODO play sound
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT nbt) {
        super.read(blockState, nbt);

        this.energySlot.deserializeNBT(nbt.getCompound("EnergySlots"));
        this.fluidSlots.deserializeNBT(nbt.getCompound("FluidSlots"));
        this.fluidTank.readFromNBT(nbt.getCompound("FluidTank"));
        this.selectionStart = NBTUtil.readBlockPos(nbt.getCompound("SelectionStart"));
        this.selectionEnd = NBTUtil.readBlockPos(nbt.getCompound("SelectionEnd"));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.put("EnergySlots", this.energySlot.serializeNBT());
        nbt.put("FluidSlots", this.fluidSlots.serializeNBT());
        nbt.put("FluidTank", this.fluidTank.writeToNBT(new CompoundNBT()));
        nbt.put("SelectionStart", NBTUtil.writeBlockPos(this.selectionStart));
        nbt.put("SelectionEnd", NBTUtil.writeBlockPos(this.selectionEnd));
        return super.write(nbt);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        this.fluidTank.readFromNBT(tag.getCompound("FluidTank"));
        this.selectionStart = NBTUtil.readBlockPos(tag.getCompound("SelectionStart"));
        this.selectionEnd = NBTUtil.readBlockPos(tag.getCompound("SelectionEnd"));
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();
        nbt.put("FluidTank", this.fluidTank.writeToNBT(new CompoundNBT()));
        nbt.put("SelectionStart", NBTUtil.writeBlockPos(this.selectionStart));
        nbt.put("SelectionEnd", NBTUtil.writeBlockPos(this.selectionEnd));
        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT tag = pkt.getNbtCompound();
        this.fluidTank.readFromNBT(tag.getCompound("FluidTank"));
        this.selectionStart = NBTUtil.readBlockPos(tag.getCompound("SelectionStart"));
        this.selectionEnd = NBTUtil.readBlockPos(tag.getCompound("SelectionEnd"));
    }

    private LazyOptional<IItemHandlerModifiable> combinedInvLazyOptional = LazyOptional.of(() -> combinedHandler);
    private LazyOptional<IItemHandlerModifiable> energySlotLazyOptional = LazyOptional.of(() -> energySlot);
    private LazyOptional<IItemHandlerModifiable> fluidSlotLazyOptional = LazyOptional.of(() -> fluidSlots);
    private LazyOptional<IFluidHandler> fluidTankLazyOptional = LazyOptional.of(() -> fluidTank);

    // TODO seperate fluid slots into 2 handlers, so you can access them from different sides

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null)
                return combinedInvLazyOptional.cast();
            if (side == Direction.UP)
                return energySlotLazyOptional.cast();
            return fluidSlotLazyOptional.cast();
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidTankLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
