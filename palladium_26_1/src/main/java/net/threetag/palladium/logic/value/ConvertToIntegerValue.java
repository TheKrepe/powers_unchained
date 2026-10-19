package net.threetag.palladium.logic.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class ConvertToIntegerValue extends IntegerValue {

    public static final MapCodec<ConvertToIntegerValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("value").forGetter(v -> v.value),
            modifyFunctionCodec()
    ).apply(instance, ConvertToIntegerValue::new));

    private final Value value;

    public ConvertToIntegerValue(Value value, String molang) {
        super(molang);
        this.value = value;
    }

    @Override
    public int getInteger(DataContext context) {
        return this.value.getAsInt(context);
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.CONVERT_TO_INTEGER.get();
    }

    public static class Serializer extends IntSerializer<ConvertToIntegerValue> {

        @Override
        public MapCodec<ConvertToIntegerValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, ConvertToIntegerValue> builder, HolderLookup.Provider provider) {
            builder.setName("Convert to Integer").setDescription("Converts the result of another value to an integer.")
                    .add("value", TYPE_VALUE, "The value to convert.")
                    .addExampleObject(new ConvertToIntegerValue(new StaticValue(3.7), ""));
        }
    }
}
