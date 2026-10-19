package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.action.Action;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.client.gui.ui.UiAnchor;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record UiWidgetProperties(UiAlignment alignment, Value x, Value y, Value width, Value height, Optional<Action> action,
                                 Optional<Component> tooltip, Condition visibility, Optional<UiAnchor> anchor) {

    public static final UiWidgetProperties DEFAULT = withSize(50, 18);
    public static final UiWidgetProperties DEFAULT_16X16 = withSize(16, 16);

    public static final Codec<UiWidgetProperties> CODEC = codecWithDefaultSize(50, 18);
    public static final Codec<UiWidgetProperties> CODEC_16X16 = codecWithDefaultSize(16, 16);

    public static Codec<UiWidgetProperties> codecWithDefaultSize(int width, int height) {
        return RecordCodecBuilder.create(instance -> instance.group(
                UiAlignment.CODEC.optionalFieldOf("alignment", UiAlignment.TOP_LEFT).forGetter(UiWidgetProperties::alignment),
                Value.CODEC.optionalFieldOf("x", StaticValue.ZERO).forGetter(UiWidgetProperties::x),
                Value.CODEC.optionalFieldOf("y", StaticValue.ZERO).forGetter(UiWidgetProperties::y),
                Value.CODEC.optionalFieldOf("width", new StaticValue(width)).forGetter(UiWidgetProperties::width),
                Value.CODEC.optionalFieldOf("height", new StaticValue(height)).forGetter(UiWidgetProperties::height),
                Action.CODEC.optionalFieldOf("action").forGetter(UiWidgetProperties::action),
                ComponentSerialization.CODEC.optionalFieldOf("tooltip").forGetter(UiWidgetProperties::tooltip),
                Condition.CODEC.optionalFieldOf("visibility", TrueCondition.INSTANCE).forGetter(UiWidgetProperties::visibility),
                UiAnchor.CODEC.optionalFieldOf("anchor").forGetter(UiWidgetProperties::anchor)
        ).apply(instance, UiWidgetProperties::new));
    }

    public static UiWidgetProperties alignWithSize(UiAlignment alignment, int width, int height) {
        return new UiWidgetProperties(
                alignment, StaticValue.ZERO, StaticValue.ZERO, new StaticValue(width), new StaticValue(height), Optional.empty(), Optional.empty(), TrueCondition.INSTANCE, Optional.empty()
        );
    }

    public static UiWidgetProperties withSize(int width, int height) {
        return new UiWidgetProperties(
                UiAlignment.TOP_LEFT, StaticValue.ZERO, StaticValue.ZERO, new StaticValue(width), new StaticValue(height), Optional.empty(), Optional.empty(), TrueCondition.INSTANCE, Optional.empty()
        );
    }

    @Override
    public @NonNull String toString() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }
}
