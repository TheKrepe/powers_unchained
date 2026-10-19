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

public class GradientRectangleUiWidget extends RenderableUiWidget {

    public static final MapCodec<GradientRectangleUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PalladiumCodecs.COLOR_CODEC.fieldOf("color_from").forGetter(c -> c.colorFrom),
            PalladiumCodecs.COLOR_CODEC.fieldOf("color_to").forGetter(c -> c.colorTo),
            propertiesCodec()
    ).apply(instance, GradientRectangleUiWidget::new));

    private final Color colorFrom;
    private final Color colorTo;

    public GradientRectangleUiWidget(Color colorFrom, Color colorTo, UiWidgetProperties properties) {
        super(properties);
        this.colorFrom = colorFrom;
        this.colorTo = colorTo;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor gui, DataContext context, int x, int y, int width, int height, int mouseX, int mouseY, UiAlignment alignment) {
        gui.fillGradient(x, y, x + width, y + height, this.colorFrom.getRGB(), this.colorTo.getRGB());
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.GRADIENT_RECTANGLE;
    }

    public static class Serializer extends UiWidgetSerializer<GradientRectangleUiWidget> {

        @Override
        public MapCodec<GradientRectangleUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, GradientRectangleUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Gradient Rectangle")
                    .setDescription("Renders a gradient rectangle.")
                    .add("color_from", TYPE_COLOR, "The color for the start of the gradient.")
                    .add("color_to", TYPE_COLOR, "The color for the end of the gradient.")
                    .addExampleObject(new GradientRectangleUiWidget(Color.RED, Color.BLUE, UiWidgetProperties.DEFAULT));
        }
    }
}
