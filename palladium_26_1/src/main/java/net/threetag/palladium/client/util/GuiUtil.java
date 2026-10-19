package net.threetag.palladium.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class GuiUtil {

    public static void drawStringWithOutline(GuiGraphicsExtractor guiGraphics, Component text, int x, int y, int textColor, int outlineColor) {
        var font = Minecraft.getInstance().font;

        guiGraphics.text(font, text, x + 2, y - 1, outlineColor, false);
        guiGraphics.text(font, text, x, y - 1, outlineColor, false);
        guiGraphics.text(font, text, x + 1, y, outlineColor, false);
        guiGraphics.text(font, text, x + 1, y - 2, outlineColor, false);
        guiGraphics.text(font, text, x, y - 2, outlineColor, false);
        guiGraphics.text(font, text, x, y, outlineColor, false);
        guiGraphics.text(font, text, x + 2, y, outlineColor, false);
        guiGraphics.text(font, text, x + 2, y - 2, outlineColor, false);

        guiGraphics.text(font, text, x + 1, y - 1, textColor, false);
    }

    public static void drawStringWithOutline(GuiGraphicsExtractor guiGraphics, FormattedCharSequence text, int x, int y, int textColor, int outlineColor) {
        var font = Minecraft.getInstance().font;

        guiGraphics.text(font, text, x + 2, y - 1, outlineColor, false);
        guiGraphics.text(font, text, x, y - 1, outlineColor, false);
        guiGraphics.text(font, text, x + 1, y, outlineColor, false);
        guiGraphics.text(font, text, x + 1, y - 2, outlineColor, false);
        guiGraphics.text(font, text, x, y - 2, outlineColor, false);
        guiGraphics.text(font, text, x, y, outlineColor, false);
        guiGraphics.text(font, text, x + 2, y, outlineColor, false);
        guiGraphics.text(font, text, x + 2, y - 2, outlineColor, false);

        guiGraphics.text(font, text, x + 1, y - 1, textColor, false);
    }

    public static void drawStringWithBlackOutline(GuiGraphicsExtractor guiGraphics, Component text, int x, int y, int textColor) {
        drawStringWithOutline(guiGraphics, text, x, y, textColor, RenderUtil.FULL_BLACK);
    }

    public static void drawStringWithBlackOutline(GuiGraphicsExtractor guiGraphics, FormattedCharSequence text, int x, int y, int textColor) {
        drawStringWithOutline(guiGraphics, text, x, y, textColor, RenderUtil.FULL_BLACK);
    }

    public static void drawCenteredWordWrap(GuiGraphicsExtractor guiGraphics, FormattedText text, int x, int y, int lineWidth, int textColor) {
        var font = Minecraft.getInstance().font;

        for (FormattedCharSequence formatted : font.split(text, lineWidth)) {
            guiGraphics.centeredText(font, formatted, x, y, textColor);
            y += 9;
        }
    }

}
