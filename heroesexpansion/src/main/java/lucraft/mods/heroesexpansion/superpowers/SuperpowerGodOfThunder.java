package lucraft.mods.heroesexpansion.superpowers;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityCallWeapon;
import lucraft.mods.heroesexpansion.abilities.AbilityGodMode;
import lucraft.mods.heroesexpansion.abilities.AbilityLightningAttack;
import lucraft.mods.heroesexpansion.abilities.AbilityLightningStrike;
import lucraft.mods.heroesexpansion.conditions.AbilityConditionThorWeapon;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.*;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionLevel;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionOr;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainerSuperpower;
import lucraft.mods.lucraftcore.superpowers.effects.Effect;
import lucraft.mods.lucraftcore.superpowers.effects.EffectConditionAbilityEnabled;
import lucraft.mods.lucraftcore.superpowers.effects.EffectFlickering;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail;
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

public class SuperpowerGodOfThunder extends Superpower {

    public List<Effect> effects = new ArrayList<>();

    public SuperpowerGodOfThunder() {
        super("god_of_thunder");
        this.setRegistryName(HeroesExpansion.MODID, "god_of_thunder");

        EffectFlickering flickering = new EffectFlickering();
        flickering.color = new Color(0.28F, 1F, 1F);
        EffectConditionAbilityEnabled condition = new EffectConditionAbilityEnabled();
        condition.ability = new ResourceLocation(HeroesExpansion.MODID, "god_mode");
        flickering.conditions.add(condition);
        this.getEffects().add(flickering);

        EffectTrail trail = new EffectTrail();
        trail.color = flickering.color;
        trail.type = EffectTrail.TrailType.ELECTRICITY;
        trail.conditions = flickering.conditions;
        this.getEffects().add(trail);
    }

    @Override
    public List<Effect> getEffects() {
        return effects;
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
    public int getCapsuleColor() {
        return 59135;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderIcon(Minecraft mc, Gui gui, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        HEIconHelper.drawSuperpowerIcon(mc, gui, x, y, 0, 2);
    }

    public UUID uuid = UUID.fromString("f1a875cb-32ad-4103-80b6-479511c4062b");

    public static final int XP_AMOUNT_MJOLNIR_THROW = 25;
    public static final int XP_AMOUNT_MJOLNIR_FLY = 2;
    public static final int XP_AMOUNT_LIGHTING_STRIKE = 1;
    public static final int XP_AMOUNT_LIGHTNING_ATTACK = 50;

    public static void addXP(EntityLivingBase entity, int xp) {
        if (SuperpowerHandler.hasSuperpower(entity, HESuperpowers.GOD_OF_THUNDER)) {
            SuitSet suitSet = SuitSet.getSuitSet(entity);

            if (suitSet != null && suitSet.getData() != null && suitSet.getData().hasKey("god_of_thunder_multiplier"))
                xp *= suitSet.getData().getFloat("god_of_thunder_multiplier");

            ((AbilityContainerSuperpower) Ability.getAbilityContainer(Ability.EnumAbilityContext.SUPERPOWER, entity)).addXP(xp, false);
        }
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("god_mode", new AbilityGodMode(entity).setDataValue(AbilityGodMode.DAMAGE, 7f).setMaxCooldown(100).addCondition(new AbilityConditionLevel(5)));
        abilities.put("lightning_strike", new AbilityLightningStrike(entity).setMaxCooldown(5 * 20).addCondition(new AbilityConditionOr(new AbilityConditionThorWeapon(), new AbilityConditionLevel(5))));
        abilities.put("lightning_attack", new AbilityLightningAttack(entity).setDataValue(AbilityLightningAttack.DISTANCE, 30f).setMaxCooldown(5 * 20).addCondition(new AbilityConditionOr(new AbilityConditionThorWeapon(), new AbilityConditionLevel(5))));
        abilities.put("tough_lungs", new AbilityToughLungs(entity));
        abilities.put("call_weapon", new AbilityCallWeapon(entity));
        abilities.put("strength", new AbilityStrength(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 11f));
        abilities.put("speed", new AbilitySprint(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 0.1f));
        abilities.put("resistance", new AbilityDamageResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 10f));
        abilities.put("jump", new AbilityJumpBoost(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 2f).setDataValue(AbilityAttributeModifier.OPERATION, 1));
        abilities.put("fall_resistance", new AbilityFallResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 20f));
        abilities.put("health", new AbilityHealth(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 20f));
        return abilities;
    }
}
