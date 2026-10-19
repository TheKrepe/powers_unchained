package net.threetag.palladium.client.screen.hub;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.hub.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AddonHubScreen extends Screen {

    public static final String TITLE_KEY = "gui.palladium.addon_hub.title";
    public static final String LOADING_KEY = "gui.palladium.addon_hub.loading";
    public static final String EMPTY_KEY = "gui.palladium.addon_hub.empty";
    public static final String ERROR_KEY = "gui.palladium.addon_hub.error";
    public static final String SEARCH_HINT_KEY = "gui.palladium.addon_hub.search";
    public static final String SUPPORT_KEY = "gui.palladium.addon_hub.support";
    public static final String BUTTON_KEY = "gui.palladium.addon_hub.button";
    private static final String SUPPORT_URL = "https://ko-fi.com/threetag";

    private static final int MODRINTH_COLOR = 0x1BD96A;
    private static final int CURSEFORGE_COLOR = 0xF16436;

    private static final int MARGIN = 0;
    private static final int GAP = 8;
    private static final int HEADER_HEIGHT = 34;
    private static final int FOOTER_HEIGHT = 34;
    private static final int SEARCH_HEIGHT = 18;
    private static final int SORT_BUTTON_HEIGHT = 20;

    @Nullable
    private final Screen lastScreen;
    private final HubImageManager images = new HubImageManager();
    private final Map<AddonSort, Button> sortButtons = new EnumMap<>(AddonSort.class);
    private final List<HubAddon> allAddons = new ArrayList<>();

    private AddonListWidget list;
    private AddonInfoPanel panel;
    private EditBox searchBox;
    private Button modrinthButton;
    private Button curseforgeButton;

    private int platformX;
    private int platformWidth;
    private int platformTopY;
    private int platformBottomY;

    private int fieldTop;
    private int fieldBottom;

    private AddonSort sort = AddonSort.DOWNLOADS;
    @Nullable
    private String modrinthUrl;
    @Nullable
    private String curseforgeUrl;

    public AddonHubScreen(@Nullable Screen lastScreen) {
        super(Component.translatable(TITLE_KEY));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int contentTop = HEADER_HEIGHT;
        int contentBottom = this.height - FOOTER_HEIGHT;
        int contentHeight = contentBottom - contentTop;

        int controlsHeight = SEARCH_HEIGHT + GAP + SORT_BUTTON_HEIGHT;
        int fieldHeight = contentHeight - controlsHeight - GAP;
        this.fieldTop = contentTop;
        this.fieldBottom = contentTop + fieldHeight;

        int listX = MARGIN;
        int listWidth = (int) ((this.width - 2 * MARGIN) * 0.38F);
        int panelX = listX + listWidth;
        int panelWidth = this.width - MARGIN - panelX;

        this.list = this.addRenderableWidget(new AddonListWidget(this.minecraft, listX, contentTop, listWidth, fieldHeight, this.images, this::onAddonSelected));
        this.panel = this.addRenderableWidget(new AddonInfoPanel(this.minecraft, panelX, contentTop, panelWidth, fieldHeight, this.images));

        int leftControlX = GAP;
        int leftControlWidth = listWidth - GAP - GAP / 2;
        int rightControlX = panelX + GAP / 2;
        int rightControlWidth = this.width - GAP - rightControlX;

        int searchY = contentTop + fieldHeight + GAP;
        this.searchBox = new EditBox(this.font, leftControlX, searchY, leftControlWidth, SEARCH_HEIGHT, Component.translatable(SEARCH_HINT_KEY));
        this.searchBox.setHint(Component.translatable(SEARCH_HINT_KEY));
        this.searchBox.setMaxLength(64);
        this.searchBox.setResponder(query -> this.applyFilter());
        this.addRenderableWidget(this.searchBox);

        int buttonRowY = searchY + SEARCH_HEIGHT + GAP;
        int sortGap = 4;
        int sortCount = AddonSort.values().length;
        int sortButtonWidth = (leftControlWidth - sortGap * (sortCount - 1)) / sortCount;
        this.sortButtons.clear();
        for (int i = 0; i < sortCount; i++) {
            AddonSort value = AddonSort.values()[i];
            int x = leftControlX + i * (sortButtonWidth + sortGap);
            int w = i == sortCount - 1 ? leftControlX + leftControlWidth - x : sortButtonWidth;
            Button button = this.addRenderableWidget(Button.builder(value.getLabel(), b -> this.selectSort(value))
                    .bounds(x, buttonRowY, w, SORT_BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(value.getTooltip())).build());
            this.sortButtons.put(value, button);
        }

        this.platformX = rightControlX;
        this.platformWidth = rightControlWidth;
        this.platformTopY = searchY;
        this.platformBottomY = buttonRowY;
        this.modrinthButton = this.addRenderableWidget(Button.builder(coloredComponent("Modrinth", MODRINTH_COLOR), b -> this.openLink(this.modrinthUrl))
                .bounds(this.platformX, this.platformTopY, this.platformWidth, SORT_BUTTON_HEIGHT).build());
        this.curseforgeButton = this.addRenderableWidget(Button.builder(coloredComponent("CurseForge", CURSEFORGE_COLOR), b -> this.openLink(this.curseforgeUrl))
                .bounds(this.platformX, this.platformBottomY, this.platformWidth, SORT_BUTTON_HEIGHT).build());
        this.layoutPlatformButtons();

        int footerButtonWidth = 150;
        int footerY = this.height - 28;
        int footerX = (this.width - (footerButtonWidth * 2 + GAP)) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable(SUPPORT_KEY).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), b -> this.openLink(SUPPORT_URL))
                .bounds(footerX, footerY, footerButtonWidth, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(footerX + footerButtonWidth + GAP, footerY, footerButtonWidth, 20).build());

        this.updateSortButtons();
        this.load();
    }

    private static Component coloredComponent(String text, int rgb) {
        return Component.literal(text).withStyle(style -> style.withColor(TextColor.fromRgb(rgb)));
    }

    private void layoutPlatformButtons() {
        boolean modrinth = this.modrinthUrl != null;
        boolean curseforge = this.curseforgeUrl != null;
        this.modrinthButton.visible = modrinth;
        this.curseforgeButton.visible = curseforge;

        if (modrinth && curseforge) {
            place(this.modrinthButton, this.platformTopY);
            place(this.curseforgeButton, this.platformBottomY);
        } else if (modrinth) {
            place(this.modrinthButton, this.platformBottomY);
        } else if (curseforge) {
            place(this.curseforgeButton, this.platformBottomY);
        }
    }

    private void place(Button button, int y) {
        button.setX(this.platformX);
        button.setY(y);
        button.setWidth(this.platformWidth);
    }

    private void selectSort(AddonSort sort) {
        if (this.sort != sort) {
            this.sort = sort;
            this.updateSortButtons();
            this.load();
        }
    }

    private void updateSortButtons() {
        this.sortButtons.forEach((value, button) -> button.active = value != this.sort);
    }

    private void load() {
        this.allAddons.clear();
        this.list.setAddons(List.of());
        this.panel.setStatus(Component.translatable(LOADING_KEY));

        PalladiumHubApi.fetchAddons(PalladiumHubApi.getCurrentVersion(), this.sort)
                .thenAcceptAsync(this::applyAddons, this.minecraft)
                .exceptionally(throwable -> {
                    Palladium.LOGGER.warn("Failed to load PalladiumHub addons", throwable);
                    this.minecraft.execute(() -> {
                        if (this.minecraft.screen == this) {
                            this.panel.setStatus(Component.translatable(ERROR_KEY));
                        }
                    });
                    return null;
                });
    }

    private void applyAddons(List<HubAddon> addons) {
        if (this.minecraft == null || this.minecraft.screen != this) {
            return;
        }

        this.allAddons.clear();
        this.allAddons.addAll(addons);
        this.applyFilter();
    }

    private void applyFilter() {
        String query = this.searchBox.getValue().trim().toLowerCase(Locale.ROOT);

        List<HubAddon> filtered;
        if (query.isEmpty()) {
            filtered = this.allAddons;
        } else {
            filtered = new ArrayList<>();
            for (HubAddon addon : this.allAddons) {
                if (matches(addon, query)) {
                    filtered.add(addon);
                }
            }
        }

        this.list.setAddons(filtered);

        if (filtered.isEmpty()) {
            this.panel.setStatus(Component.translatable(EMPTY_KEY));
        }
    }

    private static boolean matches(HubAddon addon, String query) {
        if (contains(addon.displayName(), query) || contains(addon.author(), query) || contains(addon.description(), query)) {
            return true;
        }

        for (HubTag tag : addon.tags()) {
            if (contains(tag.name(), query)) {
                return true;
            }
        }

        return false;
    }

    private static boolean contains(@Nullable String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private void onAddonSelected(@Nullable HubAddon addon) {
        this.panel.setAddon(addon);

        this.modrinthUrl = addon != null && addon.modrinth() != null ? addon.modrinth().url() : null;
        this.curseforgeUrl = addon != null && addon.curseforge() != null ? addon.curseforge().url() : null;
        this.layoutPlatformButtons();
    }

    private void openLink(@Nullable String url) {
        if (url != null) {
            this.minecraft.setScreen(new ConfirmLinkScreen(open -> {
                if (open) {
                    Util.getPlatform().openUri(url);
                }
                this.minecraft.setScreen(this);
            }, url, true));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.renderContentBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
    }

    private void renderContentBackground(GuiGraphics guiGraphics) {
        int top = this.fieldTop;
        int bottom = this.fieldBottom;

        guiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        guiGraphics.blit(Screen.BACKGROUND_LOCATION, 0, top, this.width, bottom, this.width, bottom - top, 32, 32);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.fillGradient(0, top, this.width, top + 4, 0xFF000000, 0x00000000);
        guiGraphics.fillGradient(0, bottom - 4, this.width, bottom, 0x00000000, 0xFF000000);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void removed() {
        super.removed();
        this.images.releaseAll();
    }
}
