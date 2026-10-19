package net.threetag.palladium.datagen.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.tag.PalladiumPlacedFeatureTags;
import net.threetag.palladium.worldgen.feature.PalladiumPlacedFeatures;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public class PalladiumPlacedFeatureTagProvider extends KeyTagProvider<PlacedFeature> {

    public PalladiumPlacedFeatureTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.PLACED_FEATURE, lookupProvider, Palladium.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(PalladiumPlacedFeatureTags.LEAD_ORES)
                .add(PalladiumPlacedFeatures.ORE_LEAD_UPPER, PalladiumPlacedFeatures.ORE_LEAD_MIDDLE,
                        PalladiumPlacedFeatures.ORE_LEAD_SMALL);

        this.tag(PalladiumPlacedFeatureTags.TITANIUM_ORES)
                .add(PalladiumPlacedFeatures.ORE_TITANIUM_UPPER, PalladiumPlacedFeatures.ORE_TITANIUM_MIDDLE,
                        PalladiumPlacedFeatures.ORE_TITANIUM_SMALL);
        
        this.tag(PalladiumPlacedFeatureTags.VIBRANIUM_ORES)
                .add(PalladiumPlacedFeatures.ORE_VIBRANIUM, PalladiumPlacedFeatures.ORE_VIBRANIUM_MEDIUM,
                        PalladiumPlacedFeatures.ORE_VIBRANIUM_LARGE, PalladiumPlacedFeatures.ORE_VIBRANIUM_BURIED);

        this.tag(PalladiumPlacedFeatureTags.VIBRANIUM_VEINS)
                .add(PalladiumPlacedFeatures.ORE_VIBRANIUM_VEIN_SMALL, PalladiumPlacedFeatures.ORE_VIBRANIUM_VEIN_BIG);

        this.tag(PalladiumPlacedFeatureTags.HEART_SHAPED_HERBS)
                .add(PalladiumPlacedFeatures.HEART_SHAPED_HERB_MEADOW, PalladiumPlacedFeatures.HEART_SHAPED_HERB_SURFACE);
    }
}
