package lucraft.mods.heroesexpansion.util.helper;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class HEIconHelper {

    public static final ResourceLocation SUPERPOWER_ICON_TEX = new ResourceLocation(HeroesExpansion.MODID, "textures/gui/superpower_icons.png");
    public static final ResourceLocation ICON_TEX = new ResourceLocation(HeroesExpansion.MODID, "textures/gui/icons.png");

    public static void drawIcon(Minecraft mc, Gui gui, int x, int y, int row, int column) {
        mc.renderEngine.bindTexture(ICON_TEX);
        gui.drawTexturedModalRect(x, y, column * 16, row * 16, 16, 16);
    }

    public static void drawIcon(Minecraft mc, int x, int y, int row, int column) {
        drawIcon(mc, mc.ingameGUI, x, y, row, column);
    }

    public static void drawSuperpowerIcon(Minecraft mc, Gui gui, int x, int y, int row, int column) {
        mc.renderEngine.bindTexture(SUPERPOWER_ICON_TEX);
        gui.drawTexturedModalRect(x, y, column * 32, row * 32, 32, 32);
    }

    public static void drawSuperpowerIcon(Minecraft mc, int x, int y, int row, int column) {
        drawIcon(mc, mc.ingameGUI, x, y, row, column);
    }

}
