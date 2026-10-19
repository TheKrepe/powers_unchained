package net.threetag.palladium.flag;

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.common.conditions.FeatureFlagsEnabledCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.threetag.palladium.Palladium;

public class PalladiumFeatureFlags {

    public static final FeatureFlag MECHANIC_TAILORING = FeatureFlags.REGISTRY.getFlag(Palladium.id("mechanic/tailoring"));
    public static final FeatureFlag MECHANIC_MULTIVERSAL_VARIANTS = FeatureFlags.REGISTRY.getFlag(Palladium.id("mechanic/multiversal_variants"));
    public static final FeatureFlag CUSTOMIZATION_EYE_SELECTION = FeatureFlags.REGISTRY.getFlag(Palladium.id("customization/eye_selection"));

    public static final FeatureFlag MATERIAL_CIRCUITS = FeatureFlags.REGISTRY.getFlag(Palladium.id("material/circuits"));
    public static final FeatureFlag MATERIAL_LEAD = FeatureFlags.REGISTRY.getFlag(Palladium.id("material/lead"));
    public static final FeatureFlag MATERIAL_TITANIUM = FeatureFlags.REGISTRY.getFlag(Palladium.id("material/titanium"));
    public static final FeatureFlag MATERIAL_VIBRANIUM = FeatureFlags.REGISTRY.getFlag(Palladium.id("material/vibranium"));

    public static final FeatureFlag WORLDGEN_METEORITE = FeatureFlags.REGISTRY.getFlag(Palladium.id("worldgen/meteorite_biome"));

    public static final FeatureFlag ITEM_VIALS = FeatureFlags.REGISTRY.getFlag(Palladium.id("item/vials"));

    public static final FeatureFlag BLOCK_HEART_SHAPED_HERB = FeatureFlags.REGISTRY.getFlag(Palladium.id("block/heart_shaped_herb"));

    public static ICondition conditionTailoring() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(MECHANIC_TAILORING));
    }

    public static ICondition conditionMultiverse() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(MECHANIC_MULTIVERSAL_VARIANTS));
    }

    public static ICondition conditionCircuits() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(MATERIAL_CIRCUITS));
    }

    public static ICondition conditionLead() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(MATERIAL_LEAD));
    }

    public static ICondition conditionTitanium() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(MATERIAL_TITANIUM));
    }

    public static ICondition conditionVibranium() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(MATERIAL_VIBRANIUM));
    }

    public static ICondition conditionMeteoriteBiome() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(WORLDGEN_METEORITE));
    }

    public static ICondition conditionVials() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(ITEM_VIALS));
    }

    public static ICondition conditionHeartShapedHerb() {
        return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(BLOCK_HEART_SHAPED_HERB));
    }

}
