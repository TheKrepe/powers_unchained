package net.threetag.palladium.client.screen.hub;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.threetag.palladium.client.hub.HubAddon;
import net.threetag.palladium.client.hub.HubImageManager;

import java.util.List;
import java.util.function.Consumer;

public class AddonListWidget extends ObjectSelectionList<AddonListWidget.AddonEntry> {

    private static final int ENTRY_HEIGHT = 36;
    private static final int ROW_PADDING = 4;
    private static final int SCROLLBAR_RESERVE = 12;
    private static final int DOWNLOAD_ICON_SIZE = 8;
    private static final int SELECTION_PADDING = 2;

    private final HubImageManager images;
    private final Consumer<HubAddon> onSelect;

    public AddonListWidget(Minecraft minecraft, int x, int y, int width, int height, HubImageManager images, Consumer<HubAddon> onSelect) {
        super(minecraft, width, y + height, y, y + height, ENTRY_HEIGHT);
        this.setLeftPos(x);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.images = images;
        this.onSelect = onSelect;
    }

    @Override
    public int getRowLeft() {
        return this.x0 + ROW_PADDING;
    }

    @Override
    public int getRowWidth() {
        return this.width - ROW_PADDING - SCROLLBAR_RESERVE;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
        int left = this.getRowLeft() - SELECTION_PADDING;
        int right = this.getRowLeft() + width + SELECTION_PADDING;
        guiGraphics.fill(left, top - 2, right, top + height + 2, outerColor);
        guiGraphics.fill(left + 1, top - 1, right - 1, top + height + 1, innerColor);
    }

    public void setAddons(List<HubAddon> addons) {
        this.clearEntries();

        for (HubAddon addon : addons) {
            this.addEntry(new AddonEntry(addon));
        }

        if (!this.children().isEmpty()) {
            AddonEntry first = this.children().get(0);
            this.setSelected(first);
            this.onSelect.accept(first.addon);
        } else {
            this.onSelect.accept(null);
        }

        this.setScrollAmount(0);
    }

    public class AddonEntry extends ObjectSelectionList.Entry<AddonEntry> {

        private final HubAddon addon;

        public AddonEntry(HubAddon addon) {
            this.addon = addon;
        }

        @Override
        public Component getNarration() {
            return Component.literal(this.addon.displayName());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            Minecraft minecraft = AddonListWidget.this.minecraft;
            int right = left + width;

            if (hovering) {
                guiGraphics.fill(left, top, right, top + height, FastColor.ARGB32.color(40, 255, 255, 255));
            }

            int iconSize = height - 4;
            int iconTop = top + 2;
            HubImageManager.LoadedImage icon = AddonListWidget.this.images.get(this.addon.iconUrl());
            if (icon != null) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.blit(icon.id(), left, iconTop, iconSize, iconSize, 0.0F, 0.0F, icon.width(), icon.height(), icon.width(), icon.height());
            } else {
                guiGraphics.fill(left, iconTop, left + iconSize, iconTop + iconSize, FastColor.ARGB32.color(60, 255, 255, 255));
            }

            int textLeft = left + iconSize + 6;

            String downloads = String.format("%,d", this.addon.totalDownloadCount());
            int downloadsColor = FastColor.ARGB32.color(255, 205, 205, 205);
            int downloadsWidth = minecraft.font.width(downloads);
            int downloadsX = right - downloadsWidth;
            guiGraphics.drawString(minecraft.font, downloads, downloadsX, top + 4, downloadsColor, true);

            int iconX = downloadsX - DOWNLOAD_ICON_SIZE - 2;
            drawDownloadIcon(guiGraphics, iconX, top + 4, downloadsColor);

            int textRight = iconX - 6;
            String name = clip(this.addon.displayName(), textRight - textLeft);
            guiGraphics.drawString(minecraft.font, name, textLeft, top + 4, 0xFFFFFFFF);

            if (this.addon.author() != null && !this.addon.author().isBlank()) {
                String author = clip(this.addon.author(), right - textLeft);
                guiGraphics.drawString(minecraft.font, author, textLeft, top + 16, FastColor.ARGB32.color(255, 160, 160, 160), false);
            }
        }

        private void drawDownloadIcon(GuiGraphics guiGraphics, int x, int y, int color) {
            guiGraphics.fill(x + 3, y, x + 5, y + 4, color);
            guiGraphics.fill(x + 1, y + 3, x + 7, y + 4, color);
            guiGraphics.fill(x + 2, y + 4, x + 6, y + 5, color);
            guiGraphics.fill(x + 3, y + 5, x + 5, y + 6, color);
            guiGraphics.fill(x + 1, y + 7, x + 7, y + 8, color);
        }

        private String clip(String text, int maxWidth) {
            Minecraft minecraft = AddonListWidget.this.minecraft;
            if (minecraft.font.width(text) <= maxWidth) {
                return text;
            }

            return minecraft.font.plainSubstrByWidth(text, maxWidth - minecraft.font.width("...")) + "...";
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            AddonListWidget.this.setSelected(this);
            AddonListWidget.this.onSelect.accept(this.addon);
            AddonListWidget.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
    }
}
