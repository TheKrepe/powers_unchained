package net.threetag.palladium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.particle.ParticleTypeConfiguration;
import net.threetag.palladium.client.animation.PackAnimationControllerManager;
import net.threetag.palladium.client.animation.PackAnimationControllerSerializer;
import net.threetag.palladium.client.animation.PackAnimationControllerSerializers;
import net.threetag.palladium.client.particle.AddonParticle;
import net.threetag.palladium.client.animation.PalladiumAnimationManager;
import net.threetag.palladium.client.beam.BeamManager;
import net.threetag.palladium.client.beam.BeamRendererSerializer;
import net.threetag.palladium.client.beam.BeamRendererSerializers;
import net.threetag.palladium.client.gui.screen.abilitybar.AbilityBar;
import net.threetag.palladium.client.gui.screen.abilitybar.AbilityBarSpecialRenderer;
import net.threetag.palladium.client.gui.screen.hud.AbilityGuiLayer;
import net.threetag.palladium.client.gui.screen.hud.ModeSwitchOverlay;
import net.threetag.palladium.client.gui.ui.hud.HudLayoutLayer;
import net.threetag.palladium.client.gui.screen.menu.MultiversalIteratorScreen;
import net.threetag.palladium.client.gui.screen.menu.MultiversalIteratorSuitStandScreen;
import net.threetag.palladium.client.gui.screen.menu.TailoringScreen;
import net.threetag.palladium.client.gui.ui.background.UiBackgroundSerializer;
import net.threetag.palladium.client.gui.ui.background.UiBackgroundSerializers;
import net.threetag.palladium.client.gui.ui.widget.UiWidgetSerializer;
import net.threetag.palladium.client.gui.ui.widget.UiWidgetSerializers;
import net.threetag.palladium.client.gui.ui.layout.UiLayoutManager;
import net.threetag.palladium.client.gui.ui.layout.UiLayoutSerializer;
import net.threetag.palladium.client.gui.ui.layout.UiLayoutSerializers;
import net.threetag.palladium.client.model.ModelLayerManager;
import net.threetag.palladium.client.model.item.MultiversalVariantItemModel;
import net.threetag.palladium.client.model.item.MultiversalVariantModels;
import net.threetag.palladium.client.particleemitter.ParticleEmitterManager;
import net.threetag.palladium.client.renderer.entity.CustomProjectileRenderer;
import net.threetag.palladium.client.renderer.entity.EffectEntityRenderer;
import net.threetag.palladium.client.renderer.entity.SuitStandRenderer;
import net.threetag.palladium.client.renderer.entity.GrappleHookRenderer;
import net.threetag.palladium.client.renderer.entity.SwingAnchorRenderer;
import net.threetag.palladium.client.renderer.entity.ThrownTridentRenderer;
import net.threetag.palladium.client.renderer.entity.effect.EntityEffectRenderer;
import net.threetag.palladium.client.renderer.entity.layer.pack.*;
import net.threetag.palladium.client.renderer.icon.IconRenderer;
import net.threetag.palladium.client.renderer.power.PowerRendererLayer;
import net.threetag.palladium.client.renderer.power.PowerRendererManager;
import net.threetag.palladium.client.gui.widget.tree.PowerTreeManager;
import net.threetag.palladium.client.texture.AbilityBarTextureConverter;
import net.threetag.palladium.client.texture.DynamicTextureManager;
import net.threetag.palladium.client.texture.DynamicTextureSerializer;
import net.threetag.palladium.client.texture.DynamicTextureSerializers;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.client.texture.transformer.TextureTransformerSerializer;
import net.threetag.palladium.client.texture.transformer.TextureTransformerSerializers;
import net.threetag.palladium.client.trail.TrailManager;
import net.threetag.palladium.client.trail.TrailRendererSerializer;
import net.threetag.palladium.client.trail.TrailRendererSerializers;
import net.threetag.palladium.compat.geckolib.GeckoLibCompatClient;
import net.threetag.palladium.datagen.internal.*;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.HTMLBuilder;
import net.threetag.palladium.logic.molang.MoLangQueryRegistry;
import net.threetag.palladium.entity.PalladiumEntityTypes;
import net.threetag.palladium.entity.sizechange.PalladiumSizeChangeTypes;
import net.threetag.palladium.item.tab.SectionSerializer;
import net.threetag.palladium.menu.PalladiumMenuTypes;
import net.threetag.palladium.proxy.PalladiumClientProxy;
import net.threetag.palladium.registry.PalladiumRegistries;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.worldgen.biome.PalladiumBiomeModifiers;
import net.threetag.palladium.worldgen.biome.PalladiumBiomes;
import net.threetag.palladium.worldgen.feature.PalladiumConfiguredFeatures;
import net.threetag.palladium.worldgen.feature.PalladiumPlacedFeatures;

