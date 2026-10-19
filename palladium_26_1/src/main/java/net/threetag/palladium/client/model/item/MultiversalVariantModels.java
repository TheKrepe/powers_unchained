package net.threetag.palladium.client.model.item;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;
import net.threetag.palladium.Palladium;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class MultiversalVariantModels implements UnbakedStandaloneModel<Boolean> {

    public static final Identifier ID = Palladium.id("multiversal_variant_models");
    public static final StandaloneModelKey<Boolean> KEY = new StandaloneModelKey<>(ID::toString);
    public static final MultiversalVariantModels INSTANCE = new MultiversalVariantModels();
    public static final String MODEL_PREFIX = "item/multiversal_variant/";

    private static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
    private static volatile List<Identifier> MODEL_IDS = List.of();

    public static final PreparableReloadListener SCAN_LISTENER = new PreparableReloadListener() {
        @Override
        public CompletableFuture<Void> reload(SharedState sharedState, Executor backgroundExecutor, PreparationBarrier barrier, Executor gameExecutor) {
            return CompletableFuture.<Void>supplyAsync(() -> {
                List<Identifier> ids = new ArrayList<>();
                for (Identifier file : MODEL_LISTER.listMatchingResources(sharedState.resourceManager()).keySet()) {
                    Identifier modelId = MODEL_LISTER.fileToId(file);
                    if (modelId.getPath().startsWith(MODEL_PREFIX)) {
                        ids.add(modelId);
                    }
                }
                MODEL_IDS = ids;
                return null;
            }, backgroundExecutor).thenCompose(barrier::wait);
        }

        @Override
        public String getName() {
            return ID.toString();
        }
    };

    private MultiversalVariantModels() {
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        for (Identifier id : MODEL_IDS) {
            resolver.markDependency(id);
        }
    }

    @Override
    public Boolean bake(ModelBaker baker, ModelDebugName debugName) {
        return Boolean.TRUE;
    }
}
