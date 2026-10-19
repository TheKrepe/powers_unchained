package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.threetag.palladium.client.gui.widget.FlatButton;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Collections;

public final class FlatButtonUiWidget extends UiWidget {

    public static final MapCodec<FlatButtonUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(FlatButtonUiWidget::text),
            Codec.BOOL.optionalFieldOf("close_on_press", false).forGetter(FlatButtonUiWidget::closeOnPress),
            propertiesCodec()
    ).apply(instance, FlatButtonUiWidget::new));

    private final Component text;
    private final boolean closeOnPress;

    public FlatButtonUiWidget(Component text, boolean closeOnPress, UiWidgetProperties properties) {
        super(properties);
        this.text = text;
        this.closeOnPress = closeOnPress;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.FLAT_BUTTON;
    }

    @Override
    public AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        var builder = FlatButton.flatBuilder(this.text(), button -> {
                    this.getProperties().action()
                            .flatMap(a -> a.createAction(Collections.emptyMap()))
                            .ifPresent(clickevent -> Screen.defaultHandleGameClickEvent(clickevent, Minecraft.getInstance(), screen));

                    if (this.closeOnPress()) {
                        screen.onClose();
                    }
                })
                .bounds(this.getX(rectangle, context), this.getY(rectangle, context), this.getWidth(context), this.getHeight(context));

        this.getProperties().tooltip().ifPresent(component -> builder.tooltip(Tooltip.create(component)));

        return builder.build();
    }

    public Component text() {
        return text;
    }

    public boolean closeOnPress() {
        return closeOnPress;
    }

    public static class Serializer extends UiWidgetSerializer<FlatButtonUiWidget> {

        @Override
        public MapCodec<FlatButtonUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, FlatButtonUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Flat Button")
                    .setDescription("Adds a flat button.")
                    .add("text", TYPE_TEXT_COMPONENT, "Text of the button")
                    .addOptional("close_on_press", TYPE_BOOLEAN, "Whether or not the screen will be closed when pressing the button.")
                    .addExampleObject(new FlatButtonUiWidget(Component.literal("Click me!"), true, UiWidgetProperties.DEFAULT));
        }
    }
}
