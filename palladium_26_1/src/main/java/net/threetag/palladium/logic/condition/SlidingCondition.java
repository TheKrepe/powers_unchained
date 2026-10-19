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
import net.threetag.palladium.power.ability.SlideAbility;

public record SlidingCondition(AbilityReference ability) implements Condition {

    public static final MapCodec<SlidingCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(AbilityReference.CODEC.fieldOf("ability").forGetter(SlidingCondition::ability)
            ).apply(instance, SlidingCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SlidingCondition> STREAM_CODEC = StreamCodec.composite(
            AbilityReference.STREAM_CODEC, SlidingCondition::ability, SlidingCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();
        var holder = context.getPowerInstance();

        if (entity == null) {
            return false;
        }

        AbilityInstance<?> dependency = this.ability.getInstance(entity, holder);

        return dependency != null
                && dependency.getAbility() instanceof SlideAbility
                && SlideAbility.isSliding(dependency);
    }

    @Override
    public ConditionSerializer<SlidingCondition> getSerializer() {
        return ConditionSerializers.SLIDING.get();
    }

    public static class Serializer extends ConditionSerializer<SlidingCondition> {

        @Override
        public MapCodec<SlidingCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, SlidingCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Sliding")
                    .setDescription("Checks if the referenced slide ability is actively sliding right now.")
                    .add("ability", TYPE_ABILITY_REFERENCE, "The slide ability to check.")
                    .addExampleObject(new SlidingCondition(new AbilityReference(
                            Identifier.fromNamespaceAndPath("example", "power"),
                            "slide"
                    )));
        }
    }
}
