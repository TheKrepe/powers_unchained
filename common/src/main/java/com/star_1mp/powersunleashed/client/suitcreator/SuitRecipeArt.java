package com.star_1mp.powersunleashed.client.suitcreator;

import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.suit.SuitCatalog;
import com.star_1mp.powersunleashed.suit.SuitRecipe;
import com.star_1mp.powersunleashed.suit.Suits;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * How a Suit Creator blueprint looks in the recipe viewers (JEI / REI share it): the Suit Creator's dark monitors —
 * a title bar with the suit's name, the materials on a monitor, a needle sewing along a thread, the result in a small
 * hologram (one piece, or the four pieces of the whole suit), and how the suit is unlocked. The viewers put their item
 * slots at {@link #inputSlot} / {@link #outputSlot} (item positions, 16x16).
 */
public final class SuitRecipeArt {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 84;

    private static final int AMBER = 0xFFFFB13B;
    private static final int CYAN = 0xFF59E4FF;
    private static final int CYAN_DIM = 0xFF2F8FA8;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFF9AA3AD;
    private static final int GREEN = 0xFF7CFC6A;
    private static final int GLASS = 0xFF12161B;

    /** The lock / unlock info box (tooltip area of the viewers). */
    public static final int[] UNLOCK_BOX = {0, 60, 106, 24};

    private SuitRecipeArt() {
    }

    /** Item position of material {@code i} (4 x 2 grid). */
    public static int[] inputSlot(int i) {
        return new int[] {4 + (i % 4) * 18, 21 + (i / 4) * 18};
    }

    /** Item position of output {@code i} (one big centred slot, or 2 x 2 for the whole suit). */
    public static int[] outputSlot(SuitRecipe recipe, int i) {
        if (!recipe.wholeSuit()) {
            return new int[] {134, 42};
        }
        return new int[] {125 + (i % 2) * 18, 33 + (i / 2) * 18};
    }

    public static Component title(SuitRecipe recipe) {
        return Component.translatable("screen.powers_unleashed.suit_creator.full_name",
                Component.translatable("suit_group.powers_unleashed." + SuitCatalog.info(recipe.suit()).group()),
                Component.translatable(Suits.variantNameKey(recipe.suit())));
    }

    public static Component pieceName(SuitRecipe recipe) {
        return Component.translatable(recipe.wholeSuit() ? "screen.powers_unleashed.suit_creator.all"
                : "screen.powers_unleashed.suit_creator.part." + recipe.piece());
    }

    private static boolean unlocked(SuitRecipe recipe) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && Services.GAMEPLAY.suitProgress(mc.player).isUnlocked(recipe.suit());
    }

    /** Tooltip of the unlock box. */
    public static List<Component> unlockTooltip(SuitRecipe recipe) {
        List<Component> lines = new ArrayList<>();
        if (unlocked(recipe)) {
            lines.add(Component.translatable("recipe.powers_unleashed.suit_creator.unlocked"));
        } else {
            lines.add(Component.translatable("recipe.powers_unleashed.suit_creator.locked"));
            lines.add(Component.translatable("recipe.powers_unleashed.suit_creator.unlock_hint",
                    SuitCatalog.info(recipe.suit()).xpCost()));
        }
        return lines;
    }

    public static boolean inUnlockBox(double mouseX, double mouseY) {
        return mouseX >= UNLOCK_BOX[0] && mouseX < UNLOCK_BOX[0] + UNLOCK_BOX[2]
                && mouseY >= UNLOCK_BOX[1] && mouseY < UNLOCK_BOX[1] + UNLOCK_BOX[3];
    }

    /** Draws the whole recipe background, its top-left corner at {@code x, y}. */
    public static void draw(GuiGraphicsExtractor g, SuitRecipe recipe, int x, int y) {
        Font font = Minecraft.getInstance().font;
        float time = (System.currentTimeMillis() % 100000L) / 1000.0F;

        // title bar
        monitor(g, x, y, WIDTH, 15);
        g.text(font, Component.literal(trim(font, title(recipe).getString(), WIDTH - 10)), x + 5, y + 4, WHITE, true);

        // materials
        monitor(g, x, y + 17, 78, 42);
        for (int i = 0; i < Math.min(8, recipe.inputs().size()); i++) {
            int[] s = inputSlot(i);
            slot(g, x + s[0] - 1, y + s[1] - 1);
        }

        // thread + needle sewing towards the hologram
        int tx0 = x + 81;
        int tx1 = x + 106;
        int ty = y + 38;
        for (int px = tx0; px < tx1; px += 2) {
            g.fill(px, ty, px + 1, ty + 1, 0xFF6B7682);
        }
        float p = (time % 2.0F) / 2.0F;
        int done = Math.round((tx1 - tx0) * p);
        g.fill(tx0, ty, tx0 + done, ty + 1, CYAN);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("needle"), tx0 + done - 5, ty - 5, 10, 10);

        // hologram with the result
        int hx = x + 108;
        int hy = y + 17;
        int hw = 68;
        int hh = 67;
        monitor(g, hx, hy, hw, hh);
        int floor = hy + hh - 12;
        for (int i = 0; i < 4; i++) {
            int yy = floor + i * i + i;
            if (yy < hy + hh - 1) {
                g.fill(hx + 2, yy, hx + hw - 2, yy + 1, 0x2259E4FF + (i << 25));
            }
        }
        // beam
        int cx = hx + hw / 2;
        for (int yy = hy + 12; yy < floor; yy++) {
            float k = (yy - hy - 12) / (float) Math.max(1, floor - hy - 12);
            int half = Math.round(12 + 14 * k);
            g.fill(cx - half, yy, cx + half, yy + 1, ((int) (8 + 22 * k) << 24) | 0x59E4FF);
        }
        // pulsing pedestal ring
        int pulse = Math.round(160 + 60 * Mth.sin(time * 3.0F));
        g.fill(cx - 20, floor + 1, cx + 20, floor + 2, (pulse << 24) | 0x59E4FF);
        g.fill(cx - 16, floor + 2, cx + 16, floor + 3, ((pulse / 2) << 24) | 0x59E4FF);
        brackets(g, hx + 2, hy + 2, hw - 4, hh - 4, CYAN);
        Component piece = pieceName(recipe);
        g.text(font, Component.literal(trim(font, piece.getString(), hw - 10)), hx + 6, hy + 5, CYAN_DIM, false);
        for (int i = 0; i < recipe.outputs().size(); i++) {
            int[] s = outputSlot(recipe, i);
            if (recipe.wholeSuit()) {
                slot(g, x + s[0] - 1, y + s[1] - 1);
            } else {
                // a big framed slot
                inset(g, x + s[0] - 5, y + s[1] - 5, 26, 26, 0xFF1A2027);
                outline(g, x + s[0] - 6, y + s[1] - 6, 28, 28, (pulse << 24) | 0x59E4FF);
            }
        }

        // how the suit is unlocked
        int ux = x + UNLOCK_BOX[0];
        int uy = y + UNLOCK_BOX[1];
        monitor(g, ux, uy, UNLOCK_BOX[2], UNLOCK_BOX[3]);
        if (unlocked(recipe)) {
            g.text(font, Component.translatable("screen.powers_unleashed.suit_creator.unlocked"), ux + 5, uy + 8, GREEN, true);
        } else {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("lock"), ux + 4, uy + 3, 8, 9);
            Component cost = Component.translatable("screen.powers_unleashed.suit_creator.unlock.cost",
                    SuitCatalog.info(recipe.suit()).xpCost());
            g.text(font, cost, ux + 15, uy + 4, AMBER, true);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("xp_orb"), ux + 17 + font.width(cost), uy + 3, 9, 9);
            g.text(font, Component.translatable("screen.powers_unleashed.suit_creator.unlock.or_scan"), ux + 5, uy + 14, GREY, false);
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

    private static Identifier sprite(String name) {
        return Constants.id("suit_creator/" + name);
    }

    private static void slot(GuiGraphicsExtractor g, int x, int y) {
        inset(g, x, y, 18, 18, 0xFF222932);
    }

    private static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + h, color);
        g.fill(x, y, x + w - 1, y + 1, 0xFF0A0D10);
        g.fill(x, y, x + 1, y + h - 1, 0xFF0A0D10);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFF3A4652);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFF3A4652);
    }

    private static void monitor(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, GLASS);
        g.fill(x, y, x + w - 1, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + h - 1, 0xFF373737);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
        com.star_1mp.powersunleashed.client.gui.GuiFx.scanlines(g, x, y, w, h);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x2859E4FF);
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private static void brackets(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        int l = 5;
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
