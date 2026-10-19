package lucraft.mods.heroesexpansion.superpowers;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityEnergyAbsorption;
import lucraft.mods.heroesexpansion.abilities.AbilityPhotonBlast;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.*;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionLevel;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainerSuperpower;
import lucraft.mods.lucraftcore.superpowers.effects.Effect;
import lucraft.mods.lucraftcore.superpowers.effects.EffectConditionAbilityEnabled;
import lucraft.mods.lucraftcore.superpowers.effects.EffectGlowingHand;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SuperpowerKreeHybrid extends Superpower {

    public List<Effect> effects = new ArrayList<>();

    public SuperpowerKreeHybrid() {
        super("kree_hybrid");
        this.setRegistryName(HeroesExpansion.MODID, "kree_hybrid");

        EffectGlowingHand glowingHand = new EffectGlowingHand();
        glowingHand.color = new Color(0.39F, 0.36F, 0);
        glowingHand.size = 1.5F;
        EffectConditionAbilityEnabled condition = new EffectConditionAbilityEnabled();
        condition.ability = new ResourceLocation(HeroesExpansion.MODID, "energy_absorption");
        glowingHand.conditions.add(condition);
        this.getEffects().add(glowingHand);
    }

    @Override
    public List<Effect> getEffects() {
        return effects;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderIcon(Minecraft mc, Gui gui, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        HEIconHelper.drawSuperpowerIcon(mc, gui, x, y, 1, 0);
    }

    public UUID uuid = UUID.fromString("570eff7c-088f-432a-a837-1c0fb1ae8da0");

    @Override
    public int getCapsuleColor() {
        return 16762880;
    }

    @Override
    public boolean canLevelUp() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("flight", new AbilityFlight(entity).setDataValue(AbilityFlight.SPEED, 1F).setDataValue(AbilityFlight.SPRINT_SPEED, 5F));
        abilities.put("energy_blast", new AbilityEnergyBlast(entity).setDataValue(AbilityEnergyBlast.COLOR, new Color(0.39f, 0.36f, 0f)).setMaxCooldown(30));
        abilities.put("energy_absorption", new AbilityEnergyAbsorption(entity).setDataValue(AbilityEnergyAbsorption.MAX_ENERGY, 100).setDataValue(AbilityEnergyAbsorption.MAX_DAMAGE, 10f).setMaxCooldown(20 * 20).addCondition(new AbilityConditionLevel(5)));
        abilities.put("photon_blast", new AbilityPhotonBlast(entity).setMaxCooldown(60).addCondition(new AbilityConditionLevel(3)));
        abilities.put("tough_lungs", new AbilityToughLungs(entity));
        abilities.put("strength", new AbilityStrength(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 16f));
        abilities.put("resistance", new AbilityDamageResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 10f));
        abilities.put("speed", new AbilitySprint(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 0.1f));
        abilities.put("health", new AbilityHealth(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 20f));
        abilities.put("jump", new AbilityJumpBoost(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 1.5f).setDataValue(AbilityAttributeModifier.OPERATION, 1));
        abilities.put("fall_resistance", new AbilityFallResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 20f));
        return abilities;
    }

    public static final int XP_AMOUNT_KILL = 25;
    public static final int XP_AMOUNT_ENERGY_BLAST = 50;
    public static final int XP_AMOUNT_PHOTON_BLAST = 20;

    public static void addXP(EntityLivingBase entity, int xp) {
        if (SuperpowerHandler.hasSuperpower(entity, HESuperpowers.KREE_HYBRID)) {
            SuitSet suitSet = SuitSet.getSuitSet(entity);

            if (suitSet != null && suitSet.getData() != null && suitSet.getData().hasKey("kree_hybrid_multiplier"))
                xp *= suitSet.getData().getFloat("kree_hybrid_multiplier");

            ((AbilityContainerSuperpower) Ability.getAbilityContainer(Ability.EnumAbilityContext.SUPERPOWER, entity)).addXP(xp, false);
        }
    }

}
