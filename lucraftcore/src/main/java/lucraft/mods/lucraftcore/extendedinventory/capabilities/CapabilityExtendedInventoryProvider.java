package lucraft.mods.lucraftcore.extendedinventory.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityExtendedInventoryProvider implements ICapabilitySerializable<NBTTagCompound> {

    private IExtendedInventoryCapability inventory = null;

    public CapabilityExtendedInventoryProvider(IExtendedInventoryCapability inventory) {
        this.inventory = inventory;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP.getStorage().writeNBT(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, inventory, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP.getStorage().readNBT(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, inventory, null, nbt);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP != null && capability == CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP ? CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP.<T>cast(inventory) : null;
    }

}