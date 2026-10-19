package lucraft.mods.lucraftcore.karma.capabilities;

import lucraft.mods.lucraftcore.karma.KarmaStat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IKarmaCapability {

    NBTTagCompound writeNBT();

    void readNBT(NBTTagCompound nbt);

    int getKarma();

    int getKarmaStat(KarmaStat stat);

    void setKarmaStat(KarmaStat stat, int amount);

    void syncToPlayer();

    void syncToPlayer(EntityPlayer receiver);

    void syncToAll();
}
