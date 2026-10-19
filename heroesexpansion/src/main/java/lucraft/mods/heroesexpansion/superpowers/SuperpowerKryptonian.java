package lucraft.mods.heroesexpansion.superpowers;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityFreezeBreath;
import lucraft.mods.heroesexpansion.abilities.AbilityHeatVision;
import lucraft.mods.heroesexpansion.abilities.AbilitySolarEnergy;
import lucraft.mods.heroesexpansion.abilities.AbilityXray;
import lucraft.mods.heroesexpansion.conditions.AbilityConditionSolarEnergy;
import lucraft.mods.heroesexpansion.potions.PotionKryptonitePoison;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.abilities.*;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionPotionWeakness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.LinkedList;
import java.util.UUID;

public class SuperpowerKryptonian extends Superpower {

    public SuperpowerKryptonian() {
        super("kryptonian");
        this.setRegistryName(HeroesExpansion.MODID, "kryptonian");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderIcon(Minecraft mc, Gui gui, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        HEIconHelper.drawSuperpowerIcon(mc, gui, x, y, 0, 5);
    }

    public UUID uuid = UUID.fromString("094b0779-992c-4fac-90f5-fa839dc77dbc");

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        AbilitySolarEnergy energy = (AbilitySolarEnergy) new AbilitySolarEnergy(entity).setDataValue(AbilitySolarEnergy.MAX_SOLAR_ENERGY, 100);

        abilities.put("strength", new AbilityStrength(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 20F));
        abilities.put("heat_vision", new AbilityHeatVision(entity).setDataValue(AbilityHeatVision.COLOR, Color.RED).addCondition(new AbilityConditionSolarEnergy(energy, 30)));
        abilities.put("health", new AbilityHealth(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 10f));
        abilities.put("resistance", new AbilityDamageResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 14F));
        abilities.put("flight", new AbilityFlight(entity).setDataValue(AbilityFlight.SPEED, 1F).setDataValue(AbilityFlight.SPRINT_SPEED, 5F).addCondition(new AbilityConditionSolarEnergy(energy, 10)));
        abilities.put("speed", new AbilitySprint(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 0.1f));
        abilities.put("knockback_resistance", new AbilityKnockbackResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 5f));
        abilities.put("fall_resistance", new AbilityFallResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 0.05f).setDataValue(AbilityAttributeModifier.OPERATION, 1));
        abilities.put("tough_lungs", new AbilityToughLungs(entity).addCondition(new AbilityConditionSolarEnergy(energy, 10)));
        abilities.put("water_breathing", new AbilityWaterBreathing(entity).addCondition(new AbilityConditionSolarEnergy(energy, 10)));
        abilities.put("freeze_breath", new AbilityFreezeBreath(entity).setMaxCooldown(60).addCondition(new AbilityConditionSolarEnergy(energy, 30)));
        abilities.put("x_ray", new AbilityXray(entity).setDataValue(Ability.COOLDOWN, 60).addCondition(new AbilityConditionSolarEnergy(energy, 60)));

        LinkedList<AbilitySolarEnergy.SolarAbilityEntry> list = new LinkedList<>();
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("flight", 1, 3));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("heat_vision", 1, 2));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("freeze_breath", 2, 2));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("x_ray", 2, 2));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("strength", 0, 0));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("resistance", 0, 0));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("sprint", 0, 0));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("fall_resistance", 0, 0));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("knockback_resistance", 0, 0));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("tough_lungs", 1, 3));
        list.add(new AbilitySolarEnergy.SolarAbilityEntry("water_breathing", 1, 3));
        energy.setDataValue(AbilitySolarEnergy.ABILITY_VALUES, list);
        abilities.put("solar_energy", energy);

        for (Ability ab : abilities.values())
            ab.addCondition(new AbilityConditionPotionWeakness(PotionKryptonitePoison.KRYPTONITE_POISON));

        return abilities;
    }
}
