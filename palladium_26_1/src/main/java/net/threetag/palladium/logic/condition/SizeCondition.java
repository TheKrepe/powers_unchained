package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.sizechange.PalladiumSizeChangeTypes;
import net.threetag.palladium.entity.sizechange.SizeChangeType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.Optional;

public record SizeCondition(Optional<Holder<SizeChangeType>> sizeChangeType, float min, float max) implements Condition {

    public static final MapCodec<SizeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    net.minecraft.resources.RegistryFixedCodec.create(PalladiumRegistryKeys.SIZE_CHANGE_TYPE).optionalFieldOf("size_change_type").forGetter(SizeCondition::sizeChangeType),
                    ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("min", Float.MIN_VALUE).forGetter(SizeCondition::min),
                    ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("max", Float.MAX_VALUE).forGetter(SizeCondition::max)
            ).apply(instance, SizeCondition::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SizeCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(PalladiumRegistryKeys.SIZE_CHANGE_TYPE)),
            SizeCondition::sizeChangeType,
            ByteBufCodecs.FLOAT,
            SizeCondition::min,
            ByteBufCodecs.FLOAT,
            SizeCondition::max,
            SizeCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        var sizeData = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());

        if (sizeData == null) {
            return false;
        }

        float value;

        if (this.sizeChangeType.isPresent()) {
            var key = this.sizeChangeType.get().unwrapKey().orElseThrow();
            var entry = sizeData.getModifiers().get(key);
            value = entry != null ? entry.getCurrentValue() : 1.0F;
        } else {
            value = sizeData.getCurrentSize();
        }

        return value >= this.min && value <= this.max;
    }

    @Override
    public ConditionSerializer<SizeCondition> getSerializer() {
        return ConditionSerializers.SIZE.get();
    }

    public static class Serializer extends ConditionSerializer<SizeCondition> {

        @Override
        public MapCodec<SizeCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, SizeCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Size")
                    .setDescription("Checks if the entity's size is within the given range. If a size change type is specified, checks that type's specific modifier value instead of the overall size.")
                    .addOptional("size_change_type", TYPE_IDENTIFIER, "The size change type whose modifier to check. If omitted, the overall current size is checked.")
                    .addOptional("min", TYPE_NON_NEGATIVE_FLOAT, "The minimum required size (inclusive).")
                    .addOptional("max", TYPE_NON_NEGATIVE_FLOAT, "The maximum required size (inclusive).")
                    .addExampleObject(new SizeCondition(Optional.empty(), 1.5F, Float.MAX_VALUE))
                    .addExampleObject(new SizeCondition(Optional.of(provider.holderOrThrow(PalladiumSizeChangeTypes.DEFAULT)), 0.5F, 1.0F));
        }
    }
}
