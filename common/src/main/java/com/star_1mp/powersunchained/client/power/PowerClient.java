package com.star_1mp.powersunchained.client.power;

import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

/** Client side of the superpowers: keys, per-tick updates and the movement hook (wall climbing, rolls). */
public final class PowerClient {

    private PowerClient() {
    }

    /** End of every client tick. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean roll = false;
        while (PUKeyMappings.ROLL.consumeClick()) {
            roll = true;
        }
        while (PUKeyMappings.ABILITY_WHEEL.consumeClick()) {
            if (player != null && mc.gui.screen() == null && !AbilityWheelScreen.powers(player).isEmpty()) {
                mc.gui.setScreen(new AbilityWheelScreen(true));
            }
        }
        if (player != null) {
            RollClient.tick(player, roll && mc.gui.screen() == null);
        }
        ClimbState.tick();
        RollAnimation.tick();
        SpiderSense.tick();
    }

    /** Movement input event of the local player (inside its tick, before vanilla applies the keys). */
    public static void onMovementInput(Player player, ClientInput input) {
        if (player instanceof LocalPlayer local && player == Minecraft.getInstance().player) {
            if (!RollClient.onMovementInput(local, input)) {
                WallClimbClient.onMovementInput(local, input);
            }
        }
    }
}
