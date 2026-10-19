package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityWaterBreathing extends AbilityConstant {

    public AbilityWaterBreathing(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 1, 1);
    }

    @Override
    public boolean showInAbilityBar() {
        return false;
    }

    @Override
    public void updateTick() {
        entity.setAir(280);
    }

    @Override
    public void onEntityHurt(LivingHurtEvent e) {
        if (e.getSource() == DamageSource.DROWN) {
            e.setAmount(0);
            e.setCanceled(true);
        }
        super.onHurt(e);
    }

    @Override
    public void onAttacked(LivingAttackEvent e) {
        if (e.getSource() == DamageSource.DROWN && e.getAmount() <= 0) {
            e.setCanceled(true);
        }
    }

}
