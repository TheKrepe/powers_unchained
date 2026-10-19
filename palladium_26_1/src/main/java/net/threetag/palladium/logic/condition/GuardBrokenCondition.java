package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.GuardAbility;

public record GuardBrokenCondition(AbilityReference ability) implements Condition {

    public static final MapCodec<GuardBrokenCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(AbilityReference.CODEC.fieldOf("ability").forGetter(GuardBrokenCondition::ability)
            ).apply(instance, GuardBrokenCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, GuardBrokenCondition> STREAM_CODEC = StreamCodec.composite(
            AbilityReference.STREAM_CODEC, GuardBrokenCondition::ability, GuardBrokenCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        AbilityInstance<?> dependency = this.ability.getInstance(entity, context.getPowerInstance());

        return dependency != null
                && dependency.getAbility() instanceof GuardAbility
                && GuardAbility.isLockedFromBreak(dependency);
    }

    @Override
    public ConditionSerializer<GuardBrokenCondition> getSerializer() {
        return ConditionSerializers.GUARD_BROKEN.get();
    }

    public static class Serializer extends ConditionSerializer<GuardBrokenCondition> {

        @Override
        public MapCodec<GuardBrokenCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, GuardBrokenCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Guard Broken")
                    .setDescription("Checks if the referenced guard ability is currently within its post-break lockout window.")
                    .add("ability", TYPE_ABILITY_REFERENCE, "The guard ability to check.")
                    .addExampleObject(new GuardBrokenCondition(new AbilityReference(
                            Identifier.fromNamespaceAndPath("example", "power"),
                            "guard"
                    )));
        }
    }
}
