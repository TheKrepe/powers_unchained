package net.threetag.palladium.logic.value;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;

public class StaticValue extends Value {

    public static final Value ZERO = new StaticValue(0);
    public static final Value ONE = new StaticValue(1);

    public static final MapCodec<StaticValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.PASSTHROUGH.fieldOf("value").forGetter(v -> null)
    ).apply(instance, StaticValue::new));

    public final Dynamic<?> object;

    public StaticValue(Dynamic<?> object) {
        this.object = object;
    }

    public StaticValue(Number number) {
        this(new Dynamic<>(JsonOps.INSTANCE, new JsonPrimitive(number)));
    }

    public StaticValue(String string) {
        this(new Dynamic<>(JsonOps.INSTANCE, new JsonPrimitive(string)));
    }

    public StaticValue(boolean bool) {
        this(new Dynamic<>(JsonOps.INSTANCE, new JsonPrimitive(bool)));
    }

    @Override
    public Object get(DataContext context) {
        var json = this.object.convert(JsonOps.INSTANCE).getValue();
        if (json instanceof JsonPrimitive primitive) {
            if (primitive.isNumber()) {
                return primitive.getAsNumber();
            } else if (primitive.isString()) {
                return primitive.getAsString();
            } else if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
        }
        return 0;
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.STATIC.value();
    }

    public static class Serializer extends ValueSerializer<StaticValue> {

        @Override
        public SettingType getReturnType() {
            return TYPE_ANY;
        }

        @Override
        public MapCodec<StaticValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, StaticValue> builder, HolderLookup.Provider provider) {
            builder.setName("Static Value").setDescription("Utilises a simple, static value")
                    .add("value", TYPE_ANY, "A simple, static value.")
                    .addExampleObject(new StaticValue(42));
        }
    }
}
