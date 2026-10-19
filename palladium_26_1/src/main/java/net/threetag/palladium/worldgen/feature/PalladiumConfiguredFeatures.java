package net.threetag.palladium.worldgen.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseThresholdProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.block.PalladiumBlocks;

import java.util.List;

public class PalladiumConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> METEORITE_STONE_PILLAR = create("meteorite_stone_pillar");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_BLACKSTONE = create("ore_blackstone");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_VIBRANIUM_VEIN_SMALL = create("ore_vibranium_vein_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_VIBRANIUM_VEIN_BIG = create("ore_vibranium_vein_big");

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_COAL = create("ore_coal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_COAL_BURIED = create("ore_coal_buried");

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_IRON = create("ore_iron");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_IRON_SMALL = create("ore_iron_small");

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_LEAD = create("ore_lead");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_LEAD_SMALL = create("ore_lead_small");

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_TITANIUM = create("ore_titanium");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_TITANIUM_SMALL = create("ore_titanium_small");

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_VIBRANIUM_SMALL = create("ore_vibranium_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_VIBRANIUM_MEDIUM = create("ore_vibranium_medium");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_VIBRANIUM_LARGE = create("ore_vibranium_large");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_VIBRANIUM_BURIED = create("ore_vibranium_buried");

    public static final ResourceKey<ConfiguredFeature<?, ?>> HEART_SHAPED_HERB_MEADOW_VEGETATION = create("heart_shaped_herb_meadow_vegetation");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HEART_SHAPED_HERB_MEADOW = create("heart_shaped_herb_meadow");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HEART_SHAPED_HERB = create("heart_shaped_herb");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneOreReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateOreReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest meteoriteTest = new BlockMatchTest(PalladiumBlocks.METEORITE_STONE.get());

        List<OreConfiguration.TargetBlockState> blackstoneReplace = List.of(OreConfiguration.target(meteoriteTest, Blocks.BLACKSTONE.defaultBlockState()));
        List<OreConfiguration.TargetBlockState> coalReplace = List.of(OreConfiguration.target(meteoriteTest, PalladiumBlocks.METEORITE_COAL_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> ironReplace = List.of(OreConfiguration.target(meteoriteTest, PalladiumBlocks.METEORITE_IRON_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> leadReplace = List.of(
                OreConfiguration.target(stoneOreReplaceables, PalladiumBlocks.LEAD_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateOreReplaceables, PalladiumBlocks.DEEPSLATE_LEAD_ORE.get().defaultBlockState()),
                OreConfiguration.target(meteoriteTest, PalladiumBlocks.METEORITE_LEAD_ORE.get().defaultBlockState())
        );
        List<OreConfiguration.TargetBlockState> titaniumReplace = List.of(OreConfiguration.target(meteoriteTest, PalladiumBlocks.METEORITE_TITANIUM_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> vibraniumReplace = List.of(OreConfiguration.target(meteoriteTest, PalladiumBlocks.METEORITE_VIBRANIUM_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> vibraniumVeinReplace = List.of(OreConfiguration.target(meteoriteTest, PalladiumBlocks.METEORITE_VIBRANIUM_VEIN.get().defaultBlockState()));

        FeatureUtils.register(context, METEORITE_STONE_PILLAR, PalladiumFeatures.METEORITE_STONE_PILLAR.get());
        FeatureUtils.register(context, ORE_BLACKSTONE, Feature.ORE, new OreConfiguration(blackstoneReplace, 64));
        FeatureUtils.register(context, ORE_VIBRANIUM_VEIN_SMALL, Feature.ORE, new OreConfiguration(vibraniumVeinReplace, 6));
        FeatureUtils.register(context, ORE_VIBRANIUM_VEIN_BIG, Feature.ORE, new OreConfiguration(vibraniumVeinReplace, 46));

        FeatureUtils.register(context, ORE_COAL, Feature.ORE, new OreConfiguration(coalReplace, 17));
        FeatureUtils.register(context, ORE_COAL_BURIED, Feature.ORE, new OreConfiguration(coalReplace, 17, 0.5F));

        FeatureUtils.register(context, ORE_IRON, Feature.ORE, new OreConfiguration(ironReplace, 9));
        FeatureUtils.register(context, ORE_IRON_SMALL, Feature.ORE, new OreConfiguration(ironReplace, 4));

        FeatureUtils.register(context, ORE_LEAD, Feature.ORE, new OreConfiguration(leadReplace, 7));
        FeatureUtils.register(context, ORE_LEAD_SMALL, Feature.ORE, new OreConfiguration(leadReplace, 3));

        FeatureUtils.register(context, ORE_TITANIUM, Feature.ORE, new OreConfiguration(titaniumReplace, 4));
        FeatureUtils.register(context, ORE_TITANIUM_SMALL, Feature.ORE, new OreConfiguration(titaniumReplace, 2));

        FeatureUtils.register(context, ORE_VIBRANIUM_SMALL, Feature.ORE, new OreConfiguration(vibraniumReplace, 4, 0.5F));
        FeatureUtils.register(context, ORE_VIBRANIUM_LARGE, Feature.ORE, new OreConfiguration(vibraniumReplace, 12, 0.7F));
        FeatureUtils.register(context, ORE_VIBRANIUM_BURIED, Feature.ORE, new OreConfiguration(vibraniumReplace, 8, 1.0F));
        FeatureUtils.register(context, ORE_VIBRANIUM_MEDIUM, Feature.ORE, new OreConfiguration(vibraniumReplace, 8, 0.5F));

        FeatureUtils.register(context, HEART_SHAPED_HERB_MEADOW_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(
                List.of(
                        new WeightedPlacedFeature(PlacementUtils.inlinePlaced(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.TALL_GRASS))), 0.06F),
                        new WeightedPlacedFeature(PlacementUtils.inlinePlaced(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LARGE_FERN))), 0.04F)
                ),
                PlacementUtils.inlinePlaced(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new NoiseThresholdProvider(
                        5791L,
                        new NormalNoise.NoiseParameters(0, 1.0),
                        0.12F,
                        0.35F,
                        0.35F,
                        Blocks.SHORT_GRASS.defaultBlockState(),
                        List.of(
                                Blocks.SHORT_GRASS.defaultBlockState(),
                                Blocks.SHORT_GRASS.defaultBlockState(),
                                Blocks.SHORT_GRASS.defaultBlockState(),
                                Blocks.FERN.defaultBlockState(),
                                Blocks.MOSS_CARPET.defaultBlockState()
                        ),
                        List.of(PalladiumBlocks.HEART_SHAPED_HERB.get().defaultBlockState())
                )))
        ));
        FeatureUtils.register(context, HEART_SHAPED_HERB_MEADOW, Feature.VEGETATION_PATCH, new VegetationPatchConfiguration(
                BlockTags.MOSS_REPLACEABLE,
                new WeightedStateProvider(WeightedList.<BlockState>builder()
                        .add(Blocks.GRASS_BLOCK.defaultBlockState(), 7)
                        .add(Blocks.MOSS_BLOCK.defaultBlockState(), 3)),
                PlacementUtils.inlinePlaced(context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(HEART_SHAPED_HERB_MEADOW_VEGETATION)),
                CaveSurface.FLOOR,
                ConstantInt.of(1),
                0.0F,
                5,
                0.75F,
                UniformInt.of(3, 6),
                0.35F
        ));
        FeatureUtils.register(context, HEART_SHAPED_HERB, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(PalladiumBlocks.HEART_SHAPED_HERB.get())));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> create(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Palladium.id(name));
    }

}
