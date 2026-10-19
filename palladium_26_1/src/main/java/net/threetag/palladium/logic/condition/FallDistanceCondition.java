package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public record FallDistanceCondition(float min, float max) implements Condition {

    public static final MapCodec<FallDistanceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("min", 0F).forGetter(FallDistanceCondition::min),
                    ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("max", Float.MAX_VALUE).forGetter(FallDistanceCondition::max)
            ).apply(instance, FallDistanceCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FallDistanceCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FallDistanceCondition::min,
            ByteBufCodecs.FLOAT, FallDistanceCondition::max,
            FallDistanceCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();

        if (entity == null) {
            return false;
        }

        return entity.fallDistance >= this.min && entity.fallDistance <= this.max;
    }

    @Override
    public ConditionSerializer<FallDistanceCondition> getSerializer() {
        return ConditionSerializers.FALL_DISTANCE.get();
    }

    public static class Serializer extends ConditionSerializer<FallDistanceCondition> {

        @Override
        public MapCodec<FallDistanceCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, FallDistanceCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Fall Distance")
                    .setDescription("Checks how far the entity has fallen since it last touched the ground. Mostly reliable on the server side.")
                    .addOptional("min", TYPE_NON_NEGATIVE_FLOAT, "The minimum distance in blocks.", 0F)
                    .addOptional("max", TYPE_NON_NEGATIVE_FLOAT, "The maximum distance in blocks.")
                    .addExampleObject(new FallDistanceCondition(3F, Float.MAX_VALUE));
        }
    }
}
