package net.threetag.palladium.client.renderer.entity.layer.pack;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.client.model.ModelLayerManager;

import java.util.HashMap;
import java.util.Map;

public class PackRenderLayerManager extends SimpleJsonResourceReloadListener<PackRenderLayer<?>> {

    public static final Identifier ID = Palladium.id("render_layers");
    public static final PackRenderLayerManager INSTANCE = new PackRenderLayerManager();

    private final Map<Identifier, PackRenderLayer<?>> byName = new HashMap<>();

    protected PackRenderLayerManager() {
        super(PackRenderLayer.Codecs.SIMPLE_CODEC, FileToIdConverter.json("palladium/render_layers"));
    }

    @Override
    protected void apply(Map<Identifier, PackRenderLayer<?>> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.byName.clear();
        // Individually place objects into the manager only if they have a valid layer
        for (var entry : objects.entrySet()) {
            PackRenderLayer<?> layer = entry.getValue();
            if (layer instanceof DefaultPackRenderLayer defaultLayer && !ModelLayerManager.hasLayers(defaultLayer.getModelLayers())) {
                AddonPackLog.error("Skipping render layer {} because model layer {} was not loaded",
                        entry.getKey(), ModelLayerManager.missingLayerNames(defaultLayer.getModelLayers()));
                continue;
            }
            this.byName.put(entry.getKey(), layer);
        }
        AddonPackLog.info("Loaded " + this.byName.size() + " render layers");
    }

    public PackRenderLayer<?> get(Identifier id) {
        return this.byName.get(id);
    }
}
