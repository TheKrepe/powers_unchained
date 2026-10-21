package com.thekrepe.powersunchained.client.web;

import com.thekrepe.powersunchained.web.WebType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;

/**
 * Web-shooter HUD, after Fisk's utility belt HUD ({@code HudElementUtilityBelt}): while the ability key is held a
 * ring of web types opens below the crosshair (the selected one raised and highlighted, with its shots left and
 * recharge bar, smooth rotation when scrolling); while web shooting is on, the selected web with its shots and
 * recharge bar sits under the crosshair. The ability keys and controls are listed by
 * {@link com.thekrepe.powersunchained.client.ability.AbilityHud}.
 */
public final class WebShooterHud {

    /** Colour of the highlighted wheel segment. */
    private static final int WHEEL_COLOR = 0x4F7FC8;

    private static float scroll;
    private static float prevScroll;
    private static final float[] HIGHLIGHT = new float[WebType.values().length];
    private static final float[] PREV_HIGHLIGHT = new float[WebType.values().length];

    private WebShooterHud() {
    }

    /** Client tick (called by {@link WebShooterClient#tick()}). */
    static void tick() {
        prevScroll = scroll;
        System.arraycopy(HIGHLIGHT, 0, PREV_HIGHLIGHT, 0, HIGHLIGHT.length);
        int size = WebType.values().length;
        float angleIncr = 360.0F / size;
        int slot = WebShooterClient.selected().ordinal();
        float target = slot * angleIncr;
        int dir = WebShooterClient.scrollDirection();
        if (size > 1) {
            // wrap around instead of spinning back through every entry
            if (slot == 0 && dir > 0 && Math.abs(scroll - (size - 1) * angleIncr) < angleIncr) {
                scroll -= 360.0F;
                prevScroll = scroll;
            } else if (slot == size - 1 && scroll < angleIncr && dir < 0) {
                scroll += 360.0F;
                prevScroll = scroll;
            }
        }
        scroll -= (scroll - target) / 4.0F;
        for (int i = 0; i < size; i++) {
            float goal = WebShooterClient.isActive() && i == slot ? 1.0F : 0.0F;
            HIGHLIGHT[i] -= (HIGHLIGHT[i] - goal) / 4.0F;
        }
    }

    private static int argb(int rgb, float alpha) {
        return Math.round(Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) << 24 | rgb & 0xFFFFFF;
    }

    private static int fade(int from, int to, float f) {
        int r = Math.round(Mth.lerp(f, from >> 16 & 255, to >> 16 & 255));
        int g = Math.round(Mth.lerp(f, from >> 8 & 255, to >> 8 & 255));
        int b = Math.round(Mth.lerp(f, from & 255, to & 255));
        return r << 16 | g << 8 | b;
    }

    private static void icon(GuiGraphicsExtractor graphics, WebType type, int x, int y, int size, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, type.icon(), x, y, 0.0F, 0.0F, size, size, 32, 32, 32, 32, color);
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float pt = delta.getGameTimeDeltaPartialTick(false);
        Font font = mc.font;

        float timer = WebShooterClient.wheel(pt);
        if (!WebShooterClient.isActive() && timer <= 0.0F) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        WebType[] types = WebType.values();

        if (timer > 0.0F) {
            float angleIncr = 360.0F / types.length;
            float offset = 90.0F - angleIncr / 2.0F - Mth.lerp(pt, prevScroll, scroll);
            float radius = 80.0F * timer;
            int cx = width / 2;
            int cy = height / 2 + 30;
            for (WebType type : types) {
                int i = type.ordinal();
                float hl = Mth.lerp(pt, PREV_HIGHLIGHT[i], HIGHLIGHT[i]);
                float scale = (0.75F + hl * 0.25F) * timer;
                int opacity = Math.round(255.0F * hl * timer);
                float angle = (float) Math.toRadians(offset + (int) (angleIncr / 2.0F) + i * angleIncr);
                float x = cx + Mth.cos(angle) * radius * 0.75F;
                float y = cy + Mth.sin(angle) * radius * 0.75F * 0.35F;

                graphics.pose().pushMatrix();
                graphics.pose().translate(x, y - hl * 4.0F);
                graphics.fill(-20, -16, 20, 14, argb(fade(0, WHEEL_COLOR, hl), (0.15F + hl * 0.4F) * timer));
                if (opacity > 3) {
                    graphics.centeredText(font, type.displayName(), 0, 16, argb(0xFFFFFF, opacity / 255.0F));
                }
                graphics.pose().pushMatrix();
                graphics.pose().scale(scale);
                graphics.pose().pushMatrix();
                graphics.pose().scale(2.0F);
                graphics.pose().translate(0.0F, -hl * 5.0F);
                icon(graphics, type, -9, -11, 18, argb(0xFFFFFF, 0.3F + hl * 0.7F));
                graphics.pose().popMatrix();
                graphics.pose().translate(0.0F, 4.0F - hl * 8.0F);
                graphics.fill(-18, 8, 18, 10, argb(0, hl * timer));
                float c = 1.0F - recharge(type, pt);
                if (c > 0.0F) {
                    graphics.fill(-18, 8, -18 + (int) (c * 36.0F), 10, argb(0xFFFFFF, hl * timer));
                }
                if (opacity > 3) {
                    int uses = WebShooterClient.uses(type);
                    String s = String.valueOf(uses);
                    graphics.text(font, s, 18 - font.width(s), -2, argb(uses > 0 ? 0xFFFFFF : 0xFF5555, opacity / 255.0F));
                }
                graphics.pose().popMatrix();
                graphics.pose().popMatrix();
            }
        }

        // the selected web under the crosshair while web shooting is on
        float alpha = WebShooterClient.isActive() ? 1.0F - timer : 0.0F;
        if (alpha > 0.0F) {
            WebType type = WebShooterClient.selected();
            int x = width / 2;
            int y = height / 2 + 18;
            icon(graphics, type, x - 6, y - 6, 12, argb(0xFFFFFF, alpha));
            int uses = WebShooterClient.uses(type);
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + 7, y + 1);
            graphics.pose().scale(0.75F);
            graphics.text(font, String.valueOf(uses), 0, 0, argb(uses > 0 ? 0xFFFFFF : 0xFF5555, alpha));
            graphics.pose().popMatrix();
            int bx = x - 6;
            int by = height / 2 + 10;
            graphics.fill(bx, by, bx + 12, by + 1, argb(0, alpha * 0.6F));
            float c = 1.0F - recharge(type, pt);
            if (c > 0.0F) {
                graphics.fill(bx, by, bx + (int) (c * 12.0F), by + 1, argb(0xFFFFFF, alpha));
            }
        }
    }

    /** 0..1 of the shot that is recharging (0 when all shots are loaded). */
    private static float recharge(WebType type, float pt) {
        return WebShooterClient.uses(type) < type.maxUses() ? WebShooterClient.recharge(type, pt) : 0.0F;
    }
}
