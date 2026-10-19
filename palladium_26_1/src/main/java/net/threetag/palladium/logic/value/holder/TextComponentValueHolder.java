package net.threetag.palladium.logic.value.holder;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;

import javax.annotation.Nullable;
import java.util.List;

public class TextComponentValueHolder {

    public static final TextComponentValueHolder EMPTY = new TextComponentValueHolder(Component.empty());

    public static final Codec<TextComponentValueHolder> CODEC = Codec.either(Single.CODEC.listOf(), Single.CODEC)
            .xmap(either -> either.map(
                    TextComponentValueHolder::new,
                    TextComponentValueHolder::new
            ), holder -> holder.values.size() == 1 ? Either.right(holder.values.getFirst()) : Either.left(holder.values));

    private final List<Single> values;

    public TextComponentValueHolder(String raw) {
        this.values = List.of(Single.ofString(raw));
    }

    public TextComponentValueHolder(Component raw) {
        this.values = List.of(Single.ofComponent(raw));
    }

    public TextComponentValueHolder(Single value) {
        this.values = List.of(value);
    }

    public TextComponentValueHolder(List<Single> values) {
        this.values = values;
    }

    public Component get(DataContext context) {
        MutableComponent component = null;

        if (values.isEmpty()) {
            return Component.empty();
        }

        for (Single value : values) {
            if (component == null) {
                component = value.get(context).copy();
            }
            else {
                component.append(value.get(context));
            }
        }

        return component;
    }

    public record Single(@Nullable Component raw, @Nullable ValueWithComponentFormatting value) {
        public Single {
            if (raw == null && value == null) {
                throw new IllegalArgumentException("Cannot create a Single without a Component or Value");
            }
        }

        public static final Codec<Single> CODEC = Codec.either(ValueWithComponentFormatting.CODEC, ComponentSerialization.CODEC)
            .xmap(either -> either.map(
                    Single::ofValue,
                    Single::ofComponent
            ), single -> single.value != null ? Either.left(single.value) : Either.right(single.raw));

        static Single ofString(String raw) {
            return new Single(Component.literal(raw), null);
        }

        static Single ofComponent(Component raw) {
            return new Single(raw, null);
        }

        static Single ofValue(ValueWithComponentFormatting value) {
            return new Single(null, value);
        }

        static MutableComponent valueToComponent(ValueWithComponentFormatting valueHolder, DataContext context) {
            return Component.literal(valueHolder.value.getAsString(context)).setStyle(valueHolder.style);
        }

        public Component get(DataContext context) {
            if (this.value != null) {
                return valueToComponent(this.value, context);
            } else {
                return raw;
            }
        }
    }

    public record ValueWithComponentFormatting(Value value, Style style) {
        /* It's a little ugly. Essentially looks like this
        {
          "value": 5, // primitive or json object
          "formatting": {} // Component style
        }
        **/
        public static final Codec<ValueWithComponentFormatting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Value.CODEC.fieldOf("value").forGetter(ValueWithComponentFormatting::value),
                Style.Serializer.CODEC.optionalFieldOf("formatting", Style.EMPTY).forGetter(ValueWithComponentFormatting::style)
        ).apply(instance, ValueWithComponentFormatting::new));
    }
}
