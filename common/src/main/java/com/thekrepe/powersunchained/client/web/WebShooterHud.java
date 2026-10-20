package com.thekrepe.powersunchained.client.web;

import com.thekrepe.powersunchained.web.WebType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

/**
 * HUD of the web-shooters: a small web icon next to the crosshair while web-shooting mode is on, and the web
 * selection menu (a "web lens" with the selected web in the middle and its neighbours at the sides) while the
 * ability key is held.
 */
public final class WebShooterHud {

    private static final int WHITE = 0xFFFFFFFF;

    private WebShooterHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!WebShooterClient.isActive() || mc.player == null) {
            return;
        }
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;
        WebType selected = WebShooterClient.selected();
        int uses = WebShooterClient.uses(selected);
        // bar: the shot that is recharging (empty -> full); full when all shots are loaded
        float cooldown = uses < selected.maxUses() ? WebShooterClient.recharge(selected, partial) : 0.0F;
        Font font = mc.font;

        if (!WebShooterClient.isMenuOpen()) {
            // compact indicator at the lower right of the crosshair
            icon(graphics, selected, cx + 7, cy + 5, 12, uses > 0 ? WHITE : 0x80FFFFFF);
            graphics.text(font, String.valueOf(uses), cx + 20, cy + 10, uses > 0 ? WHITE : 0xFFFF6060, true);
            return;
        }

        int y = cy + 34;  // centre of the lens, below the crosshair
        lens(graphics, cx, y);
        WebType previous = WebType.byId(selected.ordinal() - 1);
        WebType next = WebType.byId(selected.ordinal() + 1);
        if (previous != selected) {
            icon(graphics, previous, cx - 58, y - 9, 18, 0x70FFFFFF);
            graphics.text(font, "<", cx - 33, y - 4, 0x90FFFFFF, true);
        }
        if (next != selected) {
            icon(graphics, next, cx + 40, y - 9, 18, 0x70FFFFFF);
            graphics.text(font, ">", cx + 29, y - 4, 0x90FFFFFF, true);
        }
        icon(graphics, selected, cx - 16, y - 20, 32, uses > 0 ? WHITE : 0x90FFFFFF);
        graphics.text(font, String.valueOf(uses), cx + 13, y + 4, uses > 0 ? WHITE : 0xFFFF6060, true);
        bar(graphics, cx - 15, y + 14, 30, cooldown);
        Component name = selected.displayName();
        graphics.centeredText(font, name, cx, y + 20, WHITE);
    }

    private static void icon(GuiGraphicsExtractor graphics, WebType type, int x, int y, int size, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, type.icon(), x, y, 0.0F, 0.0F, size, size, 32, 32, 32, 32, color);
    }

    /** Thin bar: full = ready, otherwise the part that is still cooling down is dark. */
    private static void bar(GuiGraphicsExtractor graphics, int x, int y, int width, float cooldown) {
        int filled = Math.round(width * (1.0F - cooldown));
        graphics.fill(x, y, x + width, y + 2, 0xA0000000);
        graphics.fill(x, y, x + filled, y + 2, cooldown > 0 ? 0xFFB8C4CC : WHITE);
    }

    /** Lens-shaped translucent plate with web strands, drawn line by line. */
    private static void lens(GuiGraphicsExtractor graphics, int cx, int cy) {
        int halfHeight = 13;
        for (int dy = -halfHeight; dy <= halfHeight; dy++) {
            double t = (double) dy / halfHeight;
            int half = (int) Math.round(76 * Math.sqrt(1 - t * t));
            int alpha = 0x70 - Math.abs(dy) * 3;
            graphics.fill(cx - half, cy + dy, cx + half, cy + dy + 1, (alpha << 24) | 0x0B1A22);
            if (Math.abs(dy) == halfHeight - 1) {
                graphics.fill(cx - half, cy + dy, cx + half, cy + dy + 1, 0x80DDEEF4);
            }
        }
        // strands from the centre to the rim
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 6 + i * Math.PI / 3;
            for (int r = 22; r < 74; r += 2) {
                int x = cx + (int) Math.round(Math.cos(angle) * r);
                int y = cy + (int) Math.round(Math.sin(angle) * r * 0.17);
                graphics.fill(x, y, x + 1, y + 1, 0x50FFFFFF);
            }
        }
    }
}
