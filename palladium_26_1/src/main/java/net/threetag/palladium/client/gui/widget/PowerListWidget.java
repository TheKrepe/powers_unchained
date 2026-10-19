package net.threetag.palladium.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.ui.background.UiBackground;
import net.threetag.palladium.client.gui.widget.grid.AbstractSelectionGrid;
import net.threetag.palladium.client.renderer.icon.IconRenderer;
import net.threetag.palladium.client.util.GuiUtil;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.config.PalladiumClientConfig;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.PowerInstance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PowerListWidget extends AbstractSelectionGrid<PowerListWidget.Entry> implements TickableWidget {

    public static final WidgetSprites SPRITES = new WidgetSprites(
            Palladium.id("powers/list_entry"), Palladium.id("powers/list_entry_disabled"), Palladium.id("powers/list_entry_highlighted")
    );

    public static final int ENTRY_HEIGHT = 24;
    public static final int GAP = 4;
    public static final int PADDING = 5;
    public static final int ICON_SIZE = 16;
    private static final int MARGIN = 4;
    private static final int LOCKED_TITLE_COLOR = ARGB.opaque(0xA0A0A0);
    private static final int REFERENCE_COLOR = ARGB.opaque(0x808080);

    private final Screen parent;
    private final UiBackground background;
    @Nullable
    private PowerInstance powerInstance;

    public PowerListWidget(Screen parent, @Nullable PowerInstance powerInstance, UiBackground background,
                           int x, int y, int width, int height) {
        super(parent.getMinecraft(), x, y, width, height, ENTRY_HEIGHT, width, GAP);
        this.parent = parent;
        this.background = background;
        this.itemPerRow = 1;
        this.populate(powerInstance);
    }

    public void populate(@Nullable PowerInstance powerInstance) {
        this.clearEntries();
        this.powerInstance = powerInstance;

        if (powerInstance == null) {
            return;
        }

        List<PowerTreeWidget.AbilityElement> elements = new ArrayList<>();
        for (var abilityInstance : powerInstance.getAbilities().values()) {
            if (!abilityInstance.getAbility().getProperties().isHiddenInGUI()) {
                elements.add(new PowerTreeWidget.AbilityElement(abilityInstance));
            }
        }

        for (PowerTreeWidget.AbilityElement element : elements) {
            element.updateRelatives(elements);
            this.addEntry(new Entry(element));
        }
    }

    @Override
    public void tick() {
        if (this.children().stream().anyMatch(entry -> entry.element.needsRefresh())) {
            this.populate(this.powerInstance);
        }
    }

    @Override
    public void updateSizeAndPosition(int width, int height, int x, int y) {
        super.updateSizeAndPosition(width, height, x, y);
        this.itemPerRow = 1;
    }

    @Override
    public int getRowWidth() {
        return this.getWidth() - (MARGIN * 2) - (this.scrollable() ? AbstractScrollArea.SCROLLBAR_WIDTH * 2 : 0);
    }

    @Override
    protected void extractGridBackground(GuiGraphicsExtractor guiGraphics) {
        this.background.render(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.alpha);
    }

    @Override
    protected void extractGridSeparators(GuiGraphicsExtractor guiGraphics) {
    }

    @Override
    protected void renderGridItems(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.getItemCount() == 0) {
            int x = this.getX() + this.getWidth() / 2;
            int y = this.getY() + this.getHeight() / 2;
            GuiUtil.drawCenteredWordWrap(guiGraphics, Component.translatable(PowerTreeWidget.TRANS_KEY_NO_ABILITIES_LABEL),
                    x, y - 20, this.getWidth() - 15, -1);
            GuiUtil.drawCenteredWordWrap(guiGraphics, Component.translatable(PowerTreeWidget.TRANS_KEY_VERY_SAD_LABEL),
                    x, y + 20, this.getWidth() - 15, -1);
            return;
        }

        int left = this.getRowLeft();
        int width = this.getRowWidth();

        for (int i = 0; i < this.getItemCount(); i++) {
            if (this.getRowBottom(i) >= this.getY() && this.getRowTop(i) <= this.getBottom()) {
                this.renderItem(guiGraphics, mouseX, mouseY, partialTick, i, left,
                        this.getRowTop(i) + (this.gap / 2), width, this.itemHeight);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public class Entry extends AbstractSelectionGrid.Entry<Entry> {

        private final PowerTreeWidget.AbilityElement element;

        public Entry(PowerTreeWidget.AbilityElement element) {
            this.element = element;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            var minecraft = PowerListWidget.this.minecraft;
            var font = minecraft.font;
            boolean unlocked = this.element.isUnlocked();

            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(unlocked, hovering),
                    left, top, width, height, RenderUtil.FULL_WHITE);

            int iconX = left + PADDING;
            int iconY = top + (height - ICON_SIZE) / 2;

            if (unlocked) {
                IconRenderer.drawIcon(this.element.getIcon(), minecraft, guiGraphics,
                        DataContext.forEntity(minecraft.player), iconX, iconY, PowerListWidget.this.alpha);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PowerTreeWidget.SPRITE_LOCK,
                        iconX - 3, iconY - 3, ICON_SIZE + 6, ICON_SIZE + 6, RenderUtil.FULL_WHITE);
            }

            int textX = iconX + ICON_SIZE + PADDING;
            int textWidth = left + width - PADDING - textX;
            boolean devMode = PalladiumClientConfig.DEV_MODE.getAsBoolean();
            int textY = top + (height - (devMode ? font.lineHeight * 2 + 2 : font.lineHeight)) / 2 + 1;

            guiGraphics.enableScissor(textX, top, textX + textWidth, top + height);
            guiGraphics.text(font, this.element.getTitle(), textX, textY,
                    unlocked ? RenderUtil.FULL_WHITE : LOCKED_TITLE_COLOR);

            if (devMode) {
                guiGraphics.text(font, Component.literal(this.element.getReference().toString()),
                        textX, textY + font.lineHeight + 2, REFERENCE_COLOR, false);
            }

            guiGraphics.disableScissor();
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            var abilityInstance = this.element.getAbilityInstance();
            var handled = this.element.parentsUnlocked && !this.element.isUnlocked()
                    && abilityInstance.getAbility().getStateManager().getUnlockingHandler()
                    .onClicked(PowerListWidget.this.minecraft.player, abilityInstance);

            if (handled || this.element.openModal(PowerListWidget.this.parent)) {
                PowerListWidget.this.playDownSound(PowerListWidget.this.minecraft.getSoundManager());
            }

            return true;
        }
    }
}
