package com.thekrepe.powersunchained.client;

import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import com.thekrepe.powersunchained.client.web.WebShooterClient;
import com.thekrepe.powersunchained.client.ability.AbilityHud;
import com.thekrepe.powersunchained.client.web.WebShooterHud;
import com.thekrepe.powersunchained.client.web.WebArmAnimation;
import com.thekrepe.powersunchained.client.web.WebProjectileRenderer;
import com.thekrepe.powersunchained.web.WebShotTracker;
import com.thekrepe.powersunchained.registry.PUEntities;
import net.minecraft.world.InteractionHand;
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
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, Constants.id("web_shooters"), WebShooterHud::render);
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, Constants.id("abilities"), AbilityHud::render);
        });
        WebShooterClient.registerHud();
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(PUEntities.IMPACT_WEB.get(), WebProjectileRenderer::impact);
            event.registerEntityRenderer(PUEntities.STICKY_WEB.get(), WebProjectileRenderer::sticky);
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            WebShooterClient.tick();
            WebShotTracker.tick();
        });
        // web-shooter first person arm pose: before anything draws the arm
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (RenderArmEvent event) ->
                WebArmAnimation.applyFirstPerson(event.getPoseStack(), event.getArm()));
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
    }

    private static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                SuitRendering.extract(avatar, state);
            }
        });
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
        if (handled) {
            event.setCanceled(true);
        }
    }
}
