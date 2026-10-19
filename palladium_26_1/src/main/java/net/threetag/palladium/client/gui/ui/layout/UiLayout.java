package net.threetag.palladium.client.gui.ui.layout;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.client.gui.ui.widget.UiWidget;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;

public abstract class UiLayout {

    public static final int AUTO_SIZE = -1;

    public final void open() {
        Minecraft.getInstance().setScreen(new UiScreen(this));
    }

    public abstract UiLayoutSerializer<?> getSerializer();

    /**
     * The layout's configured width, or {@link #AUTO_SIZE} to span the full screen width.
     */
    public abstract int getWidth();

    /**
     * The layout's configured height, or {@link #AUTO_SIZE} to span the full screen height.
     */
    public abstract int getHeight();

    public int resolveWidth(int availableWidth) {
        return this.getWidth() == AUTO_SIZE ? availableWidth : this.getWidth();
    }

    public int resolveHeight(int availableHeight) {
        return this.getHeight() == AUTO_SIZE ? availableHeight : this.getHeight();
    }

    /**
     * Where on the screen this layout is placed
     */
    public UiAlignment getAlignment() {
        return UiAlignment.CENTER;
    }

    /**
     * @param layoutWidth the resolved width of the layout area
     */
    public int getX(int screenWidth, int layoutWidth) {
        var alignment = this.getAlignment();
        return alignment.isLeft() ? 0 : alignment.isRight() ? screenWidth - layoutWidth : (screenWidth - layoutWidth) / 2;
    }

    /**
     * @param layoutHeight the resolved height of the layout area
     */
    public int getY(int screenHeight, int layoutHeight) {
        var alignment = this.getAlignment();
        return alignment.isTop() ? 0 : alignment.isBottom() ? screenHeight - layoutHeight : (screenHeight - layoutHeight) / 2;
    }

    /**
     * @param width  the resolved width of the layout area
     * @param height the resolved height of the layout area
     */
    public abstract void renderBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height);

    /**
     * @param width  the resolved width of the layout area
     * @param height the resolved height of the layout area
     */
    public void addWidgets(UiScreen screen, int x, int y, int width, int height, WidgetConsumer consumer) {

    }

    @FunctionalInterface
    public interface WidgetConsumer {
        void accept(UiWidget uiWidget, AbstractWidget widget, ScreenRectangle rectangle);
    }

    public static class Codecs {

        public static final Codec<UiLayout> CODEC = Codec.withAlternative(
                UiLayoutSerializer.TYPE_CODEC.dispatch(UiLayout::getSerializer, UiLayoutSerializer::codec),
                SimpleUiLayout.CODEC.codec()
        );

    }

}
