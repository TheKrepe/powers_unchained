package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.ability.MultiJumpAbility;

import java.util.Optional;

public record MultiJumpingCondition(Optional<AbilityReference> ability) implements Condition {

    public static final MapCodec<MultiJumpingCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(AbilityReference.CODEC.optionalFieldOf("ability").forGetter(MultiJumpingCondition::ability))
            .apply(instance, MultiJumpingCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MultiJumpingCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(AbilityReference.STREAM_CODEC), MultiJumpingCondition::ability,
            MultiJumpingCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        if (this.ability.isPresent()) {
            var instance = this.ability.get().getInstance(entity, context.getPowerInstance());
            return instance != null && MultiJumpAbility.isMultiJumping(instance);
        }

        return AbilityUtil.getInstances(entity, AbilitySerializers.MULTI_JUMP.get()).stream().anyMatch(MultiJumpAbility::isMultiJumping);
    }

    @Override
    public ConditionSerializer<MultiJumpingCondition> getSerializer() {
        return ConditionSerializers.MULTI_JUMPING.get();
    }

    public static class Serializer extends ConditionSerializer<MultiJumpingCondition> {

        @Override
        public MapCodec<MultiJumpingCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, MultiJumpingCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Multi Jumping")
                    .setDescription("Checks if the entity just performed an extra mid-air jump with a multi jump ability. Stays true for " + MultiJumpAbility.MULTI_JUMP_TIME + " ticks afterwards, so it can be used to trigger animations.")
                    .addOptional("ability", TYPE_ABILITY_REFERENCE, "The multi jump ability to check. If not specified, any multi jump ability on the entity is checked.")
                    .addExampleObject(new MultiJumpingCondition(Optional.empty()));
        }
    }
}
