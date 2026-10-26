package com.star_1mp.powersunchained.client.power;

import com.star_1mp.powersunchained.client.ability.AbilityHud;
import com.star_1mp.powersunchained.client.ability.AbilitySlots;
import com.star_1mp.powersunchained.power.PowerAbility;
import com.star_1mp.powersunchained.power.Superpowers;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

/** Client side of the superpowers: keys, per-tick updates and the movement hook (wall climbing, rolls). */
public final class PowerClient {

    private PowerClient() {
    }

    /** Puts the roll and the ability wheel on ability keys (after the web-shooters' by default). */
    public static void register() {
        AbilitySlots.register(AbilitySlots.ROLL, player -> Superpowers.has(player, PowerAbility.ROLL),
                player -> new AbilityHud.Entry(Component.translatable("ability.powers_unchained.roll"), null, RollClient.ready()),
                Component.translatable("ability.powers_unchained.roll"));
        AbilitySlots.register(AbilitySlots.ABILITY_WHEEL, player -> !AbilityWheel.powers(player).isEmpty(),
                player -> new AbilityHud.Entry(Component.translatable("ability.powers_unchained.ability_wheel"), null, true),
                Component.translatable("ability.powers_unchained.ability_wheel"));
    }

    /** End of every client tick (after {@link AbilitySlots#tick}). */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean roll = AbilitySlots.clicks(AbilitySlots.ROLL) > 0;
        AbilitySlots.clicks(AbilitySlots.ABILITY_WHEEL);  // the wheel goes by the key being held
        AbilityWheel.tick();
        if (player != null) {
            RollClient.tick(player, roll && mc.gui.screen() == null);
        }
        ClimbState.tick();
        RollAnimation.tick();
        SpiderSense.tick();
        SpiderSenseLayer.tick();
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
