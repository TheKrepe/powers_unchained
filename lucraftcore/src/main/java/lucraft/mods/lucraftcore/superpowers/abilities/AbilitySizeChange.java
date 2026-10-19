package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.capabilities.ISizeChanging;
import lucraft.mods.lucraftcore.sizechanging.sizechanger.SizeChanger;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataSizeChanger;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilitySizeChange extends AbilityToggle {

    public static final AbilityData<Float> SIZE = new AbilityDataFloat("size").disableSaving().setSyncType(EnumSync.NONE).enableSetting("size", "Well...the size you want to change to");
    public static final AbilityData<SizeChanger> SIZE_CHANGER = new AbilityDataSizeChanger("size_changer").disableSaving().setSyncType(EnumSync.NONE).enableSetting("size_changer", "The way to change size");

    public AbilitySizeChange(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(SIZE, 0.1F);
        this.dataManager.register(SIZE_CHANGER, SizeChanger.DEFAULT_SIZE_CHANGER);
    }

    @Override
    public String getDisplayDescription() {
        return super.getDisplayDescription() + "\n \n" + TextFormatting.BLUE + getSize();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 0, 10);
    }

    public float getSize() {
        return this.dataManager.get(SIZE);
    }

    public SizeChanger getSizeChanger() {
        return this.dataManager.get(SIZE_CHANGER);
    }

    @Override
    public boolean action() {
        if (!entity.hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null))
            return false;

        ISizeChanging data = entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null);
        float f = isEnabled() ? 1F : this.getSize();

        if (this.getSizeChanger() == null)
            data.setSize(f);
        else
            data.setSize(f, this.getSizeChanger());

        return true;
    }

    @Override
    public void updateTick() {

    }

    @Override
    public boolean isEnabled() {
        if (!entity.hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null))
            return false;
        return entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getSize() != 1F;
    }

}
