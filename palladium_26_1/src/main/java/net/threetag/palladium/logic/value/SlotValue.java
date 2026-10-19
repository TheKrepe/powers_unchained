package net.threetag.palladium.logic.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class SlotValue extends StringValue {

    public static final MapCodec<SlotValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            modifyFunctionCodec()
    ).apply(instance, SlotValue::new));

    public SlotValue(String molang) {
        super(molang);
    }

    @Override
    public String getString(DataContext context) {
        var slot = context.getSlot();
        return slot != null ? slot.toString() : "";
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.SLOT.get();
    }

    public static class Serializer extends StringSerializer<SlotValue> {

        @Override
        public MapCodec<SlotValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, SlotValue> builder, HolderLookup.Provider provider) {
            builder.setName("Slot").setDescription("Returns the slot of the current context.")
                    .addExampleObject(new SlotValue(""));
        }
    }
}
