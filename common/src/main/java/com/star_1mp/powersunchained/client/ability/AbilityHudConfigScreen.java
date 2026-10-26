package com.star_1mp.powersunchained.client.ability;

import com.mojang.blaze3d.platform.InputConstants;
import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * HUD settings (key "HUD Settings" in the Powers Unchained controls): drag the ability HUD around with the mouse,
 * turn it and the ability names on / off, set the text opacity and the size, reset everything to the defaults.
 * The game stays visible behind it, the HUD is previewed at its place (with sample abilities if you have none).
 */
public class AbilityHudConfigScreen extends Screen {

    private static final int OUTLINE = 0xC0FFD24A;

    private final AbilityHudConfig config = AbilityHudConfig.get();
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public AbilityHudConfigScreen() {
        super(Component.translatable("screen.powers_unchained.hud_settings"));
    }

    @Override
    protected void init() {
        int w = 150;
        int gap = 4;
        int left = this.width / 2 - w - gap / 2;
        int right = this.width / 2 + gap / 2;
        int y = this.height - 4 * 24 - 8;

        this.addRenderableWidget(CycleButton.onOffBuilder(this.config.enabled)
                .create(left, y, w, 20, Component.translatable("screen.powers_unchained.hud_settings.enabled"),
                        (button, value) -> this.config.enabled = value));
        this.addRenderableWidget(CycleButton.onOffBuilder(this.config.showNames)
                .create(right, y, w, 20, Component.translatable("screen.powers_unchained.hud_settings.names"),
                        (button, value) -> this.config.showNames = value));
        y += 24;
        this.addRenderableWidget(new Slider(left, y, w, "screen.powers_unchained.hud_settings.opacity",
                (this.config.textOpacity - AbilityHudConfig.MIN_OPACITY) / (1.0F - AbilityHudConfig.MIN_OPACITY)) {
            @Override
            protected float actual() {
                return Mth.lerp((float) this.value, AbilityHudConfig.MIN_OPACITY, 1.0F);
            }

            @Override
            protected void applyValue() {
                AbilityHudConfigScreen.this.config.textOpacity = this.actual();
            }
        });
        this.addRenderableWidget(new Slider(right, y, w, "screen.powers_unchained.hud_settings.size",
                (this.config.scale - AbilityHudConfig.MIN_SCALE) / (AbilityHudConfig.MAX_SCALE - AbilityHudConfig.MIN_SCALE)) {
            @Override
            protected float actual() {
                // steps of 5 %
                return Math.round(Mth.lerp((float) this.value, AbilityHudConfig.MIN_SCALE, AbilityHudConfig.MAX_SCALE) * 20.0F) / 20.0F;
            }

            @Override
            protected void applyValue() {
                AbilityHudConfigScreen.this.config.scale = this.actual();
            }
        });
        y += 24;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.powers_unchained.hud_settings.reset"), button -> {
            this.config.reset();
            this.rebuildWidgets();
        }).bounds(left, y, w, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(right, y, w, 20).build());

        // order of the abilities on the keys: move one up / down (swaps it with its neighbour)
        List<String> shown = this.shownAbilities();
        int ox = this.orderX();
        for (int i = 0; i < shown.size(); i++) {
            String id = shown.get(i);
            int ry = ORDER_Y + 14 + i * ORDER_ROW;
            Button up = Button.builder(Component.literal("\u25B2"), button -> {
                AbilitySlots.move(this.shownAbilities(), id, -1);
                this.rebuildWidgets();
            }).bounds(ox + ORDER_W - 40, ry, 20, 20).build();
            up.active = i > 0;
            Button down = Button.builder(Component.literal("\u25BC"), button -> {
                AbilitySlots.move(this.shownAbilities(), id, 1);
                this.rebuildWidgets();
            }).bounds(ox + ORDER_W - 20, ry, 20, 20).build();
            down.active = i < shown.size() - 1;
            this.addRenderableWidget(up);
            this.addRenderableWidget(down);
        }
    }

    /** Dev/CI only: like pressing an ability's arrow. */
    public void debugMove(String id, int dir) {
        AbilitySlots.move(this.shownAbilities(), id, dir);
        this.rebuildWidgets();
    }

    private static final int ORDER_W = 170;
    private static final int ORDER_Y = 44;
    private static final int ORDER_ROW = 22;

    private int orderX() {
        return this.width - ORDER_W - 8;
    }

    /** The abilities in the order list: the player's current ones, or all of them if it has none. */
    private List<String> shownAbilities() {
        if (this.minecraft.player != null) {
            List<String> list = AbilitySlots.available(this.minecraft.player);
            if (!list.isEmpty()) {
                return list;
            }
        }
        List<String> all = AbilitySlots.ordered();
        return all.subList(0, Math.min(all.size(), PUKeyMappings.ALL.size()));
    }

    /** The ability order panel: key, ability name, arrows (the arrows are buttons). */
    private void drawOrder(GuiGraphicsExtractor graphics) {
        List<String> shown = this.shownAbilities();
        int ox = this.orderX();
        int h = 14 + shown.size() * ORDER_ROW + 2;
        graphics.fill(ox - 4, ORDER_Y - 4, ox + ORDER_W + 4, ORDER_Y + h, 0x90000000);
        graphics.text(this.font, Component.translatable("screen.powers_unchained.hud_settings.order"), ox, ORDER_Y, 0xFFFFD24A, true);
        for (int i = 0; i < shown.size(); i++) {
            AbilitySlots.Ability ability = AbilitySlots.get(shown.get(i));
            int ry = ORDER_Y + 14 + i * ORDER_ROW;
            Component key = AbilityHud.keyLabel(PUKeyMappings.ALL.get(i));
            int box = Math.max(this.font.width(key) + 6, 14);
            graphics.fill(ox, ry + 3, ox + box, ry + 17, 0xC0000000);
            graphics.centeredText(this.font, key, ox + box / 2, ry + 6, 0xFFFFFFFF);
            if (ability != null) {
                graphics.text(this.font, ability.previewName(), ox + box + 5, ry + 6, 0xFFFFFFFF, true);
            }
        }
    }

    private AbilityHud.Entry[] entries() {
        AbilityHud.Entry[] entries = this.minecraft.player != null ? AbilityHud.collect(this.minecraft.player) : null;
        if (entries != null) {
            return entries;
        }
        // nothing yet: preview every ability in the current order
        List<String> shown = this.shownAbilities();
        AbilityHud.Entry[] preview = new AbilityHud.Entry[PUKeyMappings.ALL.size()];
        for (int i = 0; i < shown.size(); i++) {
            AbilitySlots.Ability ability = AbilitySlots.get(shown.get(i));
            if (ability != null) {
                preview[i] = new AbilityHud.Entry(ability.previewName(), null, true);
            }
        }
        return preview;
    }

    /** {x, y, width, height} of the HUD on screen. */
    private int[] hudRect(AbilityHud.Entry[] entries) {
        int[] pos = AbilityHud.position(this.font, entries, this.config, this.width, this.height);
        int[] size = AbilityHud.size(this.font, entries, this.config);
        return new int[] {pos[0], pos[1], Math.round(size[0] * this.config.scale), Math.round(size[1] * this.config.scale)};
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // keep the game visible: just darken it a little
        graphics.fill(0, 0, this.width, this.height, 0x50000000);
    }

    /** Moves the HUD so its grabbed point sits under the mouse. */
    private void dragTo(double mouseX, double mouseY) {
        int[] r = this.hudRect(this.entries());
        double x = Mth.clamp(mouseX - this.dragOffsetX, 0, Math.max(0, this.width - r[2]));
        double y = Mth.clamp(mouseY - this.dragOffsetY, 0, Math.max(0, this.height - r[3]));
        this.config.x = (float) (x / this.width);
        this.config.y = (float) (y / this.height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.dragging) {
            // follow the mouse every frame (does not depend on drag events reaching the screen)
            this.dragTo(mouseX, mouseY);
        }
        AbilityHud.Entry[] entries = this.entries();
        int[] r = this.hudRect(entries);
        boolean hover = this.dragging || mouseX >= r[0] && mouseX < r[0] + r[2] && mouseY >= r[1] && mouseY < r[1] + r[3];
        int pad = 3;
        graphics.fill(r[0] - pad, r[1] - pad, r[0] + r[2] + pad, r[1] + r[3] + pad, hover ? 0x40FFFFFF : 0x20FFFFFF);
        outline(graphics, r[0] - pad, r[1] - pad, r[0] + r[2] + pad, r[1] + r[3] + pad, this.config.enabled ? OUTLINE : 0x80FF5555);
        AbilityHud.draw(graphics, this.font, entries, this.config, r[0], r[1]);
        if (!this.config.enabled) {
            graphics.centeredText(this.font, Component.translatable("screen.powers_unchained.hud_settings.hidden"),
                    r[0] + r[2] / 2, r[1] + r[3] + pad + 3, 0xFFFF7777);
        }
        this.drawOrder(graphics);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("screen.powers_unchained.hud_settings.drag"), this.width / 2, 26, 0xFFB0B0B0);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    private static void outline(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.fill(x0, y0, x1, y0 + 1, color);
        graphics.fill(x0, y1 - 1, x1, y1, color);
        graphics.fill(x0, y0 + 1, x0 + 1, y1 - 1, color);
        graphics.fill(x1 - 1, y0 + 1, x1, y1 - 1, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        int[] r = this.hudRect(this.entries());
        int pad = 3;
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && event.x() >= r[0] - pad && event.x() < r[0] + r[2] + pad && event.y() >= r[1] - pad
                && event.y() < r[1] + r[3] + pad) {
            this.dragging = true;
            this.setDragging(true);
            this.dragOffsetX = event.x() - r[0];
            this.dragOffsetY = event.y() - r[1];
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.dragging) {
            this.dragTo(event.x(), event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging) {
            this.dragging = false;
            this.setDragging(false);
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void removed() {
        this.config.save();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** A 0..1 slider showing its value in percent. */
    private abstract static class Slider extends AbstractSliderButton {

        private final String key;

        Slider(int x, int y, int width, String key, double value) {
            super(x, y, width, 20, Component.empty(), Mth.clamp(value, 0.0, 1.0));
            this.key = key;
            this.updateMessage();
        }

        protected abstract float actual();

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(this.key, Math.round(this.actual() * 100.0F) + "%"));
        }
    }
}
