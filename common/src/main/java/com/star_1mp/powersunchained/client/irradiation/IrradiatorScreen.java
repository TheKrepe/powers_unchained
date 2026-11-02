package com.star_1mp.powersunchained.client.irradiation;

import com.mojang.blaze3d.platform.InputConstants;
import com.star_1mp.powersunchained.block.entity.IrradiatorBlockEntity;
import com.star_1mp.powersunchained.client.gui.GuiFx;
import com.star_1mp.powersunchained.irradiation.Irradiation;
import com.star_1mp.powersunchained.irradiation.IrradiationRecipes;
import com.star_1mp.powersunchained.menu.IrradiatorMenu;
import com.star_1mp.powersunchained.registry.PUItems;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Irradiator screen (owner: "технологичный-зелёный"): our machine panel with dark green monitors. Left: the irradiation
 * chamber — a glass cylinder with the glowing core and whatever is being irradiated (the creature standing on the block,
 * live, or the sample item); middle: the dose gauge (needle dial + bar); right: capsule / sample / result slots;
 * under the chamber the IRRADIATE button (owner: no time left, no chance text).
 */
public class IrradiatorScreen extends AbstractContainerScreen<IrradiatorMenu> {

    private static final int W = 260;
    private static final int H = 236;
    private static final int CH_X = 8;
    private static final int CH_Y = 20;
    private static final int CH_W = 110;
    private static final int CH_H = 110;
    private static final int GA_X = 124;
    private static final int GA_W = 48;
    private static final int RP_X = 178;
    private static final int RP_W = 74;
    private static final int BTN_X = 8;
    private static final int BTN_Y = 134;
    private static final int BTN_W = 110;
    private static final int BTN_H = 15;

    private static final int LABEL = 0xFF404040;
    private static final int GREEN = 0xFF58F05A;
    private static final int GREEN_DIM = 0xFF2C8A3A;
    private static final int GREEN_DARK = 0xFF16391E;
    private static final int GLASS = 0xFF0A140D;
    private static final int GREY = 0xFF9AA3AD;
    private static final int AMBER = 0xFFFFB13B;
    private static final int RED = 0xFFFF5A4A;

    private float time;

