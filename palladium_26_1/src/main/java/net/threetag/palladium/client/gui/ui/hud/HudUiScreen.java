package net.threetag.palladium.client.gui.ui.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.threetag.palladium.client.gui.ui.layout.UiLayout;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.gui.ui.widget.UiWidget;
import net.threetag.palladium.logic.context.DataContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class HudUiScreen extends UiScreen {

    private final Map<UiWidget, AbstractWidget> hudWidgets = new LinkedHashMap<>();
    private final Map<UiWidget, ScreenRectangle> hudRectangles = new LinkedHashMap<>();

    public HudUiScreen(UiLayout layout, int width, int height) {
        super(layout);
        this.init(width, height);
    }

    @Override
    protected void init() {
        this.hudWidgets.clear();
        this.hudRectangles.clear();

        var layout = this.getLayout();
        int layoutWidth = layout.resolveWidth(this.width);
        int layoutHeight = layout.resolveHeight(this.height);
        int x = layout.getX(this.width, layoutWidth);
        int y = layout.getY(this.height, layoutHeight);

        layout.addWidgets(this, x, y, layoutWidth, layoutHeight, (uiWidget, widget, rectangle) -> {
            this.hudWidgets.put(uiWidget, widget);
            this.hudRectangles.put(uiWidget, rectangle);
        });
    }

    public boolean matches(UiLayout layout, int width, int height) {
        return this.getLayout() == layout && this.width == width && this.height == height;
    }

    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        var player = this.minecraft.player;

        if (player == null) {
            return;
        }

        var context = DataContext.forEntity(player);

        for (Map.Entry<UiWidget, AbstractWidget> entry : this.hudWidgets.entrySet()) {
            var uiWidget = entry.getKey();
            var widget = entry.getValue();
            widget.visible = uiWidget.getProperties().visibility().test(context);

            var rectangle = this.hudRectangles.get(uiWidget);
            if (rectangle != null) {
                uiWidget.applyBounds(widget, rectangle, context);
            }

            if (widget.visible) {
                widget.extractRenderState(guiGraphics, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTick);
            }
        }
    }
}
