package lucraft.mods.lucraftcore.util.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

public class EnergyStorageExt extends EnergyStorage implements INBTSerializable<NBTTagCompound> {

    public boolean update = false;

    public EnergyStorageExt(int capacity) {
        super(capacity);
    }

    public EnergyStorageExt(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public EnergyStorageExt(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public EnergyStorageExt(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int i = super.receiveEnergy(maxReceive, simulate);
        if (i > 0 && !simulate)
            update = true;
        return i;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int i = super.extractEnergy(maxExtract, simulate);
        if (i > 0 && !simulate)
            update = true;
        return i;
    }

    public void setEnergyStored(int energy) {
        this.energy = energy;

        if (this.energy > capacity) {
            this.energy = capacity;
        } else if (this.energy < 0) {
            this.energy = 0;
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (energy < 0)
            energy = 0;
        nbt.setInteger("Energy", energy);
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.energy = nbt.getInteger("Energy");

        if (energy > capacity)
            energy = capacity;
    }
}
