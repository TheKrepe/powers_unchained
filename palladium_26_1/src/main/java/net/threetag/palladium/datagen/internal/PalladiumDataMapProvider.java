package net.threetag.palladium.datagen.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.threetag.palladium.flag.PalladiumFeatureFlags;
import net.threetag.palladium.item.PalladiumItems;

import java.util.concurrent.CompletableFuture;

public class PalladiumDataMapProvider extends DataMapProvider {

    public PalladiumDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        this.builder(NeoForgeDataMaps.COMPOSTABLES)
                .add(PalladiumItems.HEART_SHAPED_HERB, new Compostable(0.65F), false, PalladiumFeatureFlags.conditionHeartShapedHerb());
    }
}
