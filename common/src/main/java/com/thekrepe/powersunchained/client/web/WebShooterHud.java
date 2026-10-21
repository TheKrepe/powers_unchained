package com.thekrepe.powersunchained.client.web;

import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import com.thekrepe.powersunchained.web.WebType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Web-shooter HUD, after Fisk's utility belt HUD ({@code HudElementUtilityBelt}): while the ability key is held a
 * ring of web types opens below the crosshair (the selected one raised and highlighted, with its shots left and
 * recharge bar, smooth rotation when scrolling); while web shooting is on, the selected web with its shots and
 * recharge bar sits under the crosshair. After putting the web-shooters on, the controls are listed at the top left.
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
        renderHints(graphics, font);

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
            int y = height / 2 + 25;
            icon(graphics, type, x - 9, y - 9, 18, argb(0xFFFFFF, alpha));
            int uses = WebShooterClient.uses(type);
            String s = String.valueOf(uses);
            graphics.text(font, s, x + 10 - font.width(s), y + 1, argb(uses > 0 ? 0xFFFFFF : 0xFF5555, alpha));
            int bx = width / 2 - 9;
            int by = height / 2 + 14;
            graphics.fill(bx, by, bx + 18, by + 2, argb(0, alpha));
            graphics.fill(bx, by, bx + 17, by + 1, argb(0x3F3F3F, alpha));
            float c = 1.0F - recharge(type, pt);
            if (c > 0.0F) {
                graphics.fill(bx, by, bx + (int) (c * 17.0F), by + 1, argb(0xFFFFFF, alpha));
            }
        }
    }

    /** 0..1 of the shot that is recharging (0 when all shots are loaded). */
    private static float recharge(WebType type, float pt) {
        return WebShooterClient.uses(type) < type.maxUses() ? WebShooterClient.recharge(type, pt) : 0.0F;
    }

    /** Controls of the web-shooters, for a few seconds after putting them on. */
    private static void renderHints(GuiGraphicsExtractor graphics, Font font) {
        int timer = WebShooterClient.hintTimer();
        if (timer <= 0) {
            return;
        }
        float alpha = Math.min(timer / 20.0F, 1.0F);
        Component key = PUKeyMappings.ABILITY_1.getTranslatedKeyMessage();
        Component[][] lines = {
            {Component.empty().append(key).append(" + ").append(Component.translatable("hint.powers_unchained.scroll")),
                Component.translatable("hint.powers_unchained.web_shooters.toggle")},
            {Component.translatable("hint.powers_unchained.use"),
                Component.translatable("hint.powers_unchained.web_shooters.shoot")},
        };
        int y = 4;
        for (Component[] line : lines) {
            Component text = Component.empty()
                    .append(line[0].copy().withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(line[1]);
            int w = font.width(text);
            graphics.fill(2, y - 2, 2 + w + 6, y + 10, argb(0x3A4E6E, 0.55F * alpha));
            graphics.text(font, text, 5, y, argb(0xFFFFFF, alpha), true);
            y += 14;
        }
    }
}
