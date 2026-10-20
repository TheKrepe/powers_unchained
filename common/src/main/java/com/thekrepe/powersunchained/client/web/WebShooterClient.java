package com.thekrepe.powersunchained.client.web;

import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import com.thekrepe.powersunchained.network.ShootWebPayload;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.web.WebShooting;
import com.thekrepe.powersunchained.web.WebType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Client side of the "Web Shot" ability (ability key 1, default C) for players wearing web-shooters.
 * <ul>
 *   <li>Tap the key: web-shooting mode on; tap again: off.</li>
 *   <li>Hold the key: the web selection menu opens; the mouse wheel picks the web type (up = next, down = previous).</li>
 *   <li>While the mode is on, the use key (right click) fires the selected web instead of using items.</li>
 * </ul>
 */
public final class WebShooterClient {

    /** Held this many ticks the key opens the menu (and releasing it no longer turns the mode off). */
    private static final int HOLD_TICKS = 5;

    private static boolean active;
    private static WebType selected = WebType.IMPACT;
    private static boolean wasDown;
    private static boolean activeAtPress;
    private static int heldTicks;
    private static boolean scrolledWhileHeld;

    private WebShooterClient() {
    }

    public static boolean isActive() {
        return active;
    }

    public static WebType selected() {
        return selected;
    }

    /** The selection menu is shown while the key is held (or right after scrolling). */
    public static boolean isMenuOpen() {
        return active && wasDown && (heldTicks >= HOLD_TICKS || scrolledWhileHeld);
    }

    /** Called at the end of every client tick. */
    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            active = false;
            wasDown = false;
            return;
        }
        boolean has = WebShooting.hasWebShooters(player);
        if (!has) {
            active = false;
        }
        boolean down = PUKeyMappings.ABILITY_1.isDown();
        if (down && !wasDown) {
            activeAtPress = active;
            heldTicks = 0;
            scrolledWhileHeld = false;
            if (!active && has) {
                active = true;
            }
        }
        if (down) {
            heldTicks++;
        } else if (wasDown && activeAtPress && !scrolledWhileHeld && heldTicks < HOLD_TICKS) {
            active = false;  // a short tap while the mode was on turns it off
        }
        wasDown = down;
    }

    /** @return {@code true} if the scroll was used to pick a web type (then the hotbar must not scroll) */
    public static boolean onScroll(double deltaY) {
        if (!active || !PUKeyMappings.ABILITY_1.isDown() || deltaY == 0) {
            return false;
        }
        selected = WebType.byId(selected.ordinal() + (deltaY > 0 ? 1 : -1));
        scrolledWhileHeld = true;
        return true;
    }

    /** @return {@code true} if the use key fired a web (then normal item use must be cancelled) */
    public static boolean onUseKey(boolean mainHand) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!active || player == null) {
            return false;
        }
        if (mainHand && !player.getCooldowns().isOnCooldown(WebShooting.cooldownStack())) {
            Services.GAMEPLAY.sendToServer(new ShootWebPayload(selected));
            player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        return true;
    }

    /** 0..1, how much of the cooldown is left. */
    public static float cooldown(float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? 0.0F : player.getCooldowns().getCooldownPercent(WebShooting.cooldownStack(), partialTick);
    }
}
