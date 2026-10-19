package net.threetag.palladium.client.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.entity.SkinTypedValue;

import java.util.HashMap;
import java.util.Map;

public class ModelLayerManager extends SimpleJsonResourceReloadListener<BedrockModel> {

    public static final Identifier ID = Palladium.id("entity_models");
    public static final ModelLayerManager INSTANCE = new ModelLayerManager();

    public ModelLayerManager() {
        super(BedrockModel.CODEC, FileToIdConverter.json("palladium/model_layers"));
    }

    @Override
    protected void apply(Map<Identifier, BedrockModel> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ModelLayerLocation, LayerDefinition> jsonRoots = new HashMap<>();
        EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
        var codeRoots = entityModels.roots;
        var roots = new HashMap<>(codeRoots);
        int loaded = 0;

        for (var entry : object.entrySet()) {
            Identifier id = entry.getKey();
            ModelLayerLocation layerLocation = mapPathToModelLayerLoc(id);
            try {
                LayerDefinition layerDefinition = entry.getValue().buildLayerDefinition();
                if (layerLocation != null)
                    jsonRoots.put(layerLocation, layerDefinition);
                jsonRoots.put(new ModelLayerLocation(id, "main"), layerDefinition);
                loaded++;
            } catch (Exception e) {
                AddonPackLog.error(e, "Error parsing entity model json {}", id);
            }
        }

        roots.putAll(jsonRoots);
        entityModels.roots = ImmutableMap.copyOf(roots);
        AddonPackLog.info("Loaded {} model layers", loaded);
    }

    // Helper to check if model actually exists in Minecraft's registry
    public static boolean hasLayer(ModelLayerLocation location) {
        return Minecraft.getInstance().getEntityModels().roots.containsKey(location);
    }

    // Helper that ensures both wide and slim layers exist
    public static boolean hasLayers(SkinTypedValue<ModelLayerLocationCodec> layers) {
        return hasLayer(layers.getWide().get()) && hasLayer(layers.getSlim().get());
    }

    // Parser for model names. Called when debugging missing layer issues
    public static String missingLayerNames(SkinTypedValue<ModelLayerLocationCodec> layers) {
        ModelLayerLocation wide = layers.getWide().get();
        ModelLayerLocation slim = layers.getSlim().get();
        boolean wideMissing = !hasLayer(wide);
        boolean slimMissing = !hasLayer(slim);

        if (wideMissing && slimMissing && !wide.equals(slim)) {
            return wide + " and " + slim;
        }

        if (wideMissing) {
            return wide.toString();
        }

        if (slimMissing) {
            return slim.toString();
        }
        return "";
    }

    private static ModelLayerLocation mapPathToModelLayerLoc(Identifier path) {
        int idx = path.getPath().lastIndexOf('/');
        if (idx == -1) {
            return null;
        }

        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(path.getNamespace(), path.getPath().substring(0, idx)), path.getPath().substring(idx + 1));
    }
}
