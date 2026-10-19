package lucraft.mods.lucraftcore.superpowers;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityCommandOnGain;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityCommandOnLose;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import lucraft.mods.lucraftcore.superpowers.capabilities.ISuperpowerCapability;
import lucraft.mods.lucraftcore.superpowers.events.LivingSuperpowerEvent;
import lucraft.mods.lucraftcore.util.triggers.LCCriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = LucraftCore.MODID)
public class SuperpowerHandler {

    public static IForgeRegistry<Superpower> SUPERPOWER_REGISTRY;

    @SubscribeEvent
    public static void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
        SUPERPOWER_REGISTRY = new RegistryBuilder<Superpower>().setName(new ResourceLocation(LucraftCore.MODID, "superpower")).setType(Superpower.class).setIDRange(0, 512).create();
    }

    public static ISuperpowerCapability getSuperpowerCapability(EntityLivingBase entity) {
        return entity.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null);
    }

    /**
     * Sets the entity's superpower without paying attention to the current one.
     *
     * @param entity
     * @param superpower
     */
    public static void setSuperpower(EntityLivingBase entity, Superpower superpower) {
        if (superpower != null && !SUPERPOWER_REGISTRY.containsValue(superpower)) {
            LucraftCore.LOGGER.error("That superpower isn't registered!");
            return;
        }

        if (getSuperpowerCapability(entity) == null)
            return;

        getSuperpowerCapability(entity).setSuperpower(superpower);
        getSuperpowerCapability(entity).setSuperpowerGained(true);
        syncToAll(entity);

        if (entity instanceof EntityPlayerMP)
            LCCriteriaTriggers.GET_SUPERPOWER.trigger((EntityPlayerMP) entity, superpower);
    }

    /**
     * Sets the player's superpower if the current superpower allows to be
     * overriden/removed.
     *
     * @param entity
     * @param superpower
     */
    public static void giveSuperpower(EntityLivingBase entity, Superpower superpower) {
        if (MinecraftForge.EVENT_BUS.post(new LivingSuperpowerEvent(entity, superpower)))
            return;

        if (hasSuperpower(entity))
            return;

        setSuperpower(entity, superpower);
    }

    public static void removeSuperpower(EntityLivingBase entity) {
        Superpower current = getSuperpower(entity);

        if (current != null) {
            for (AbilityCommandOnLose ability : Ability.getAbilitiesFromClass(getSuperpowerCapability(entity).getAbilityContainer(Ability.EnumAbilityContext.SUPERPOWER).getAbilities(), AbilityCommandOnLose.class)) {
                if (ability != null && ability.isUnlocked()) {
                    ability.executeCommands();
                }
            }

            if (entity instanceof EntityPlayerMP)
                LCCriteriaTriggers.LOSE_SUPERPOWER.trigger((EntityPlayerMP) entity, current);

            setSuperpower(entity, null);
        }
    }

    public static Superpower getSuperpower(EntityLivingBase entity) {
        ISuperpowerCapability cap = getSuperpowerCapability(entity);
        return cap == null ? null : cap.getSuperpower();
    }

    public static boolean hasSuperpower(EntityLivingBase entity) {
        return getSuperpower(entity) != null;
    }

    public static boolean hasSuperpower(EntityLivingBase entity, Superpower superpower) {
        return getSuperpower(entity) == superpower;
    }

    /**
     * Syncs the superpower data only to the player itself.
     *
     * @param entity The entity that is about to get information about his superpower.
     */
    public static void syncToPlayer(EntityLivingBase entity) {
        getSuperpowerCapability(entity).syncToPlayer();
    }

    /**
     * Syncs the superpower data to the given receiver.
     *
     * @param entity   The entity whose superpower data will be sent.
     * @param receiver The player that is about to get information about the superpower.
     */
    public static void syncToPlayer(EntityLivingBase entity, EntityPlayer receiver) {
        getSuperpowerCapability(entity).syncToPlayer(receiver);
    }

    /**
     * Syncs the superpower data to all players.
     *
     * @param entity The entity whose superpower data will be sent to everyone.
     */
    public static void syncToAll(EntityLivingBase entity) {
        getSuperpowerCapability(entity).syncToAll();
    }

}
