package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataColor;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class AbilityForceField extends AbilityHeld {

    public static final AbilityData<Color> COLOR = new AbilityDataColor("color").disableSaving().setSyncType(EnumSync.EVERYONE).enableSetting("color", "Sets the color of the shield");
    public static final AbilityData<Float> OPACITY = new AbilityDataFloat("opacity").disableSaving().setSyncType(EnumSync.EVERYONE);

    public AbilityForceField(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(COLOR, new Color(2, 85, 255));
        this.dataManager.register(OPACITY, 0F);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        Color color = this.dataManager.get(COLOR);
        GlStateManager.enableBlend();
        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 15);
        GlStateManager.color(1, 1, 1);
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 14);
    }

    @Override
    public void updateTick() {
        if(this.dataManager.get(OPACITY) > 0F) {
            float f = MathHelper.clamp(this.dataManager.get(OPACITY) - 0.05F, 0F, 1F);
            this.dataManager.set(OPACITY, f);
        }
    }

    @Override
    public void onAttacked(LivingAttackEvent e) {
        if(this.isUnlocked() && this.isEnabled()) {
            e.setCanceled(true);
            this.dataManager.set(OPACITY, 1F);
        }
    }

    @Override
    public void onHurt(LivingHurtEvent e) {
        if(this.isUnlocked() && this.isEnabled()) {
            e.setAmount(0);
            e.setCanceled(true);
        }
    }

}
