package lucraft.mods.heroesexpansion.superpowers;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityPhotonBlast;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityEnergyBlast;
import lucraft.mods.lucraftcore.superpowers.events.AbilityKeyEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HESuperpowers {

    public static SuperpowerGodOfThunder GOD_OF_THUNDER = new SuperpowerGodOfThunder();
    public static SuperpowerKryptonian KRYPTONIAN = new SuperpowerKryptonian();
    public static SuperpowerBlackPanther BLACK_PANTHER = new SuperpowerBlackPanther();
    public static SuperpowerSuperSoldier SUPER_SOLDIER = new SuperpowerSuperSoldier();
    public static SuperpowerKreeHybrid KREE_HYBRID = new SuperpowerKreeHybrid();
    public static SuperpowerSpiderPowers SPIDER_POWERS = new SuperpowerSpiderPowers();
    public static SuperpowerBlindness BLINDNESS = new SuperpowerBlindness();

    @SubscribeEvent
    public static void onRegisterSuperpowers(RegistryEvent.Register<Superpower> e) {
        e.getRegistry().register(GOD_OF_THUNDER);
        e.getRegistry().register(KRYPTONIAN);
        e.getRegistry().register(BLACK_PANTHER);
        e.getRegistry().register(SUPER_SOLDIER);
        e.getRegistry().register(KREE_HYBRID);
        e.getRegistry().register(SPIDER_POWERS);
        e.getRegistry().register(BLINDNESS);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent e) {
        if (e.getSource() != null && e.getSource().getTrueSource() != null && e.getSource().getTrueSource() instanceof EntityLivingBase) {
            SuperpowerKreeHybrid.addXP((EntityLivingBase) e.getSource().getTrueSource(), SuperpowerKreeHybrid.XP_AMOUNT_KILL);
        }
    }

    @SubscribeEvent
    public static void onKill(AbilityKeyEvent e) {
        if (e.pressed) {
            if (e.ability instanceof AbilityEnergyBlast)
                SuperpowerKreeHybrid.addXP(e.ability.getEntity(), SuperpowerKreeHybrid.XP_AMOUNT_ENERGY_BLAST);
            else if (e.ability instanceof AbilityPhotonBlast)
                SuperpowerKreeHybrid.addXP(e.ability.getEntity(), SuperpowerKreeHybrid.XP_AMOUNT_PHOTON_BLAST);
        }
    }

}