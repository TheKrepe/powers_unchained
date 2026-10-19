package lucraft.mods.lucraftcore.superpowers.capabilities;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilitySupplier;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail.EntityTrail;
import lucraft.mods.lucraftcore.superpowers.events.AbilityKeyEvent;
import lucraft.mods.lucraftcore.superpowers.network.MessageChooseSuperpowerGui;
import lucraft.mods.lucraftcore.superpowers.network.MessageSyncSuperpower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class CapabilitySuperpower implements ISuperpowerCapability {

    @CapabilityInject(ISuperpowerCapability.class)
    public static final Capability<ISuperpowerCapability> SUPERPOWER_CAP = null;

    public final EntityLivingBase entity;
    public Superpower superpower;
    public Map<Ability.EnumAbilityContext, AbilityContainer> containerMap;
    public NBTTagCompound nbt;
    public boolean gainedSuperpower = false;

    public boolean hasPlayedBefore = false;

    public LinkedList<Entity> trailEntities = new LinkedList<>();

    public CapabilitySuperpower(EntityLivingBase entity) {
        this.entity = entity;
        this.containerMap = new HashMap<>();
        this.nbt = new NBTTagCompound();
        for (Ability.EnumAbilityContext context : Ability.EnumAbilityContext.values()) {
            AbilitySupplier supplier = Ability.getAbilitySupplier(context);
            if (supplier != null) {
                containerMap.put(context, supplier.containerFactory.create(entity, context));
            }
        }
    }

    @Override
    public void setSuperpower(Superpower superpower, boolean update) {
        if (this.superpower != superpower) {
            this.superpower = superpower;

            if (update)
                this.syncToAll();
        }
    }

    @Override
    public void setSuperpower(Superpower superpower) {
        this.setSuperpower(superpower, true);
    }

    @Override
    public Superpower getSuperpower() {
        return this.superpower;
    }

    @Override
    public AbilityContainer getAbilityContainer(Ability.EnumAbilityContext context) {
        return this.containerMap.get(context);
    }

    @Override
    public NBTTagCompound getData() {
        return this.nbt;
    }

    @Override
    public boolean hasPlayedBefore() {
        return hasPlayedBefore;
    }

    @Override
    public void setHasPlayedBefore(boolean played) {
        this.hasPlayedBefore = played;
    }

    @Override
    public void onUpdate() {
        for (AbilityContainer abilityContainer : containerMap.values()) {
            if (abilityContainer != null) {
                abilityContainer.onUpdate();
            }
        }

        if (!hasPlayedBefore() && LCConfig.superpowers.startSuperpowersEnabled && entity.ticksExisted == 100) {
            if (entity instanceof EntityPlayerMP) {
                LCPacketDispatcher.sendTo(new MessageChooseSuperpowerGui(LCConfig.superpowers.startSuperpowers), (EntityPlayerMP) entity);
            }
        }
    }

    @Override
    public EntityLivingBase getEntity() {
        return this.entity;
    }

    @Override
    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        if (this.superpower != null)
            nbt.setString("Superpower", this.superpower.getRegistryName().toString());

        for (AbilityContainer value : containerMap.values())
            value.save();
        nbt.setTag("Data", this.nbt);
        nbt.setBoolean("HasPlayedBefore", hasPlayedBefore);

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        this.superpower = SuperpowerHandler.SUPERPOWER_REGISTRY.getValue(new ResourceLocation(nbt.getString("Superpower")));
        this.nbt = nbt.getCompoundTag("Data");
        this.hasPlayedBefore = nbt.getBoolean("HasPlayedBefore");
        for (AbilityContainer value : containerMap.values())
            value.load();
    }

    @Override
    public void syncToPlayer() {
        if (this.entity instanceof EntityPlayerMP)
            this.syncToPlayer((EntityPlayer) this.entity);
    }

    @Override
    public void syncToPlayer(EntityPlayer receiver) {
        if (receiver instanceof EntityPlayerMP) {
            for (AbilityContainer container : this.containerMap.values()) {
                container.save();
            }

            LCPacketDispatcher.sendTo(new MessageSyncSuperpower(this.entity), (EntityPlayerMP) receiver);
        }
    }

    @Override
    public void syncToAll() {
        this.syncToPlayer();
        if (entity.world instanceof WorldServer) {
            for (AbilityContainer container : this.containerMap.values()) {
                container.save();
            }
            for (EntityPlayer players : ((WorldServer) entity.world).getEntityTracker().getTrackingPlayers(entity)) {
                if (players instanceof EntityPlayerMP) {
                    LCPacketDispatcher.sendTo(new MessageSyncSuperpower(entity), (EntityPlayerMP) players);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public LinkedList<EntityTrail> getTrailEntities() {
        LinkedList<EntityTrail> list = new LinkedList<>();
        for (Entity en : this.trailEntities) {
            if (en instanceof EntityTrail) {
                list.add((EntityTrail) en);
            }
        }
        return list;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addTrailEntity(Entity entity) {
        this.trailEntities.add(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void removeTrailEntity(Entity entity) {
        this.trailEntities.remove(entity);
    }

    @Override
    public boolean hasGainedSuperpower() {
        return this.gainedSuperpower;
    }

    @Override
    public void setSuperpowerGained(boolean gained) {
        this.gainedSuperpower = gained;
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    public static class CapabilitySuperpowerEventHandler {

        @SubscribeEvent
        public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> evt) {
            if (!(evt.getObject() instanceof EntityLivingBase))
                return;

            evt.addCapability(new ResourceLocation(LucraftCore.MODID, "superpower_capability"), new CapabilitySuperpowerProvider(new CapabilitySuperpower((EntityLivingBase) evt.getObject())));
        }

        @SubscribeEvent
        public void onPlayerStartTracking(PlayerEvent.StartTracking e) {
            if (e.getTarget().hasCapability(CapabilitySuperpower.SUPERPOWER_CAP, null)) {
                e.getTarget().getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null).syncToPlayer(e.getEntityPlayer());
            }
        }

        @SubscribeEvent
        public void onEntityJoinWorld(EntityJoinWorldEvent e) {
            if (e.getEntity() instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) e.getEntity();
                SuperpowerHandler.syncToAll(entity);
            }
        }

        @SubscribeEvent
        public void onPlayerChangedDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent e) {
            SuperpowerHandler.syncToAll(e.player);
        }

        @SubscribeEvent
        public void onPlayerClone(PlayerEvent.Clone e) {
            NBTTagCompound compound = (NBTTagCompound) CapabilitySuperpower.SUPERPOWER_CAP.getStorage().writeNBT(CapabilitySuperpower.SUPERPOWER_CAP, e.getOriginal().getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null), null);
            CapabilitySuperpower.SUPERPOWER_CAP.getStorage().readNBT(CapabilitySuperpower.SUPERPOWER_CAP, e.getEntityPlayer().getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null), null, compound);
        }

        @SubscribeEvent
        public void onUpdate(LivingEvent.LivingUpdateEvent e) {
            e.getEntityLiving().getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null).onUpdate();

            if (e.getEntityLiving().world.isRemote)
                return;

            if (!(e.getEntityLiving() instanceof EntityPlayer) && !(e.getEntityLiving() instanceof EntityArmorStand) && e.getEntityLiving().ticksExisted % 20 == 0) {
                List<Ability> abilities = Ability.getAbilities(e.getEntityLiving());
                if (abilities.isEmpty())
                    return;
                Random random = new Random();
                Ability ability = getRandomAbilityForAI(abilities, random, 5);

                if (ability != null) {
                    if (ability.getAbilityType() == Ability.AbilityType.HELD) {
                        if (ability.isEnabled()) {
                            if (!MinecraftForge.EVENT_BUS.post(new AbilityKeyEvent.Server(ability, e.getEntityLiving(), false)))
                                ability.onKeyReleased();
                        } else {
                            if (!MinecraftForge.EVENT_BUS.post(new AbilityKeyEvent.Server(ability, e.getEntityLiving(), true)))
                                ability.onKeyPressed();
                        }
                    } else {
                        if (!MinecraftForge.EVENT_BUS.post(new AbilityKeyEvent.Server(ability, e.getEntityLiving(), true)))
                            ability.onKeyPressed();
                    }
                }
            }
        }

        public static Ability getRandomAbilityForAI(List<Ability> abilities, Random random, int tries) {
            if (tries == 0)
                return null;
            Ability ability = abilities.get(random.nextInt(abilities.size()));
            if (ability.getAbilityType() == Ability.AbilityType.CONSTANT || !ability.isUnlocked())
                return getRandomAbilityForAI(abilities, random, tries - 1);
            return ability;
        }

        @SubscribeEvent
        public void onLivingAttackEvent(LivingAttackEvent e) {
            for (Ability ab : Ability.getAbilities(e.getEntityLiving())) {
                if (ab.isUnlocked()) {
                    ab.onAttacked(e);
                }
            }
        }

        @SubscribeEvent
        public void onLivingHurtEvent(LivingHurtEvent e) {
            for (Ability ab : Ability.getAbilities(e.getEntityLiving())) {
                if (ab.isUnlocked()) {
                    ab.onEntityHurt(e);
                }
            }

            if (e.getSource() != null && e.getSource().getImmediateSource() != null && e.getSource().getImmediateSource() instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) e.getSource().getImmediateSource();

                for (Ability ab : Ability.getAbilities(entity)) {
                    if (ab.isUnlocked()) {
                        ab.onHurt(e);
                    }
                }
            }

        }

        @SubscribeEvent
        public void onAttackEntityEvent(AttackEntityEvent e) {
            for (Ability ab : Ability.getAbilities(e.getEntityLiving())) {
                if (ab.isUnlocked()) {
                    ab.onAttackEntity(e);
                }
            }
        }

        @SubscribeEvent
        public void onBreakSpeed(BreakSpeed e) {
            if (e.getEntityLiving() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) e.getEntityLiving();

                for (Ability ab : Ability.getAbilities(player)) {
                    if (ab.isUnlocked()) {
                        ab.onBreakSpeed(e);
                    }
                }
            }
        }

    }

    public static class CapabilitySuperpowerStorage implements IStorage<ISuperpowerCapability> {

        @Override
        public NBTBase writeNBT(Capability<ISuperpowerCapability> capability, ISuperpowerCapability instance, EnumFacing side) {
            return instance.writeNBT();
        }

        @Override
        public void readNBT(Capability<ISuperpowerCapability> capability, ISuperpowerCapability instance, EnumFacing side, NBTBase nbt) {
            instance.readNBT((NBTTagCompound) nbt);
        }

    }

}
