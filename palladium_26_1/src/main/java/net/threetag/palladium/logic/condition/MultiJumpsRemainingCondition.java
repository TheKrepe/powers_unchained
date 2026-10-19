package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.MultiJumpAbility;

public record MultiJumpsRemainingCondition(AbilityReference ability, int min, int max) implements Condition {

    public static final MapCodec<MultiJumpsRemainingCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    AbilityReference.CODEC.fieldOf("ability").forGetter(MultiJumpsRemainingCondition::ability),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("min", 0).forGetter(MultiJumpsRemainingCondition::min),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("max", Integer.MAX_VALUE).forGetter(MultiJumpsRemainingCondition::max)
            ).apply(instance, MultiJumpsRemainingCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MultiJumpsRemainingCondition> STREAM_CODEC = StreamCodec.composite(
            AbilityReference.STREAM_CODEC, MultiJumpsRemainingCondition::ability,
            ByteBufCodecs.VAR_INT, MultiJumpsRemainingCondition::min,
            ByteBufCodecs.VAR_INT, MultiJumpsRemainingCondition::max,
            MultiJumpsRemainingCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        var instance = this.ability.getInstance(entity, context.getPowerInstance());

        if (instance == null || !(instance.getAbility() instanceof MultiJumpAbility)) {
            return false;
        }

        int remaining = MultiJumpAbility.getRemainingJumps(instance);
        return remaining >= this.min && remaining <= this.max;
    }

    @Override
    public ConditionSerializer<MultiJumpsRemainingCondition> getSerializer() {
        return ConditionSerializers.MULTI_JUMPS_REMAINING.get();
    }

    public static class Serializer extends ConditionSerializer<MultiJumpsRemainingCondition> {

        @Override
        public MapCodec<MultiJumpsRemainingCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, MultiJumpsRemainingCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Multi Jumps Remaining")
                    .setDescription("Checks how many extra mid-air jumps a multi jump ability has left before the entity needs to land again. Only available on the server side.")
                    .add("ability", TYPE_ABILITY_REFERENCE, "The multi jump ability to check.")
                    .addOptional("min", TYPE_NON_NEGATIVE_INT, "The minimum amount of remaining jumps.", 0)
                    .addOptional("max", TYPE_NON_NEGATIVE_INT, "The maximum amount of remaining jumps.")
                    .addExampleObject(new MultiJumpsRemainingCondition(new AbilityReference(Identifier.fromNamespaceAndPath("example", "power"), "multi_jump"), 0, 0));
        }
    }
}
