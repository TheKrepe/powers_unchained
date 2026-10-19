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

public record ComboAttackFinalCondition(Optional<AbilityReference> ability) implements Condition {

    public static final MapCodec<ComboAttackFinalCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(AbilityReference.CODEC.optionalFieldOf("ability").forGetter(ComboAttackFinalCondition::ability))
            .apply(instance, ComboAttackFinalCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ComboAttackFinalCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(AbilityReference.STREAM_CODEC), ComboAttackFinalCondition::ability,
            ComboAttackFinalCondition::new
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

        return instance != null && ComboAttackAbility.isFinalAttackHappening(instance);
    }

    @Override
    public ConditionSerializer<ComboAttackFinalCondition> getSerializer() {
        return ConditionSerializers.COMBO_ATTACK_FINAL.get();
    }

    public static class Serializer extends ConditionSerializer<ComboAttackFinalCondition> {

        @Override
        public MapCodec<ComboAttackFinalCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, ComboAttackFinalCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Combo Attack Final")
                    .setDescription("Checks if the final, bonus-damage attack of a combo attack ability just landed. Stays true for " + ComboAttackAbility.FINAL_ATTACK_TIME + " ticks afterwards, so it can be caught by things that only run once per tick.")
                    .addOptional("ability", TYPE_ABILITY_REFERENCE, "The combo attack ability to check. If not specified, the highest-priority combo attack ability on the entity is used.")
                    .addExampleObject(new ComboAttackFinalCondition(Optional.empty()));
        }
    }
}
