package net.threetag.palladium.logic.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class ConvertToFloatValue extends FloatValue {

    public static final MapCodec<ConvertToFloatValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("value").forGetter(v -> v.value),
            modifyFunctionCodec()
    ).apply(instance, ConvertToFloatValue::new));

    private final Value value;

    public ConvertToFloatValue(Value value, String molang) {
        super(molang);
        this.value = value;
    }

    @Override
    public float getFloat(DataContext context) {
        return this.value.getAsFloat(context);
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.CONVERT_TO_FLOAT.get();
    }

    public static class Serializer extends FloatSerializer<ConvertToFloatValue> {

        @Override
        public MapCodec<ConvertToFloatValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, ConvertToFloatValue> builder, HolderLookup.Provider provider) {
            builder.setName("Convert to Float").setDescription("Converts the result of another value to a float.")
                    .add("value", TYPE_VALUE, "The value to convert.")
                    .addExampleObject(new ConvertToFloatValue(new StaticValue(3), ""));
        }
    }
}
