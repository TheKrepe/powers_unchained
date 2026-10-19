package net.threetag.palladium.client.gui.widget;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.threetag.palladium.Palladium;

public class ArrowButton extends ImageButton {

    public static final WidgetSprites SPRITES_LEFT = new WidgetSprites(
            Palladium.id("widget/arrow_left"), Palladium.id("widget/arrow_left_disabled"), Palladium.id("widget/arrow_left_highlighted")
    );
    public static final WidgetSprites SPRITES_RIGHT = new WidgetSprites(
            Palladium.id("widget/arrow_right"), Palladium.id("widget/arrow_right_disabled"), Palladium.id("widget/arrow_right_highlighted")
    );

    public ArrowButton(int x, int y, boolean right, OnPress onPress) {
        super(x, y, 11, 17, right ? SPRITES_RIGHT : SPRITES_LEFT, onPress);
    }

    public ArrowButton(boolean right, OnPress onPress) {
        super(11, 17, right ? SPRITES_RIGHT : SPRITES_LEFT, onPress, Component.empty());
    }
}
