package lucraft.mods.lucraftcore.util.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyStorageItem implements IEnergyStorage {

    public ItemStack stack;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public EnergyStorageItem(ItemStack stack, int capacity) {
        this(stack, capacity, capacity, capacity, 0);
    }

    public EnergyStorageItem(ItemStack stack, int capacity, int maxTransfer) {
        this(stack, capacity, maxTransfer, maxTransfer, 0);
    }

    public EnergyStorageItem(ItemStack stack, int capacity, int maxReceive, int maxExtract) {
        this(stack, capacity, maxReceive, maxExtract, 0);
    }

    public EnergyStorageItem(ItemStack stack, int capacity, int maxReceive, int maxExtract, int energy) {
        this.stack = stack;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
            this.setEnergyStored(Math.max(0, Math.min(capacity, energy)));
        }
    }

    public void setEnergyStored(int energy) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        int i = MathHelper.clamp(energy, 0, getMaxEnergyStored());
        stack.getTagCompound().setInteger("Energy", i);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(capacity - getEnergyStored(), Math.min(this.maxReceive, maxReceive));
        if (!simulate)
            this.setEnergyStored(getEnergyStored() + energyReceived);
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int energyExtracted = Math.min(getEnergyStored(), Math.min(this.maxExtract, maxExtract));
        if (!simulate)
            this.setEnergyStored(getEnergyStored() - energyExtracted);
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return stack != null && stack.hasTagCompound() ? stack.getTagCompound().getInteger("Energy") : 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }
}