    public IrradiatorScreen(IrradiatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, W, H);
    }

    private static Component tr(String key, Object... args) {
        return Component.translatable("screen.powers_unchained.irradiator." + key, args);
    }

    private @Nullable LivingEntity target() {
        int id = this.menu.targetId();
        if (id < 0 || this.minecraft.level == null) {
            return null;
        }
        Entity e = this.minecraft.level.getEntity(id);
        return e instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private boolean hasFuel() {
        return this.menu.slotItem(IrradiatorBlockEntity.SLOT_FUEL).is(PUItems.ISOTOPE_CAPSULE.get());
    }

    private boolean itemReady() {
        return IrradiationRecipes.find(this.menu.slotItem(IrradiatorBlockEntity.SLOT_INPUT)) != null;
    }

    private boolean canStart() {
        return !this.menu.running() && this.hasFuel() && (this.target() != null || this.itemReady());
    }

    // =====================================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        this.time = (this.minecraft.player != null ? this.minecraft.player.tickCount : 0) + partialTick;
        int x = this.leftPos;
        int y = this.topPos;
        panel(g, x, y, W, H);
        g.text(this.font, this.title, x + 8, y + 7, LABEL, false);
        hazard(g, x + 124, y + 7, W - 132, 6);
        this.drawChamber(g, x + CH_X, y + CH_Y);
        this.drawGauge(g, x + GA_X, y + CH_Y);
        this.drawSlots(g, x + RP_X, y + CH_Y);
        this.drawButton(g, x, y, mouseX, mouseY);
        // inventory slot frames
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slot(g, x + IrradiatorMenu.INVENTORY_X + col * 18, y + IrradiatorMenu.INVENTORY_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            slot(g, x + IrradiatorMenu.INVENTORY_X + col * 18, y + IrradiatorMenu.INVENTORY_Y + 58);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        // drawn in the background
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        if (in(mouseX, mouseY, x + BTN_X, y + BTN_Y, BTN_W, BTN_H) && !this.canStart() && !this.menu.running()) {
            g.setTooltipForNextFrame(this.font, this.status(), mouseX, mouseY);
        }
    }

    /** The glass cylinder with the core and the creature / item. */
    private void drawChamber(GuiGraphicsExtractor g, int cx, int cy) {
        monitor(g, cx, cy, CH_W, CH_H);
        boolean running = this.menu.running();
        int mid = cx + CH_W / 2;
        int top = cy + 10;
        int bottom = cy + CH_H - 18;
        // core glow behind the glass, brighter while it works
        float pulse = 0.6F + 0.4F * Mth.sin(this.time * (running ? 0.35F : 0.08F));
        int glow = (int) ((running ? 0x70 : 0x30) * pulse);
        g.fillGradient(mid - 30, top, mid + 30, bottom, glow << 24 | 0x58F05A, (glow / 3) << 24 | 0x58F05A);
        // the cylinder: walls, rims, a highlight
        g.fill(mid - 34, top, mid - 32, bottom, 0x9058F05A);
        g.fill(mid + 32, top, mid + 34, bottom, 0x9058F05A);
        g.fill(mid - 30, top + 3, mid - 29, bottom - 3, 0x50FFFFFF);
        ellipse(g, mid, top, 34, 4, 0xB058F05A);
        ellipse(g, mid, bottom, 34, 4, 0xB058F05A);
        // base plate
        g.fill(mid - 36, bottom + 3, mid + 37, bottom + 7, 0xFF3C4148);
        g.fill(mid - 36, bottom + 3, mid + 37, bottom + 4, 0xFF5A616A);
        LivingEntity target = this.target();
        if (target != null) {
            float size = Math.max(target.getBbHeight(), target.getBbWidth() * 1.3F);
            int scale = (int) Mth.clamp(56.0F / size, 10.0F, 60.0F);
            float yaw = running ? 0.0F : this.time * 1.5F;
            GuiFx.entity(g, mid - 32, top - 2, mid + 32, bottom + 3, scale, yaw, target);
        } else {
            ItemStack sample = this.menu.slotItem(IrradiatorBlockEntity.SLOT_INPUT);
            if (!sample.isEmpty()) {
                g.pose().pushMatrix();
                g.pose().translate(mid - 16, (top + bottom) / 2.0F - 16);
                g.pose().scale(2.0F, 2.0F);
                g.item(sample, 0, 0);
                g.pose().popMatrix();
            }
        }
        // status line
        Component status = this.status();
        int color = running ? GREEN : this.canStart() ? AMBER : GREY;
        g.centeredText(this.font, status, mid, cy + CH_H - 11, color);
    }

    private Component status() {
        if (this.menu.running()) {
            return tr("status.running");  // owner: no time left shown
        }
        if (!this.hasFuel()) {
            return tr("status.no_fuel");
        }
        if (this.target() == null && !this.itemReady()) {
            return tr("status.no_sample");
        }
        return tr("status.ready");
    }

    /** Needle dial + dose bar. */
    private void drawGauge(GuiGraphicsExtractor g, int gx, int gy) {
        monitor(g, gx, gy, GA_W, CH_H);
        float dose = this.menu.running() ? this.menu.progress() / (float) Irradiation.DURATION : 0.0F;
        int ox = gx + GA_W / 2;
        int oy = gy + 30;
        int r = 18;
        // the scale: green -> amber -> red
        for (int i = 0; i <= 36; i++) {
            float t = i / 36.0F;
            double a = Math.PI * (1.0 - t);
            int px = ox + (int) Math.round(Math.cos(a) * r);
            int py = oy - (int) Math.round(Math.sin(a) * r);
            int c = t < 0.6F ? GREEN : t < 0.85F ? AMBER : RED;
            g.fill(px, py, px + 1, py + 1, c);
            if (i % 6 == 0) {
                int qx = ox + (int) Math.round(Math.cos(a) * (r - 3));
                int qy = oy - (int) Math.round(Math.sin(a) * (r - 3));
                g.fill(qx, qy, qx + 1, qy + 1, c);
            }
        }
        // the needle (jitters a little while it works, like a real meter)
        float jitter = this.menu.running() ? 0.02F * Mth.sin(this.time * 1.9F) : 0.0F;
        double na = Math.PI * (1.0 - Mth.clamp(dose + jitter, 0.0F, 1.0F));
        for (int s = 0; s < r - 2; s++) {
            int px = ox + (int) Math.round(Math.cos(na) * s);
            int py = oy - (int) Math.round(Math.sin(na) * s);
            g.fill(px, py, px + 1, py + 1, 0xFFFFFFFF);
        }
        g.fill(ox - 1, oy - 1, ox + 2, oy + 2, 0xFFC8D0D8);
        String pct = Math.round(dose * 100.0F) + "%";
        g.centeredText(this.font, pct, ox, oy + 5, dose > 0.85F ? RED : GREEN);
        // vertical dose bar
        int bx = gx + GA_W / 2 - 5;
        int by = gy + 48;
        int bh = CH_H - 56;
        inset(g, bx, by, 10, bh, 0xFF06100A);
        int fill = Math.round((bh - 2) * dose);
        if (fill > 0) {
            g.fillGradient(bx + 1, by + bh - 1 - fill, bx + 9, by + bh - 1, 0xFFB8FF7A, 0xFF2C8A3A);
        }
        for (int t = 1; t < 5; t++) {
            int ty = by + bh * t / 5;
            g.fill(bx - 3, ty, bx, ty + 1, GREEN_DIM);
        }
    }

    /**
     * Owner's variant A, "fuel rod": the capsule sits in the neck of a tall flask whose green isotope drains as the dose
     * rises (bubbles, a rippling surface, a breathing glow); a dashed pipe carries pulses to the chamber while it works;
     * on the right two petri dishes: sample -> radiation sign -> result.
     */
    private void drawSlots(GuiGraphicsExtractor g, int px, int py) {
        monitor(g, px, py, RP_W, CH_H);
        int x = this.leftPos;
        int y = this.topPos;
        boolean running = this.menu.running();
        boolean fuel = this.hasFuel();

        // the flask (under the capsule's neck)
        int fx = x + IrradiatorMenu.FUEL_X;
        int fy = y + IrradiatorMenu.FUEL_Y + 18;
        int fw = 16;
        int fh = py + CH_H - 12 - fy;
        outline(g, fx - 1, fy - 1, fw + 2, fh + 2, 0xA058F05A);
        g.fill(fx, fy, fx + fw, fy + fh, 0xFF06100A);
        float left = !fuel ? 0.0F : running ? 1.0F - this.menu.progress() / (float) Irradiation.DURATION : 1.0F;
        int level = Math.round((fh - 2) * Mth.clamp(left, 0.0F, 1.0F));
        if (level > 0) {
            int surface = fy + fh - level;
            float breathe = 0.75F + 0.25F * Mth.sin(this.time * (running ? 0.3F : 0.08F));
            for (int yy = surface; yy < fy + fh; yy++) {
                float depth = (yy - surface) / (float) Math.max(1, level);
                int a = (int) ((150 + 80 * depth) * breathe);
                int c = depth < 0.15F ? 0xC8FFB0 : 0x58F05A;
                g.fill(fx, yy, fx + fw, yy + 1, Mth.clamp(a, 0, 255) << 24 | c);
            }
            // a rippling surface
            for (int i = 0; i < fw; i++) {
                int wave = Math.round(Mth.sin(this.time * 0.25F + i * 0.8F) * (running ? 1.0F : 0.5F));
                if (wave < 0) {
                    g.fill(fx + i, surface - 1, fx + i + 1, surface, 0xA0C8FFB0);
                }
            }
            // bubbles rising through the isotope
            int bubbles = running ? 5 : 2;
            for (int b = 0; b < bubbles; b++) {
                float speed = running ? 0.6F + b * 0.13F : 0.15F + b * 0.05F;
                int travel = Math.max(1, level - 2);
                int by = fy + fh - 2 - (int) ((this.time * speed + b * 17) % travel);
                int bx = fx + 2 + (b * 5 + (int) (Mth.sin(this.time * 0.2F + b) * 1.5F) + 13) % (fw - 4);
                g.fill(bx, by, bx + 1, by + 1, 0xFFE8FFE0);
            }
        }
        // glass highlight + marks
        g.fill(fx + 2, fy + 1, fx + 3, fy + fh - 1, 0x40FFFFFF);
        for (int t = 1; t < 4; t++) {
            int ty = fy + fh * t / 4;
            g.fill(fx + fw - 3, ty, fx + fw, ty + 1, 0x8058F05A);
        }
        // the capsule's slot = the flask's neck
        slot(g, fx, y + IrradiatorMenu.FUEL_Y);
        if (this.menu.slotItem(IrradiatorBlockEntity.SLOT_FUEL).isEmpty()) {
            g.item(new ItemStack(PUItems.ISOTOPE_CAPSULE.get()), fx, y + IrradiatorMenu.FUEL_Y);
            g.fill(fx, y + IrradiatorMenu.FUEL_Y, fx + 16, y + IrradiatorMenu.FUEL_Y + 16, 0xA00A140D);
        }

        // dashed pipe to the chamber, pulses run along it while it works
        int pipeY = py - 4 + 2;
        int from = x + CH_X + CH_W;
        int to = fx + fw / 2;
        for (int xx = from; xx < to; xx++) {
            int phase = running ? (int) (this.time * 1.5F) : 0;
            boolean dash = Math.floorMod(xx + phase, 6) < 4;
            boolean pulse = running && Math.floorMod(xx + (int) (this.time * 3.0F), 24) < 3;
            if (dash) {
                g.fill(xx, pipeY, xx + 1, pipeY + 1, pulse ? 0xFFC8FFB0 : running ? GREEN : GREEN_DIM);
            }
        }
        g.fill(to, pipeY, to + 1, y + IrradiatorMenu.FUEL_Y - 1, running ? GREEN : GREEN_DIM);

        // petri dishes: sample -> radiation sign -> result
        this.dish(g, x + IrradiatorMenu.INPUT_X, y + IrradiatorMenu.INPUT_Y, tr("slot.sample"));
        this.dish(g, x + IrradiatorMenu.OUTPUT_X, y + IrradiatorMenu.OUTPUT_Y, tr("slot.result"));
        int cx = x + IrradiatorMenu.INPUT_X + 8;
        int top = y + IrradiatorMenu.INPUT_Y + 22;
        int bottom = y + IrradiatorMenu.OUTPUT_Y - 12;
        g.fill(cx, top, cx + 1, top + 4, AMBER);
        g.fill(cx, bottom - 4, cx + 1, bottom, AMBER);
        float spin = running ? this.time * 0.08F : 0.0F;
        trefoil(g, cx, (top + bottom) / 2, spin, running ? AMBER : 0xFF8A6A2A);
    }

    private void dish(GuiGraphicsExtractor g, int sx, int sy, Component label) {
        g.centeredText(this.font, label, sx + 8, sy - 10, GREEN_DIM);
        slot(g, sx, sy);
        ellipse(g, sx + 8, sy + 19, 13, 3, 0xFF9FD8B0);
        ellipse(g, sx + 8, sy + 19, 11, 2, 0xFF4E7A5A);
    }

    /** A small radiation sign (three blades round a dot), turning while it works. */
    private static void trefoil(GuiGraphicsExtractor g, int cx, int cy, float angle, int color) {
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
        for (int blade = 0; blade < 3; blade++) {
            double base = angle + blade * Math.PI * 2 / 3 - Math.PI / 2;
            for (double r = 2.5; r <= 5.5; r += 0.5) {
                for (double s = -0.5; s <= 0.5; s += 0.12) {
                    int px = cx + (int) Math.round(Math.cos(base + s) * r);
                    int py = cy + (int) Math.round(Math.sin(base + s) * r);
                    g.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void drawButton(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        boolean active = this.canStart();
        boolean hover = active && in(mx, my, x + BTN_X, y + BTN_Y, BTN_W, BTN_H);
        Identifier id = Identifier.withDefaultNamespace(!active ? "widget/button_disabled" : hover ? "widget/button_highlighted" : "widget/button");
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, x + BTN_X, y + BTN_Y, BTN_W, BTN_H);
        Component text = this.menu.running() ? tr("button.running") : tr("button.start");
        g.centeredText(this.font, text, x + BTN_X + BTN_W / 2, y + BTN_Y + 4, active ? 0xFFB8FF7A : 0xFFA0A0A0);
        // owner: no chance text ("Мутация: 70%") on the screen
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && this.canStart()
                && in(event.x(), event.y(), this.leftPos + BTN_X, this.topPos + BTN_Y, BTN_W, BTN_H)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, IrradiatorMenu.BUTTON_START);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    /** Dev/CI: the button. */
    public void debugStart() {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, IrradiatorMenu.BUTTON_START);
    }

    // =====================================================================================================
    // drawing helpers
    // =====================================================================================================

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void ellipse(GuiGraphicsExtractor g, int cx, int cy, int rx, int ry, int color) {
        for (int i = 0; i < 48; i++) {
            double a = Math.PI * 2 * i / 48.0;
            int px = cx + (int) Math.round(Math.cos(a) * rx);
            int py = cy + (int) Math.round(Math.sin(a) * ry);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    /** Yellow / black hazard stripes. */
    private static void hazard(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        for (int i = -h; i < w; i += 6) {
            for (int r = 0; r < h; r++) {
                int sx = x + i + r;
                int x0 = Math.max(x, sx);
                int x1 = Math.min(x + w, sx + 3);
                if (x1 > x0) {
                    g.fill(x0, y + h - 1 - r, x1, y + h - r, 0xFFE8C21A);
                }
            }
        }
    }

    private static void slot(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y, 0xFF373737);
        g.fill(x - 1, y - 1, x, y + 17, 0xFF373737);
        g.fill(x, y + 16, x + 17, y + 17, 0xFFFFFFFF);
        g.fill(x + 16, y, x + 17, y + 17, 0xFFFFFFFF);
        g.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
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

    /** Dark green glass monitor with scan lines and a green top edge. */
    private static void monitor(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        inset(g, x, y, w, h, GLASS);
        GuiFx.scanlines(g, x, y, w, h);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x4058F05A);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, GREEN_DARK);
    }
}
