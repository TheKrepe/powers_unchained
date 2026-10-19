package net.threetag.palladium.worldgen.biome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.flag.PalladiumFeatureFlags;
import net.threetag.palladium.tag.PalladiumBiomeTags;
import net.threetag.palladium.tag.PalladiumPlacedFeatureTags;

public class PalladiumBiomeModifiers {

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Palladium.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<FeatureFlagBiomeModifier>> FEATURE_FLAG_ADD_FEATURES =
            BIOME_MODIFIER_SERIALIZERS.register("feature_flag_add_features", () -> FeatureFlagBiomeModifier.CODEC);

    private static final ResourceKey<BiomeModifier> LEAD_ORES = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Palladium.id("lead_ores"));
    private static final ResourceKey<BiomeModifier> TITANIUM_ORES = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Palladium.id("titanium_ores"));
    private static final ResourceKey<BiomeModifier> VIBRANIUM_ORES = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Palladium.id("vibranium_ores"));
    private static final ResourceKey<BiomeModifier> VIBRANIUM_VEINS = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Palladium.id("vibranium_veins"));
    private static final ResourceKey<BiomeModifier> HEART_SHAPED_HERBS = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Palladium.id("heart_shaped_herbs"));

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        context.register(LEAD_ORES, new FeatureFlagBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                placedFeatures.getOrThrow(PalladiumPlacedFeatureTags.LEAD_ORES),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                FeatureFlagSet.of(PalladiumFeatureFlags.MATERIAL_LEAD)
        ));

        context.register(TITANIUM_ORES, new FeatureFlagBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                placedFeatures.getOrThrow(PalladiumPlacedFeatureTags.TITANIUM_ORES),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                FeatureFlagSet.of(PalladiumFeatureFlags.MATERIAL_TITANIUM)
        ));

        context.register(VIBRANIUM_ORES, new FeatureFlagBiomeModifier(
                biomes.getOrThrow(PalladiumBiomeTags.CONTAINS_VIBRANIUM_ORE),
                placedFeatures.getOrThrow(PalladiumPlacedFeatureTags.VIBRANIUM_ORES),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                FeatureFlagSet.of(PalladiumFeatureFlags.MATERIAL_VIBRANIUM)
        ));

        context.register(VIBRANIUM_VEINS, new FeatureFlagBiomeModifier(
                biomes.getOrThrow(PalladiumBiomeTags.CONTAINS_VIBRANIUM_VEINS),
                placedFeatures.getOrThrow(PalladiumPlacedFeatureTags.VIBRANIUM_VEINS),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                FeatureFlagSet.of(PalladiumFeatureFlags.MATERIAL_VIBRANIUM)
        ));

        context.register(HEART_SHAPED_HERBS, new FeatureFlagBiomeModifier(
                biomes.getOrThrow(PalladiumBiomeTags.CONTAINS_HEART_SHAPED_HERBS),
                placedFeatures.getOrThrow(PalladiumPlacedFeatureTags.HEART_SHAPED_HERBS),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                FeatureFlagSet.of(PalladiumFeatureFlags.WORLDGEN_METEORITE, PalladiumFeatureFlags.BLOCK_HEART_SHAPED_HERB)
        ));
    }
}
