package com.thekrepe.powersunchained.client.web;

import com.thekrepe.powersunchained.client.ability.AbilityHud;
import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import com.thekrepe.powersunchained.entity.web.WebSwingLine;
import com.thekrepe.powersunchained.network.WebSwingPayload;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.registry.PUSounds;
import com.thekrepe.powersunchained.web.WebCartridges;
import com.thekrepe.powersunchained.web.WebShooting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Client side of web swinging (ability key 3, default B), with Fisk's controls: the key turns web swinging on / off;
 * while on, the use key (right click) with an empty main hand shoots a swing rope, the attack key (left click)
 * lets go of it with a boost, a new rope replaces the old one. Web swinging and web shooting both use the right
 * click, so turning one on turns the other off.
 */
public final class WebSwingClient {

    /** Ticks on the ground after which the rope is let go. */
    private static final int GROUND_TICKS = 6;

    private static boolean active;
    private static boolean hadWebShooters;
    private static int cooldown;
    private static int groundTicks;

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

    /** Called at the end of every client tick. */
    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            active = false;
            hadWebShooters = false;
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
    }

    /** Local player, after it moved this tick: rope physics, letting go after landing. */
    public static void playerTick(Player player) {
        WebSwingLine line = WebSwingLine.of(player);
        if (line == null) {
            groundTicks = 0;
            return;
        }
        line.applySwing(player);
        if (player.onGround() && line.holdsShooter()) {
            if (++groundTicks >= GROUND_TICKS) {
                groundTicks = 0;
                line.letGo();
                Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.DROP));
            }
        } else {
            groundTicks = 0;
        }
    }

    /** @return {@code true} if the use key shot a rope (then normal item use must be cancelled) */
    public static boolean onUseKey(boolean mainHand) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!active || player == null || !player.getMainHandItem().isEmpty()) {
            return false;
        }
        if (mainHand && cooldown == 0) {
            if (!WebCartridges.hasFluid(player)) {
                player.playSound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, 0.5F, 1.6F);
                player.sendOverlayMessage(Component.translatable("message.powers_unchained.no_web_fluid"));
            } else {
                cooldown = WebShooting.SWING_COOLDOWN;
                Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.SHOOT));
            }
        }
        return true;
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
        boolean flying = line.holdsShooter() && !player.onGround() && line.tickCount > 10;
        if (line.getHooked() != null) {
            line.letGo();
        } else {
            line.releaseWithBoost(player);
        }
        Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.RELEASE));
        if (flying) {
            player.playSound(PUSounds.WEB_SWING_WHOOSH.get(), 0.6F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        }
        return true;
    }
}
