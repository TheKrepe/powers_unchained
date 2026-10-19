package lucraft.mods.heroesexpansion.capabilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class CapabilityOnceInWorldStructures implements IOnceInWorldStructures {

    @CapabilityInject(IOnceInWorldStructures.class)
    public static final Capability<IOnceInWorldStructures> ONCE_IN_WORLD_STRUCTURES_CAP = null;

    public Map<EnumOnceInWorld, BlockPos> POSITIONS = new HashMap<>();

    @Override
    public void addToWorld(EnumOnceInWorld enumOnceInWorld, BlockPos pos) {
        POSITIONS.put(enumOnceInWorld, pos);
    }

    @Override
    public void removeFromWorld(EnumOnceInWorld enumOnceInWorld) {
        POSITIONS.remove(enumOnceInWorld);
    }

    @Override
    public boolean isInWorld(EnumOnceInWorld enumOnceInWorld) {
        return POSITIONS.containsKey(enumOnceInWorld);
    }

    @Override
    public BlockPos getPosition(EnumOnceInWorld enumOnceInWorld) {
        if (!isInWorld(enumOnceInWorld))
            return null;
        else
            return POSITIONS.get(enumOnceInWorld);
    }

    @Override
    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();

        for (EnumOnceInWorld enumOnceInWorld : POSITIONS.keySet()) {
            BlockPos pos = getPosition(enumOnceInWorld);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Type", enumOnceInWorld.toString());
            tag.setString("Position", pos.getX() + ":" + pos.getY() + ":" + pos.getZ());
            nbttaglist.appendTag(tag);
        }

        nbt.setTag("Positions", nbttaglist);
        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagList nbttaglist = nbt.getTagList("Positions", 10);
        this.POSITIONS = new HashMap<>();
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound tag = nbttaglist.getCompoundTagAt(i);
            EnumOnceInWorld enumOnceInWorld = EnumOnceInWorld.getFromString(tag.getString("Type"));
            String[] posString = tag.getString("Position").split(":");

            if (enumOnceInWorld != null && posString.length == 3) {
                this.addToWorld(enumOnceInWorld, new BlockPos(Integer.parseInt(posString[0]), Integer.parseInt(posString[1]), Integer.parseInt(posString[2])));
            }
        }
    }

    public static enum EnumOnceInWorld {

        T;

        public static EnumOnceInWorld getFromString(String name) {
            for (EnumOnceInWorld enumOnceInWorld : EnumOnceInWorld.values()) {
                if (enumOnceInWorld.toString().equalsIgnoreCase(name)) {
                    return enumOnceInWorld;
                }
            }
            return null;
        }

    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<World> evt) {
            evt.addCapability(new ResourceLocation(HeroesExpansion.MODID, "once_in_world_structures"), new CapabilityOnceInWorldStructuresProvider(new CapabilityOnceInWorldStructures()));
        }

    }

    public static class Storage implements Capability.IStorage<IOnceInWorldStructures> {

        @Override
        public NBTBase writeNBT(Capability<IOnceInWorldStructures> capability, IOnceInWorldStructures instance, EnumFacing side) {
            return instance.writeNBT();
        }

        @Override
        public void readNBT(Capability<IOnceInWorldStructures> capability, IOnceInWorldStructures instance, EnumFacing side, NBTBase nbt) {
            instance.readNBT((NBTTagCompound) nbt);
        }

    }

}
