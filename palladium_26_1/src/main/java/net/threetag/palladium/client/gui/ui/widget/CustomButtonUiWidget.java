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
import net.threetag.palladium.client.gui.screen.ModalScreen;
import net.threetag.palladium.client.gui.ui.background.ButtonBackground;
import net.threetag.palladium.client.gui.ui.background.SpriteBackground;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.gui.widget.CustomButton;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Collections;

public final class CustomButtonUiWidget extends UiWidget {

    public static final MapCodec<CustomButtonUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(CustomButtonUiWidget::text),
            ButtonBackground.CODEC.fieldOf("background").forGetter(CustomButtonUiWidget::background),
            Codec.BOOL.optionalFieldOf("close_on_press", false).forGetter(CustomButtonUiWidget::closeOnPress),
            propertiesCodec()
    ).apply(instance, CustomButtonUiWidget::new));

    private final Component text;
    private final ButtonBackground background;
    private final boolean closeOnPress;

    public CustomButtonUiWidget(Component text, ButtonBackground background, boolean closeOnPress, UiWidgetProperties properties) {
        super(properties);
        this.text = text;
        this.background = background;
        this.closeOnPress = closeOnPress;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.CUSTOM_BUTTON;
    }

    @Override
    public AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        var builder = CustomButton.customBuilder(this.text(), this.background(), button -> {
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

    public ButtonBackground background() {
        return background;
    }

    public boolean closeOnPress() {
        return closeOnPress;
    }

    public static class Serializer extends UiWidgetSerializer<CustomButtonUiWidget> {

        @Override
        public MapCodec<CustomButtonUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, CustomButtonUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Custom Button")
                    .setDescription("Adds a button with a custom texture.")
                    .add("text", TYPE_TEXT_COMPONENT, "Text of the button")
                    .add("background", TYPE_BUTTON_BACKGROUND, "The background that is drawn for the button. Can specify separate textures for normal, disabled and highlighted states.")
                    .addOptional("close_on_press", TYPE_BOOLEAN, "Whether or not the screen will be closed when pressing the button.")
                    .addExampleObject(new CustomButtonUiWidget(Component.literal("Hello World!"), new ButtonBackground(SpriteBackground.DEFAULT), true, UiWidgetProperties.DEFAULT))
                    .addExampleObject(new CustomButtonUiWidget(Component.literal("Hello World!"), new ButtonBackground(SpriteBackground.DEFAULT, new SpriteBackground(ModalScreen.BACKGROUND_MODAL_HEADER), new SpriteBackground(ModalScreen.BACKGROUND_MODAL_FOOTER)), true, UiWidgetProperties.DEFAULT));
        }
    }
}
