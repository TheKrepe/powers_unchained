package com.star_1mp.powersunchained.client.power;

import com.star_1mp.powersunchained.power.Superpowers;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Spider-Sense (client side): senses danger around the player — hostile mobs that are after them or very close,
 * projectiles flying at them, a hissing creeper, lit TNT — and warns with a tingle sound and wavy marks around the
 * crosshair pointing towards each threat (the closer / more urgent, the stronger), like the head lines of the games.
 */
public final class SpiderSense {

    private static final double RANGE = 16.0;
    private static final int SOUND_COOLDOWN = 50;
    private static final Map<Integer, Threat> THREATS = new HashMap<>();
    private static int soundCooldown;
    /** Ticks left of the tingle flash after a new threat (and its length). */
    private static int flash;
    private static final int FLASH_TICKS = 18;
    private static boolean debugForce;

    private static final class Threat {
        Vec3 pos;
        float strength;
        float prevStrength;
        float target;
        boolean seen;

        Threat(Vec3 pos) {
            this.pos = pos;
        }
    }

    private SpiderSense() {
    }

    /** Dev/CI only: keep the flash on. */
    public static void debugForce(boolean on) {
        debugForce = on;
        if (on && Minecraft.getInstance().player != null) {
            SpiderSenseLayer.burst(Minecraft.getInstance().player.getId());
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (soundCooldown > 0) {
            soundCooldown--;
        }
        if (flash > 0) {
            flash--;
        }
        if (player == null || mc.level == null || !Superpowers.isActive(player, com.star_1mp.powersunchained.power.PowerAbility.SPIDER_SENSE) || player.isSpectator()) {
            THREATS.clear();
            return;
        }
        for (Threat t : THREATS.values()) {
            t.seen = false;
        }
        boolean fresh = false;
        AABB area = player.getBoundingBox().inflate(RANGE);
        Vec3 eye = player.getEyePosition();
        for (Entity e : mc.level.getEntities(player, area)) {
            float danger = danger(player, e, eye);
            if (danger <= 0.0F) {
                continue;
            }
            Threat t = THREATS.get(e.getId());
            if (t == null) {
                t = new Threat(e.position());
                THREATS.put(e.getId(), t);
                fresh = true;
            }
            t.pos = e.getBoundingBox().getCenter();
            t.target = danger;
            t.seen = true;
        }
        Iterator<Threat> it = THREATS.values().iterator();
        while (it.hasNext()) {
            Threat t = it.next();
            if (!t.seen) {
                t.target = 0.0F;
            }
            t.prevStrength = t.strength;
            t.strength = Mth.approach(t.strength, t.target, t.target > t.strength ? 0.35F : 0.08F);
            if (!t.seen && t.strength <= 0.0F) {
                it.remove();
            }
        }
        if (fresh) {
            flash = FLASH_TICKS;
            // comic bolts over the head, for us and everyone watching
            SpiderSenseLayer.burst(player.getId());
            com.star_1mp.powersunchained.platform.Services.GAMEPLAY.sendToServer(
                    new com.star_1mp.powersunchained.network.SenseAlertPayload(player.getId()));
            if (soundCooldown == 0) {
                soundCooldown = SOUND_COOLDOWN;
                // louder: full volume, a few layers
                player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.7F);
                player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.9F);
                player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.25F);
                player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 0.7F, 1.6F);
            }
        }
    }

    /** The strongest danger sensed right now (0..1). */
    public static float danger() {
        float max = 0.0F;
        for (Threat t : THREATS.values()) {
            max = Math.max(max, t.strength);
        }
        return max;
    }

    /** 0 = harmless, up to 1 = urgent. */
    private static float danger(LocalPlayer player, Entity e, Vec3 eye) {
        if (!e.isAlive()) {
            return 0.0F;
        }
        double dist = e.distanceTo(player);
        if (e instanceof Creeper creeper && creeper.getSwelling(1.0F) > 0.0F && dist < 10.0) {
            return 1.0F;
        }
        if (e instanceof PrimedTnt && dist < 10.0) {
            return 1.0F;
        }
        if (e instanceof Projectile projectile) {
            if (projectile.getOwner() == player || e.onGround()) {
                return 0.0F;
            }
            Vec3 v = e.getDeltaMovement();
            double speed = v.length();
            if (speed < 0.15) {
                return 0.0F;
            }
            // closest approach of its straight path to the player's middle, within ~2 seconds
            Vec3 to = player.getBoundingBox().getCenter().subtract(e.position());
            double t = to.dot(v) / (speed * speed);
            if (t < 0.0 || t > 40.0) {
                return 0.0F;
            }
            double miss = to.subtract(v.scale(t)).length();
            return miss < 2.0 ? (float) Mth.clamp(1.2 - t / 40.0, 0.5, 1.0) : 0.0F;
        }
        if (e instanceof Enemy && e instanceof Mob mob) {
            boolean after = mob.isAggressive();
            if (after && dist < RANGE) {
                return (float) Mth.clamp(1.1 - dist / RANGE, 0.35, 1.0);
            }
            if (dist < 5.0) {
                return 0.4F;
            }
        }
        return 0.0F;
    }

    // ---- HUD ----

    private static final int COLOR = 0xFFC83C;

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || (THREATS.isEmpty() && flash <= 0 && !debugForce)) {
            return;
        }
        float pt = delta.getGameTimeDeltaPartialTick(false);
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        float time = player.tickCount + pt;
        // the tingle: wavy lines at both sides of the head (crosshair) right after a new danger
        float f = debugForce ? 1.0F : Math.max(0.0F, (flash - pt) / FLASH_TICKS);
        if (f > 0.0F) {
            float a = f < 0.8F ? f / 0.8F : 1.0F;
            for (int side = -1; side <= 1; side += 2) {
                for (int k = 0; k < 3; k++) {
                    wave(g, cx + side * (14 + k * 5), cy, side, 8 + k * 3, time, a * (1.0F - k * 0.22F));
                }
            }
        }
        // a mark towards every threat, at the edge of a ring around the crosshair
        float yaw = Mth.lerp(pt, player.yRotO, player.getYRot());
        Vec3 eye = player.getEyePosition(pt);
        for (Threat t : THREATS.values()) {
            float s = Mth.lerp(pt, t.prevStrength, t.strength);
            if (s <= 0.02F) {
                continue;
            }
            Vec3 d = t.pos.subtract(eye);
            float dirYaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0F;
            float rel = Mth.wrapDegrees(dirYaw - yaw) * Mth.DEG_TO_RAD;
            float pulse = 0.75F + 0.25F * Mth.sin(time * 0.6F);
            arc(g, cx, cy, rel, s * pulse);
        }
    }

    /** A short vertical zigzag line of pixels. */
    private static void wave(GuiGraphicsExtractor g, int x, int cy, int side, int half, float time, float alpha) {
        int color = argb(alpha);
        for (int y = -half; y <= half; y++) {
            int dx = Math.round(Mth.sin(y * 0.9F + time * 0.8F) * 1.5F);
            g.fill(x + dx * side, cy + y, x + dx * side + 1, cy + y + 1, color);
        }
    }

    /** Three nested wavy arcs facing the threat ({@code rel} radians, 0 = straight ahead = up). */
    private static void arc(GuiGraphicsExtractor g, int cx, int cy, float rel, float strength) {
        for (int k = 0; k < 3; k++) {
            float radius = 22.0F + k * 4.0F;
            float span = (14.0F + k * 4.0F) * Mth.DEG_TO_RAD;
            int color = argb(strength * (1.0F - k * 0.25F));
            for (float a = -span; a <= span; a += 0.045F) {
                float r = radius + Mth.sin(a * 22.0F + k) * 1.2F;
                float ang = rel + a;
                int x = cx + Math.round(Mth.sin(ang) * r);
                int y = cy - Math.round(Mth.cos(ang) * r);
                g.fill(x, y, x + 1, y + 1, color);
            }
        }
    }

    private static int argb(float alpha) {
        int a = Mth.clamp(Math.round(alpha * 230.0F), 0, 255);
        return a << 24 | COLOR;
    }
}
