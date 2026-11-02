package com.star_1mp.powersunchained.client.irradiation;

import com.star_1mp.powersunchained.client.power.SpiderSenseLayer;
import com.star_1mp.powersunchained.power.Superpower;
import com.star_1mp.powersunchained.power.Superpowers;
import com.star_1mp.powersunchained.registry.PUEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Client side of the Radioactive Spider's bite (owner): the bite = 1 s of a red, shaking screen; the 30 s fever = dark
 * pulsing edges; its end = the first spider-sense (chime + bolts round the head) and the "you got the powers" toast.
 * Everything is found from the synced fever effect / power data, no packets.
 */
public final class FeverClient {

    private static final int FLASH_TICKS = 20;
    private static int flash;
    private static boolean hadFever;
    /** Ticks left to wait for the synced power after the fever ended. */
    private static int awaitPower;
    private static @Nullable LocalPlayer lastPlayer;
    private static int sinceJoin;
    private static long ticks;

    private FeverClient() {
    }

    public static void tick() {
        ticks++;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            lastPlayer = null;
            return;
        }
        if (player != lastPlayer) {
            lastPlayer = player;
            sinceJoin = 0;
        }
        sinceJoin++;
        boolean fever = player.hasEffect(PUEffects.RADIOACTIVE_FEVER.get());
        if (fever && !hadFever && sinceJoin > 20) {
            flash = FLASH_TICKS;  // just bitten
        }
        if (!fever && hadFever) {
            awaitPower = 60;
        }
        hadFever = fever;
        if (flash > 0) {
            flash--;
        }
        if (awaitPower > 0) {
            awaitPower--;
            if (Superpowers.data(player).has(Superpower.SPIDERMAN)) {
                awaitPower = 0;
                awaken(player);
            }
        }
    }

    /** The first spider-sense and the toast. */
    private static void awaken(LocalPlayer player) {
        SpiderSenseLayer.burst(player.getId());
        com.star_1mp.powersunchained.platform.Services.GAMEPLAY.sendToServer(
                new com.star_1mp.powersunchained.network.SenseAlertPayload(player.getId()));
        player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.7F);
        player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.9F);
        player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.25F);
        player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 0.7F, 1.6F);
        Minecraft.getInstance().gui.toastManager().addToast(new PowerGainedToast());
    }

    /** Dev/CI: show the bite flash now. */
    public static void debugFlash() {
        flash = FLASH_TICKS;
    }

    /** GUI layer: the red bite flash and the fever's dark edges. */
    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int w = g.guiWidth();
        int h = g.guiHeight();
        float pt = delta.getGameTimeDeltaPartialTick(false);
        if (player.hasEffect(PUEffects.RADIOACTIVE_FEVER.get())) {
            float pulse = 0.75F + 0.25F * Mth.sin((ticks + pt) * 0.12F);
            int a = (int) (0x70 * pulse);
            int edge = Math.max(24, w / 7);
            int dark = a << 24 | 0x06100A;
            g.fillGradient(0, 0, w, edge, dark, 0x00000000);
            g.fillGradient(0, h - edge, w, h, 0x00000000, dark);
            // left / right: thin strips with falling alpha
            for (int i = 0; i < edge; i += 2) {
                int alpha = (int) (a * (1.0F - i / (float) edge));
                g.fill(i, 0, i + 2, h, alpha << 24 | 0x06100A);
                g.fill(w - i - 2, 0, w - i, h, alpha << 24 | 0x06100A);
            }
        }
        if (flash > 0) {
            float f = (flash - pt) / FLASH_TICKS;
            int alpha = (int) (Mth.clamp(f, 0.0F, 1.0F) * 0x80);
            g.fill(0, 0, w, h, alpha << 24 | 0xB00000);
        }
    }

    /** Camera roll / yaw jitter while the bite flash lasts. */
    public static float shakeRoll(float partialTick) {
        if (flash <= 0) {
            return 0.0F;
        }
        float t = ticks + partialTick;
        return Mth.sin(t * 2.7F) * 3.0F * (flash / (float) FLASH_TICKS);
    }

    public static float shakeYaw(float partialTick) {
        if (flash <= 0) {
            return 0.0F;
        }
        float t = ticks + partialTick;
        return Mth.cos(t * 3.1F) * 1.5F * (flash / (float) FLASH_TICKS);
    }
}
