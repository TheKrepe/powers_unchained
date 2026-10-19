package com.thekrepe.powersunchained.client;

import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.client.dev.CiClientDriver;
import com.thekrepe.powersunchained.client.suit.SuitFirstPersonRenderer;
import com.thekrepe.powersunchained.client.suit.SuitLayer;
import com.thekrepe.powersunchained.client.suit.SuitModelLayers;
import com.thekrepe.powersunchained.client.suit.SuitRendering;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only NeoForge entry point: wires the common client code to NeoForge events. */
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class PowersUnchainedNeoForgeClient {

    public PowersUnchainedNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(PowersUnchainedNeoForgeClient::registerLayerDefinitions);
        modEventBus.addListener(PowersUnchainedNeoForgeClient::addLayers);
        modEventBus.addListener(PowersUnchainedNeoForgeClient::registerRenderStateModifiers);
        NeoForge.EVENT_BUS.addListener(PowersUnchainedNeoForgeClient::onRenderArm);
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
        SuitFirstPersonRenderer.bakeModels(event.getEntityModels());
    }

    private static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                SuitRendering.extract(avatar, state);
            }
        });
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
