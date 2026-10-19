package net.threetag.palladium.client.gui.ui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.threetag.palladium.logic.context.DataContext;

public class RenderableUiWidgetWidget extends AbstractWidget {

    private final RenderableUiWidget component;

    public RenderableUiWidgetWidget(RenderableUiWidget widget, ScreenRectangle rectangle, DataContext context) {
        super(widget.getX(rectangle, context), widget.getY(rectangle, context), widget.getWidth(context), widget.getHeight(context), Component.empty());
        this.component = widget;
        widget.getProperties().tooltip().ifPresent(tooltip -> this.setTooltip(Tooltip.create(tooltip)));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.component.render(Minecraft.getInstance(), guiGraphics, DataContext.forEntity(Minecraft.getInstance().player),
                this.getX(), this.getY(), this.getWidth(), this.getHeight(), mouseX, mouseY, this.component.getProperties().alignment());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return false;
    }
}
