package com.thekrepe.powersunchained;

import com.thekrepe.powersunchained.command.SuitCommand;
import com.thekrepe.powersunchained.dev.CiServerHook;
import com.thekrepe.powersunchained.network.ShootWebPayload;
import com.thekrepe.powersunchained.network.WebShotAnimPayload;
import com.thekrepe.powersunchained.network.WebZipPayload;
import com.thekrepe.powersunchained.web.WebShotTracker;
import com.thekrepe.powersunchained.platform.NeoForgeRegistryHelper;
import com.thekrepe.powersunchained.web.WebShooting;
import com.thekrepe.powersunchained.web.WebSlowness;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Constants.MOD_ID)
public class PowersUnchainedNeoForge {

    public PowersUnchainedNeoForge(IEventBus modEventBus) {
        PowersUnchained.init();
        NeoForgeRegistryHelper.register(modEventBus);
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            WebShooting.tick();
            WebSlowness.endTick();
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> WebShooting.forget(event.getEntity()));
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1")
                .playToServer(ShootWebPayload.TYPE, ShootWebPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        WebShooting.shoot(player, payload.webType());
                    }
                })
                .playToServer(WebZipPayload.TYPE, WebZipPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        WebShooting.zip(player);
                    }
                })
                .playToClient(WebShotAnimPayload.TYPE, WebShotAnimPayload.STREAM_CODEC,
                        (payload, context) -> WebShotTracker.onShot(payload.entityId(), payload.side())));
        CiServerHook.register();
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> SuitCommand.register(event.getDispatcher()));
    }
}
