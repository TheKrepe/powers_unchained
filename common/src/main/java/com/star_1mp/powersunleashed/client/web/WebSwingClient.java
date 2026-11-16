package com.star_1mp.powersunleashed.client.web;

import com.star_1mp.powersunleashed.client.ability.AbilityHud;
import com.star_1mp.powersunleashed.client.input.PUKeyMappings;
import com.star_1mp.powersunleashed.entity.web.WebSwingLine;
import com.star_1mp.powersunleashed.network.WebSwingPayload;
import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.registry.PUSounds;
import com.star_1mp.powersunleashed.web.WebCartridges;
import com.star_1mp.powersunleashed.web.WebShooting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
    /** Upside down on the rope (Fisk's web rappel), the wheel's rope direction (+1 up / -1 down) and target length. */
    private static boolean rappelling;
    private static int rappelDir;
    private static float rappelTarget = -1.0F;

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
        com.star_1mp.powersunleashed.client.ability.AbilitySlots.register(
                com.star_1mp.powersunleashed.client.ability.AbilitySlots.WEB_SWING, WebShooting::hasWebShooters,
                player -> new AbilityHud.Entry(Component.translatable("ability.powers_unleashed.web_swing"), null, active),
                Component.translatable("ability.powers_unleashed.web_swing"));
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
        if (com.star_1mp.powersunleashed.client.ability.AbilitySlots.clicks(
                com.star_1mp.powersunleashed.client.ability.AbilitySlots.WEB_SWING) % 2 == 1 && has) {
            setActive(!active);
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
                        player.sendOverlayMessage(Component.translatable("message.powers_unleashed.no_web_fluid"));
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
            setRappel(player, false);
            rappelDir = 0;
            rappelTarget = -1.0F;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        rappelTick(player, line, mc);
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

    /**
     * Fisk's web rappel: hanging on a caught rope (not on the ground), sneak flips you upside down once you hang
     * (nearly) still, and keeps you there while held; the mouse wheel then lets the rope out / pulls it in 0.1 a tick
     * (another notch the other way stops); a changed rope is dropped on landing.
     */
    private static void rappelTick(Player player, WebSwingLine line, Minecraft mc) {
        boolean caught = line.isCaught() && line.getHooked() == null && line.holdsShooter();
        double moved = player.position().distanceTo(new net.minecraft.world.phys.Vec3(player.xo, player.yo, player.zo));
        boolean want = caught && !player.onGround() && mc.options.keyShift.isDown() && (rappelling || moved < 0.05);
        setRappel(player, want);
        if (rappelDir != 0) {
            if (WebRappel.timer(player.getId()) < 1.0F) {
                rappelDir = 0;
            } else {
                if (rappelTarget < 0.0F) {
                    rappelTarget = line.length();
                }
                float f = Math.max(rappelTarget - 0.1F * rappelDir, 0.5F);
                if (f != rappelTarget) {
                    rappelTarget = f;
                    line.setLocalLength(f);
                } else {
                    rappelDir = 0;
                }
            }
        }
        if (rappelTarget >= 0.0F && player.onGround()) {
            rappelTarget = -1.0F;
            line.letGo();
            Services.GAMEPLAY.sendToServer(new WebSwingPayload(WebSwingPayload.DROP));
        }
    }

    private static void setRappel(Player player, boolean on) {
        if (rappelling != on) {
            rappelling = on;
            WebRappel.set(player.getId(), on);
            Services.GAMEPLAY.sendToServer(new com.star_1mp.powersunleashed.network.WebRappelPayload(player.getId(), on));
        }
    }

    /** Mouse wheel while upside down: rope out / in. @return whether the wheel was used */
    public static boolean onScroll(double deltaY) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || deltaY == 0 || !rappelling || WebRappel.timer(player.getId()) < 1.0F) {
            return false;
        }
        // wheel up = climb (shorter rope), down = lower
        rappelDir = Mth.clamp(rappelDir + (deltaY > 0 ? 1 : -1), -1, 1);
        return true;
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
