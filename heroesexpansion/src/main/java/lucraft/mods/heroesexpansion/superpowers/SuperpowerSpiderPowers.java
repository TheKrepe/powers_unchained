package lucraft.mods.heroesexpansion.superpowers;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilitySpiderSense;
import lucraft.mods.heroesexpansion.abilities.AbilityWallCrawling;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.abilities.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class SuperpowerSpiderPowers extends Superpower {

    public SuperpowerSpiderPowers() {
        super("spider_powers");
        this.setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "spider_powers"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderIcon(Minecraft mc, Gui gui, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        HEIconHelper.drawSuperpowerIcon(mc, gui, x, y, 0, 4);
    }

    public UUID uuid = UUID.fromString("1c98ba6f-a551-4b01-b02a-54ba675da498");

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("spider_sense", new AbilitySpiderSense(entity));
        abilities.put("wall_crawling", new AbilityWallCrawling(entity));
        abilities.put("strength", new AbilityStrength(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 10f));
        abilities.put("resistance", new AbilityDamageResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 6f));
        abilities.put("jump", new AbilityJumpBoost(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 3f).setDataValue(AbilityAttributeModifier.OPERATION, 1));
        abilities.put("health", new AbilityHealth(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 10f));
        abilities.put("fall_resistance", new AbilityFallResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 50f));
        abilities.put("speed", new AbilitySprint(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 0.06f));
        return abilities;
    }
}
