package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.conditions.AbilityConditionSolarEnergy;
import lucraft.mods.heroesexpansion.conditions.AbilityConditionThorWeapon;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityEntry;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityCondition;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HEAbilities {

    @SubscribeEvent
    public static void onRegisterAbilities(RegistryEvent.Register<AbilityEntry> e) {
        e.getRegistry().register(new AbilityEntry(AbilityGodMode.class, new ResourceLocation(HeroesExpansion.MODID, "god_mode")));
        e.getRegistry().register(new AbilityEntry(AbilityLightningStrike.class, new ResourceLocation(HeroesExpansion.MODID, "lightning_strike")));
        e.getRegistry().register(new AbilityEntry(AbilityLightningAttack.class, new ResourceLocation(HeroesExpansion.MODID, "lightning_attack")));
        e.getRegistry().register(new AbilityEntry(AbilitySolarEnergy.class, new ResourceLocation(HeroesExpansion.MODID, "solar_energy")));
        e.getRegistry().register(new AbilityEntry(AbilityHeatVision.class, new ResourceLocation(HeroesExpansion.MODID, "heat_vision")));
        e.getRegistry().register(new AbilityEntry(AbilityKineticEnergyAbsorption.class, new ResourceLocation(HeroesExpansion.MODID, "kinetic_energy_absorption")));
        e.getRegistry().register(new AbilityEntry(AbilityCallWeapon.class, new ResourceLocation(HeroesExpansion.MODID, "call_weapon")));
        e.getRegistry().register(new AbilityEntry(AbilityEnergyAbsorption.class, new ResourceLocation(HeroesExpansion.MODID, "energy_absorption")));
        e.getRegistry().register(new AbilityEntry(AbilityIDontFeelSoGood.class, new ResourceLocation(HeroesExpansion.MODID, "i_dont_feel_so_good")));
        e.getRegistry().register(new AbilityEntry(AbilitySpiderSense.class, new ResourceLocation(HeroesExpansion.MODID, "spider_sense")));
        e.getRegistry().register(new AbilityEntry(AbilityWallCrawling.class, new ResourceLocation(HeroesExpansion.MODID, "wall_crawling")));
        e.getRegistry().register(new AbilityEntry(AbilityWebWings.class, new ResourceLocation(HeroesExpansion.MODID, "web_wings")));
        e.getRegistry().register(new AbilityEntry(AbilityBlindness.class, new ResourceLocation(HeroesExpansion.MODID, "blindness")));
        e.getRegistry().register(new AbilityEntry(AbilityPrecision.class, new ResourceLocation(HeroesExpansion.MODID, "precision")));
        e.getRegistry().register(new AbilityEntry(AbilityPortal.class, new ResourceLocation(HeroesExpansion.MODID, "portal")));
        e.getRegistry().register(new AbilityEntry(AbilityGrabEntity.class, new ResourceLocation(HeroesExpansion.MODID, "grab_entity")));
        e.getRegistry().register(new AbilityEntry(AbilityPhotonBlast.class, new ResourceLocation(HeroesExpansion.MODID, "photon_blast")));
        e.getRegistry().register(new AbilityEntry(AbilityForceField.class, new ResourceLocation(HeroesExpansion.MODID, "force_field")));
        e.getRegistry().register(new AbilityEntry(AbilityBlackHole.class, new ResourceLocation(HeroesExpansion.MODID, "black_hole")));
        e.getRegistry().register(new AbilityEntry(AbilityFreezeBreath.class, new ResourceLocation(HeroesExpansion.MODID, "freeze_breath")));
        e.getRegistry().register(new AbilityEntry(AbilityXray.class, new ResourceLocation(HeroesExpansion.MODID, "x_ray")));
    }

    @SubscribeEvent
    public static void onRegisterConditions(RegistryEvent.Register<AbilityCondition.ConditionEntry> e) {
        e.getRegistry().register(new AbilityCondition.ConditionEntry(AbilityConditionSolarEnergy.class, new ResourceLocation(HeroesExpansion.MODID, "")));

        AbilityCondition.ConditionEntry.register(e.getRegistry(), AbilityConditionThorWeapon.class, new ResourceLocation(HeroesExpansion.MODID, "thor_weapon"), (j, a, l) -> new AbilityConditionThorWeapon());

        AbilityCondition.ConditionEntry.register(e.getRegistry(), AbilityConditionSolarEnergy.class, new ResourceLocation(LucraftCore.MODID, "solar_energy"), (j, a, l) -> {
            Ability ab = l.get(JsonUtils.getString(j, "ability"));
            int minimum = JsonUtils.getInt(j, "minimum");
            return ab == null || !(ab instanceof AbilitySolarEnergy) || minimum < 0 ? null : new AbilityConditionSolarEnergy((AbilitySolarEnergy) ab, minimum);
        });
    }

}