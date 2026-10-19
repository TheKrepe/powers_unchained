package lucraft.mods.lucraftcore.sizechanging.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilitySizeChangingProvider implements ICapabilitySerializable<NBTTagCompound> {

    private ISizeChanging instance = null;

    public CapabilitySizeChangingProvider(ISizeChanging inventory) {
        this.instance = inventory;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) CapabilitySizeChanging.SIZE_CHANGING_CAP.getStorage().writeNBT(CapabilitySizeChanging.SIZE_CHANGING_CAP, instance, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CapabilitySizeChanging.SIZE_CHANGING_CAP.getStorage().readNBT(CapabilitySizeChanging.SIZE_CHANGING_CAP, instance, null, nbt);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return CapabilitySizeChanging.SIZE_CHANGING_CAP != null && capability == CapabilitySizeChanging.SIZE_CHANGING_CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilitySizeChanging.SIZE_CHANGING_CAP ? CapabilitySizeChanging.SIZE_CHANGING_CAP.<T>cast(instance) : null;
    }

}