package net.threetag.palladium.entity.sizechange;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.Easing;

import java.util.List;
import java.util.Optional;

public class PalladiumSizeChangeTypes {

    public static final ResourceKey<SizeChangeType> DEFAULT = ResourceKey.create(PalladiumRegistryKeys.SIZE_CHANGE_TYPE, Palladium.id("default"));

    public static void bootstrap(BootstrapContext<SizeChangeType> context) {
        context.register(DEFAULT, new SizeChangeType(20, 0.0625F, 16F, 1F, 1F, 1F, 1F, false, Easing.INCUBIC, Optional.empty(), Optional.empty(), List.of()));
    }

}
