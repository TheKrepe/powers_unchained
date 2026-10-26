package com.star_1mp.powersunchained.client;

import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.star_1mp.powersunchained.client.web.WebSwingClient;
import com.star_1mp.powersunchained.entity.web.WebSwingLine;
import net.neoforged.neoforge.client.ClientHooks;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunchained.client.web.WebZipAnimation;
import com.star_1mp.powersunchained.entity.web.WebZipLine;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.star_1mp.powersunchained.client.web.WebCocoonLayer;
import com.google.common.reflect.TypeToken;
import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import com.star_1mp.powersunchained.client.web.WebShooterClient;
import com.star_1mp.powersunchained.client.ability.AbilityHud;
import com.star_1mp.powersunchained.client.ability.AbilityHudConfigScreen;
import com.star_1mp.powersunchained.client.web.ClientWebShootersTooltip;
import com.star_1mp.powersunchained.client.web.WebCocoonRenderer;
import com.star_1mp.powersunchained.client.web.WebOverlay;
import com.star_1mp.powersunchained.item.WebShootersItem;
import com.star_1mp.powersunchained.client.web.WebShooterHud;
import com.star_1mp.powersunchained.client.web.WebArmAnimation;
import com.star_1mp.powersunchained.client.web.WebProjectileRenderer;
import com.star_1mp.powersunchained.web.WebShotTracker;
import com.star_1mp.powersunchained.registry.PUEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import com.star_1mp.powersunchained.compat.curios.CuriosClientCompat;
import com.star_1mp.powersunchained.client.dev.CiClientDriver;
import com.star_1mp.powersunchained.client.gadget.WebShootersModel;
import com.star_1mp.powersunchained.client.suit.SuitArmorStandLayer;
import com.star_1mp.powersunchained.client.suit.SuitFirstPersonRenderer;
import com.star_1mp.powersunchained.client.suit.SuitLayer;
import com.star_1mp.powersunchained.client.suit.SuitModelLayers;
import com.star_1mp.powersunchained.client.suit.SuitRendering;
import com.star_1mp.powersunchained.client.suit.SuitVariantScroller;
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
            PUKeyMappings.REGISTER.forEach(event::register);
        });
        modEventBus.addListener((RegisterGuiLayersEvent event) -> {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, Constants.id("web_shooters"), WebShooterHud::render);
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, Constants.id("abilities"), AbilityHud::render);
            event.registerBelow(VanillaGuiLayers.CROSSHAIR, Constants.id("web_overlay"), WebOverlay::render);
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, Constants.id("spider_sense"),
                    com.star_1mp.powersunchained.client.power.SpiderSense::render);
        });
        WebShooterClient.registerHud();
        WebZipLine.zipListener = WebZipAnimation::zip;
        WebSwingLine.swingListener = WebZipAnimation::swing;
        WebSwingClient.registerHud();
        com.star_1mp.powersunchained.client.power.PowerClient.register();
        modEventBus.addListener((RegisterClientTooltipComponentFactoriesEvent event) ->
                event.register(WebShootersItem.WebShootersTooltip.class, ClientWebShootersTooltip::new));
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(PUEntities.IMPACT_WEB.get(), WebProjectileRenderer::impact);
            event.registerEntityRenderer(PUEntities.STICKY_WEB.get(), WebProjectileRenderer::sticky);
            event.registerEntityRenderer(PUEntities.WEB_ZIP_LINE.get(), WebProjectileRenderer::impact);
            event.registerEntityRenderer(PUEntities.WEB_SWING_LINE.get(), WebProjectileRenderer::impact);
            event.registerEntityRenderer(PUEntities.WEB_COCOON.get(), WebCocoonRenderer::new);
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            while (PUKeyMappings.HUD_SETTINGS.consumeClick()) {
                if (Minecraft.getInstance().gui.screen() == null) {
                    Minecraft.getInstance().gui.setScreen(new AbilityHudConfigScreen());
                }
            }
            com.star_1mp.powersunchained.client.ability.AbilitySlots.tick();  // which ability is on which key, before they read them
            WebShooterClient.tick();
            WebSwingClient.tick();
            WebShotTracker.tick();
            WebZipAnimation.tick();
            com.star_1mp.powersunchained.client.suitcreator.SuitCreatorClient.tick();
            com.star_1mp.powersunchained.client.power.PowerClient.tick();
        });
        // wall climbing / rolls take the movement keys over (after the keys are read, before vanilla applies them)
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.MovementInputUpdateEvent event) ->
                com.star_1mp.powersunchained.client.power.PowerClient.onMovementInput(event.getEntity(), event.getInput()));
        // Suit Creator
        com.star_1mp.powersunchained.client.suitcreator.SuitCreatorClient.init();
        modEventBus.addListener((net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) -> event.register(
                com.star_1mp.powersunchained.registry.PUMenus.SUIT_CREATOR.get(),
                com.star_1mp.powersunchained.client.suitcreator.SuitCreatorScreen::new));
        // first person: the empty off hand arm shows up while it shoots webs (rapid fire alternates wrists)
        NeoForge.EVENT_BUS.addListener(PowersUnchainedNeoForgeClient::renderWebShootingOffArm);
        // web-shooter first person arm pose: before anything draws the arm
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (RenderArmEvent event) ->
                WebArmAnimation.applyFirstPerson(event.getPoseStack(), event.getArm(), event.getArmPart()));
        NeoForge.EVENT_BUS.addListener((InputEvent.MouseScrollingEvent event) -> {
            if (WebShooterClient.onScroll(event.getScrollDeltaY())) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((InputEvent.InteractionKeyMappingTriggered event) -> {
            boolean mainHand = event.getHand() == InteractionHand.MAIN_HAND;
            if (event.isUseItem() && (WebShooterClient.onUseKey(mainHand) || WebSwingClient.onUseKey(mainHand))) {
                event.setSwingHand(false);
                event.setCanceled(true);
            } else if (event.isAttack() && WebSwingClient.onAttackKey()) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        });
        // web swinging rope physics: on the local player, before it moves
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Pre event) -> {
            if (event.getEntity() == Minecraft.getInstance().player) {
                WebSwingClient.playerTick(event.getEntity());
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
                player.addLayer(new com.star_1mp.powersunchained.client.web.WebHandLayer(player));
            }
            AvatarRenderer<?> mannequin = event.getMannequinRenderer(skin);
            if (mannequin != null) {
                mannequin.addLayer(new SuitLayer(mannequin, event.getEntityModels(), slim));
            }
        }
        // web cocoon over every living entity's model
        for (EntityType<?> type : event.getEntityTypes()) {
            if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?, ?> living) {
                addCocoonLayer(living);
            }
        }
        for (PlayerModelType skin : event.getSkins()) {
            if (event.getPlayerRenderer(skin) instanceof LivingEntityRenderer<?, ?, ?> player) {
                addCocoonLayer(player);
            }
            if (event.getMannequinRenderer(skin) instanceof LivingEntityRenderer<?, ?, ?> mannequin) {
                addCocoonLayer(mannequin);
            }
        }
        ArmorStandRenderer armorStand = event.getRenderer(EntityTypes.ARMOR_STAND);
        if (armorStand != null) {
            armorStand.addLayer(new SuitArmorStandLayer(armorStand, event.getEntityModels()));
        }
        SuitFirstPersonRenderer.bakeModels(event.getEntityModels());
        WebShootersModel.bake(event.getEntityModels());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addCocoonLayer(LivingEntityRenderer renderer) {
        renderer.addLayer(new WebCocoonLayer(renderer));
    }

    private static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {
        }, WebCocoonLayer::extract);
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                SuitRendering.extract(avatar, state);
            }
        });
        event.registerEntityModifier(ArmorStandRenderer.class,
                (ArmorStand armorStand, ArmorStandRenderState state) -> SuitRendering.extractEquipment(armorStand, state));
    }

    /**
     * Vanilla draws no arm for an empty off hand in first person; while that arm fires webs, draw it like the main
     * arm (same placement as vanilla's renderPlayerArm, mirrored), sliding up as it raises. The arm render event is
     * fired for it, so the web aim pose, suit sleeves and web-shooters apply as on the main arm.
     */
    private static void renderWebShootingOffArm(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.OFF_HAND || !event.getItemStack().isEmpty()) {
            return;
        }
        PlayerRenderState playerState = event.getPlayerState();
        AvatarRenderState avatar = playerState.avatarRenderState;
        if (avatar == null || avatar.isInvisible) {
            return;
        }
        HumanoidArm arm = avatar.mainArm.getOpposite();
        float aim = WebArmAnimation.firstPersonAim(arm, event.getPartialTick());
        if (aim <= 0.0F) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float lowered = 1.0F - Mth.clamp(aim * 1.5F, 0.0F, 1.0F);
        pose.translate(invert * 0.64000005F, -0.6F + lowered * -0.6F, -0.71999997F);
        pose.rotateDegrees(Axis.YP, invert * 45.0F);
        pose.translate(invert * -1.0F, 3.6F, 3.5F);
        pose.rotateDegrees(Axis.ZP, invert * 120.0F);
        pose.rotateDegrees(Axis.XP, 200.0F);
        pose.rotateDegrees(Axis.YP, invert * -135.0F);
        pose.translate(invert * 5.6F, 0.0F, 0.0F);
        AvatarRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(avatar);
        Identifier skin = avatar.skin.body().texturePath();
        boolean right = arm == HumanoidArm.RIGHT;
        boolean sleeve = right ? avatar.showRightSleeve : avatar.showLeftSleeve;
        int light = event.getPackedLight();
        if (!ClientHooks.renderSpecificFirstPersonArm(pose, event.getSubmitNodeCollector(), light, skin, sleeve, playerState, arm,
                right ? renderer.getModel().rightArm : renderer.getModel().leftArm)) {
            if (right) {
                renderer.renderRightHand(pose, event.getSubmitNodeCollector(), light, skin, sleeve);
            } else {
                renderer.renderLeftHand(pose, event.getSubmitNodeCollector(), light, skin, sleeve);
            }
        }
        pose.popPose();
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
