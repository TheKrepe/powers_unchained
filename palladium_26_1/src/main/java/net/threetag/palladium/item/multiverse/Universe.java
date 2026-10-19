package net.threetag.palladium.item.multiverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record Universe(int weight, Optional<Component> description, List<ConditionalWeight> conditionalWeights) {

    public static final Codec<Universe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("weight", 50).forGetter(Universe::weight),
            ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(Universe::description),
            ConditionalWeight.CODEC.listOf().optionalFieldOf("conditional_weights", List.of()).forGetter(Universe::conditionalWeights)
    ).apply(instance, Universe::new));

    public Universe() {
        this(50, Optional.empty(), Collections.emptyList());
    }

    public Universe(int weight) {
        this(weight, Optional.empty(), Collections.emptyList());
    }

    public Universe(int weight, Component description) {
        this(weight, Optional.of(description), Collections.emptyList());
    }

    public int getWeight(DataContext context) {
        for (ConditionalWeight conditionalWeight : this.conditionalWeights) {
            if (conditionalWeight.condition().test(context)) {
                return conditionalWeight.weight();
            }
        }
        return this.weight;
    }

    @Nullable
    public static ResourceKey<Universe> getWeightedRandom(RegistryAccess registryAccess, DataContext context, RandomSource random) {
        var universes = registryAccess.lookup(PalladiumRegistryKeys.MULTIVERSE)
                .map(lookup -> lookup.listElements().toList())
                .orElseGet(List::of);
        int totalWeight = 0;
        for (Holder.Reference<Universe> universe : universes) {
            totalWeight += universe.value().getWeight(context);
        }

        if (totalWeight <= 0) {
            return null;
        }

        int pick = random.nextInt(totalWeight);
        for (Holder.Reference<Universe> universe : universes) {
            pick -= universe.value().getWeight(context);
            if (pick < 0) {
                return universe.key();
            }
        }

        return null;
    }

    public static String makeTranslationKey(Universe universe, RegistryAccess registryAccess) {
        return makeTranslationKey(registryAccess.lookupOrThrow(PalladiumRegistryKeys.MULTIVERSE).getKey(universe));
    }

    public static String makeTranslationKey(ResourceKey<Universe> key) {
        return makeTranslationKey(key.identifier());
    }

    public static String makeTranslationKey(Identifier id) {
        return Util.makeDescriptionId(Palladium.MOD_ID + ".universe", id);
    }

    public static String makeDescTranslationKey(ResourceKey<Universe> key) {
        return makeDescTranslationKey(key.identifier());
    }

    public static String makeDescTranslationKey(Identifier id) {
        return Util.makeDescriptionId(Palladium.MOD_ID + ".universe", id) + ".desc";
    }

    public static Component getTitle(Identifier id) {
        return Component.translatable(makeTranslationKey(id));
    }

    public static Component getGenericUniverseComponent(String universe) {
        return Component.translatable(makeTranslationKey(Palladium.id("generic")), universe);
    }

    public record ConditionalWeight(int weight, Condition condition) {

        public static final Codec<ConditionalWeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(ConditionalWeight::weight),
                Condition.CODEC.fieldOf("if").forGetter(ConditionalWeight::condition)
        ).apply(instance, ConditionalWeight::new));

    }

}
