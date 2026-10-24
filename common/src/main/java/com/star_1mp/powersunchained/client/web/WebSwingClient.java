package com.star_1mp.powersunchained.client.web;

import com.star_1mp.powersunchained.client.ability.AbilityHud;
import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import com.star_1mp.powersunchained.entity.web.WebSwingLine;
import com.star_1mp.powersunchained.network.WebSwingPayload;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.registry.PUSounds;
import com.star_1mp.powersunchained.web.WebCartridges;
import com.star_1mp.powersunchained.web.WebShooting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Client side of web swinging (ability key 3, default B): the key turns web swinging on / off (like Fisk's); while
 * on, holding the use key (right click) with an empty main hand shoots a rope and swings on it, letting go of the key
 * (or the attack key) releases it with a launch (like Powerborne's / Marvel Legacy's swing). Jump reels the rope in. Web swinging and web shooting both use the right
 * click, so turning one on turns the other off.
 */
public final class WebSwingClient {

    /** Ticks standing on the ground after which a rope that does not hold the player is let go. */
    private static final int GROUND_TICKS = 40;

    private static boolean active;
    private static boolean hadWebShooters;
    private static int cooldown;
    private static int groundTicks;
    private static boolean wasHolding;
    /** Ticks of extra momentum left after a launch. */
    private static int momentumTicks;
    private static final int MOMENTUM_TICKS = 40;
    private static final double MOMENTUM = 1.05;

    private WebSwingClient() {
    }

    public static boolean isActive() {
        return active;
    }

    /** Dev/CI only. */
    public static void debugState(boolean on) {
        active = on;
    }

    /** "Web Swing" on ability key 3 in the ability HUD while web-shooters are worn. */
    public static void registerHud() {
        AbilityHud.register((player, slot) -> slot == 2 && hadWebShooters
                ? new AbilityHud.Entry(Component.translatable("ability.powers_unchained.web_swing"), null, active)
                : null);
    }

    public static void setActive(boolean on) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (active == on) {
            return;
        }
        active = on;
        if (player == null) {
            return;
        }
        player.playSound(on ? PUSounds.WEB_SWING_ENABLE.get() : PUSounds.WEB_SWING_DISABLE.get(), 0.6F, 1.0F);
        if (on) {
            WebShooterClient.deactivate();
        } else {
            WebSwingLine line = WebSwingLine.of(player);
            if (line != null) {
                line.letGo();
                Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.DROP));
            }
        }
    }

    /** Called at the end of every client tick: holding the use key shoots / keeps the rope, letting go releases. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            active = false;
            hadWebShooters = false;
            wasHolding = false;
            return;
        }
        if (cooldown > 0) {
            cooldown--;
        }
        boolean has = WebShooting.hasWebShooters(player);
        hadWebShooters = has;
        if (!has && active) {
            setActive(false);
        }
        while (PUKeyMappings.ABILITY_3.consumeClick()) {
            if (has) {
                setActive(!active);
            }
        }
        boolean holding = active && mc.gui.screen() == null && mc.options.keyUse.isDown() && player.getMainHandItem().isEmpty()
                && !player.isSpectator();
        WebSwingLine line = WebSwingLine.of(player);
        if (holding) {
            // no rope out (or it missed): shoot one, retrying every few ticks like Powerborne's
            if (line == null && cooldown == 0 && WebSwingLine.canReachAnything(player)) {
                if (!WebCartridges.hasFluid(player)) {
                    if (!wasHolding) {
                        player.playSound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, 0.5F, 1.6F);
                        player.sendOverlayMessage(Component.translatable("message.powers_unchained.no_web_fluid"));
                    }
                } else {
                    cooldown = WebShooting.SWING_COOLDOWN;
                    Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.SHOOT));
                }
            }
        } else if (wasHolding && line != null) {
            release(player, line);
        }
        wasHolding = holding;
        // a little more momentum for a while after a launch (vanilla air drag would eat the swing's speed)
        if (momentumTicks > 0) {
            if (player.onGround() || player.isInWater() || WebSwingLine.of(player) != null) {
                momentumTicks = 0;
            } else {
                momentumTicks--;
            }
        }
    }

    private static void release(LocalPlayer player, WebSwingLine line) {
        if (line.getHooked() != null || !line.isCaught()) {
            line.letGo();
            Services.GAMEPLAY.sendToServer(new WebSwingPayload(line.getHooked() != null ? WebSwingPayload.RELEASE : WebSwingPayload.DROP));
            return;
        }
        if (line.releaseWithBoost(player)) {
            momentumTicks = MOMENTUM_TICKS;
            player.playSound(PUSounds.WEB_SWING_WHOOSH.get(), 0.6F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        }
        Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.RELEASE));
    }

    /** Local player, before it moves this tick: rope physics, letting go after standing on the ground a while. */
    public static void playerTick(Player player) {
        if (momentumTicks > 0 && !player.onGround()) {
            net.minecraft.world.phys.Vec3 m = player.getDeltaMovement();
            player.setDeltaMovement(m.x * MOMENTUM, m.y, m.z * MOMENTUM);
            player.resetFallDistance();
        }
        WebSwingLine line = WebSwingLine.of(player);
        if (line == null) {
            groundTicks = 0;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!line.applySwing(player, mc.options.keyJump.isDown())) {
            line.letGo();  // the rope snapped
            Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.DROP));
            return;
        }
        if (player.onGround() && line.holdsShooter() && !mc.options.keyJump.isDown()) {
            if (++groundTicks >= GROUND_TICKS) {
                groundTicks = 0;
                line.letGo();
                Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.DROP));
            }
        } else {
            groundTicks = 0;
        }
    }

    /** @return {@code true} if the use key belongs to web swinging now (then normal item use must be cancelled) */
    public static boolean onUseKey(boolean mainHand) {
        LocalPlayer player = Minecraft.getInstance().player;
        return active && player != null && player.getMainHandItem().isEmpty();
    }

    /** @return {@code true} if the attack key let go of the rope (then the attack must be cancelled) */
    public static boolean onAttackKey() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!active || player == null) {
            return false;
        }
        WebSwingLine line = WebSwingLine.of(player);
        if (line == null || !line.isCaught()) {
            return false;
        }
        release(player, line);
        return true;
    }
}
