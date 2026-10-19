package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.ComboAttackAbility;

import java.util.Optional;

public record ComboAttackReadyCondition(Optional<AbilityReference> ability) implements Condition {

    public static final MapCodec<ComboAttackReadyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(AbilityReference.CODEC.optionalFieldOf("ability").forGetter(ComboAttackReadyCondition::ability))
            .apply(instance, ComboAttackReadyCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ComboAttackReadyCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(AbilityReference.STREAM_CODEC), ComboAttackReadyCondition::ability,
            ComboAttackReadyCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        AbilityInstance<?> instance = this.ability.isPresent()
                ? this.ability.get().getInstance(entity, context.getPowerInstance())
                : ComboAttackAbility.getTopPriorityInstance(entity);

        return instance != null && ComboAttackAbility.isFinalAttackReady(instance);
    }

    @Override
    public ConditionSerializer<ComboAttackReadyCondition> getSerializer() {
        return ConditionSerializers.COMBO_ATTACK_READY.get();
    }

    public static class Serializer extends ConditionSerializer<ComboAttackReadyCondition> {

        @Override
        public MapCodec<ComboAttackReadyCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, ComboAttackReadyCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Combo Attack Ready")
                    .setDescription("Checks if the next melee attack will be the final, bonus-damage attack of a combo attack ability.")
                    .addOptional("ability", TYPE_ABILITY_REFERENCE, "The combo attack ability to check. If not specified, the highest-priority combo attack ability on the entity is used.")
                    .addExampleObject(new ComboAttackReadyCondition(Optional.empty()));
        }
    }
}
