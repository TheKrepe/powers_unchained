package com.star_1mp.powersunchained;

import com.star_1mp.powersunchained.command.SuitCommand;
import com.star_1mp.powersunchained.dev.CiServerHook;
import com.star_1mp.powersunchained.network.ShootWebPayload;
import com.star_1mp.powersunchained.network.WebShotAnimPayload;
import com.star_1mp.powersunchained.network.WebSwingPayload;
import com.star_1mp.powersunchained.network.WebZipPayload;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.star_1mp.powersunchained.web.WebShotTracker;
import com.star_1mp.powersunchained.platform.NeoForgeRegistryHelper;
import com.star_1mp.powersunchained.web.WebShooting;
import com.star_1mp.powersunchained.web.WebSlowness;
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
        PUAttachments.register(modEventBus);
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            WebShooting.tick();
            WebSlowness.endTick();
        });
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WebShooting.playerTick(player);
                com.star_1mp.powersunchained.power.Superpowers.serverTick(player);
            }
        });
        // no fall damage when landing after travelling by web (zip, swing)
        NeoForge.EVENT_BUS.addListener((LivingFallEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player
                    && (WebShooting.isFallSafe(player) || com.star_1mp.powersunchained.power.WallClimbing.isClimbing(player)
                            || com.star_1mp.powersunchained.power.Superpowers.fallImmune(player))) {
                event.setCanceled(true);
            }
        });
        // skill trees: monsters killed train the powers; Dodge / Sixth Sense change the damage taken
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) -> {
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                com.star_1mp.powersunchained.power.PowerSkills.onKill(player, event.getEntity());
            }
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                float amount = com.star_1mp.powersunchained.power.PowerSkills.incomingDamage(player, event.getSource(), event.getAmount());
                if (amount <= 0.0F) {
                    event.setCanceled(true);
                } else if (amount != event.getAmount()) {
                    event.setAmount(amount);
                }
            }
        });
        // Electro-Web module condition: struck by lightning while wearing web-shooters
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                com.star_1mp.powersunchained.tech.TechFeats.onStruckByLightning(player);
            }
        });
        // Tech Table link cache: re-search the redstone line only after block changes
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) ->
                com.star_1mp.powersunchained.tech.TechLink.blocksChanged());
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.block.BreakBlockEvent event) ->
                com.star_1mp.powersunchained.tech.TechLink.blocksChanged());
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.ExplosionEvent.Detonate event) ->
                com.star_1mp.powersunchained.tech.TechLink.blocksChanged());
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            WebShooting.forget(event.getEntity());
            if (event.getEntity() instanceof ServerPlayer player) {
                com.star_1mp.powersunchained.power.WallClimbing.forget(player);
                com.star_1mp.powersunchained.power.PowerSkills.forget(player);
            }
        });
        // creative web-shooters (endless web) in the Operator Utilities tab
        modEventBus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.OP_BLOCKS && event.hasPermissions()) {
                event.accept(com.star_1mp.powersunchained.item.WebShootersItem.creative());
            }
        });
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
                .playToServer(WebSwingPayload.TYPE, WebSwingPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        switch (payload.action()) {
                            case WebSwingPayload.SHOOT -> WebShooting.swingShoot(player);
                            case WebSwingPayload.RELEASE -> WebShooting.swingRelease(player, true);
                            default -> WebShooting.swingRelease(player, false);
                        }
                    }
                })
                .playToServer(com.star_1mp.powersunchained.network.PowerTogglePayload.TYPE,
                        com.star_1mp.powersunchained.network.PowerTogglePayload.STREAM_CODEC, (payload, context) -> {
                    com.star_1mp.powersunchained.power.PowerAbility power =
                            com.star_1mp.powersunchained.power.PowerAbility.byId(payload.power());
                    if (power != null && context.player() instanceof ServerPlayer player) {
                        com.star_1mp.powersunchained.power.Superpowers.toggle(player, power);
                    }
                })
                .playToServer(com.star_1mp.powersunchained.network.SkillBuyPayload.TYPE,
                        com.star_1mp.powersunchained.network.SkillBuyPayload.STREAM_CODEC, (payload, context) -> {
                    com.star_1mp.powersunchained.power.Superpower power = com.star_1mp.powersunchained.power.Superpower.byId(payload.power());
                    if (power != null && context.player() instanceof ServerPlayer player) {
                        com.star_1mp.powersunchained.power.PowerSkills.buy(player, power, payload.node());
                    }
                })
                .playToServer(com.star_1mp.powersunchained.network.ClimbPayload.TYPE,
                        com.star_1mp.powersunchained.network.ClimbPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        com.star_1mp.powersunchained.power.WallClimbing.update(player, payload.mode(), payload.direction());
                    }
                })
                .playToClient(com.star_1mp.powersunchained.network.ClimbSyncPayload.TYPE,
                        com.star_1mp.powersunchained.network.ClimbSyncPayload.STREAM_CODEC, (payload, context) ->
                                com.star_1mp.powersunchained.client.power.ClimbState.onSync(payload.entityId(), payload.mode(), payload.direction()))
                .playToServer(com.star_1mp.powersunchained.network.RollPayload.TYPE,
                        com.star_1mp.powersunchained.network.RollPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        com.star_1mp.powersunchained.power.Rolling.roll(player, payload.kind(), payload.yaw());
                    }
                })
                .playToClient(com.star_1mp.powersunchained.network.RollAnimPayload.TYPE,
                        com.star_1mp.powersunchained.network.RollAnimPayload.STREAM_CODEC, (payload, context) ->
                                com.star_1mp.powersunchained.client.power.RollAnimation.onRoll(payload.entityId(), payload.kind(), payload.yaw()))
                .playToServer(com.star_1mp.powersunchained.network.SenseAlertPayload.TYPE,
                        com.star_1mp.powersunchained.network.SenseAlertPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player && com.star_1mp.powersunchained.power.Superpowers.isActive(player,
                            com.star_1mp.powersunchained.power.PowerAbility.SPIDER_SENSE)) {
                        com.star_1mp.powersunchained.power.PowerSkills.onSenseAlert(player);
                        com.star_1mp.powersunchained.platform.Services.GAMEPLAY.sendToTrackingAndSelf(player,
                                new com.star_1mp.powersunchained.network.SenseAlertSyncPayload(player.getId()));
                    }
                })
                .playToClient(com.star_1mp.powersunchained.network.SenseAlertSyncPayload.TYPE,
                        com.star_1mp.powersunchained.network.SenseAlertSyncPayload.STREAM_CODEC, (payload, context) ->
                                com.star_1mp.powersunchained.client.power.SpiderSenseLayer.onAlert(payload.entityId()))
                .playToServer(com.star_1mp.powersunchained.network.WebRappelPayload.TYPE,
                        com.star_1mp.powersunchained.network.WebRappelPayload.STREAM_CODEC, (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        com.star_1mp.powersunchained.platform.Services.GAMEPLAY.sendToTrackingAndSelf(player,
                                new com.star_1mp.powersunchained.network.WebRappelSyncPayload(player.getId(), payload.on()));
                    }
                })
                .playToClient(com.star_1mp.powersunchained.network.WebRappelSyncPayload.TYPE,
                        com.star_1mp.powersunchained.network.WebRappelSyncPayload.STREAM_CODEC, (payload, context) ->
                                com.star_1mp.powersunchained.client.web.WebRappel.onSync(payload.entityId(), payload.on()))
                .playToClient(WebShotAnimPayload.TYPE, WebShotAnimPayload.STREAM_CODEC,
                        (payload, context) -> WebShotTracker.onShot(payload.entityId(), payload.side())));
        CiServerHook.register();
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            SuitCommand.register(event.getDispatcher());
            com.star_1mp.powersunchained.command.TechTableCommand.register(event.getDispatcher());
            com.star_1mp.powersunchained.command.SuperpowerCommand.register(event.getDispatcher());
        });
    }
}
