package net.threetag.palladium.client.renderer.entity.layer.pack;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;

import java.util.*;

public class GlobalRenderLayerManager extends SimpleJsonResourceReloadListener<GlobalRenderLayer> {

    public static final Identifier ID = Palladium.id("global_render_layers");
    public static final GlobalRenderLayerManager INSTANCE = new GlobalRenderLayerManager();

    private final Map<EntityType<?>, List<Identifier>> byEntityType = new HashMap<>();

    protected GlobalRenderLayerManager() {
        super(GlobalRenderLayer.CODEC, FileToIdConverter.json("palladium/global_render_layers"));
    }

    @Override
    protected void apply(Map<Identifier, GlobalRenderLayer> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.byEntityType.clear();

        for (GlobalRenderLayer entry : objects.values()) {
            for (EntityType<?> entityType : entry.entities()) {
                this.byEntityType.computeIfAbsent(entityType, k -> new ArrayList<>()).addAll(entry.renderLayers());
            }
        }

        AddonPackLog.info("Loaded " + objects.size() + " global render layer assignments");
    }

    public List<Identifier> getLayersForEntity(EntityType<?> entityType) {
        return this.byEntityType.getOrDefault(entityType, Collections.emptyList());
    }

}
