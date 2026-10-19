package lucraft.mods.lucraftcore.util.gui;

import lucraft.mods.lucraftcore.util.helper.LCMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiColorSlider extends GuiSlider {

    public GuiColorSlider(int id, int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, ISlider parent) {
        super(id, xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, parent);
    }

    @Override
    protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
        super.mouseDragged(par1Minecraft, par2, par3);
        this.sliderValue = LCMathHelper.round(sliderValue, 2);
        updateText();
    }

    public void updateText() {
        this.displayString = this.dispString + ": " + sliderValue;
    }

}