import java.util.Objects;

@Mod(value = Palladium.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class PalladiumClient {

    public PalladiumClient(ModContainer container) {
        Palladium.PROXY = new PalladiumClientProxy();
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // Render Layers
        PackRenderLayerSerializers.init();
        PackAnimationControllerSerializers.init();

        // Misc
        BeamRendererSerializers.init();
        TrailRendererSerializers.init();
        TextureTransformerSerializers.init();
        DynamicTextureSerializers.init();
        UiBackgroundSerializers.init();
        UiLayoutSerializers.init();
        UiWidgetSerializers.init();
        TextureReference.DYNAMIC_TEXTURE_RESOLVER = (path, context) -> {
            var dyn = DynamicTextureManager.INSTANCE.get(path);
            return dyn != null ? dyn.getTexture(context) : null;
        };

        // Compat
        if (ModList.get().isLoaded("geckolib")) {
            GeckoLibCompatClient.init();
        }
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        IconRenderer.registerRenderers();
        EntityEffectRenderer.registerRenderers();
        AbilityBarSpecialRenderer.registerRenderers();
        PalladiumAnimationManager.registerLayers(event);
    }

    @SubscribeEvent
    static void particleProviders(RegisterParticleProvidersEvent e) {
        ParticleTypeConfiguration.REGISTERED.forEach((id, config) ->
                e.registerSpriteSet(config.getOrCreateParticleType(), spriteSet -> new AddonParticle.Provider(config, spriteSet)));
    }

    @SubscribeEvent
    static void menuScreen(RegisterMenuScreensEvent e) {
        e.register(PalladiumMenuTypes.TAILORING.get(), TailoringScreen::new);
        e.register(PalladiumMenuTypes.MULTIVERSAL_ITERATOR.get(), MultiversalIteratorScreen::new);
        e.register(PalladiumMenuTypes.MULTIVERSAL_ITERATOR_SUIT_STAND.get(), MultiversalIteratorSuitStandScreen::new);
    }

    @SubscribeEvent
    static void entityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(PalladiumEntityTypes.SUIT_STAND.get(), SuitStandRenderer::new);
        e.registerEntityRenderer(PalladiumEntityTypes.EFFECT.get(), EffectEntityRenderer::new);
        e.registerEntityRenderer(PalladiumEntityTypes.SWING_ANCHOR.get(), SwingAnchorRenderer::new);
        e.registerEntityRenderer(PalladiumEntityTypes.CUSTOM_PROJECTILE.get(), CustomProjectileRenderer::new);
        e.registerEntityRenderer(PalladiumEntityTypes.GRAPPLE_HOOK.get(), GrappleHookRenderer::new);
        e.registerEntityRenderer(PalladiumEntityTypes.THROWN_TRIDENT.get(), ThrownTridentRenderer::new);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @SubscribeEvent
    static void entityRenderLayers(EntityRenderersEvent.AddLayers e) {
        PowerRendererLayer.setupHeadRendering(e.getEntityModels(), e.getContext().getPlayerSkinRenderCache());

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (e.getRenderer(entityType) instanceof LivingEntityRenderer renderer) {
                renderer.addLayer(new PackRenderLayerRenderer<>(renderer));
            }
        }
        for (PlayerModelType skin : e.getSkins()) {
            var playerRenderer = e.getPlayerRenderer(skin);
            Objects.requireNonNull(playerRenderer).addLayer(new PowerRendererLayer<>(playerRenderer));
            Objects.requireNonNull(playerRenderer).addLayer(new PackRenderLayerRenderer<>(playerRenderer));
            var mannequinRenderer = e.getMannequinRenderer(skin);
            Objects.requireNonNull(mannequinRenderer).addLayer(new PowerRendererLayer<>(mannequinRenderer));
            Objects.requireNonNull(mannequinRenderer).addLayer(new PackRenderLayerRenderer<>(mannequinRenderer));
        }
    }

    @SubscribeEvent
    static void guiOverlays(RegisterGuiLayersEvent e) {
        e.registerAboveAll(Palladium.id("ability_bar"), AbilityBar.INSTANCE);
        e.registerAboveAll(Palladium.id("gui_overlay_abilities"), AbilityGuiLayer.INSTANCE);
        e.registerAboveAll(Palladium.id("hud_layouts"), HudLayoutLayer.INSTANCE);
        e.registerAboveAll(Palladium.id("mode_switch_overlay"), ModeSwitchOverlay.INSTANCE);
    }

    @SubscribeEvent
    static void reloadListeners(AddClientReloadListenersEvent e) {
        e.addListener(ModelLayerManager.ID, ModelLayerManager.INSTANCE);
        e.addListener(DynamicTextureManager.ID, DynamicTextureManager.INSTANCE);
        e.addListener(PackRenderLayerManager.ID, PackRenderLayerManager.INSTANCE);
        e.addListener(PowerRendererManager.ID, PowerRendererManager.INSTANCE);
        e.addListener(PowerTreeManager.ID, PowerTreeManager.INSTANCE);
        e.addListener(PackAnimationControllerManager.ID, PackAnimationControllerManager.INSTANCE);
        e.addListener(ParticleEmitterManager.ID, ParticleEmitterManager.INSTANCE);
        e.addListener(BeamManager.ID, BeamManager.INSTANCE);
        e.addListener(TrailManager.ID, TrailManager.INSTANCE);
        e.addListener(UiLayoutManager.ID, UiLayoutManager.INSTANCE);
        e.addListener(GlobalRenderLayerManager.ID, GlobalRenderLayerManager.INSTANCE);
        e.addListener(MultiversalVariantModels.ID, MultiversalVariantModels.SCAN_LISTENER);
        e.addListener(AbilityBarTextureConverter.ID, AbilityBarTextureConverter.INSTANCE);

        e.addDependency(ModelLayerManager.ID, DynamicTextureManager.ID);
        e.addDependency(MultiversalVariantModels.ID, e.getNameLookup().apply(Minecraft.getInstance().getModelManager()));
        e.addDependency(DynamicTextureManager.ID, PackRenderLayerManager.ID);
        e.addDependency(e.getNameLookup().apply(Minecraft.getInstance().getModelManager()), ModelLayerManager.ID);
        e.addDependency(ModelLayerManager.ID, e.getNameLookup().apply(Minecraft.getInstance().getEntityRenderDispatcher().equipmentAssets));
        e.addDependency(ModelLayerManager.ID, PowerRendererManager.ID);
    }

    @SubscribeEvent
    static void itemModels(RegisterItemModelsEvent e) {
        e.register(Palladium.id("multiversal_variant"), MultiversalVariantItemModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerStandaloneModels(ModelEvent.RegisterStandalone e) {
        e.register(MultiversalVariantModels.KEY, MultiversalVariantModels.INSTANCE);
    }

    @SubscribeEvent
    static void gatherData(GatherDataEvent.Client e) {
        e.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(PalladiumRegistryKeys.POWER, context -> {
                })
                .add(Registries.CONFIGURED_FEATURE, PalladiumConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, PalladiumPlacedFeatures::bootstrap)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, PalladiumBiomeModifiers::bootstrap)
                .add(Registries.BIOME, PalladiumBiomes::bootstrap)
                .add(PalladiumRegistryKeys.SIZE_CHANGE_TYPE, PalladiumSizeChangeTypes::bootstrap));

        // Client
        e.createProvider(PalladiumLangProvider.English::new);
        e.createProvider(PalladiumLangProvider.German::new);
        e.createProvider(PalladiumLangProvider.Saxon::new);
        e.createProvider(PalladiumModelProvider::new);
        e.createProvider(PalladiumRenderLayerProvider::new);
        e.createProvider(PalladiumBeamProvider::new);
        e.createProvider(PalladiumUiLayoutProvider::new);
        e.createProvider(PalladiumSoundProvider::new);

        // Server
        e.createProvider(PalladiumLootTableProvider::new);
        e.createProvider(PalladiumBlockTagProvider::new);
        e.createProvider(PalladiumItemTagProvider::new);
        e.createProvider(PalladiumCustomizationProvider::new);
        e.createProvider(PalladiumCustomizationCategoryProvider::new);
        e.createProvider(PalladiumFlightTypeProvider::new);
        e.createProvider(PalladiumRecipeProvider.Runner::new);
        e.createProvider(PalladiumPowerTagProvider::new);
        e.createProvider(PalladiumPlacedFeatureTagProvider::new);
        e.createProvider(PalladiumBiomeTagProvider::new);
        e.createProvider(PalladiumDataMapProvider::new);
    }

    @SubscribeEvent
    static void documentation(LevelEvent.Load e) {
        var clientLevel = e.getLevel();
        CodecDocumentationBuilder.startListening();
        HTMLBuilder.documentedPage(Palladium.id("texture_transformers"), TextureTransformerSerializer.getTypes(), "Texture Transformers", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("dynamic_textures"), DynamicTextureSerializer.getTypes(), "Dynamic Textures", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("render_layers"), PackRenderLayerSerializer.getTypes(), "Render Layers", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("animation_controllers"), PackAnimationControllerSerializer.getTypes(), "Animation Controllers", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("beams"), BeamRendererSerializer.getTypes(), "Beams", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("trails"), TrailRendererSerializer.getTypes(), "Trails", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("ui/layouts"), UiLayoutSerializer.getTypes(), "UI Layouts", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("ui/widgets"), UiWidgetSerializer.getTypes(), "UI Widgets", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("ui/backgrounds"), UiBackgroundSerializer.getTypes(), "UI Backgrounds", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(Palladium.id("creative_tab_sections"), SectionSerializer.getTypes(), "Creative Tab Sections", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(PalladiumRegistryKeys.ACTION_SERIALIZER, PalladiumRegistries.ACTION_SERIALIZER, "Actions", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(PalladiumRegistryKeys.VALUE_SERIALIZER, PalladiumRegistries.VALUE_SERIALIZER, "Values", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(PalladiumRegistryKeys.COST_SERIALIZER, PalladiumRegistries.COST_SERIALIZER, "Costs", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(PalladiumRegistryKeys.ABILITY_SERIALIZER, PalladiumRegistries.ABILITY_SERIALIZER, "Abilities", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(PalladiumRegistryKeys.CONDITION_SERIALIZER, PalladiumRegistries.CONDITION_SERIALIZER, "Conditions", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(PalladiumRegistryKeys.FLIGHT_TYPE_SERIALIZERS, PalladiumRegistries.FLIGHT_TYPE_SERIALIZERS, "Flight Types", clientLevel.registryAccess()).save();
        HTMLBuilder.documentedPage(PalladiumRegistryKeys.ICON_SERIALIZER, PalladiumRegistries.ICON_SERIALIZER, "Icons", clientLevel.registryAccess()).save();
        HTMLBuilder.moLangQueryPage(Palladium.id("molang"), MoLangQueryRegistry.getDocumentation(), "MoLang Queries").save();
        CodecDocumentationBuilder.createDocFiles();
    }

}
