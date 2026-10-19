package net.threetag.palladium.client.gui.screen.hub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.threetag.palladium.client.hub.HubAddon;
import net.threetag.palladium.client.hub.HubImageManager;
import net.threetag.palladium.client.util.RenderUtil;

import java.util.List;
import java.util.function.Consumer;


public class AddonListWidget extends ObjectSelectionList<AddonListWidget.AddonEntry> {

    private static final int ENTRY_HEIGHT = 36;
    private static final int ROW_PADDING = 4;
    private static final int SCROLLBAR_RESERVE = 12;
    private static final int DOWNLOAD_ICON_SIZE = 8;

    private final HubImageManager images;
    private final Consumer<HubAddon> onSelect;

    public AddonListWidget(Minecraft minecraft, int x, int y, int width, int height, HubImageManager images, Consumer<HubAddon> onSelect) {
        super(minecraft, width, height, y, ENTRY_HEIGHT);
        this.setX(x);
        this.images = images;
        this.onSelect = onSelect;
    }

    @Override
    public int getRowLeft() {
        return this.getX() + ROW_PADDING;
    }

    @Override
    public int getRowWidth() {
        return this.width - ROW_PADDING - SCROLLBAR_RESERVE;
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - 6;
    }

    public void setAddons(List<HubAddon> addons) {
        this.clearEntries();

        for (HubAddon addon : addons) {
            this.addEntry(new AddonEntry(addon));
        }

        if (!this.children().isEmpty()) {
            AddonEntry first = this.children().getFirst();
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
        public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            Minecraft minecraft = AddonListWidget.this.minecraft;
            int left = this.getContentX();
            int top = this.getContentY();
            int iconSize = this.getContentHeight();
            int right = this.getContentRight();

            if (hovered) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ARGB.color(40, 255, 255, 255));
            }

            // Icon
            HubImageManager.LoadedImage icon = AddonListWidget.this.images.get(this.addon.iconUrl());
            if (icon != null) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.id(), left, top, 0.0F, 0.0F, iconSize, iconSize, icon.width(), icon.height(), icon.width(), icon.height());
            } else {
                guiGraphics.fill(left, top, left + iconSize, top + iconSize, ARGB.color(60, 255, 255, 255));
            }

            int textLeft = left + iconSize + 6;

            String downloads = String.format("%,d", this.addon.totalDownloadCount());
            int downloadsColor = ARGB.color(255, 205, 205, 205);
            int downloadsWidth = minecraft.font.width(downloads);
            int downloadsX = right - downloadsWidth;
            guiGraphics.text(minecraft.font, downloads, downloadsX, top + 2, downloadsColor, true);

            // Download icon
            int iconX = downloadsX - DOWNLOAD_ICON_SIZE - 2;
            drawDownloadIcon(guiGraphics, iconX, top + 2, downloadsColor);

            int textRight = iconX - 6;
            String name = clip(this.addon.displayName(), textRight - textLeft);
            guiGraphics.text(minecraft.font, name, textLeft, top + 2, RenderUtil.FULL_WHITE);

            if (this.addon.author() != null && !this.addon.author().isBlank()) {
                String author = clip(this.addon.author(), right - textLeft);
                guiGraphics.text(minecraft.font, author, textLeft, top + 14, ARGB.color(255, 160, 160, 160), false);
            }
        }

        private static void drawDownloadIcon(GuiGraphicsExtractor guiGraphics, int x, int y, int color) {
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
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            AddonListWidget.this.setSelected(this);
            AddonListWidget.this.onSelect.accept(this.addon);
            AddonListWidget.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
    }
}
