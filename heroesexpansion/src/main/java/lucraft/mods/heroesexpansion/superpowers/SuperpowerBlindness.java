package lucraft.mods.heroesexpansion.superpowers;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityBlindness;
import lucraft.mods.heroesexpansion.abilities.AbilityPrecision;
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

public class SuperpowerBlindness extends Superpower {

    public SuperpowerBlindness() {
        super("blindness");
        this.setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "blindness"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderIcon(Minecraft mc, Gui gui, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        HEIconHelper.drawSuperpowerIcon(mc, gui, x, y, 1, 1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getCapsuleColor() {
        return 7798784;
    }

    public UUID uuid = UUID.fromString("98584577-5477-4ff7-bb96-b52ebf639045");

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("blindness", new AbilityBlindness(entity));
        abilities.put("precision", new AbilityPrecision(entity));
        abilities.put("strength", new AbilityStrength(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 10f));
        abilities.put("health", new AbilityHealth(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 5f));
        abilities.put("resistance", new AbilityDamageResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 10f));
        abilities.put("fall_resistance", new AbilityFallResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 20f));

        return abilities;
    }
}
