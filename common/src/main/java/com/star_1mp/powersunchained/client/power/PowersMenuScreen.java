package com.star_1mp.powersunchained.client.power;

import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

/**
 * Placeholder of the powers menu (abilities + progression): the frame in the style of the Suit Creator / Tech Table and a
 * "coming soon" note. The real menu comes after the owner picks a concept ({@code docs/concepts/powers_menu}).
 */
public class PowersMenuScreen extends Screen {

    private static final int W = 416;
    private static final int H = 236;

    private final @Nullable Screen parent;

    public PowersMenuScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.powers_unchained.powers_menu"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (PUKeyMappings.POWERS_MENU.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        int x = (this.width - W) / 2;
        int y = (this.height - H) / 2;
        panel(g, x, y, W, H);
        g.text(this.font, this.title, x + 8, y + 7, 0xFF404040, false);
        int mx = x + 8;
        int my = y + 21;
        int mw = W - 16;
        int mh = H - 29;
        inset(g, mx, my, mw, mh, 0xFF12161B);
        for (int yy = my + 2; yy < my + mh - 1; yy += 2) {
            g.fill(mx + 1, yy, mx + mw - 1, yy + 1, 0x163C5A6E);
        }
        g.fill(mx + 1, my + 1, mx + mw - 1, my + 2, 0x28FF9A52);
        int ty = my + mh / 2 - 14;
        g.centeredText(this.font, Component.translatable("screen.powers_unchained.powers_menu.soon"), x + W / 2, ty, 0xFFFF9A52);
        ty += 14;
        for (FormattedCharSequence line : this.font.split(Component.translatable("screen.powers_unchained.powers_menu.soon_desc"), mw - 60)) {
            g.text(this.font, line, x + W / 2 - this.font.width(line) / 2, ty, 0xFF9AA3AD, false);
            ty += 10;
        }
    }

    private static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFFC6C6C6);
        g.fill(x + 2, y, x + w - 2, y + 1, 0xFF000000);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, 0xFF000000);
        g.fill(x, y + 2, x + 1, y + h - 2, 0xFF000000);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, 0xFF000000);
        g.fill(x + 1, y + 1, x + 2, y + 2, 0xFF000000);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, 0xFF000000);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, 0xFF000000);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, 0xFF000000);
        g.fill(x + 2, y + 1, x + w - 3, y + 3, 0xFFFFFFFF);
        g.fill(x + 1, y + 2, x + 3, y + h - 3, 0xFFFFFFFF);
        g.fill(x + 3, y + h - 3, x + w - 2, y + h - 1, 0xFF555555);
        g.fill(x + w - 3, y + 3, x + w - 1, y + h - 2, 0xFF555555);
    }

    private static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + h, color);
        g.fill(x, y, x + w - 1, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + h - 1, 0xFF373737);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
    }
}
