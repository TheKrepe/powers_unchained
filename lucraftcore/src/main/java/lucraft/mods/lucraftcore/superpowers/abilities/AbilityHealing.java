package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityHealing extends AbilityConstant {

    public static AbilityData<Integer> FREQUENCY = new AbilityDataInteger("frequency").disableSaving().setSyncType(EnumSync.NONE).enableSetting("frequency", "Sets the frequency of healing (in ticks)");
    public static AbilityData<Float> AMOUNT = new AbilityDataFloat("amount").disableSaving().setSyncType(EnumSync.NONE).enableSetting("amount", "Sets the amount of hearts for each healing");

    public AbilityHealing(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(FREQUENCY, 20);
        this.dataManager.register(AMOUNT, 0.5F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 0, 0);
    }

    @Override
    public boolean showInAbilityBar() {
        return false;
    }

    @Override
    public void updateTick() {
        int frequency = this.dataManager.get(FREQUENCY);
        if (frequency != 0 && ticks % frequency == 0) {
            entity.heal(this.dataManager.get(AMOUNT));
        }
    }

}
