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

    private void drawSlots(GuiGraphicsExtractor g, int px, int py) {
        monitor(g, px, py, RP_W, CH_H);
        int sx = this.leftPos + IrradiatorMenu.SLOT_X;
        label(g, tr("slot.fuel"), px, this.topPos + IrradiatorMenu.FUEL_Y - 10);
        slot(g, sx, this.topPos + IrradiatorMenu.FUEL_Y);
        label(g, tr("slot.sample"), px, this.topPos + IrradiatorMenu.INPUT_Y - 10);
        slot(g, sx, this.topPos + IrradiatorMenu.INPUT_Y);
        label(g, tr("slot.result"), px, this.topPos + IrradiatorMenu.OUTPUT_Y - 10);
        slot(g, sx, this.topPos + IrradiatorMenu.OUTPUT_Y);
        if (!this.hasFuel() && this.menu.slotItem(IrradiatorBlockEntity.SLOT_FUEL).isEmpty()) {
            // ghost capsule in the empty fuel slot
            g.item(new ItemStack(PUItems.ISOTOPE_CAPSULE.get()), sx, this.topPos + IrradiatorMenu.FUEL_Y);
            g.fill(sx, this.topPos + IrradiatorMenu.FUEL_Y, sx + 16, this.topPos + IrradiatorMenu.FUEL_Y + 16, 0xA00A140D);
        }
    }

    private void label(GuiGraphicsExtractor g, Component text, int px, int y) {
        g.centeredText(this.font, text, px + RP_W / 2, y, GREEN_DIM);
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
