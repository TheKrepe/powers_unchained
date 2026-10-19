package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Collections;

public final class ButtonUiWidget extends UiWidget {

    public static final MapCodec<ButtonUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(ButtonUiWidget::text),
            Codec.BOOL.optionalFieldOf("close_on_press", false).forGetter(ButtonUiWidget::closeOnPress),
            propertiesCodec()
    ).apply(instance, ButtonUiWidget::new));

    private final Component text;
    private final boolean closeOnPress;

    public ButtonUiWidget(Component text, boolean closeOnPress, UiWidgetProperties properties) {
        super(properties);
        this.text = text;
        this.closeOnPress = closeOnPress;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.BUTTON;
    }

    @Override
    public AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        var builder = Button.builder(this.text(), button -> {
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

    public static class Serializer extends UiWidgetSerializer<ButtonUiWidget> {

        @Override
        public MapCodec<ButtonUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, ButtonUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Button")
                    .setDescription("Adds a button.")
                    .add("text", TYPE_TEXT_COMPONENT, "Text of the button")
                    .addOptional("close_on_press", TYPE_BOOLEAN, "Whether or not the screen will be closed when pressing the button.")
                    .addExampleObject(new ButtonUiWidget(Component.literal("Click me!"), true, UiWidgetProperties.DEFAULT));
        }
    }
}
