package net.threetag.palladium.client.gui.ui.widget;

import net.threetag.palladium.Palladium;

public class UiWidgetSerializers {

    public static final UiWidgetSerializer<ColoredRectangleUiWidget> COLORED_RECTANGLE = register("colored_rectangle", new ColoredRectangleUiWidget.Serializer());
    public static final UiWidgetSerializer<GradientRectangleUiWidget> GRADIENT_RECTANGLE = register("gradient_rectangle", new GradientRectangleUiWidget.Serializer());
    public static final UiWidgetSerializer<BlitUiWidget> BLIT = register("blit", new BlitUiWidget.Serializer());
    public static final UiWidgetSerializer<ProgressBarUiWidget> PROGRESS_BAR = register("progress_bar", new ProgressBarUiWidget.Serializer());
    public static final UiWidgetSerializer<TextUiWidget> TEXT = register("text", new TextUiWidget.Serializer());
    public static final UiWidgetSerializer<PowerNameUiWidget> POWER_NAME = register("power_name", new PowerNameUiWidget.Serializer());
    public static final UiWidgetSerializer<TextBoxUiWidget> TEXT_BOX = register("text_box", new TextBoxUiWidget.Serializer());
    public static final UiWidgetSerializer<IconUiWidget> ICON = register("icon", new IconUiWidget.Serializer());
    public static final UiWidgetSerializer<ItemUiWidget> ITEM = register("item", new ItemUiWidget.Serializer());
    public static final UiWidgetSerializer<ButtonUiWidget> BUTTON = register("button", new ButtonUiWidget.Serializer());
    public static final UiWidgetSerializer<CustomButtonUiWidget> CUSTOM_BUTTON = register("custom_button", new CustomButtonUiWidget.Serializer());
    public static final UiWidgetSerializer<IconButtonUiWidget> ICON_BUTTON = register("icon_button", new IconButtonUiWidget.Serializer());
    public static final UiWidgetSerializer<FlatButtonUiWidget> FLAT_BUTTON = register("flat_button", new FlatButtonUiWidget.Serializer());
    public static final UiWidgetSerializer<PlayerDisplayUiWidget> PLAYER_DISPLAY = register("player_display", new PlayerDisplayUiWidget.Serializer());
    public static final UiWidgetSerializer<PowerTreeUiWidget> POWER_TREE = register("power_tree", new PowerTreeUiWidget.Serializer());
    public static final UiWidgetSerializer<PowerListUiWidget> POWER_LIST = register("power_list", new PowerListUiWidget.Serializer());

    private static <T extends UiWidget> UiWidgetSerializer<T> register(String id, UiWidgetSerializer<T> serializer) {
        UiWidgetSerializer.register(Palladium.id(id), serializer);
        return serializer;
    }

    public static void init() {
        // nothing
    }
}
