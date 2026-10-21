package com.thekrepe.powersunchained.client.web;

import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import com.thekrepe.powersunchained.network.ShootWebPayload;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.registry.PUSounds;
import com.thekrepe.powersunchained.web.WebAmmo;
import com.thekrepe.powersunchained.web.WebShooting;
import com.thekrepe.powersunchained.web.WebType;
import com.thekrepe.powersunchained.client.ability.AbilityHud;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Client side of the "Web Shot" ability (ability key 1, default C) for players wearing web-shooters, with the
 * controls of Fisk's web-shooter equipment:
 * <ul>
 *   <li>Hold the key: the web wheel opens; the mouse wheel picks the web type (up = next, down = previous) and turns
 *       web shooting on.</li>
 *   <li>Release the key without scrolling: web shooting on / off.</li>
 *   <li>While on, the use key (right click) with an empty main hand fires the selected web. Every web type has a few
 *       shots that recharge one by one.</li>
 * </ul>
 */
public final class WebShooterClient {

    /** Ticks the wheel takes to open / close. */
    private static final float WHEEL_TICKS = 4.0F;
    /** How long the extra "hold + scroll" hint stays after putting the web-shooters on. */
    private static final int HINT_TICKS = 200;

    private static boolean active;
    private static WebType selected = WebType.IMPACT;
    private static boolean wasDown;
    private static boolean scrolledWhileHeld;
    private static int scrollDirection;
    private static float wheel;
    private static float prevWheel;
    private static boolean hadWebShooters;
    private static int hintTimer;
    /** Dev/CI only: keeps the wheel open without holding the key. */
    private static boolean debugMenu;
    /** Client copy of the shots left, for the HUD (the server runs the same rules). */
    private static final WebAmmo AMMO = new WebAmmo();

    private WebShooterClient() {
    }

    /** Puts "Web Shot" on ability key 1 in the ability HUD while web-shooters are worn. */
    public static void registerHud() {
        AbilityHud.register(new AbilityHud.Provider() {
            @Override
            public AbilityHud.@Nullable Entry get(LocalPlayer player, int slot) {
                if (slot != 0 || !hadWebShooters) {
                    return null;
                }
                Component status;
                if (active) {
                    int uses = uses(selected);
                    status = Component.empty().append(selected.displayName()).append(" " + uses + "/" + selected.maxUses())
                            .withStyle(uses > 0 ? ChatFormatting.AQUA : ChatFormatting.RED);
                } else {
                    status = Component.translatable("hud.powers_unchained.off").withStyle(ChatFormatting.DARK_GRAY);
                }
                return new AbilityHud.Entry(Component.translatable("ability.powers_unchained.web_shot"), status, active);
            }

            @Override
            public void hints(LocalPlayer player, List<Component[]> out) {
                if (!hadWebShooters) {
                    return;
                }
                if (hintTimer > 0 || wheel > 0.0F) {
                    out.add(new Component[] {
                        Component.empty().append(PUKeyMappings.ABILITY_1.getTranslatedKeyMessage()).append(" + ")
                                .append(Component.translatable("hint.powers_unchained.scroll")),
                        Component.translatable("hint.powers_unchained.web_shooters.toggle")});
                }
                if (active) {
                    out.add(new Component[] {Component.translatable("hint.powers_unchained.use"),
                        Component.translatable("hint.powers_unchained.web_shooters.shoot")});
                }
            }
        });
    }

    public static boolean isActive() {
        return active;
    }

    public static WebType selected() {
        return selected;
    }

    /** 0..1 how far the web wheel is open (interpolated). */
    public static float wheel(float partialTick) {
        return prevWheel + (wheel - prevWheel) * partialTick;
    }

    /** Last scroll direction (+1 / -1), for the wheel's wrap-around animation. */
    public static int scrollDirection() {
        return scrollDirection;
    }

    /** Dev/CI only (screenshot tests): sets the state as if the player used the key. */
    public static void debugState(boolean on, boolean menu, WebType type) {
        active = on;
        debugMenu = menu;
        selected = type;
    }

    /** Called at the end of every client tick. */
    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        prevWheel = wheel;
        if (player == null) {
            active = false;
            wasDown = false;
            hadWebShooters = false;
            return;
        }
        AMMO.tick();
        if (hintTimer > 0) {
            hintTimer--;
        }
        boolean wasActive = active;
        boolean has = WebShooting.hasWebShooters(player) || debugMenu;
        if (has && !hadWebShooters) {
            hintTimer = HINT_TICKS;
        }
        hadWebShooters = has;
        if (!has) {
            active = false;
        }
        boolean down = has && PUKeyMappings.ABILITY_1.isDown();
        if (down && !wasDown) {
            scrolledWhileHeld = false;
        }
        if (!down && wasDown && !scrolledWhileHeld) {
            active = !active;  // released without scrolling: toggle
        }
        wasDown = down;
        if (down || debugMenu) {
            wheel = Math.min(wheel + 1.0F / WHEEL_TICKS, 1.0F);
        } else {
            wheel = Math.max(wheel - 1.0F / WHEEL_TICKS, 0.0F);
        }
        WebShooterHud.tick();
        if (wasActive != active) {
            player.playSound(PUSounds.WEB_SWITCH.get(), 0.6F, active ? 1.1F : 0.8F);
        }
    }

    /** @return {@code true} if the scroll was used to pick a web type (then the hotbar must not scroll) */
    public static boolean onScroll(double deltaY) {
        if (!wasDown || deltaY == 0) {
            return false;
        }
        scrollDirection = deltaY > 0 ? 1 : -1;
        selected = WebType.byId(selected.ordinal() + scrollDirection);
        scrolledWhileHeld = true;
        active = true;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(PUSounds.WEB_SWITCH.get(), 0.5F, 1.3F);
        }
        return true;
    }

    /** @return {@code true} if the use key fired a web (then normal item use must be cancelled) */
    public static boolean onUseKey(boolean mainHand) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!active || player == null || !player.getMainHandItem().isEmpty()) {
            return false;  // items in the main hand are used normally
        }
        if (mainHand && AMMO.canUse(selected)) {
            AMMO.use(selected);
            Services.GAMEPLAY.sendToServer(new ShootWebPayload(selected));
        }
        return true;
    }

    public static int uses(WebType type) {
        return AMMO.uses(type);
    }

    /** 0..1 progress of the recharging shot of the given type. */
    public static float recharge(WebType type, float partialTick) {
        return AMMO.recharge(type, partialTick);
    }
}
