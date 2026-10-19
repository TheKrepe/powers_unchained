package lucraft.mods.lucraftcore.karma.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityKarmaProvider implements ICapabilitySerializable<NBTTagCompound> {

    private IKarmaCapability instance = null;

    public CapabilityKarmaProvider(IKarmaCapability inventory) {
        this.instance = inventory;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) CapabilityKarma.KARMA_CAP.getStorage().writeNBT(CapabilityKarma.KARMA_CAP, instance, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CapabilityKarma.KARMA_CAP.getStorage().readNBT(CapabilityKarma.KARMA_CAP, instance, null, nbt);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return CapabilityKarma.KARMA_CAP != null && capability == CapabilityKarma.KARMA_CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityKarma.KARMA_CAP ? CapabilityKarma.KARMA_CAP.<T>cast(instance) : null;
    }

}