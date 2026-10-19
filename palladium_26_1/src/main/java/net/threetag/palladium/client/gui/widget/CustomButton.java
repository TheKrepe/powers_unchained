package net.threetag.palladium.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.threetag.palladium.client.gui.ui.background.ButtonBackground;
import org.jspecify.annotations.Nullable;

public class CustomButton extends Button {

    private final ButtonBackground background;

    public CustomButton(int x, int y, int width, int height, Component message, ButtonBackground background, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
        this.background = background;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.background.render(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.active, this.isHovered(), this.alpha);
        this.extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }

    public static Builder customBuilder(Component message, ButtonBackground background, Button.OnPress onPress) {
        return new Builder(message, background, onPress);
    }

    public static class Builder {
        private final Component message;
        private final ButtonBackground background;
        private final Button.OnPress onPress;
        private @Nullable Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;

        public Builder(Component message, ButtonBackground background, Button.OnPress onPress) {
            this.message = message;
            this.background = background;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public CustomButton build() {
            CustomButton button = new CustomButton(this.x, this.y, this.width, this.height, this.message, this.background, this.onPress, Button.DEFAULT_NARRATION);
            button.setTooltip(this.tooltip);
            return button;
        }
    }
}
