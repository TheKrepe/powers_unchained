package net.threetag.palladium.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.threetag.palladium.entity.sizechange.SizeChangeType;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.concurrent.CompletableFuture;

public abstract class SizeChangeTypeProvider extends AdvancedJsonCodecProvider<SizeChangeType> {

    public SizeChangeTypeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, PackOutput.Target.DATA_PACK, PalladiumRegistryKeys.getPackFolder(PalladiumRegistryKeys.SIZE_CHANGE_TYPE), SizeChangeType.CODEC, lookupProvider, modId);
    }
}
