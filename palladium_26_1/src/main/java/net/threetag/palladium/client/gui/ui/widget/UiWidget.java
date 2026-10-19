package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.logic.context.DataContext;

public abstract class UiWidget {

    public static final Codec<UiWidget> CODEC = UiWidgetSerializer.TYPE_CODEC.dispatch(UiWidget::getSerializer, UiWidgetSerializer::codec);

    private final UiWidgetProperties properties;

    protected UiWidget(UiWidgetProperties properties) {
        this.properties = properties;
    }

    public final UiWidgetProperties getProperties() {
        return this.properties;
    }

    public final int getWidth(DataContext context) {
        return this.getProperties().width().getAsInt(context, 0);
    }

    public final int getHeight(DataContext context) {
        return this.getProperties().height().getAsInt(context, 0);
    }

    public abstract UiWidgetSerializer<?> getSerializer();

    public int getX(ScreenRectangle parent, DataContext context) {
        var props = this.getProperties();
        var anchor = this.getHorizontalAnchor(context, props);

        return switch (props.alignment().getHorizontalAlignment()) {
            case LEFT -> parent.left() - anchor;
            case CENTER -> parent.left() + (parent.width() / 2) - anchor;
            case RIGHT -> parent.right() - anchor;
        } + props.x().getAsInt(context, 0);
    }

    public int getY(ScreenRectangle parent, DataContext context) {
        var props = this.getProperties();
        var anchor = this.getVerticalAnchor(context, props);

        return switch (props.alignment().getVerticalAlignment()) {
            case LEFT -> parent.top() - anchor;
            case CENTER -> parent.top() + (parent.height() / 2) - anchor;
            case RIGHT -> parent.bottom() - anchor;
        } + props.y().getAsInt(context, 0);
    }

    private int getHorizontalAnchor(DataContext context, UiWidgetProperties props) {
        return props.anchor()
                .map(anchor -> anchor.resolveHorizontalAnchor(this.getWidth(context), context))
                .orElseGet(() -> switch (props.alignment().getHorizontalAlignment()) {
                    case LEFT -> 0;
                    case CENTER -> this.getWidth(context) / 2;
                    case RIGHT -> this.getWidth(context);
                });
    }

    private int getVerticalAnchor(DataContext context, UiWidgetProperties props) {
        return props.anchor()
                .map(anchor -> anchor.resolveVerticalAnchor(this.getHeight(context), context))
                .orElseGet(() -> switch (props.alignment().getVerticalAlignment()) {
                    case LEFT -> 0;
                    case CENTER -> this.getHeight(context) / 2;
                    case RIGHT -> this.getHeight(context);
                });
    }

    public abstract AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context);

    /**
     * Re-applies this widget's resolved position and size to the built widget. Called every tick so
     * dynamic {@link net.threetag.palladium.logic.value.Value}-based x/y/width/height update live.
     */
    public void applyBounds(AbstractWidget widget, ScreenRectangle rectangle, DataContext context) {
        widget.setPosition(this.getX(rectangle, context), this.getY(rectangle, context));
        widget.setSize(this.getWidth(context), this.getHeight(context));
    }

    protected static <B extends UiWidget> RecordCodecBuilder<B, UiWidgetProperties> propertiesCodec() {
        return UiWidgetProperties.CODEC.optionalFieldOf("properties", UiWidgetProperties.DEFAULT).forGetter(UiWidget::getProperties);
    }

    protected static <B extends UiWidget> RecordCodecBuilder<B, UiWidgetProperties> propertiesCodec16X16() {
        return UiWidgetProperties.CODEC_16X16.optionalFieldOf("properties", UiWidgetProperties.DEFAULT_16X16).forGetter(UiWidget::getProperties);
    }

    protected static <B extends UiWidget> RecordCodecBuilder<B, UiWidgetProperties> propertiesCodec(int width, int height) {
        return UiWidgetProperties.codecWithDefaultSize(width, height).optionalFieldOf("properties", UiWidgetProperties.withSize(width, height)).forGetter(UiWidget::getProperties);
    }
}
