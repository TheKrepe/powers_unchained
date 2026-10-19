package lucraft.mods.heroesexpansion.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface IOnceInWorldStructures {

    void addToWorld(CapabilityOnceInWorldStructures.EnumOnceInWorld enumOnceInWorld, BlockPos pos);

    void removeFromWorld(CapabilityOnceInWorldStructures.EnumOnceInWorld enumOnceInWorld);

    boolean isInWorld(CapabilityOnceInWorldStructures.EnumOnceInWorld enumOnceInWorld);

    BlockPos getPosition(CapabilityOnceInWorldStructures.EnumOnceInWorld enumOnceInWorld);

    NBTTagCompound writeNBT();

    void readNBT(NBTTagCompound nbt);

}
