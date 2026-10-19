package net.threetag.palladium.client.screen.hub;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.threetag.palladium.client.hub.HubAddon;
import net.threetag.palladium.client.hub.HubImageManager;
import net.threetag.palladium.client.hub.HubTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AddonInfoPanel extends AbstractWidget {

    public static final String BY_KEY = "gui.palladium.addon_hub.by";
    public static final String VERSIONS_KEY = "gui.palladium.addon_hub.versions";
    public static final String DOWNLOADS_KEY = "gui.palladium.addon_hub.downloads";

    private static final int PADDING = 8;
    private static final int LINE_GAP = 4;
    private static final int TAG_HEIGHT = 12;
    private static final int TAG_PADDING = 4;

    private final Minecraft minecraft;
    private final HubImageManager images;
    @Nullable
    private HubAddon addon;
    private Component status = Component.empty();

    public AddonInfoPanel(Minecraft minecraft, int x, int y, int width, int height, HubImageManager images) {
        super(x, y, width, height, Component.empty());
        this.minecraft = minecraft;
        this.images = images;
    }

    public void setAddon(@Nullable HubAddon addon) {
        this.addon = addon;
    }

    public void setStatus(Component status) {
        this.addon = null;
        this.status = status;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.addon == null) {
            guiGraphics.drawCenteredString(this.minecraft.font, this.status, this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2 - 4, FastColor.ARGB32.color(255, 160, 160, 160));
            return;
        }

        guiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
        this.renderAddon(guiGraphics);
        guiGraphics.disableScissor();
    }

    private void renderAddon(GuiGraphics guiGraphics) {
        var font = this.minecraft.font;
        int left = this.getX() + PADDING;
        int right = this.getX() + this.getWidth() - PADDING;
        int innerWidth = right - left;
        int y = this.getY() + PADDING;

        HubImageManager.LoadedImage banner = this.images.get(this.addon.previewImage());
        if (this.addon.previewImage() != null) {
            int boxHeight = Math.min(innerWidth * 9 / 16, this.getHeight() / 3);
            if (banner != null) {
                float scale = Math.min(innerWidth / (float) banner.width(), boxHeight / (float) banner.height());
                int drawWidth = Math.max(1, (int) (banner.width() * scale));
                int drawHeight = Math.max(1, (int) (banner.height() * scale));
                int drawX = left + (innerWidth - drawWidth) / 2;
                int drawY = y + (boxHeight - drawHeight) / 2;
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.blit(banner.id(), drawX, drawY, drawWidth, drawHeight, 0.0F, 0.0F, banner.width(), banner.height(), banner.width(), banner.height());
            } else {
                guiGraphics.fill(left, y, right, y + boxHeight, FastColor.ARGB32.color(60, 255, 255, 255));
            }
            y += boxHeight + PADDING;
        }

        // Title
        guiGraphics.drawString(font, Component.literal(this.addon.displayName()).withStyle(ChatFormatting.BOLD), left, y, 0xFFFFFFFF);
        y += font.lineHeight + LINE_GAP;

        int metaColor = FastColor.ARGB32.color(255, 170, 170, 170);

        // Author
        if (this.addon.author() != null && !this.addon.author().isBlank()) {
            guiGraphics.drawString(font, Component.translatable(BY_KEY, this.addon.author()), left, y, metaColor, false);
            y += font.lineHeight + LINE_GAP;
        }

        // Versions
        if (!this.addon.versions().isEmpty()) {
            String versions = String.join(", ", this.addon.versions());
            guiGraphics.drawString(font, Component.translatable(VERSIONS_KEY, versions), left, y, metaColor, false);
            y += font.lineHeight + LINE_GAP;
        }

        // Downloads
        guiGraphics.drawString(font, Component.translatable(DOWNLOADS_KEY, String.format("%,d", this.addon.totalDownloadCount())), left, y, metaColor, false);
        y += font.lineHeight + LINE_GAP + 2;

        // Tags
        y = this.renderTags(guiGraphics, left, right, y);

        // Description
        if (this.addon.description() != null && !this.addon.description().isBlank()) {
            y += 2;
            List<FormattedCharSequence> lines = font.split(Component.literal(this.addon.description()), innerWidth);
            for (FormattedCharSequence line : lines) {
                guiGraphics.drawString(font, line, left, y, FastColor.ARGB32.color(255, 200, 200, 200), false);
                y += font.lineHeight + 1;
            }
        }
    }

    private int renderTags(GuiGraphics guiGraphics, int left, int right, int y) {
        var font = this.minecraft.font;
        int x = left;

        for (HubTag tag : this.addon.tags()) {
            int chipWidth = font.width(tag.name()) + TAG_PADDING * 2;

            if (x + chipWidth > right) {
                x = left;
                y += TAG_HEIGHT + LINE_GAP;
            }

            guiGraphics.fill(x, y, x + chipWidth, y + TAG_HEIGHT, tag.color());
            guiGraphics.drawString(font, tag.name(), x + TAG_PADDING, y + 2, contrastColor(tag.color()), false);
            x += chipWidth + LINE_GAP;
        }

        if (!this.addon.tags().isEmpty()) {
            y += TAG_HEIGHT + LINE_GAP;
        }

        return y;
    }

    private static int contrastColor(int color) {
        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        return luminance > 0.6 ? 0xFF000000 : 0xFFFFFFFF;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
