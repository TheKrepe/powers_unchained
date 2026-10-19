package lucraft.mods.heroesexpansion.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityOnceInWorldStructuresProvider implements ICapabilitySerializable<NBTTagCompound> {

    public IOnceInWorldStructures instance;

    public CapabilityOnceInWorldStructuresProvider(IOnceInWorldStructures instance) {
        this.instance = instance;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP.getStorage().writeNBT(CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP, instance, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP.getStorage().readNBT(CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP, instance, null, nbt);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP != null && capability == CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP ? CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP.<T>cast(instance) : null;
    }

}