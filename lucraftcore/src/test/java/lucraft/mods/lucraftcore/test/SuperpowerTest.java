package lucraft.mods.lucraftcore.test;

import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.abilities.*;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.*;
import lucraft.mods.lucraftcore.utilities.items.ItemInjection;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.UUID;

public class SuperpowerTest extends Superpower {

    public SuperpowerTest(String name) {
        super(name);
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("jump_boost", new AbilityJumpBoost(entity).setDataValue(AbilityAttributeModifier.AMOUNT, 5F).setDataValue(AbilityAttributeModifier.OPERATION, 0).setDataValue(AbilityAttributeModifier.UUID, UUID.fromString("2cf0af54-6129-473d-9a98-eddbbc35e490")));
        abilities.put("health", new AbilityHealth(entity).setDataValue(AbilityAttributeModifier.AMOUNT, 10F).setDataValue(AbilityAttributeModifier.OPERATION, 0).setDataValue(AbilityAttributeModifier.UUID, UUID.fromString("2cf0af54-6129-473d-9a98-eddbbc35e490")));
        abilities.put("fall_resistance", new AbilityFallResistance(entity).setDataValue(AbilityAttributeModifier.AMOUNT, 2F).setDataValue(AbilityAttributeModifier.OPERATION, 0).setDataValue(AbilityAttributeModifier.UUID, UUID.fromString("2cf0af54-6129-473d-9a98-eddbbc35e490")));
        abilities.put("teleport", new AbilityTeleport(entity).setDataValue(AbilityTeleport.DISTANCE, 30F));
        abilities.put("fire_punch", new AbilityFirePunch(entity).setDataValue(AbilityFirePunch.DURATION, 30));
        abilities.put("potion_punch", new AbilityPotionPunch(entity).setDataValue(AbilityPotionPunch.POTION, MobEffects.LEVITATION).setDataValue(AbilityPotionPunch.AMPLIFIER, 1).setDataValue(AbilityPotionPunch.DURATION, 5*20));
        abilities.put("slowfall", new AbilitySlowfall(entity).addCondition(new AbilityConditionSuperpower(this)).addCondition(new AbilityCondition((a) -> false, new TextComponentString("test"))));
        abilities.put("flight", new AbilityFlight(entity).setDataValue(AbilityFlight.SPEED, 1F).setDataValue(AbilityFlight.SPRINT_SPEED, 5F));
        abilities.put("water_breathing", new AbilityWaterBreathing(entity));
        abilities.put("energy_blast", new AbilityEnergyBlast(entity).setDataValue(AbilityEnergyBlast.COLOR, Color.GREEN).setMaxCooldown(5 * 20).addCondition(new AbilityConditionOr(new AbilityConditionHeldItem(Items.APPLE, EnumHand.MAIN_HAND), new AbilityConditionLevel(5))));
        return abilities;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderIcon(net.minecraft.client.Minecraft mc, Gui gui, int x, int y) {
        GlStateManager.color(1, 1, 1);
        mc.renderEngine.bindTexture(TestMod.SUPERPOWER_ICON_TEX);
        gui.drawTexturedModalRect(x, y, 128, 0, 32, 32);
    }

    @Override
    public boolean canLevelUp() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCapsuleColor() {
        return 15073989;
    }

}