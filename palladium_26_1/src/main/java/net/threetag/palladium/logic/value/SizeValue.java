package net.threetag.palladium.logic.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.sizechange.PalladiumSizeChangeTypes;
import net.threetag.palladium.entity.sizechange.SizeChangeType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SizeValue extends FloatValue {

    public static final MapCodec<SizeValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryFixedCodec.create(PalladiumRegistryKeys.SIZE_CHANGE_TYPE).optionalFieldOf("size_change_type").forGetter(v -> v.sizeChangeType),
            modifyFunctionCodec()
    ).apply(instance, SizeValue::new));

    public final Optional<Holder<SizeChangeType>> sizeChangeType;

    public SizeValue(Optional<Holder<SizeChangeType>> sizeChangeType, String molang) {
        super(molang);
        this.sizeChangeType = sizeChangeType;
    }

    @Override
    public float getFloat(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return 0;
        }

        var sizeData = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());

        if (sizeData == null) {
            return 1.0F;
        }

        if (this.sizeChangeType.isPresent()) {
            ResourceKey<SizeChangeType> key = this.sizeChangeType.get().unwrapKey().orElseThrow();
            var entry = sizeData.getModifiers().get(key);
            return entry != null ? entry.getCurrentValue() : 1.0F;
        }

        return sizeData.getCurrentSize();
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.SIZE.get();
    }

    public static class Serializer extends FloatSerializer<SizeValue> {

        @Override
        public MapCodec<SizeValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, SizeValue> builder, HolderLookup.Provider provider) {
            builder.setName("Size")
                    .setDescription("Returns the entity's current size. If a size change type is specified, returns that type's specific modifier value instead of the overall size.")
                    .addOptional("size_change_type", TYPE_IDENTIFIER, "The size change type whose modifier to return. If omitted, the overall current size is returned.")
                    .addExampleObject(new SizeValue(Optional.empty(), ""))
                    .addExampleObject(new SizeValue(Optional.of(provider.holderOrThrow(PalladiumSizeChangeTypes.DEFAULT)), ""));
        }
    }
}
