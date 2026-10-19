package net.threetag.palladium.addonpack.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.threetag.palladium.addonpack.AddonPackManager;
import net.threetag.palladium.addonpack.log.AddonPackLog;

import java.util.Map;

public class ParticleTypeLoader extends SimpleJsonResourceReloadListener<ParticleTypeConfiguration> {

    private final AddonPackManager.RegisterCallback<ParticleType<?>> callback;

    public ParticleTypeLoader(AddonPackManager.RegisterCallback<ParticleType<?>> callback) {
        super(ParticleTypeConfiguration.CODEC, FileToIdConverter.json(Registries.PARTICLE_TYPE.identifier().getPath()));
        this.callback = callback;
    }

    @Override
    protected void apply(Map<Identifier, ParticleTypeConfiguration> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ParticleTypeConfiguration.REGISTERED.clear();

        objects.forEach((id, config) -> {
            this.callback.register(Registries.PARTICLE_TYPE, id, config.getOrCreateParticleType());
            ParticleTypeConfiguration.REGISTERED.put(id, config);
        });

        AddonPackLog.info("Registered " + objects.size() + " addonpack particle types");
    }
}
