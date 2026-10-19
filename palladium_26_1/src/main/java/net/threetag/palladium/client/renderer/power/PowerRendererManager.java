package net.threetag.palladium.client.renderer.power;

import net.minecraft.core.Holder;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.client.gui.ui.layout.UiLayout;
import net.threetag.palladium.client.gui.ui.layout.UiLayoutManager;
import net.threetag.palladium.client.model.ModelLayerManager;
import net.threetag.palladium.power.Power;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class PowerRendererManager extends SimpleJsonResourceReloadListener<PowerRenderer> {

    public static final Identifier ID = Palladium.id("power_renderer");
    public static final PowerRendererManager INSTANCE = new PowerRendererManager();

    private final Map<Identifier, PowerRenderer> byName = new HashMap<>();

    protected PowerRendererManager() {
        super(PowerRenderer.CODEC, FileToIdConverter.json("palladium/power_renderers"));
    }

    @Override
    protected void apply(Map<Identifier, PowerRenderer> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.byName.clear();
        // Instead of trusting all entityRenderer's blindly, verify they exist before adding them
        objects.forEach((id, renderer) -> {
            var entityRenderer = renderer.entityRenderer();
            if (entityRenderer != null && !ModelLayerManager.hasLayers(entityRenderer.modelLayer())) {
                AddonPackLog.error("Skipping entity renderer for power {} because model layer {} was not loaded",
                        id, ModelLayerManager.missingLayerNames(entityRenderer.modelLayer()));
                renderer = new PowerRenderer(null, renderer.screenLayout(), renderer.renderLayers(), renderer.trails(),
                        renderer.hudLayouts(), renderer.hiddenModelParts(), renderer.animationControllers(), renderer.abilityBarSprites());
            }
            this.byName.put(id, renderer);
        });
        AddonPackLog.info("Loaded " + this.byName.size() + " power renderers");
    }

    @Nullable
    public PowerRenderer get(Identifier id) {
        return this.byName.get(id);
    }

    @Nullable
    public static PowerRenderer getRenderer(Identifier powerId) {
        return INSTANCE.get(powerId);
    }

    @Nullable
    public static PowerRenderer getRenderer(ResourceKey<Power> powerId) {
        return getRenderer(powerId.identifier());
    }

    @Nullable
    public static PowerRenderer getRenderer(Holder<Power> powerHolder) {
        return getRenderer(Objects.requireNonNull(powerHolder.getKey()).identifier());
    }

    @Nullable
    public static <T> T getProperty(Holder<Power> powerHolder, Function<PowerRenderer, T> propertyGetter) {
        var renderer = getRenderer(powerHolder);
        return renderer != null ? propertyGetter.apply(renderer) : null;
    }

    @NonNull
    public static <T> T getPropertyOrDefault(Holder<Power> powerHolder, Function<PowerRenderer, T> propertyGetter, Supplier<T> fallback) {
        var renderer = getRenderer(powerHolder);

        if (renderer != null) {
            var property = propertyGetter.apply(renderer);
            return property != null ? property : fallback.get();
        } else {
            return fallback.get();
        }
    }

    public static UiLayout getPowerScreenLayout(Holder<Power> powerHolder) {
        var rendererProp = getPropertyOrDefault(powerHolder, PowerRenderer::screenLayout, Optional::empty);

        if (rendererProp.isPresent()) {
            return UiLayoutManager.INSTANCE.get((Identifier) rendererProp.get());
        } else {
            var layout = UiLayoutManager.INSTANCE.get(Objects.requireNonNull(powerHolder.getKey()).identifier().withPrefix("power/"));
            return layout != null ? layout : UiLayoutManager.INSTANCE.get(PowerRenderer.DEFAULT_SCREEN_LAYOUT);
        }
    }
}
