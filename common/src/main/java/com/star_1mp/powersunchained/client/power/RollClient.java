package com.star_1mp.powersunchained.client.power;

import com.star_1mp.powersunchained.entity.web.WebSwingLine;
import com.star_1mp.powersunchained.mixin.client.ClientInputAccessor;
import com.star_1mp.powersunchained.network.RollPayload;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.power.Rolling;
import com.star_1mp.powersunchained.power.Superpower;
import com.star_1mp.powersunchained.power.Superpowers;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * The roll of the local player (roll key, default Z): on the ground a quick dodge roll towards the held movement
 * keys (forward without any), in the air a front flip with a little lift — once per jump.
 */
public final class RollClient {

    /** Ground roll length (ticks). */
    public static final int ROLL_TICKS = 10;
    /** Air flip length (ticks). */
    public static final int FLIP_TICKS = 13;
    private static final int COOLDOWN = 16;
    private static final double ROLL_SPEED = 0.62;

    private static int rollTicks;
    private static Vec3 rollDir = Vec3.ZERO;
    private static int cooldown;
    private static boolean flipUsed;

    private RollClient() {
    }

    public static boolean isRolling() {
        return rollTicks > 0;
    }

    /** Client tick: the roll key. */
    public static void tick(LocalPlayer player, boolean keyPressed) {
        if (cooldown > 0) {
            cooldown--;
        }
        if (player.onGround() || WallClimbClient.isClimbing() || WebSwingLine.of(player) != null) {
            flipUsed = false;
        }
        if (!keyPressed || cooldown > 0 || rollTicks > 0 || !Superpowers.isActive(player, Superpower.ROLL)
                || player.isSpectator() || player.getAbilities().flying || player.isPassenger() || player.isFallFlying()
                || player.isInWater() || player.isInLava() || player.isSleeping() || WallClimbClient.isClimbing()) {
            return;
        }
        Vec3 dir = wish(player, player.input.getMoveVector());
        if (dir.lengthSqr() < 0.01) {
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            dir = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        }
        dir = dir.normalize();
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0F;
        if (player.onGround()) {
            rollDir = dir;
            rollTicks = ROLL_TICKS;
            cooldown = COOLDOWN;
            send(player, Rolling.KIND_ROLL, yaw);
            player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.35F, 1.5F);
        } else if (!flipUsed && WebSwingLine.of(player) == null) {
            flipUsed = true;
            cooldown = COOLDOWN;
            Vec3 m = player.getDeltaMovement();
            Vec3 h = new Vec3(m.x, 0.0, m.z).add(dir.scale(0.25));
            double max = Math.max(0.6, Math.sqrt(m.x * m.x + m.z * m.z));
            if (h.length() > max) {
                h = h.normalize().scale(max);
            }
            player.setDeltaMovement(h.x, Math.max(m.y, 0.0) + 0.32, h.z);
            send(player, Rolling.KIND_FLIP, yaw);
            player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.35F, 1.8F);
        }
    }

    private static void send(LocalPlayer player, int kind, float yaw) {
        RollAnimation.start(player.getId(), kind, yaw);
        Services.GAMEPLAY.sendToServer(new RollPayload(player.getId(), kind, yaw));
    }

    /** Movement input event: the dodge roll carries the player, the movement keys wait. */
    public static boolean onMovementInput(LocalPlayer player, ClientInput input) {
        if (rollTicks <= 0) {
            return false;
        }
        int t = ROLL_TICKS - rollTicks;
        rollTicks--;
        double f = 1.0 - t / (double) ROLL_TICKS;
        double speed = ROLL_SPEED * (0.35 + 0.65 * f * f);
        Vec3 m = player.getDeltaMovement();
        player.setDeltaMovement(rollDir.x * speed, m.y, rollDir.z * speed);
        ((ClientInputAccessor) input).powers_unchained$setMoveVector(Vec2.ZERO);
        return true;
    }

    private static Vec3 wish(LocalPlayer player, Vec2 move) {
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        Vec3 left = new Vec3(Mth.cos(yaw), 0.0, Mth.sin(yaw));
        return forward.scale(move.y).add(left.scale(move.x));
    }
}
