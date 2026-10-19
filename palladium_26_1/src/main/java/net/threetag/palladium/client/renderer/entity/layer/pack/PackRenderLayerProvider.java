package net.threetag.palladium.client.renderer.entity.layer.pack;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.renderer.power.PowerRenderer;
import net.threetag.palladium.client.renderer.power.PowerRendererManager;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.customization.CustomizationCategory;
import net.threetag.palladium.customization.EntityCustomizationHandler;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.EntityPowerHandler;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class PackRenderLayerProvider {

    private static final List<Provider> PROVIDERS = new ArrayList<>();

    public static void register(Provider provider) {
        PROVIDERS.add(provider);
    }

    public static void forEach(Entity entity, BiConsumer<DataContext, PackRenderLayer<?>> consumer) {
        Lookup lookup = PackRenderLayerManager.INSTANCE::get;
        for (Provider provider : PROVIDERS) {
            provider.provide(entity, lookup, consumer);
        }
    }

    static {
        // Customizations
        register((entity, lookup, layers) -> {
            if (entity instanceof LivingEntity living) {
                var context = DataContext.forEntity(living);
                var registry = entity.registryAccess().lookupOrThrow(PalladiumRegistryKeys.CUSTOMIZATION_CATEGORY);
                var handler = EntityCustomizationHandler.get(living);

                for (CustomizationCategory category : registry) {
                    if (category.isVisible(context)) {
                        var customization = handler.get(registry.wrapAsHolder(category));

                        if (customization != null && (category.hiddenByEquipment() == null || living.getItemBySlot(category.hiddenByEquipment()).isEmpty())) {
                            var layer = lookup.get(customization.value().getRenderLayerId(entity.registryAccess(), false));

                            if (layer != null) {
                                layers.accept(DataContext.forEntity(entity), layer);
                            }
                        }
                    }
                }
            }
        });

        register((entity, lookup, layers) -> {
            if (entity instanceof LivingEntity living
                    && PalladiumEntityData.get(living, PalladiumEntityDataTypes.POWER_HANDLER.get()) instanceof EntityPowerHandler handler) {
                var context = DataContext.forEntity(living);
                for (PowerInstance power : handler.getPowers()) {
                    PowerRenderer renderer = PowerRendererManager.INSTANCE.get(power.getPowerId());
                    if (renderer == null) {
                        continue;
                    }
                    renderer.renderLayers().forEach((layerId, condition) -> {
                        if (condition.test(context)) {
                            var layer = lookup.get(layerId);
                            if (layer != null) {
                                layers.accept(context, layer);
                            }
                        }
                    });
                }
            }
        });

        // Global render layers
        register((entity, lookup, layers) -> {
            for (Identifier layerId : GlobalRenderLayerManager.INSTANCE.getLayersForEntity(entity.getType())) {
                var layer = lookup.get(layerId);
                if (layer != null) {
                    layers.accept(DataContext.forEntity(entity), layer);
                }
            }
        });

        // Items
        register((entity, lookup, layers) -> {
            if (entity instanceof LivingEntity living) {
                for (PlayerSlot slot : PlayerSlot.values(living.level())) {
                    for (int i = 0; i < slot.getSize(living); i++) {
                        var stack = slot.getItem(living, i);

                        if (stack.has(PalladiumDataComponents.Items.RENDER_LAYERS.get())) {
                            var itemLayers = stack.get(PalladiumDataComponents.Items.RENDER_LAYERS.get());

                            for (Identifier layerId : Objects.requireNonNull(itemLayers).forSlot(slot)) {
                                var layer = lookup.get(layerId);
                                if (layer != null) {
                                    layers.accept(DataContext.forItemInSlot(living, slot, stack), layer);
                                }
                            }
                        }

                        var appearance = MultiversalItemVariant.resolve(living.level().registryAccess(), stack);
                        if (appearance != null) {
                            for (Identifier layerId : appearance.renderLayers().forSlot(slot)) {
                                var layer = lookup.get(layerId);
                                if (layer != null) {
                                    layers.accept(DataContext.forItemInSlot(living, slot, stack), layer);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @FunctionalInterface
    public interface Provider {

        void provide(Entity entity, Lookup lookup, BiConsumer<DataContext, PackRenderLayer<?>> layers);

    }

    @FunctionalInterface
    public interface Lookup {

        PackRenderLayer<?> get(Identifier id);

    }

}
