package net.threetag.palladium.entity.data;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.entity.layer.pack.EntityRenderLayers;
import net.threetag.palladium.client.renderer.power.EntityPowerRendererHandler;
import net.threetag.palladium.client.renderer.entity.skin.PlayerSkinHandler;
import net.threetag.palladium.client.trail.EntityTrailHandler;
import net.threetag.palladium.customization.EntityCustomizationHandler;
import net.threetag.palladium.entity.PalladiumHubData;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.entity.sizechange.EntitySizeChangeHandler;
import net.threetag.palladium.item.suitset.EntitySuitSetHandler;
import net.threetag.palladium.power.EntityPowerHandler;
import net.threetag.palladium.power.superpower.EntitySuperpowerHandler;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class PalladiumEntityDataTypes {

    public static final DeferredRegister<PalladiumEntityDataType<?>> DATA_TYPES = DeferredRegister.create(PalladiumRegistryKeys.ENTITY_DATA_TYPE, Palladium.MOD_ID);

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<PalladiumHubData>> PALLADIUM_HUB =
            DATA_TYPES.register("palladium_hub_data", () -> new PalladiumEntityDataType<>(PalladiumHubData.CODEC, PalladiumEntityDataType.FILTER_PLAYER));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntityPowerHandler>> POWER_HANDLER =
            DATA_TYPES.register("powers", () -> new PalladiumEntityDataType<>(EntityPowerHandler.CODEC, PalladiumEntityDataType.FILTER_LIVING));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntitySuperpowerHandler>> SUPERPOWER_HANDLER =
            DATA_TYPES.register("superpowers", () -> new PalladiumEntityDataType<>(EntitySuperpowerHandler.CODEC, PalladiumEntityDataType.FILTER_LIVING));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntityRenderLayers>> RENDER_LAYERS =
            DATA_TYPES.register("render_layers", () -> new PalladiumEntityDataType<>(EntityRenderLayers.CODEC, PalladiumEntityDataType.FILTER_LIVING));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntityCustomizationHandler>> CUSTOMIZATION_HANDLER =
            DATA_TYPES.register("customizations", () -> new PalladiumEntityDataType<>(EntityCustomizationHandler.CODEC, PalladiumEntityDataType.FILTER_LIVING));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntityFlightHandler>> FLIGHT =
            DATA_TYPES.register("flight", () -> new PalladiumEntityDataType<>(EntityFlightHandler.CODEC, PalladiumEntityDataType.FILTER_LIVING));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntityTrailHandler>> TRAIL_HANDLER =
            DATA_TYPES.register("trails", () -> new PalladiumEntityDataType<>(EntityTrailHandler.CODEC, PalladiumEntityDataType.FILTER_ALL));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntitySuitSetHandler>> SUIT_SETS =
            DATA_TYPES.register("suit_sets", () -> new PalladiumEntityDataType<>(EntitySuitSetHandler.CODEC, PalladiumEntityDataType.FILTER_LIVING));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<PlayerSkinHandler>> SKIN_OVERRIDE =
            DATA_TYPES.register("skin_override", () -> new PalladiumEntityDataType<>(PlayerSkinHandler.CODEC, PalladiumEntityDataType.FILTER_AVATAR));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntitySizeChangeHandler>> SIZE_CHANGE =
            DATA_TYPES.register("size_change", () -> new PalladiumEntityDataType<>(EntitySizeChangeHandler.CODEC, PalladiumEntityDataType.FILTER_LIVING));

    public static final DeferredHolder<PalladiumEntityDataType<?>, PalladiumEntityDataType<EntityPowerRendererHandler>> POWER_RENDERER_HANDLER =
            DATA_TYPES.register("power_renderer", () -> new PalladiumEntityDataType<>(EntityPowerRendererHandler.CODEC, PalladiumEntityDataType.FILTER_LIVING));

}
