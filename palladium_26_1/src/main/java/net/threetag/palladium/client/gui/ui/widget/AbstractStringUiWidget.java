package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.gui.widget.StringWidget;
import net.threetag.palladium.client.gui.widget.ValueStringWidget;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.TextComponentValueHolder;
import net.threetag.palladium.util.PalladiumCodecs;

public abstract class AbstractStringUiWidget extends UiWidget {

    public static final Codec<StringWidget.TextOverflow> TEXT_OVERFLOW_CODEC = Codec.STRING.validate(s -> {
        if (s.equalsIgnoreCase("clamped") || s.equalsIgnoreCase("scrolling")) {
            return DataResult.success(s);
        } else {
            return DataResult.error(() -> "Unknown text overflow");
        }
    }).xmap(s -> s.equalsIgnoreCase("clamped") ? StringWidget.TextOverflow.CLAMPED : StringWidget.TextOverflow.SCROLLING,
            textOverflow -> textOverflow == StringWidget.TextOverflow.CLAMPED ? "clamped" : "scrolling");

    public static final Codec<TextAlignment> TEXT_ALIGNMENT_CODEC = Codec.STRING.validate(s -> {
        if (s.equalsIgnoreCase("left") || s.equalsIgnoreCase("center") || s.equalsIgnoreCase("right")) {
            return DataResult.success(s);
        } else {
            return DataResult.error(() -> "Unknown text alignment");
        }
    }).xmap(s -> s.equalsIgnoreCase("left") ? TextAlignment.LEFT : s.equalsIgnoreCase("center") ? TextAlignment.CENTER : TextAlignment.RIGHT,
            textAlignment -> textAlignment == TextAlignment.LEFT ? "left" : textAlignment == TextAlignment.CENTER ? "center" : "right");

    private final int color;
    private final boolean shadow;
    private final TextAlignment alignment;
    private final StringWidget.TextOverflow textOverflow;

    public AbstractStringUiWidget(int color, boolean shadow, TextAlignment alignment, StringWidget.TextOverflow textOverflow, UiWidgetProperties properties) {
        super(properties);
        this.color = color;
        this.shadow = shadow;
        this.alignment = alignment;
        this.textOverflow = textOverflow;
    }

    public abstract TextComponentValueHolder getText(UiScreen screen);

    @Override
    public net.minecraft.client.gui.components.AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        var text = getText(screen);
        var widget = new ValueStringWidget(this.getX(rectangle, context), this.getY(rectangle, context), this.getWidth(context), this.getHeight(context), text, screen.getFont())
                .setColor(this.color)
                .setShadow(this.shadow)
                .setAlignment(this.alignment);
        widget.setMaxWidth(this.getWidth(context), this.textOverflow);
        return widget;
    }

    public int getColor() {
        return color;
    }

    public boolean hasShadow() {
        return shadow;
    }

    public TextAlignment getTextAlignment() {
        return alignment;
    }

    public StringWidget.TextOverflow getTextOverflow() {
        return textOverflow;
    }

    public abstract static class AbstractStringUiWidgetSerializer<T extends AbstractStringUiWidget> extends UiWidgetSerializer<T> {

        @Override
        public CodecDocumentationBuilder<UiWidget, T> getDocumentation(HolderLookup.Provider provider) {
            return super.getDocumentation(provider)
                    .addOptional("color", TYPE_COLOR, "Color of this text", PalladiumCodecs.COLOR_CODEC.encodeStart(JsonOps.INSTANCE, RenderUtil.DEFAULT_GRAY_COLOR).getOrThrow().toString())
                    .addOptional("shadow", TYPE_BOOLEAN, "Whether or not this text has a shadow", false)
                    .addOptional("alignment", TYPE_TEXT_ALIGNMENT, "Alignment of this text", "left")
                    .addOptional("overflow", TYPE_TEXT_OVERFLOW, "How to behave if the text is longer than the widget width", "clamped");
        }

    }
}
