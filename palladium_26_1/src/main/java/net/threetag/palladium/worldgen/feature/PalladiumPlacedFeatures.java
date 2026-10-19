package net.threetag.palladium.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.block.PalladiumBlocks;

import java.util.List;

public class PalladiumPlacedFeatures {

    public static final ResourceKey<PlacedFeature> METEORITE_STONE_PILLAR = create("meteorite_stone_pillar");
    public static final ResourceKey<PlacedFeature> ORE_BLACKSTONE = create("ore_blackstone");
    public static final ResourceKey<PlacedFeature> ORE_VIBRANIUM_VEIN_SMALL = create("ore_vibranium_vein_small");
    public static final ResourceKey<PlacedFeature> ORE_VIBRANIUM_VEIN_BIG = create("ore_vibranium_vein_big");

    public static final ResourceKey<PlacedFeature> ORE_COAL_UPPER = create("ore_coal_upper");
    public static final ResourceKey<PlacedFeature> ORE_COAL_LOWER = create("ore_coal_lower");

    public static final ResourceKey<PlacedFeature> ORE_IRON_UPPER = create("ore_iron_upper");
    public static final ResourceKey<PlacedFeature> ORE_IRON_MIDDLE = create("ore_iron_middle");
    public static final ResourceKey<PlacedFeature> ORE_IRON_SMALL = create("ore_iron_small");

    public static final ResourceKey<PlacedFeature> ORE_LEAD_UPPER = create("ore_lead_upper");
    public static final ResourceKey<PlacedFeature> ORE_LEAD_MIDDLE = create("ore_lead_middle");
    public static final ResourceKey<PlacedFeature> ORE_LEAD_SMALL = create("ore_lead_small");

    public static final ResourceKey<PlacedFeature> ORE_TITANIUM_UPPER = create("ore_titanium_upper");
    public static final ResourceKey<PlacedFeature> ORE_TITANIUM_MIDDLE = create("ore_titanium_middle");
    public static final ResourceKey<PlacedFeature> ORE_TITANIUM_SMALL = create("ore_titanium_small");

    public static final ResourceKey<PlacedFeature> ORE_VIBRANIUM = create("ore_vibranium");
    public static final ResourceKey<PlacedFeature> ORE_VIBRANIUM_MEDIUM = create("ore_vibranium_medium");
    public static final ResourceKey<PlacedFeature> ORE_VIBRANIUM_LARGE = create("ore_vibranium_large");
    public static final ResourceKey<PlacedFeature> ORE_VIBRANIUM_BURIED = create("ore_vibranium_buried");

    public static final ResourceKey<PlacedFeature> HEART_SHAPED_HERB_MEADOW = create("heart_shaped_herb_meadow");
    public static final ResourceKey<PlacedFeature> HEART_SHAPED_HERB_SURFACE = create("heart_shaped_herb_surface");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        PlacementUtils.register(
                context, METEORITE_STONE_PILLAR, holdergetter.getOrThrow(PalladiumConfiguredFeatures.METEORITE_STONE_PILLAR), CountPlacement.of(40), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
        );
        PlacementUtils.register(
                context, ORE_BLACKSTONE, holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_BLACKSTONE), commonOrePlacement(20, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top()))
        );
        PlacementUtils.register(
                context, ORE_VIBRANIUM_VEIN_SMALL, holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_VIBRANIUM_VEIN_SMALL), commonOrePlacement(96, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top()))
        );
        PlacementUtils.register(
                context, ORE_VIBRANIUM_VEIN_BIG, holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_VIBRANIUM_VEIN_BIG), commonOrePlacement(24, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top()))
        );

        PlacementUtils.register(
                context, ORE_COAL_UPPER, holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_COAL), commonOrePlacement(30, HeightRangePlacement.uniform(VerticalAnchor.absolute(136), VerticalAnchor.top()))
        );
        PlacementUtils.register(
                context,
                ORE_COAL_LOWER,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_COAL_BURIED),
                commonOrePlacement(20, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192)))
        );

        PlacementUtils.register(
                context,
                ORE_IRON_UPPER,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_IRON),
                commonOrePlacement(90, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384)))
        );
        PlacementUtils.register(
                context,
                ORE_IRON_MIDDLE,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_IRON),
                commonOrePlacement(10, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56)))
        );
        PlacementUtils.register(
                context, ORE_IRON_SMALL, holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_IRON_SMALL), commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72)))
        );

        PlacementUtils.register(
                context,
                ORE_LEAD_UPPER,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_LEAD),
                commonOrePlacement(65, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384)))
        );
        PlacementUtils.register(
                context,
                ORE_LEAD_MIDDLE,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_LEAD),
                commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56)))
        );
        PlacementUtils.register(
                context, ORE_LEAD_SMALL, holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_LEAD_SMALL), commonOrePlacement(7, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72)))
        );

        PlacementUtils.register(
                context,
                ORE_TITANIUM_UPPER,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_TITANIUM),
                commonOrePlacement(65, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384)))
        );
        PlacementUtils.register(
                context,
                ORE_TITANIUM_MIDDLE,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_TITANIUM),
                commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56)))
        );
        PlacementUtils.register(
                context, ORE_TITANIUM_SMALL, holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_TITANIUM_SMALL), commonOrePlacement(7, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72)))
        );

        PlacementUtils.register(
                context,
                ORE_VIBRANIUM,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_VIBRANIUM_SMALL),
                commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)))
        );
        PlacementUtils.register(
                context,
                ORE_VIBRANIUM_MEDIUM,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_VIBRANIUM_MEDIUM),
                commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-4)))
        );
        PlacementUtils.register(
                context,
                ORE_VIBRANIUM_LARGE,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_VIBRANIUM_LARGE),
                rareOrePlacement(9, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)))
        );
        PlacementUtils.register(
                context,
                ORE_VIBRANIUM_BURIED,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.ORE_VIBRANIUM_BURIED),
                commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)))
        );

        PlacementUtils.register(
                context,
                HEART_SHAPED_HERB_MEADOW,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.HEART_SHAPED_HERB_MEADOW),
                CountPlacement.of(20),
                InSquarePlacement.spread(),
                PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(1)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                context,
                HEART_SHAPED_HERB_SURFACE,
                holdergetter.getOrThrow(PalladiumConfiguredFeatures.HEART_SHAPED_HERB),
                CountPlacement.of(UniformInt.of(1, 2)),
                InSquarePlacement.spread(),
                PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
                EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                BiomeFilter.biome(),
                PlacementUtils.HEIGHTMAP_NO_LEAVES,
                CountPlacement.of(UniformInt.of(1, 4)),
                RandomOffsetPlacement.ofTriangle(3, 1),
                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                        BlockPredicate.wouldSurvive(PalladiumBlocks.HEART_SHAPED_HERB.get().defaultBlockState(), BlockPos.ZERO)
                ))
        );
    }

    public static ResourceKey<PlacedFeature> create(String key) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Palladium.id(key));
    }

    private static List<PlacementModifier> commonOrePlacement(int count, PlacementModifier heightRange) {
        return orePlacement(CountPlacement.of(count), heightRange);
    }

    private static List<PlacementModifier> rareOrePlacement(int chance, PlacementModifier heightRange) {
        return orePlacement(RarityFilter.onAverageOnceEvery(chance), heightRange);
    }

    private static List<PlacementModifier> orePlacement(PlacementModifier countPlacement, PlacementModifier heightRange) {
        return List.of(countPlacement, InSquarePlacement.spread(), heightRange, BiomeFilter.biome());
    }
}
