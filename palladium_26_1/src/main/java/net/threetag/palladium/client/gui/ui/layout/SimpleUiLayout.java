package net.threetag.palladium.client.gui.ui.layout;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.client.gui.ui.background.SpriteBackground;
import net.threetag.palladium.client.gui.ui.background.UiBackground;
import net.threetag.palladium.client.gui.ui.widget.UiWidget;
import net.threetag.palladium.client.gui.ui.screen.UiPadding;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class SimpleUiLayout extends UiLayout {

    public static final MapCodec<SimpleUiLayout> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("width", AUTO_SIZE).forGetter(SimpleUiLayout::getWidth),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("height", AUTO_SIZE).forGetter(SimpleUiLayout::getHeight),
            UiAlignment.CODEC.optionalFieldOf("alignment", UiAlignment.CENTER).forGetter(SimpleUiLayout::getAlignment),
            UiPadding.CODEC.optionalFieldOf("padding", UiPadding.SEVEN).forGetter(SimpleUiLayout::padding),
            UiBackground.Codecs.CODEC.optionalFieldOf("background", SpriteBackground.DEFAULT).forGetter(SimpleUiLayout::background),
            UiWidget.CODEC.listOf().optionalFieldOf("widgets", Collections.emptyList()).forGetter(SimpleUiLayout::widgets)
    ).apply(instance, SimpleUiLayout::new));

    private final int width;
    private final int height;
    private final UiAlignment alignment;
    private final UiPadding padding;
    private final UiBackground background;
    private final List<UiWidget> widgets;

    public SimpleUiLayout(int width, int height, UiAlignment alignment, UiPadding padding, UiBackground background, List<UiWidget> widgets) {
        this.width = width;
        this.height = height;
        this.alignment = alignment;
        this.padding = padding;
        this.background = background;
        this.widgets = widgets;
    }

    @Override
    public UiAlignment getAlignment() {
        return this.alignment;
    }

    @Override
    public UiLayoutSerializer<?> getSerializer() {
        return UiLayoutSerializers.SIMPLE;
    }

    @Override
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        this.background().render(guiGraphics, x, y, width, height, 1F);
    }

    @Override
    public void addWidgets(UiScreen screen, int x, int y, int width, int height, WidgetConsumer consumer) {
        var rectangle = new ScreenRectangle(
                x + this.padding().left(),
                y + this.padding().top(),
                width - this.padding().horizontal(),
                height - this.padding().vertical());

        var context = screen.getContext();
        for (UiWidget uiWidget : this.widgets) {
            var widget = uiWidget.buildWidget(screen, rectangle, context);
            consumer.accept(uiWidget, widget, rectangle);
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public UiPadding padding() {
        return padding;
    }

    public UiBackground background() {
        return background;
    }

    public List<UiWidget> widgets() {
        return widgets;
    }

    public static class Serializer extends UiLayoutSerializer<SimpleUiLayout> {

        @Override
        public MapCodec<SimpleUiLayout> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiLayout, SimpleUiLayout> builder, HolderLookup.Provider provider) {
            builder.setName("Simple")
                    .setDescription("Used for simple layouts.")
                    .addOptional("width", TYPE_POSITIVE_INT, "Width of this layout. If left out, it spans the full available width (the whole screen).", "full width")
                    .addOptional("height", TYPE_POSITIVE_INT, "Height of this layout. If left out, it spans the full available height (the whole screen).", "full height")
                    .addOptional("alignment", TYPE_UI_ALIGNMENT, "Where on the screen the layout is placed.", "center")
                    .addOptional("padding", TYPE_UI_PADDING, "Padding for each side of the layout", 0)
                    .addOptional("background", TYPE_UI_BACKGROUND, "The background that is drawn for the layout.", SpriteBackground.DEFAULT.toString())
                    .add("widgets", TYPE_UI_WIDGETS, "List of UI widgets");
        }
    }

}
