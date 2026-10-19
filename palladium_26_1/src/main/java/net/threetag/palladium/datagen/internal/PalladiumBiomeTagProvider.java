package net.threetag.palladium.datagen.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.neoforged.neoforge.common.Tags;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.tag.PalladiumBiomeTags;
import net.threetag.palladium.worldgen.biome.PalladiumBiomes;

import java.util.concurrent.CompletableFuture;

public class PalladiumBiomeTagProvider extends BiomeTagsProvider {

    public PalladiumBiomeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, Palladium.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(PalladiumBiomeTags.CONTAINS_VIBRANIUM_ORE)
                .addOptional(PalladiumBiomes.METEORITE);
        this.tag(PalladiumBiomeTags.CONTAINS_VIBRANIUM_VEINS)
                .addOptional(PalladiumBiomes.METEORITE);
        this.tag(PalladiumBiomeTags.CONTAINS_HEART_SHAPED_HERBS)
                .addOptional(PalladiumBiomes.METEORITE);

        this.tag(BiomeTags.IS_OVERWORLD)
                .addOptional(PalladiumBiomes.METEORITE);
        this.tag(BiomeTags.HAS_MINESHAFT)
                .addOptional(PalladiumBiomes.METEORITE);
        this.tag(Tags.Biomes.IS_CAVE)
                .addOptional(PalladiumBiomes.METEORITE);
        this.tag(Tags.Biomes.IS_UNDERGROUND)
                .addOptional(PalladiumBiomes.METEORITE);
        this.tag(Tags.Biomes.IS_RARE)
                .addOptional(PalladiumBiomes.METEORITE);
    }
}
