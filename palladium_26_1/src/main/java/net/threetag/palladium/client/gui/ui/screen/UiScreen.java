package net.threetag.palladium.client.gui.ui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.threetag.palladium.client.gui.screen.DelayedRenderCallReceiver;
import net.threetag.palladium.client.gui.ui.widget.UiWidget;
import net.threetag.palladium.client.gui.ui.layout.UiLayout;
import net.threetag.palladium.logic.context.DataContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class UiScreen extends Screen implements DelayedRenderCallReceiver {

    private UiLayout layout;
    protected int layoutWidth, layoutHeight;
    protected int leftPos;
    protected int topPos;
    private final Map<UiWidget, AbstractWidget> widgets = new LinkedHashMap<>();
    private final Map<UiWidget, ScreenRectangle> widgetRectangles = new LinkedHashMap<>();
    private final List<Consumer<GuiGraphicsExtractor>> delayedRenderCalls = new ArrayList<>();

    public UiScreen(UiLayout layout) {
        super(Component.empty());
        this.layout = layout;
        this.layoutWidth = layout.getWidth();
        this.layoutHeight = layout.getHeight();
    }

    @Override
    protected void init() {
        super.init();

        this.layoutWidth = this.layout.resolveWidth(this.width);
        this.layoutHeight = this.layout.resolveHeight(this.height);
        this.leftPos = this.layout.getX(this.width, this.layoutWidth);
        this.topPos = this.layout.getY(this.height, this.layoutHeight);
        this.loadWidgets(this.layout);
    }

    protected void loadWidgets(UiLayout layout) {
        this.widgets.clear();
        this.widgetRectangles.clear();

        var context = this.getContext();
        layout.addWidgets(this, this.leftPos, this.topPos, this.layoutWidth, this.layoutHeight, (uiWidget, widget, rectangle) -> {
            widget.visible = uiWidget.getProperties().visibility().test(context);
            this.widgets.put(uiWidget, widget);
            this.widgetRectangles.put(uiWidget, rectangle);
            this.addRenderableWidget(widget);
        });
    }

    public DataContext getContext() {
        return DataContext.forEntity(this.minecraft.player);
    }

    protected void changeLayout(UiLayout layout) {
        for (AbstractWidget widget : this.widgets.values()) {
            this.removeWidget(widget);
        }

        this.layout = layout;
        this.layoutWidth = layout.resolveWidth(this.width);
        this.layoutHeight = layout.resolveHeight(this.height);
        this.leftPos = layout.getX(this.width, this.layoutWidth);
        this.topPos = layout.getY(this.height, this.layoutHeight);
        this.loadWidgets(layout);
    }

    @Override
    public void tick() {
        super.tick();
        var context = this.getContext();

        for (Map.Entry<UiWidget, AbstractWidget> e : this.widgets.entrySet()) {
            var uiWidget = e.getKey();
            var widget = e.getValue();
            widget.visible = uiWidget.getProperties().visibility().test(context);

            var rectangle = this.widgetRectangles.get(uiWidget);
            if (rectangle != null) {
                uiWidget.applyBounds(widget, rectangle, context);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.layout.renderBackground(guiGraphics, this.leftPos, this.topPos, this.layoutWidth, this.layoutHeight);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        for (Consumer<GuiGraphicsExtractor> renderCall : this.delayedRenderCalls) {
            renderCall.accept(guiGraphics);
        }

        this.delayedRenderCalls.clear();
    }

    public UiLayout getLayout() {
        return this.layout;
    }

    @Override
    public void renderDelayed(Consumer<GuiGraphicsExtractor> consumer) {
        this.delayedRenderCalls.add(consumer);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
