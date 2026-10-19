package net.threetag.palladium.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.TextComponentValueHolder;

public class ValueStringWidget extends net.minecraft.client.gui.components.StringWidget {

    private final TextComponentValueHolder valueHolder;
    private int color = RenderUtil.DEFAULT_GRAY;
    private boolean shadow = false;
    private TextAlignment alignment = TextAlignment.LEFT;

    public ValueStringWidget(TextComponentValueHolder valueHolder, Font font) {
        super(Component.empty(), font);
        this.valueHolder = valueHolder;
    }

    public ValueStringWidget(int width, int height, TextComponentValueHolder valueHolder, Font font) {
        super(width, height, Component.empty(), font);
        this.setMaxWidth(width);
        this.valueHolder = valueHolder;
    }

    public ValueStringWidget(int x, int y, int width, int height, TextComponentValueHolder valueHolder, Font font) {
        super(x, y, width, height, Component.empty(), font);
        this.setMaxWidth(width);
        this.valueHolder = valueHolder;
    }

    public ValueStringWidget setColor(int color) {
        this.color = color;
        return this;
    }

    public ValueStringWidget setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public ValueStringWidget setAlignment(TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.setMessage(this.valueHolder.get(DataContext.forEntity(Minecraft.getInstance().player)));
        RenderUtil.flashStringColorOverride(this.color);
        RenderUtil.flashStringShadowOverride(this.shadow);
        super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void visitLines(ActiveTextCollector textCollector) {
        Component component = this.getMessage();
        Font font = this.getFont();
        int i = this.maxWidth > 0 ? this.maxWidth : this.getWidth();
        int j = font.width(component);
        int k = this.getX();
        int l = this.getY() + (this.getHeight() - 9) / 2;
        boolean flag = j > i;
        if (flag) {
            switch (this.textOverflow.ordinal()) {
                case 0 -> textCollector.accept(this.alignment, k, l, ComponentRenderUtils.clipText(component, font, i));
                case 1 -> this.extractScrollingStringOverContents(textCollector, component, 2);
            }
        } else {
            textCollector.accept(this.alignment, k, l, component.getVisualOrderText());
        }
    }
}
