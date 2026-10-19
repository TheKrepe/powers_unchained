package lucraft.mods.lucraftcore.extendedinventory.capabilities;

import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IExtendedInventoryCapability {

    public InventoryExtendedInventory getInventory();

    public NBTTagCompound writeNBT();

    public void readNBT(NBTTagCompound nbt);

    public void syncToPlayer();

    public void syncToPlayer(EntityPlayer receiver);

    public void syncToAll();

}
