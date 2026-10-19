package lucraft.mods.lucraftcore.karma.capabilities;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.karma.KarmaHandler;
import lucraft.mods.lucraftcore.karma.KarmaStat;
import lucraft.mods.lucraftcore.karma.network.MessageSyncKarma;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class CapabilityKarma implements IKarmaCapability {

    @CapabilityInject(IKarmaCapability.class)
    public static final Capability<IKarmaCapability> KARMA_CAP = null;

    public EntityPlayer player;
    public int karma;
    public Map<KarmaStat, Integer> karmaStats = new HashMap<KarmaStat, Integer>();
    public boolean knockOutMode;

    public CapabilityKarma(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        NBTTagCompound statsTag = new NBTTagCompound();
        for (KarmaStat stat : karmaStats.keySet()) {
            NBTTagCompound karmaTag = new NBTTagCompound();
            karmaTag.setInteger("Amount", karmaStats.get(stat));
            statsTag.setTag(stat.getRegistryName().toString(), karmaTag);
        }

        nbt.setTag("Stats", statsTag);
        nbt.setInteger("Karma", karma);
        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound statsTag = nbt.getCompoundTag("Stats");

        for (KarmaStat stat : KarmaStat.KARMA_STAT_REGISTRY.getValues()) {
            karmaStats.put(stat, statsTag.getCompoundTag(stat.getRegistryName().toString()).getInteger("Amount"));
        }

        this.karma = nbt.getInteger("Karma");
    }

    @Override
    public int getKarma() {
        return karma;
    }

    @Override
    public void setKarmaStat(KarmaStat stat, int amount) {
        karmaStats.put(stat, amount);
        updateKarmaValue();
        syncToPlayer();
    }

    @Override
    public int getKarmaStat(KarmaStat stat) {
        return karmaStats.containsKey(stat) ? karmaStats.get(stat) : 0;
    }

    public void updateKarmaValue() {
        int i = 0;

        for (KarmaStat stat : karmaStats.keySet())
            i += stat.getAmount() * KarmaHandler.getKarmaStat(player, stat);

        this.karma = i;
    }

    @Override
    public void syncToPlayer() {
        this.syncToPlayer(this.player);
    }

    @Override
    public void syncToPlayer(EntityPlayer receiver) {
        if (receiver instanceof EntityPlayerMP)
            LCPacketDispatcher.sendTo(new MessageSyncKarma(this.player), (EntityPlayerMP) receiver);
    }

    @Override
    public void syncToAll() {
        this.syncToPlayer();
        if (player.world instanceof WorldServer) {
            for (EntityPlayer players : ((WorldServer) player.world).getEntityTracker().getTrackingPlayers(player)) {
                if (players instanceof EntityPlayerMP) {
                    LCPacketDispatcher.sendTo(new MessageSyncKarma(player), (EntityPlayerMP) players);
                }
            }
        }
    }

    public static class EventHandler {

        @SubscribeEvent
        public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> evt) {
            if (!(evt.getObject() instanceof EntityPlayer) || evt.getObject().hasCapability(KARMA_CAP, null))
                return;

            evt.addCapability(new ResourceLocation(LucraftCore.MODID, "karma"), new CapabilityKarmaProvider(new CapabilityKarma((EntityPlayer) evt.getObject())));
        }

        @SubscribeEvent
        public void onPlayerStartTracking(PlayerEvent.StartTracking e) {
            if (e.getTarget().hasCapability(KARMA_CAP, null)) {
                e.getTarget().getCapability(KARMA_CAP, null).syncToPlayer(e.getEntityPlayer());
            }
        }

        @SubscribeEvent
        public void onPlayerClone(PlayerEvent.Clone e) {
            NBTTagCompound compound = new NBTTagCompound();
            compound = (NBTTagCompound) KARMA_CAP.getStorage().writeNBT(KARMA_CAP, e.getOriginal().getCapability(KARMA_CAP, null), null);
            KARMA_CAP.getStorage().readNBT(KARMA_CAP, e.getEntityPlayer().getCapability(KARMA_CAP, null), null, compound);
        }

    }

    public static class Storage implements IStorage<IKarmaCapability> {

        @Override
        public NBTBase writeNBT(Capability<IKarmaCapability> capability, IKarmaCapability instance, EnumFacing side) {
            return instance.writeNBT();
        }

        @Override
        public void readNBT(Capability<IKarmaCapability> capability, IKarmaCapability instance, EnumFacing side, NBTBase nbt) {
            instance.readNBT((NBTTagCompound) nbt);
        }

    }

}
