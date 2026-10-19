package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityFireResistance extends AbilityConstant {

    public AbilityFireResistance(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 0, 6);
    }

    @Override
    public boolean showInAbilityBar() {
        return false;
    }

    @Override
    public void updateTick() {

    }

    @Override
    public void onAttacked(LivingAttackEvent e) {
        if (e.getSource().isFireDamage()) {
            e.setCanceled(true);
        }
    }

    @Override
    public void onHurt(LivingHurtEvent e) {
        if (e.getSource().isFireDamage()) {
            e.setAmount(0);
            e.setCanceled(true);
        }
    }

}
