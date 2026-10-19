package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.gui.widget.BackgroundlessTextBoxWidget;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class TextBoxUiWidget extends UiWidget {

    public static final MapCodec<TextBoxUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(t -> t.text),
            propertiesCodec()
    ).apply(instance, TextBoxUiWidget::new));

    private final Component text;

    public TextBoxUiWidget(Component text, UiWidgetProperties properties) {
        super(properties);
        this.text = text;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.TEXT_BOX;
    }

    @Override
    public AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        return new BackgroundlessTextBoxWidget(
                this.getX(rectangle, context),
                this.getY(rectangle, context),
                this.getWidth(context),
                this.getHeight(context),
                this.text,
                screen.getFont()
        );
    }

    public static class Serializer extends UiWidgetSerializer<TextBoxUiWidget> {

        @Override
        public MapCodec<TextBoxUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, TextBoxUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Text Box")
                    .setDescription("Renders a scrollable text box")
                    .add("text", TYPE_TEXT_COMPONENT, "The text to be drawn")
                    .addExampleObject(new TextBoxUiWidget(Component.literal("Hello World!"), UiWidgetProperties.DEFAULT));
        }
    }
}
