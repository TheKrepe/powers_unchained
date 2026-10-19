package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PalladiumCodecs;

import java.awt.*;

public class ColoredRectangleUiWidget extends RenderableUiWidget {

    public static final MapCodec<ColoredRectangleUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PalladiumCodecs.COLOR_CODEC.fieldOf("color").forGetter(c -> c.color),
            propertiesCodec()
    ).apply(instance, ColoredRectangleUiWidget::new));

    private final Color color;

    public ColoredRectangleUiWidget(Color color, UiWidgetProperties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor gui, DataContext context, int x, int y, int width, int height, int mouseX, int mouseY, UiAlignment alignment) {
        gui.fill(x, y, x + width, y + height, this.color.getRGB());
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.COLORED_RECTANGLE;
    }

    public static class Serializer extends UiWidgetSerializer<ColoredRectangleUiWidget> {

        @Override
        public MapCodec<ColoredRectangleUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, ColoredRectangleUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Colored Rectangle")
                    .setDescription("Renders a colored rectangle.")
                    .add("color", TYPE_COLOR, "The color for the rectangle.")
                    .addExampleObject(new ColoredRectangleUiWidget(Color.RED, UiWidgetProperties.DEFAULT));
        }
    }
}
