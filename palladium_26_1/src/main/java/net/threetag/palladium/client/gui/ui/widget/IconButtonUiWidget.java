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
import net.threetag.palladium.client.gui.ui.background.ButtonBackground;
import net.threetag.palladium.client.gui.ui.background.SpriteBackground;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.gui.widget.IconButton;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.icon.ItemIcon;
import net.threetag.palladium.logic.context.DataContext;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;

public final class IconButtonUiWidget extends UiWidget {

    public static final MapCodec<IconButtonUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Icon.CODEC.fieldOf("icon").forGetter(IconButtonUiWidget::icon),
            ButtonBackground.CODEC.optionalFieldOf("background").forGetter(c -> Optional.ofNullable(c.background)),
            Codec.BOOL.optionalFieldOf("close_on_press", false).forGetter(IconButtonUiWidget::closeOnPress),
            propertiesCodec16X16()
    ).apply(instance, (icon, background, closeOnPress, props) -> new IconButtonUiWidget(icon, background.orElse(null), closeOnPress, props)));

    private final Icon icon;
    @Nullable
    private final ButtonBackground background;
    private final boolean closeOnPress;

    public IconButtonUiWidget(Icon icon, @Nullable ButtonBackground background, boolean closeOnPress, UiWidgetProperties properties) {
        super(properties);
        this.icon = icon;
        this.background = background;
        this.closeOnPress = closeOnPress;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.ICON_BUTTON;
    }

    @Override
    public AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        var builder = IconButton.builder(this.icon(), button -> {
                    this.getProperties().action()
                            .flatMap(a -> a.createAction(Collections.emptyMap()))
                            .ifPresent(clickevent -> Screen.defaultHandleGameClickEvent(clickevent, Minecraft.getInstance(), screen));

                    if (this.closeOnPress()) {
                        screen.onClose();
                    }
                })
                .pos(this.getX(rectangle, context), this.getY(rectangle, context));

        this.getProperties().tooltip().ifPresent(component -> builder.tooltip(Tooltip.create(component)));
        var button = builder.build();

        if (this.background() != null) {
            ((IconButton) button).setBackground(this.background());
        }

        return button;
    }

    @Override
    public void applyBounds(AbstractWidget widget, ScreenRectangle rectangle, DataContext context) {
        widget.setPosition(this.getX(rectangle, context), this.getY(rectangle, context));
    }

    public Icon icon() {
        return icon;
    }

    @Nullable
    public ButtonBackground background() {
        return background;
    }

    public boolean closeOnPress() {
        return closeOnPress;
    }

    public static class Serializer extends UiWidgetSerializer<IconButtonUiWidget> {

        @Override
        public MapCodec<IconButtonUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, IconButtonUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Icon Button")
                    .setDescription("Adds a button that renders an icon instead of text.")
                    .add("icon", TYPE_ICON, "Icon that is rendered on the button")
                    .addOptional("background", TYPE_BUTTON_BACKGROUND, "The background that is drawn for the button. If not specified, the default vanilla button texture is used.")
                    .addOptional("close_on_press", TYPE_BOOLEAN, "Whether or not the screen will be closed when pressing the button.")
                    .addExampleObject(new IconButtonUiWidget(new ItemIcon(new ItemStackTemplate(Items.APPLE)), null, true, UiWidgetProperties.DEFAULT_16X16))
                    .addExampleObject(new IconButtonUiWidget(new ItemIcon(new ItemStackTemplate(Items.APPLE)), new ButtonBackground(SpriteBackground.DEFAULT), true, UiWidgetProperties.DEFAULT_16X16));
        }
    }
}
