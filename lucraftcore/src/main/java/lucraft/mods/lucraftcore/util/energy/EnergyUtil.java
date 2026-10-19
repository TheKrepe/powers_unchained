package lucraft.mods.lucraftcore.util.energy;

import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EnergyUtil {

    public static final String ENERGY_UNIT = "FE";

    @SideOnly(Side.CLIENT)
    public static String getFormattedEnergy(int energy, int maxEnergy) {
        return StringHelper.translateToLocal("lucraftcore.info.energy_storage_display", energy, maxEnergy, ENERGY_UNIT);
    }

    @SideOnly(Side.CLIENT)
    public static String getFormattedEnergy(IEnergyStorage energyStorage) {
        return getFormattedEnergy(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored());
    }

    @SideOnly(Side.CLIENT)
    public static void drawTooltip(EnergyStorage energyStorage, GuiContainer gui, int x, int y, int width, int height, int mouseX, int mouseY) {
        drawTooltip(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored(), gui, x, y, width, height, mouseX, mouseY);
    }

    @SideOnly(Side.CLIENT)
    public static void drawTooltip(int energy, int maxEnergy, GuiContainer gui, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            String s = getFormattedEnergy(energy, maxEnergy);
            gui.drawHoveringText(s, mouseX + 10, mouseY);
        }
    }

}
