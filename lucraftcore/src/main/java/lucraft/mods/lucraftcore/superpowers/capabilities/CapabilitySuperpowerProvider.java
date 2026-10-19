package lucraft.mods.lucraftcore.superpowers.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilitySuperpowerProvider implements ICapabilitySerializable<NBTTagCompound> {

    public ISuperpowerCapability instance;

    public CapabilitySuperpowerProvider(ISuperpowerCapability instance) {
        this.instance = instance;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return CapabilitySuperpower.SUPERPOWER_CAP != null && capability == CapabilitySuperpower.SUPERPOWER_CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilitySuperpower.SUPERPOWER_CAP ? CapabilitySuperpower.SUPERPOWER_CAP.<T>cast(instance) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) CapabilitySuperpower.SUPERPOWER_CAP.getStorage().writeNBT(CapabilitySuperpower.SUPERPOWER_CAP, instance, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CapabilitySuperpower.SUPERPOWER_CAP.getStorage().readNBT(CapabilitySuperpower.SUPERPOWER_CAP, instance, null, nbt);
    }

}
