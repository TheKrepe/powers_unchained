package net.threetag.palladium.client.gui.screen.hub;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.threetag.palladium.client.hub.HubAddon;
import net.threetag.palladium.client.hub.HubImageManager;
import net.threetag.palladium.client.hub.HubTag;
import net.threetag.palladium.client.util.RenderUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class AddonInfoPanel extends AbstractScrollArea {

    public static final String BY_KEY = "gui.palladium.addon_hub.by";
    public static final String VERSIONS_KEY = "gui.palladium.addon_hub.versions";
    public static final String DOWNLOADS_KEY = "gui.palladium.addon_hub.downloads";

    private static final int PADDING = 8;
    private static final int LINE_GAP = 4;
    private static final int TAG_HEIGHT = 12;
    private static final int TAG_PADDING = 4;

    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    private final Minecraft minecraft;
    private final HubImageManager images;
    @Nullable
    private HubAddon addon;
    private Component status = Component.empty();

    private int contentHeight = 0;

    public AddonInfoPanel(Minecraft minecraft, int x, int y, int width, int height, HubImageManager images) {
        super(x, y, width, height, Component.empty(), AbstractScrollArea.defaultSettings(10));
        this.minecraft = minecraft;
        this.images = images;

    }

    public void setAddon(@Nullable HubAddon addon) {
        this.addon = addon;

        this.setScrollAmount(0);
    }

    public void setStatus(Component status) {
        this.addon = null;
        this.status = status;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(guiGraphics);

        if (this.addon == null) {
            guiGraphics.centeredText(this.minecraft.font, this.status, this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2 - 4, RenderUtil.DEFAULT_GRAY);
            return;
        }

        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        this.renderAddon(guiGraphics);
        guiGraphics.disableScissor();

        this.extractScrollbar(guiGraphics, mouseX, mouseY);
    }

    private void extractBackground(GuiGraphicsExtractor guiGraphics) {
        boolean inWorld = this.minecraft.level != null;
        Identifier background = inWorld ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, this.getX(), this.getY(), (float) this.getRight(), (float) this.getBottom(), this.getWidth(), this.getHeight(), 32, 32);

        Identifier header = inWorld ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        Identifier footer = inWorld ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, header, this.getX(), this.getY() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, footer, this.getX(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
    }

    private void renderAddon(GuiGraphicsExtractor guiGraphics) {
        assert this.addon != null;
        var font = this.minecraft.font;
        int left = this.getX() + PADDING;
        int right = this.getRight() - PADDING - (scrollable() ? scrollbarWidth() : 0);
        int innerWidth = right - left;

        int minY = this.getY() + PADDING;
        int y = (int) Math.clamp(minY - scrollAmount(), minY - maxScrollAmount(), minY);

        this.contentHeight = PADDING;

        // Banner
        HubImageManager.LoadedImage banner = this.images.get(this.addon.previewImage());
        if (this.addon.previewImage() != null) {
            int boxHeight = Math.min(innerWidth * 9 / 16, this.getHeight() / 3);
            if (banner != null) {
                float scale = Math.min(innerWidth / (float) banner.width(), boxHeight / (float) banner.height());
                int drawWidth = Math.max(1, (int) (banner.width() * scale));
                int drawHeight = Math.max(1, (int) (banner.height() * scale));
                int drawX = left + (innerWidth - drawWidth) / 2;
                int drawY = y + (boxHeight - drawHeight) / 2;
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, banner.id(), drawX, drawY, 0.0F, 0.0F, drawWidth, drawHeight, banner.width(), banner.height(), banner.width(), banner.height());
            } else {
                guiGraphics.fill(left, y, right, y + boxHeight, ARGB.color(60, 255, 255, 255));
            }

            y += boxHeight + PADDING;
            this.contentHeight += boxHeight + PADDING;
        }

        // Title
        guiGraphics.text(font, Component.literal(this.addon.displayName()).withStyle(ChatFormatting.BOLD), left, y, RenderUtil.FULL_WHITE);
        y += font.lineHeight + LINE_GAP;
        this.contentHeight += font.lineHeight + LINE_GAP;

        // Author
        if (this.addon.author() != null && !this.addon.author().isBlank()) {
            guiGraphics.text(font, Component.translatable(BY_KEY, this.addon.author()), left, y, ARGB.color(255, 170, 170, 170), false);
            y += font.lineHeight + LINE_GAP;
            this.contentHeight += font.lineHeight + LINE_GAP;
        }

        // Versions
        if (!this.addon.versions().isEmpty()) {
            String versions = String.join(", ", this.addon.versions());
            guiGraphics.text(font, Component.translatable(VERSIONS_KEY, versions), left, y, ARGB.color(255, 170, 170, 170), false);
            y += font.lineHeight + LINE_GAP;
            this.contentHeight += font.lineHeight + LINE_GAP;
        }

        // Downloads
        guiGraphics.text(font, Component.translatable(DOWNLOADS_KEY, String.format("%,d", this.addon.totalDownloadCount())), left, y, ARGB.color(255, 170, 170, 170), false);
        y += font.lineHeight + LINE_GAP + 2;
        this.contentHeight += font.lineHeight + LINE_GAP + 2;

        // Tags
        int preTagsY = y;
        y = this.renderTags(guiGraphics, left, right, y);
        this.contentHeight += y - preTagsY;

        // Description
        if (this.addon.description() != null && !this.addon.description().isBlank()) {
            y += 2;
            this.contentHeight += 2;

            int index = 0;
            List<FormattedCharSequence> lines = font.split(Component.literal(this.addon.description()), innerWidth);
            for (FormattedCharSequence line : lines) {
                guiGraphics.text(font, line, left, y, ARGB.color(255, 200, 200, 200), false);

                y += font.lineHeight + 1;
                this.contentHeight += index == lines.size() - 1 ? font.lineHeight - 2 : font.lineHeight + 1;

                index++;
            }
        }

        this.contentHeight += PADDING;
    }

    private int renderTags(GuiGraphicsExtractor guiGraphics, int left, int right, int y) {
        var font = this.minecraft.font;
        int x = left;

        for (HubTag tag : Objects.requireNonNull(this.addon).tags()) {
            int chipWidth = font.width(tag.name()) + TAG_PADDING * 2;

            if (x + chipWidth > right) {
                x = left;
                y += TAG_HEIGHT + LINE_GAP;
            }

            guiGraphics.fill(x, y, x + chipWidth, y + TAG_HEIGHT, tag.color());
            guiGraphics.text(font, tag.name(), x + TAG_PADDING, y + 2, contrastColor(tag.color()), false);
            x += chipWidth + LINE_GAP;
        }

        if (!this.addon.tags().isEmpty()) {
            y += TAG_HEIGHT + LINE_GAP;
        }

        return y;
    }

    private static int contrastColor(int color) {
        int r = ARGB.red(color);
        int g = ARGB.green(color);
        int b = ARGB.blue(color);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        return luminance > 0.6 ? ARGB.opaque(0x000000) : RenderUtil.FULL_WHITE;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    protected int contentHeight() {
        return contentHeight;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean scrolling = this.updateScrolling(event);
        return super.mouseClicked(event, doubleClick) || scrolling;
    }
}
