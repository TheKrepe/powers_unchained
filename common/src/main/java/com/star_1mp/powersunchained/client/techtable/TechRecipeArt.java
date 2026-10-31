package com.star_1mp.powersunchained.client.techtable;

import com.star_1mp.powersunchained.tech.TechRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * How a Tech Table recipe looks in the recipe viewers (JEI / REI share it), in the Tech Table's own style: a steel
 * title bar with a copper edge and the result's name (owner: no numbering), then a sheet of blueprint paper with the 3x3 sockets, a dashed
 * arrow that fills up, and the result in a glowing copper frame ("shapeless" noted under it). The viewers put their
 * item slots at {@link #cellSlot} / {@link #RESULT} (item positions, 16x16).
 */
public final class TechRecipeArt {

    public static final int WIDTH = 150;
    public static final int HEIGHT = 84;
    /** Item position of the result. */
    public static final int[] RESULT = {112, 44};

    private static final int PAPER = 0xFF1D3F73;
    private static final int PAPER_L = 0xFF2B5592;
    private static final int INK = 0xFFE9F3FF;
    private static final int INK_D = 0xFF9FC2EE;
    private static final int COPPER = 0xFFFF9A52;
    private static final int COPPER_DARK = 0xFF9C5426;
    private static final int STEEL = 0xFF2A2F35;
    private static final int SOCKET = 0xC80A1932;

    private TechRecipeArt() {
    }

    /** Item position of grid cell {@code i} (0..8, row by row). */
    public static int[] cellSlot(int i) {
        return new int[] {10 + (i % 3) * 21, 23 + (i / 3) * 20};
    }

    /** Draws the whole recipe background, its top-left corner at {@code x, y}. */
    public static void draw(GuiGraphicsExtractor g, TechRecipe recipe, int x, int y) {
        Font font = Minecraft.getInstance().font;
        float time = (System.currentTimeMillis() % 100000L) / 1000.0F;

        // steel title bar: copper edge, name, drawing number
        g.fill(x, y, x + WIDTH, y + 15, STEEL);
        g.fill(x, y, x + WIDTH, y + 1, COPPER);
        g.fill(x, y + 1, x + WIDTH, y + 2, COPPER_DARK);
        g.fill(x, y + 14, x + WIDTH, y + 15, 0xFF15181C);
        g.text(font, Component.literal(trim(font, recipe.resultStack().getHoverName().getString(), WIDTH - 8)), x + 4, y + 4, 0xFFFFFFFF, true);

        // blueprint paper with a grid and a double border
        int py = y + 16;
        int ph = HEIGHT - 16;
        g.fill(x, py, x + WIDTH, py + ph, PAPER);
        for (int gx = x + 4; gx < x + WIDTH - 1; gx += 8) {
            g.fill(gx, py + 1, gx + 1, py + ph - 1, PAPER_L);
        }
        for (int gy = py + 4; gy < py + ph - 1; gy += 8) {
            g.fill(x + 1, gy, x + WIDTH - 1, gy + 1, PAPER_L);
        }
        outline(g, x, py, WIDTH, ph, INK_D);
        outline(g, x + 2, py + 2, WIDTH - 4, ph - 4, 0x809FC2EE);

        // 3x3 sockets: corner ticks like drawn parts, used ones brighter
        var layout = recipe.layout();
        for (int i = 0; i < 9; i++) {
            int[] s = cellSlot(i);
            boolean used = layout.get(i) != null;
            g.fill(x + s[0] - 1, y + s[1] - 1, x + s[0] + 17, y + s[1] + 17, SOCKET);
            outline(g, x + s[0] - 1, y + s[1] - 1, 18, 18, used ? INK : 0x709FC2EE);
            if (used) {
                corners(g, x + s[0] - 3, y + s[1] - 3, 22, 22, INK_D);
            }
        }

        // dashed arrow that fills up, like ink running along it
        int ax0 = x + 77;
        int ax1 = x + 102;
        int ay = y + RESULT[1] + 8;
        float p = (time % 1.6F) / 1.6F;
        int filled = Math.round((ax1 - ax0) * p);
        for (int px = ax0; px < ax1; px++) {
            if (((px - ax0) / 2) % 2 == 0) {
                g.fill(px, ay, px + 1, ay + 1, px - ax0 <= filled ? INK : 0xA09FC2EE);
            }
        }
        for (int k = 0; k < 4; k++) {
            g.fill(ax1 - k, ay - k, ax1 - k + 1, ay + k + 1, INK);
        }

        // the result: a glowing copper frame
        int rx = x + RESULT[0];
        int ry = y + RESULT[1];
        int pulse = Math.round(150 + 90 * Mth.sin(time * 3.0F));
        g.fill(rx - 5, ry - 5, rx + 21, ry + 21, 0xE0101C30);
        outline(g, rx - 5, ry - 5, 26, 26, COPPER);
        outline(g, rx - 6, ry - 6, 28, 28, (pulse << 24) | 0xFF9A52);
        corners(g, rx - 8, ry - 8, 32, 32, COPPER_DARK);
        g.centeredText(font, Component.translatable("recipe.powers_unchained.tech_table.result"), rx + 8, ry - 17, INK_D);
        if (!recipe.shaped()) {
            g.centeredText(font, Component.translatable("recipe.powers_unchained.tech_table.shapeless"), rx + 8, ry + 26, INK_D);
        }
    }

    private static String trim(Font font, String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        while (!text.isEmpty() && font.width(text + "…") > width) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "…";
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    /** Short L-shaped marks on the four corners of a box. */
    private static void corners(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        int l = 3;
        g.fill(x, y, x + l, y + 1, color);
        g.fill(x, y, x + 1, y + l, color);
        g.fill(x + w - l, y, x + w, y + 1, color);
        g.fill(x + w - 1, y, x + w, y + l, color);
        g.fill(x, y + h - 1, x + l, y + h, color);
        g.fill(x, y + h - l, x + 1, y + h, color);
        g.fill(x + w - l, y + h - 1, x + w, y + h, color);
        g.fill(x + w - 1, y + h - l, x + w, y + h, color);
    }
}
