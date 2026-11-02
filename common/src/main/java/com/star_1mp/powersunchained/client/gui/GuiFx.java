package com.star_1mp.powersunchained.client.gui;

import com.star_1mp.powersunchained.Constants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Shared GUI drawing helpers of our machine screens (Suit Creator, Tech Table, powers menu, JEI / REI art). */
public final class GuiFx {

    /** 16x2 tile: a clear row, then a faint blue-grey line ({@code 0x163C5A6E}); mcmeta scaling = tile. */
    private static final Identifier SCANLINES = Constants.id("ui/scanlines");

    private GuiFx() {
    }

    /**
     * The monitors' scan lines (a faint line on every 2nd row, from {@code y + 2}) inside the frame of a {@code w x h}
     * monitor at {@code x, y}. Optimisation: ONE tiled sprite instead of one {@code fill} per line (a tall monitor used
     * to cost 100 draw calls a frame).
     */
    public static void scanlines(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (w > 2 && h > 3) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, SCANLINES, x + 1, y + 1, w - 2, h - 2);
        }
    }
}
