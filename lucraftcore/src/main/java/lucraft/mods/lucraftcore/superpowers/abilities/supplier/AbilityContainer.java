package lucraft.mods.lucraftcore.superpowers.abilities.supplier;

import com.google.common.collect.ImmutableMap;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityCommandOnGain;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import lucraft.mods.lucraftcore.superpowers.events.InitAbilitiesEvent;
import lucraft.mods.lucraftcore.superpowers.network.MessageSyncAbilityContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;

public class AbilityContainer implements INBTSerializable<NBTTagCompound> {

    public final EntityLivingBase entity;
    public final Ability.EnumAbilityContext context;
    public IAbilityProvider provider;
    protected Ability.AbilityMap abilities;
    public EnumSync sync = EnumSync.NONE;

    public AbilityContainer(EntityLivingBase entity, Ability.EnumAbilityContext context) {
        this.entity = entity;
        this.context = context;
        this.abilities = new Ability.AbilityMap();
    }

    public Collection<Ability> getAbilities() {
        return abilities.values();
    }

    public Ability getAbility(String key) {
        return this.abilities.get(key);
    }

    public String getKeyForAbility(Ability ability) {
        for (String s : this.abilities.keySet()) {
            if (this.abilities.get(s) == ability) {
                return s;
            }
        }

        return null;
    }

    public void onUpdate() {
        IAbilityProvider currentProvider = Ability.getAbilityProvider(this.entity, this.context);

        if (currentProvider != this.provider)
            this.switchProvider(currentProvider);

        for (Ability ab : getAbilities()) {
            ab.onUpdate();

            if (ab.sync != null && ab.sync != EnumSync.NONE) {
                sync = sync.add(ab.sync);
                ab.sync = EnumSync.NONE;
            }
        }

        if (sync != EnumSync.NONE) {
            sync();
            this.sync = EnumSync.NONE;
        }
    }

    public void switchProvider(IAbilityProvider provider) {
        for (Ability ab : getAbilities())
            if (ab.isUnlocked())
                ab.lastTick();

        if (this.provider != null)
            save();

        this.provider = provider;

        if (this.provider != null) {
            this.abilities = filterAbilities(provider.addDefaultAbilities(this.entity, new Ability.AbilityMap(), this.context));
            load();
        } else {
            this.abilities.clear();
        }

        if(this.context == Ability.EnumAbilityContext.SUPERPOWER && SuperpowerHandler.getSuperpowerCapability(this.entity).hasGainedSuperpower()) {
            for (AbilityCommandOnGain ability : Ability.getAbilitiesFromClass(getAbilities(), AbilityCommandOnGain.class)) {
                if (ability != null && ability.isUnlocked()) {
                    ability.executeCommands();
                }
            }
            SuperpowerHandler.getSuperpowerCapability(entity).setSuperpowerGained(false);
        }

        this.sync = this.sync.add(EnumSync.EVERYONE);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.abilities.forEach((s, a) -> nbt.setTag(s, a.serializeNBT()));
        return nbt;
    }

    public NBTTagCompound serializeNBTSync() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.abilities.forEach((s, a) -> nbt.setTag(s, a.serializeNBTSync()));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (this.provider == null) {
            this.abilities = new Ability.AbilityMap();
            return;
        }

        this.abilities = this.provider.addDefaultAbilities(this.entity, new Ability.AbilityMap(), this.context);
        this.abilities.forEach((s, ability) -> {
            if (nbt.hasKey(s))
                ability.deserializeNBT(nbt.getCompoundTag(s));
        });
        this.abilities = filterAbilities(this.abilities);
    }

    public void deserializeNBTSync(NBTTagCompound nbt) {
        if (this.provider == null) {
            this.abilities = new Ability.AbilityMap();
            return;
        }
        this.abilities = this.provider.addDefaultAbilities(this.entity, new Ability.AbilityMap(), this.context);
        this.abilities.forEach((s, ability) -> {
            if (nbt.hasKey(s)) {
                ability.deserializeNBTSync(nbt.getCompoundTag(s));
            }
        });
        this.abilities = filterAbilities(this.abilities);
    }

    public Ability.AbilityMap filterAbilities(Ability.AbilityMap abilityList) {
        Ability.AbilityMap l = new Ability.AbilityMap();
        abilityList.forEach((s, a) -> l.put(s, a));
        for (Ability ab : l.values())
            ab.context = context;
        MinecraftForge.EVENT_BUS.post(new InitAbilitiesEvent.Pre(entity, l, context));
        for (Ability ab : l.values()) {
            ab.init(ImmutableMap.copyOf(l));
        }
        MinecraftForge.EVENT_BUS.post(new InitAbilitiesEvent.Post(entity, l, context));
        return l;
    }

    public void save() {
        if (entity != null && entity.world != null && !entity.world.isRemote && entity.hasCapability(CapabilitySuperpower.SUPERPOWER_CAP, null))
            entity.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null).getData().setTag(context.toString(), this.serializeNBT());
    }

    public void load() {
        this.deserializeNBT(entity.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null).getData().getCompoundTag(context.toString()));
        for(Ability ability : this.getAbilities()) {
            ability.updateConditions();
        }
    }

    public void sync() {
        if (entity.world.isRemote)
            return;
        if ((sync == EnumSync.SELF || sync == EnumSync.EVERYONE) && entity instanceof EntityPlayerMP)
            LCPacketDispatcher.sendTo(new MessageSyncAbilityContainer(entity, this), (EntityPlayerMP) entity);
        if (sync == EnumSync.EVERYONE)
            ((WorldServer) entity.world).getEntityTracker().getTrackingPlayers(entity).forEach((p) -> LCPacketDispatcher.sendTo(new MessageSyncAbilityContainer(entity, this), (EntityPlayerMP) p));
    }

}
