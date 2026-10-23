package com.thekrepe.powersunchained.client.web;

import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import com.thekrepe.powersunchained.network.ShootWebPayload;
import com.thekrepe.powersunchained.network.WebZipPayload;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.registry.PUSounds;
import com.thekrepe.powersunchained.web.WebAmmo;
import com.thekrepe.powersunchained.web.WebCartridges;
import com.thekrepe.powersunchained.web.WebShooting;
import com.thekrepe.powersunchained.web.WebType;
import com.thekrepe.powersunchained.client.ability.AbilityHud;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

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

    private static boolean active;
    private static WebType selected = WebType.IMPACT;
    private static boolean wasDown;
    private static boolean scrolledWhileHeld;
    private static int scrollDirection;
    private static float wheel;
    private static float prevWheel;
    private static boolean hadWebShooters;
    private static int zipCooldown;
    /** Dev/CI only: keeps the wheel open without holding the key. */
    private static boolean debugMenu;
    /** Client copy of the shots left, for the HUD (the server runs the same rules). */
    private static final WebAmmo AMMO = new WebAmmo();

    private WebShooterClient() {
    }

    /** Sample abilities for the HUD settings preview when the player has none. */
    public static AbilityHud.Entry[] previewEntries() {
        return new AbilityHud.Entry[] {
            new AbilityHud.Entry(Component.translatable("ability.powers_unchained.web_shot"), Component.literal("64").withStyle(ChatFormatting.GRAY), true),
            new AbilityHud.Entry(Component.translatable("ability.powers_unchained.web_zip"), null, true)
        };
    }

    /** Puts "Web Shot" on ability key 1 in the ability HUD while web-shooters are worn, with the web fluid left. */
    public static void registerHud() {
        AbilityHud.register((player, slot) -> {
            if (slot == 1 && hadWebShooters) {
                return new AbilityHud.Entry(Component.translatable("ability.powers_unchained.web_zip"), null, zipCooldown == 0);
            }
            if (slot != 0 || !hadWebShooters) {
                return null;
            }
            Component extra = null;
            if (!player.getAbilities().instabuild) {
                int fluid = WebCartridges.totalFluid(WebCartridges.wornShooters(player));
                extra = Component.literal(String.valueOf(fluid)).withStyle(fluid > 0 ? ChatFormatting.GRAY : ChatFormatting.RED);
            }
            return new AbilityHud.Entry(Component.translatable("ability.powers_unchained.web_shot"), extra, active);
        });
    }

    /** Turns web shooting off (web swinging was turned on). */
    public static void deactivate() {
        active = false;
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
        if (zipCooldown > 0) {
            zipCooldown--;
        }
        boolean wasActive = active;
        boolean has = WebShooting.hasWebShooters(player) || debugMenu;
        hadWebShooters = has;
        if (!has) {
            active = false;
        }
        while (PUKeyMappings.ABILITY_2.consumeClick()) {
            if (has) {
                tryZip(player);
            }
        }
        boolean down = has && PUKeyMappings.ABILITY_1.isDown();
        if (down && !wasDown) {
            scrolledWhileHeld = false;
        }
        if (!down && wasDown && !scrolledWhileHeld) {
            active = !active;  // released without scrolling: toggle
            if (active) {
                WebSwingClient.setActive(false);  // both use the right click
            }
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

    /** Web Zip (ability key 2): empty main hand, off cooldown, web fluid needed. */
    private static void tryZip(LocalPlayer player) {
        if (!player.getMainHandItem().isEmpty() || zipCooldown > 0) {
            return;
        }
        if (!WebCartridges.hasFluid(player)) {
            noFluid(player);
            return;
        }
        zipCooldown = WebShooting.ZIP_COOLDOWN;
        Services.GAMEPLAY.sendToServer(WebZipPayload.INSTANCE);
    }

    private static void noFluid(LocalPlayer player) {
        player.playSound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, 0.5F, 1.6F);
        player.sendOverlayMessage(Component.translatable("message.powers_unchained.no_web_fluid"));
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
        WebSwingClient.setActive(false);
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
        if (!WebCartridges.hasFluid(player)) {
            if (mainHand) {
                noFluid(player);
            }
            return true;
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
