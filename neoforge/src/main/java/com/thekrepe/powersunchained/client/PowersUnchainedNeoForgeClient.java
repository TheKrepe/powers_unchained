package com.thekrepe.powersunchained.client;

import com.thekrepe.powersunchained.Constants;
import com.google.common.reflect.TypeToken;
import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import com.thekrepe.powersunchained.client.web.WebShooterClient;
import com.thekrepe.powersunchained.client.web.WebShooterHud;
import com.thekrepe.powersunchained.client.web.WebbedRendering;
import com.thekrepe.powersunchained.registry.PUEntities;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import com.thekrepe.powersunchained.compat.curios.CuriosClientCompat;
import com.thekrepe.powersunchained.client.dev.CiClientDriver;
import com.thekrepe.powersunchained.client.gadget.WebShootersModel;
import com.thekrepe.powersunchained.client.suit.SuitArmorStandLayer;
import com.thekrepe.powersunchained.client.suit.SuitFirstPersonRenderer;
import com.thekrepe.powersunchained.client.suit.SuitLayer;
import com.thekrepe.powersunchained.client.suit.SuitModelLayers;
import com.thekrepe.powersunchained.client.suit.SuitRendering;
import com.thekrepe.powersunchained.client.suit.SuitVariantScroller;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Client-only NeoForge entry point: wires the common client code to NeoForge events. */
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class PowersUnchainedNeoForgeClient {

    /** Render data: the entity is covered in sticky web. */
    private static final ContextKey<Boolean> WEBBED = new ContextKey<>(Constants.id("webbed"));

    public PowersUnchainedNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(PowersUnchainedNeoForgeClient::registerLayerDefinitions);
        modEventBus.addListener(PowersUnchainedNeoForgeClient::addLayers);
        modEventBus.addListener(PowersUnchainedNeoForgeClient::registerRenderStateModifiers);
        // lowest priority: other mods (e.g. Curios accessories) draw on the arm before we cancel vanilla's arm
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PowersUnchainedNeoForgeClient::onRenderArm);
        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            if (ModList.get().isLoaded("curios")) {
                CuriosClientCompat.init();
            }
        }));
        NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseScrolled.Pre event) -> {
            Slot hovered = event.getScreen() instanceof AbstractContainerScreen<?> container ? container.getHoveredSlot() : null;
            if (SuitVariantScroller.onScroll(event.getScreen(), hovered, event.getScrollDeltaY())) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) ->
                SuitVariantScroller.appendTooltip(event.getItemStack(), event.getToolTip()));
        // abilities / web-shooters
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.registerCategory(PUKeyMappings.CATEGORY);
            PUKeyMappings.ALL.forEach(event::register);
        });
        modEventBus.addListener((RegisterGuiLayersEvent event) -> {
            event.registerBelow(VanillaGuiLayers.CROSSHAIR, Constants.id("webbed_overlay"), WebbedRendering::renderOverlay);
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, Constants.id("web_shooters"), WebShooterHud::render);
        });
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(PUEntities.IMPACT_WEB.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(PUEntities.STICKY_WEB.get(), ThrownItemRenderer::new);
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> WebShooterClient.tick());
        NeoForge.EVENT_BUS.addListener((InputEvent.MouseScrollingEvent event) -> {
            if (WebShooterClient.onScroll(event.getScrollDeltaY())) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((InputEvent.InteractionKeyMappingTriggered event) -> {
            if (event.isUseItem() && WebShooterClient.onUseKey(event.getHand() == InteractionHand.MAIN_HAND)) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        });
        CiClientDriver.register();
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        SuitModelLayers.register(event::registerLayerDefinition);
    }

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            boolean slim = skin == PlayerModelType.SLIM;
            AvatarRenderer<?> player = event.getPlayerRenderer(skin);
            if (player != null) {
                player.addLayer(new SuitLayer(player, event.getEntityModels(), slim));
            }
            AvatarRenderer<?> mannequin = event.getMannequinRenderer(skin);
            if (mannequin != null) {
                mannequin.addLayer(new SuitLayer(mannequin, event.getEntityModels(), slim));
            }
        }
        ArmorStandRenderer armorStand = event.getRenderer(EntityTypes.ARMOR_STAND);
        if (armorStand != null) {
            armorStand.addLayer(new SuitArmorStandLayer(armorStand, event.getEntityModels()));
        }
        SuitFirstPersonRenderer.bakeModels(event.getEntityModels());
        WebShootersModel.bake(event.getEntityModels());

        // sticky web over every living entity
        for (PlayerModelType skin : event.getSkins()) {
            addWebbedLayer(event.getPlayerRenderer(skin));
            addWebbedLayer(event.getMannequinRenderer(skin));
        }
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?, ?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer<?, ?, ?> living) {
                addWebbedLayer(living);
            }
        }
    }

    private static <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void addWebbedLayer(
            LivingEntityRenderer<?, S, M> renderer) {
        if (renderer == null) {
            return;
        }
        renderer.addLayer(new WebbedRendering.Layer<S, M>(renderer) {
            @Override
            protected boolean isWebbed(S state) {
                return Boolean.TRUE.equals(state.getRenderData(WEBBED));
            }
        });
    }

    private static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                SuitRendering.extract(avatar, state);
            }
        });
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {},
                (LivingEntity entity, LivingEntityRenderState state) -> state.setRenderData(WEBBED, WebbedRendering.isWebbed(entity)));
        event.registerEntityModifier(ArmorStandRenderer.class,
                (ArmorStand armorStand, ArmorStandRenderState state) -> SuitRendering.extractEquipment(armorStand, state));
    }

    private static void onRenderArm(RenderArmEvent event) {
        boolean handled = SuitFirstPersonRenderer.renderArm(
                event.getPoseStack(),
                event.getSubmitNodeCollector(),
                event.getLightCoords(),
                event.getSkinTexture(),
                event.getArmPart(),
                event.getArm(),
                event.getPlayerRenderState().avatarRenderState);
        WebbedRendering.renderArm(event.getPoseStack(), event.getSubmitNodeCollector(), event.getLightCoords(),
                event.getArmPart(), event.getArm());
        if (handled) {
            event.setCanceled(true);
        }
    }
}
