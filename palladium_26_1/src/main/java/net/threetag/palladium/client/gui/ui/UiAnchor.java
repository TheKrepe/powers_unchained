package net.threetag.palladium.client.gui.ui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public record UiAnchor(UiAlignment alignment, Value x, Value y) {

    public static final Codec<UiAnchor> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UiAlignment.CODEC.optionalFieldOf("alignment", UiAlignment.TOP_LEFT).forGetter(UiAnchor::alignment),
            Value.CODEC.optionalFieldOf("x", StaticValue.ZERO).forGetter(UiAnchor::x),
            Value.CODEC.optionalFieldOf("y", StaticValue.ZERO).forGetter(UiAnchor::y)
    ).apply(instance, UiAnchor::new));

    public static final Codec<UiAnchor> CODEC = Codec.withAlternative(
            RECORD_CODEC,
            UiAlignment.CODEC.xmap(
                    alignment -> new UiAnchor(alignment, StaticValue.ZERO, StaticValue.ZERO),
                    UiAnchor::alignment
            )
    );

    public int resolveHorizontalAnchor(int width, DataContext context) {
        int offset = switch (this.alignment().getHorizontalAlignment()) {
            case LEFT -> 0;
            case CENTER -> width / 2;
            case RIGHT -> width;
        };
        return offset + this.x().getAsInt(context, 0);
    }

    public int resolveVerticalAnchor(int height, DataContext context) {
        int offset = switch (this.alignment().getVerticalAlignment()) {
            case LEFT -> 0;
            case CENTER -> height / 2;
            case RIGHT -> height;
        };
        return offset + this.y().getAsInt(context, 0);
    }
}
