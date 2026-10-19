package net.threetag.palladium.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.client.gui.ui.background.ButtonBackground;
import net.threetag.palladium.client.renderer.icon.IconRenderer;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.logic.context.DataContext;
import org.jetbrains.annotations.Nullable;

public class IconButton extends Button {

    private final Icon icon;
    private ButtonBackground background = new ButtonBackground(new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted")));

    public IconButton(int x, int y, Icon icon, OnPress onPress, CreateNarration createNarration) {
        super(x, y, 20, 20, Component.empty(), onPress, createNarration);
        this.icon = icon;
    }

    public static Builder builder(Icon icon, OnPress onPress) {
        return new Builder(icon, onPress);
    }

    public IconButton disableBackgroundRendering() {
        this.background = null;
        return this;
    }

    public void setBackground(ButtonBackground background) {
        this.background = background;
    }

    public void setBackground(WidgetSprites background) {
        this.background = new ButtonBackground(background);
    }

    public Icon getIcon() {
        return this.icon;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (this.background != null) {
            this.background.render(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.active, this.isHovered(), this.alpha);
        }

        IconRenderer.drawIcon(this.getIcon(), minecraft, guiGraphics, minecraft.player != null ? DataContext.forEntity(minecraft.player) : DataContext.create(), this.getX() + 2, this.getY() + 2, this.alpha);
    }

    public static class Builder {

        private final Icon icon;
        private final OnPress onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private CreateNarration createNarration;
        private boolean disableBackground = false;

        public Builder(Icon icon, OnPress onPress) {
            this.createNarration = Button.DEFAULT_NARRATION;
            this.icon = icon;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public Builder disableBackgroundRendering() {
            this.disableBackground = true;
            return this;
        }

        public Button build() {
            IconButton button = new IconButton(this.x, this.y, this.icon, this.onPress, this.createNarration);
            button.setTooltip(this.tooltip);
            return this.disableBackground ? button.disableBackgroundRendering() : button;
        }
    }

}
