package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;

public record HasEffectCondition(PalladiumHolderSet<MobEffect> mobEffect) implements Condition {

    public static final MapCodec<HasEffectCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(PalladiumHolderSet.codec(Registries.MOB_EFFECT).fieldOf("effect").forGetter(HasEffectCondition::mobEffect)
            ).apply(instance, HasEffectCondition::new)
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity != null) {
            for (Holder<MobEffect> effectHolder : this.mobEffect.resolve(entity.registryAccess())) {
                if (entity.hasEffect(effectHolder)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ConditionSerializer<HasEffectCondition> getSerializer() {
        return ConditionSerializers.HAS_EFFECT.get();
    }

    public static class Serializer extends ConditionSerializer<HasEffectCondition> {

        @Override
        public MapCodec<HasEffectCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, HasEffectCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Has Effect")
                    .setDescription("Checks if the entity has a (potion) effect.")
                    .add("effect", TYPE_MOB_EFFECT_TYPE_HOLDER_SET, "IDs or tags of the required mob/potion effect")
                    .addExampleObject(new HasEffectCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("slowness")))))))
                    .addExampleObject(new HasEffectCondition(PalladiumHolderSet.direct(HolderSet.direct(
                            provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("slowness"))),
                            provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("mining_fatigue"))
                    )))));
        }
    }
}
