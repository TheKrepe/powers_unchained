package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.suitsets.HESuitSet;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityToggle;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityGodMode extends AbilityToggle {

    public static final AbilityData<Float> DAMAGE = new AbilityDataFloat("damage").disableSaving().setSyncType(EnumSync.SELF).enableSetting("damage", "The amount of damage boost given by the ability");

    public AbilityGodMode(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.getDataManager().register(DAMAGE, 7F);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.getCooldown() != 0 && enabled) {
            if (entity instanceof EntityPlayer)
                ((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.ability_cooling_down"), true);
        } else
            super.setEnabled(enabled);
    }

    @Override
    public void updateTick() {

    }

    @Override
    public boolean action() {
        boolean b = super.action();

        if (isEnabled() && SuitSet.getSuitSet(entity) == HESuitSet.THOR && !entity.world.isRemote) {
            LCEntityHelper.setSuitOfPlayer(entity, HESuitSet.THOR_IW);
            entity.world.addWeatherEffect(new EntityLightningBolt(this.entity.world, entity.posX, entity.posY, entity.posZ, true));
        }

        return b;
    }

    @Override
    public void onHurt(LivingHurtEvent e) {
        if (this.isEnabled())
            e.setAmount(e.getAmount() + this.getDataManager().get(DAMAGE));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 5);
    }
}
