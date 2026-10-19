package net.threetag.palladium.worldgen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public record FeatureFlagBiomeModifier(
        HolderSet<Biome> biomes,
        HolderSet<PlacedFeature> features,
        GenerationStep.Decoration step,
        FeatureFlagSet requiredFlags
) implements BiomeModifier {

    public static final MapCodec<FeatureFlagBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(FeatureFlagBiomeModifier::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(FeatureFlagBiomeModifier::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(FeatureFlagBiomeModifier::step),
            FeatureFlags.CODEC.fieldOf("flags").forGetter(FeatureFlagBiomeModifier::requiredFlags)
    ).apply(instance, FeatureFlagBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || !this.requiredFlags.isSubsetOf(server.getWorldData().enabledFeatures())) {
            return;
        }

        if (this.biomes.contains(biome)) {
            this.features.forEach(f -> builder.getGenerationSettings().addFeature(this.step, f));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return PalladiumBiomeModifiers.FEATURE_FLAG_ADD_FEATURES.get();
    }
}
