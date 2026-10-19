package net.threetag.palladium.worldgen.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.threetag.palladium.flag.PalladiumFeatureFlags;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

public class MeteoriteRegion extends Region {

    public MeteoriteRegion(Identifier name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
        });

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || !server.getWorldData().enabledFeatures().contains(PalladiumFeatureFlags.WORLDGEN_METEORITE)) {
            return;
        }

        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();
        new ParameterUtils.ParameterPointListBuilder()
                .continentalness(ParameterUtils.Continentalness.FAR_INLAND)
                .depth(ParameterUtils.Depth.UNDERGROUND)
                .build().forEach(point -> builder.add(point, PalladiumBiomes.METEORITE));

        builder.build().forEach(mapper);
    }
}
