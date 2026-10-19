package lucraft.mods.heroesexpansion.tileentities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.container.ContainerPortalDevice;
import lucraft.mods.heroesexpansion.entities.EntityPortal;
import lucraft.mods.heroesexpansion.events.OpenSpaceStonePortalEvent;
import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import lucraft.mods.lucraftcore.infinity.items.ItemInfinityStone;
import lucraft.mods.lucraftcore.util.energy.EnergyStorageExt;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntityPortalDevice extends TileEntityLockable implements ITickable, IInventory {

    private NonNullList<ItemStack> furnaceItemStacks = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);
    private String customName;
    public boolean opened;
    public int progress;
    public int prevProgress;
    public static final int maxProgress = 100;
    public EntityPortal portal;
    public UUID portalUUID;
    public EnergyStorageExt energyStorage = new EnergyStorageExt(100000, 100000, 100000);
    public EnumPortalDeviceDestination destination = EnumPortalDeviceDestination.NONE;
    public int destinationDim;
    public Vec3d destinationPos;
    public static final int energyPerTick = 100;

    public boolean isPortalOpened() {
        return this.opened;
    }

    public void openPortal() {
        if (MinecraftForge.EVENT_BUS.post(new OpenSpaceStonePortalEvent(null, this.world, new Vec3d(this.getPos().getX() + 0.5F, this.getPos().getY() + 20, this.getPos().getZ() + 0.5F), this.destinationDim, this.destinationPos, true)))
            return;

        this.opened = true;

        if (!this.world.isRemote) {
            if (this.destination == EnumPortalDeviceDestination.POSITION)
                this.portal = EntityPortal.createPortal(world, new Vec3d(this.getPos().getX() + 0.5F, this.getPos().getY() + 20, this.getPos().getZ() + 0.5F), 0, 90, this.destinationDim, this.destinationPos, 7F);
            else if (this.destination == EnumPortalDeviceDestination.INVASION)
                this.portal = EntityPortal.createInvasionPortal(world, new Vec3d(this.getPos().getX() + 0.5F, this.getPos().getY() + 20, this.getPos().getZ() + 0.5F), 0, 90, 20F);
            this.portalUUID = this.portal.getPersistentID();
            this.markDirty();
        }
    }

    public void closePortal() {
        this.opened = false;

        if (this.portal != null && !this.world.isRemote) {
            this.portal.closePortal();
        }
    }

    @Override
    public int getSizeInventory() {
        return furnaceItemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.furnaceItemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.furnaceItemStacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.furnaceItemStacks, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.furnaceItemStacks, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.furnaceItemStacks.set(index, stack);
        this.markDirty();
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
        return index == 0 ? isElectricItem(stack) : (index == 1 ? stack.getItem() instanceof ItemInfinityStone && ((ItemInfinityStone) stack.getItem()).getType() == EnumInfinityStone.SPACE : false);
    }

    public static boolean isElectricItem(ItemStack stack) {
        return stack.hasCapability(CapabilityEnergy.ENERGY, null);
    }

    @Override
    public int getField(int id) {
        if (id == 0)
            return progress;
        else if (id == 1)
            return this.energyStorage.getEnergyStored();
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0)
            this.progress = value;
        else if (id == 1)
            this.energyStorage.setEnergyStored(value);
    }

    @Override
    public int getFieldCount() {
        return 2;
    }

    @Override
    public void clear() {
        this.furnaceItemStacks.clear();
    }

    @Override
    public void update() {
        this.prevProgress = progress;

        if (this.portalUUID != null && (this.portal == null || !this.portal.getPersistentID().equals(this.portalUUID))) {
            Entity entity = LCEntityHelper.getEntityByUUID(this.getWorld(), this.portalUUID);

            if (entity != null && entity instanceof EntityPortal) {
                this.portal = (EntityPortal) entity;
            }
        }

        if (!getStackInSlot(0).isEmpty() && getStackInSlot(0).hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energy = getStackInSlot(0).getCapability(CapabilityEnergy.ENERGY, null);

            if (energy.getEnergyStored() > 0 && this.energyStorage.getEnergyStored() < this.energyStorage.getMaxEnergyStored()) {
                this.energyStorage.receiveEnergy(energy.extractEnergy(100, false), false);
            }
        }

        if (this.opened) {
            this.energyStorage.extractEnergy(energyPerTick, false);
            if (this.getStackInSlot(1).isEmpty() || this.energyStorage.getEnergyStored() < energyPerTick)
                closePortal();
            else {
                if (progress < maxProgress)
                    progress++;
                else if (this.portal == null || this.portal.isDead)
                    openPortal();
            }
        } else {
            if (progress > 0)
                progress--;
            if (this.portal != null && !this.portal.isDead && !this.portal.isClosing())
                closePortal();
        }
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerPortalDevice(playerInventory, this);
    }

    @Override
    public String getGuiID() {
        return HeroesExpansion.MODID + "portal_device";
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "container.portal_device";
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setCustomInventoryName(String name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? new TextComponentString(customName) : new TextComponentTranslation("tile.portal_device.name", new Object[0]);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || (capability == CapabilityEnergy.ENERGY && facing != EnumFacing.UP);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != EnumFacing.UP)
            return CapabilityEnergy.ENERGY.cast(this.energyStorage);
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.furnaceItemStacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.furnaceItemStacks);

        if (compound.hasKey("CustomName", 8))
            this.customName = compound.getString("CustomName");

        this.progress = compound.getInteger("Progress");
        this.opened = compound.getBoolean("Opened");
        this.energyStorage.deserializeNBT(compound);
        this.destination = EnumPortalDeviceDestination.values()[compound.getInteger("DestinationType")];
        this.destinationDim = compound.getInteger("DestinationDim");
        this.destinationPos = new Vec3d(compound.getDouble("DestinationX"), compound.getDouble("DestinationY"), compound.getDouble("DestinationZ"));

        if (compound.hasKey("PortalEntity") && !compound.getString("PortalEntity").isEmpty()) {
            this.portalUUID = UUID.fromString(compound.getString("PortalEntity"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.furnaceItemStacks);

        if (this.hasCustomName())
            compound.setString("CustomName", this.customName);

        compound.setBoolean("Opened", this.opened);
        compound.setInteger("Progress", this.progress);
        compound.setInteger("Energy", this.energyStorage.getEnergyStored());
        compound.setInteger("DestinationType", this.destination.ordinal());
        compound.setInteger("DestinationDim", this.destinationDim);

        if (this.destinationPos != null) {
            compound.setDouble("DestinationX", this.destinationPos.x);
            compound.setDouble("DestinationY", this.destinationPos.y);
            compound.setDouble("DestinationZ", this.destinationPos.z);
        }

        if (this.portalUUID != null)
            compound.setString("PortalEntity", this.portalUUID.toString());

        return compound;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
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

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos, this.pos.add(1, 1, 1)).expand(0, this.opened ? 20 : 0, 0);
    }

    public enum EnumPortalDeviceDestination {

        NONE, POSITION, INVASION;

    }

}
