package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.holder.TextComponentValueHolder;
import net.threetag.palladium.util.PalladiumCodecs;

public class TextUiWidget extends AbstractStringUiWidget {

    public static final MapCodec<TextUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextComponentValueHolder.CODEC.fieldOf("text").forGetter(t -> t.text),
            PalladiumCodecs.COLOR_INT_CODEC.optionalFieldOf("color", RenderUtil.DEFAULT_GRAY).forGetter(TextUiWidget::getColor),
            Codec.BOOL.optionalFieldOf("shadow", false).forGetter(TextUiWidget::hasShadow),
            TEXT_ALIGNMENT_CODEC.optionalFieldOf("alignment", TextAlignment.LEFT).forGetter(TextUiWidget::getTextAlignment),
            TEXT_OVERFLOW_CODEC.optionalFieldOf("overflow", StringWidget.TextOverflow.CLAMPED).forGetter(TextUiWidget::getTextOverflow),
            propertiesCodec()
    ).apply(instance, TextUiWidget::new));

    private final TextComponentValueHolder text;

    public TextUiWidget(TextComponentValueHolder text, int color, boolean shadow, TextAlignment alignment, StringWidget.TextOverflow textOverflow, UiWidgetProperties properties) {
        super(color, shadow, alignment, textOverflow, properties);
        this.text = text;
    }

    @Override
    public TextComponentValueHolder getText(UiScreen screen) {
        return this.text;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.TEXT;
    }

    public static class Serializer extends AbstractStringUiWidgetSerializer<TextUiWidget> {

        @Override
        public MapCodec<TextUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, TextUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Text")
                    .setDescription("Renders a text component")
                    .add("text", TYPE_TEXT_COMPONENT_VALUE, "The text to be drawn")
                    .addExampleObject(new TextUiWidget(new TextComponentValueHolder("Hello World!"), RenderUtil.DEFAULT_GRAY, false, TextAlignment.LEFT, StringWidget.TextOverflow.CLAMPED, UiWidgetProperties.DEFAULT));
        }
    }
}
