package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Web Zip body animations for every player in view, put together from three Spider-Man mods (reference only, own
 * code): the phases and poses of Powerborne Heroes' web zip, Marvel Legacy's {@code web_zip_1} keyframes for the
 * pull, and Fisk's Superheroes' {@code swing_zip} lean, release flips and dives ({@code .fsk} scripts of TheKrepe's
 * port, used with permission).
 * <ol>
 *   <li><b>Shoot</b> (the line flies): right arm aimed where you look, left arm swung back, legs pulled up in the
 *       air (Powerborne {@code web_zip_shoot}).</li>
 *   <li><b>Pull</b> (the line caught): both arms throw forward and yank back, legs kick out to the sides and tuck
 *       (Marvel Legacy {@code web_zip_1}, 15 ticks), the whole body leans into the flight by speed (Fisk
 *       {@code swing_zip}).</li>
 *   <li><b>Release</b> in the air: one of seven flips / spins (Fisk {@code swing_roll}, {@code swing_roll2},
 *       {@code swing_roll5}, {@code swing_springboard}, Powerborne release spins and spread).</li>
 *   <li><b>Dive</b> while falling afterwards, until landing (Fisk {@code swing_dive}, {@code swing_dive2},
 *       Powerborne dive).</li>
 * </ol>
 * Limbs are set after the vanilla pose ({@code PlayerModelMixin}), the whole body right after the body yaw
 * ({@code LivingEntityRendererMixin}).
 */
public final class WebZipAnimation {

    private static final Map<Integer, State> STATES = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    private static final float DEG = Mth.DEG_TO_RAD;

    /** Ticks of Marvel Legacy's pull keyframes. */
    private static final float PULL_TICKS = 15.0F;
    /** Ticks of a release flip. */
    private static final int RELEASE_TICKS = 16;
    private static final int RELEASE_VARIANTS = 11;
    private static final int PULL_VARIANTS = 3;
    /** Ticks of the landing (Fisk's superhero landing) after a fast fall. */
    private static final int LAND_TICKS = 18;
    private static final int DIVE_VARIANTS = 3;

    private WebZipAnimation() {
    }

    /** Client, every tick while the entity has a web zip line out: {@code pulling} once it caught. */
    public static void zip(int entityId, boolean pulling) {
        State s = STATES.computeIfAbsent(entityId, id -> new State());
        s.lineOut = true;
        s.pulling |= pulling;
    }

    /**
     * Dev/CI only (screenshot tests): holds an entity in one pose. {@code phase}: 0 shoot, 1 pull at tick
     * {@code value}, 2 release flip {@code variant} at progress {@code value}, 3 dive {@code variant}.
     */
    public static void debugPose(int entityId, int phase, float value, int variant, Vec3 motion) {
        State s = new State();
        s.frozen = true;
        s.motion = s.prevMotion = motion;
        switch (phase) {
            case 0 -> s.shoot = s.prevShoot = 1.0F;
            case 1 -> {
                s.pull = s.prevPull = 1.0F;
                s.pullAge = (int) value + 1;
            }
            case 2 -> {
                s.releaseKind = variant;
                s.releaseAge = (int) (value * RELEASE_TICKS);
            }
            default -> {
                s.dive = s.prevDive = 1.0F;
                s.diveKind = variant;
            }
        }
        STATES.put(entityId, s);
    }

    /** Client tick. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            STATES.clear();
            return;
        }
        Iterator<Map.Entry<Integer, State>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, State> e = it.next();
            Entity entity = mc.level.getEntity(e.getKey());
            if (entity == null || !entity.isAlive()) {
                it.remove();
                continue;
            }
            if (e.getValue().frozen) {
                continue;
            }
            e.getValue().tick(entity);
            if (e.getValue().idle()) {
                it.remove();
            }
        }
    }

    // ---- easing / helpers ----

    private static float outCubic(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        float u = 1.0F - t;
        return 1.0F - u * u * u;
    }

    private static float inOutSine(float t) {
        return -(Mth.cos(Mth.PI * Mth.clamp(t, 0.0F, 1.0F)) - 1.0F) / 2.0F;
    }

    /** Fisk's {@code curve}. */
    private static float curve(float d) {
        return (Mth.sin((d * 2.0F - 1.0F) * Mth.PI / 2.0F) + 1.0F) / 2.0F;
    }

    private static float lerp(float from, float to, float d) {
        return from + (to - from) * d;
    }

    private static float rotLerp(float from, float to, float d) {
        return from + d * Mth.wrapDegrees((to - from) / DEG) * DEG;
    }

    /** Blends the part's angles towards {@code x, y, z} degrees. */
    private static void rot(ModelPart p, float x, float y, float z, float d) {
        if (d <= 0.0F) {
            return;
        }
        p.xRot = rotLerp(p.xRot, x * DEG, d);
        p.yRot = rotLerp(p.yRot, y * DEG, d);
        p.zRot = rotLerp(p.zRot, z * DEG, d);
    }

    /** Blends the part's position towards {@code x, y, z} (model pixels). */
    private static void pos(ModelPart p, float x, float y, float z, float d) {
        if (d <= 0.0F) {
            return;
        }
        p.x = lerp(p.x, x, d);
        p.y = lerp(p.y, y, d);
        p.z = lerp(p.z, z, d);
    }

    // ---- limbs ----

    public static void applyParts(PlayerModel model, int entityId, float partialTick) {
        State s = STATES.get(entityId);
        if (s == null) {
            return;
        }
        Vec3 mot = s.motion(partialTick);
        float air = 1.0F - s.ground(partialTick);
        float dive = s.dive(partialTick);
        if (dive > 0.0F) {
            applyDive(model, s.diveKind, dive * air, mot);
        }
        float release = s.release(partialTick);
        if (s.releaseKind >= 0 && release < 1.0F) {
            applyReleaseLimbs(model, s.releaseKind, release, s.releaseWeight(partialTick));
        }
        float land = s.land(partialTick);
        if (land > 0.0F) {
            applyLanding(model, land);
        }
        float shoot = s.shoot(partialTick);
        if (shoot > 0.0F) {
            applyShoot(model, outCubic(shoot), air, s.entity);
        }
        float pull = s.pull(partialTick);
        if (pull > 0.0F) {
            switch (s.pullKind) {
                case 0 -> applyPull(model, s.pullTime(partialTick), outCubic(pull), air);
                case 1 -> applyBurst(model, outCubic(pull), air);
                default -> applyReach(model, outCubic(pull), air);
            }
        }
    }

    /** Powerborne {@code web_zip_shoot}: aim the right arm, left arm back, legs up in the air. */
    private static void applyShoot(PlayerModel m, float d, float air, LivingEntity entity) {
        float pitch = entity != null ? Mth.clamp(entity.getXRot(), -90.0F, 90.0F) : 0.0F;
        float yaw = entity != null ? Mth.clamp(Mth.wrapDegrees(entity.getYHeadRot() - entity.yBodyRot), -75.0F, 75.0F) : 0.0F;
        rot(m.rightArm, pitch - 90.0F, yaw, 6.0F, d);
        pos(m.rightArm, -5.0F, 3.0F, -1.0F, d);
        rot(m.leftArm, 65.0F, -8.0F, -16.0F, d);
        pos(m.leftArm, 5.0F, 2.0F, 2.0F, d);
        float legs = d * air;
        rot(m.rightLeg, 60.0F, -15.0F, 4.0F, legs);
        pos(m.rightLeg, -2.0F, 10.0F, -3.0F, legs);
        rot(m.leftLeg, 75.0F, -65.0F, -30.0F, legs);
        pos(m.leftLeg, 3.5F, 8.0F, 0.0F, legs);
    }

    // Marvel Legacy web_zip_1 keyframes: {tick, x, y, z} rotations (degrees) and position offsets (pixels, y up)
    private static final float[][] R_ARM_ROT = {{0, -90, -25, 0}, {2.5F, -108.68F, -27.18F, -10.99F}, {5, -7.35F, -29.36F, -21.98F}, {15, 50.77F, -31.46F, 2.46F}};
    private static final float[][] R_ARM_POS = {{0, 0, 0, -3}, {5, 1, 0, -2}, {15, 1, 0, 0}};
    private static final float[][] L_ARM_ROT = {{0, -90, 22.5F, 0}, {2.5F, -111.14F, 27.06F, 8.41F}, {5, -12.28F, 31.62F, 16.81F}, {15, 41.3F, 31.17F, -15.5F}};
    private static final float[][] L_ARM_POS = {{0, 0, 0, -3}, {5, -1, 0, -2}, {15, -1, 0, 0}};
    private static final float[][] R_LEG_ROT = {{0, -90, 0, 0}, {5, 0, 0, 77.5F}, {15, 9.52F, 3.1F, -14.75F}};
    private static final float[][] R_LEG_POS = {{0, 0, 0, -2}, {4.17F, 0.92F, 2, -0.17F}, {5, 0, 1, 0}, {15, -3, 7, -4}};
    private static final float[][] L_LEG_ROT = {{0, -90, 0, 0}, {5, -0.54F, -2.44F, -77.49F}, {15, 14.13F, -5.08F, 19.37F}};
    private static final float[][] L_LEG_POS = {{0, 0, 0, -2}, {4.17F, -0.83F, 2, -0.17F}, {5, 0, 1, 0}, {15, 4, 7, -5}};

    private static float[] sample(float[][] keys, float t) {
        if (t <= keys[0][0]) {
            return new float[] {keys[0][1], keys[0][2], keys[0][3]};
        }
        for (int i = 1; i < keys.length; i++) {
            if (t <= keys[i][0]) {
                float f = (t - keys[i - 1][0]) / (keys[i][0] - keys[i - 1][0]);
                return new float[] {lerp(keys[i - 1][1], keys[i][1], f), lerp(keys[i - 1][2], keys[i][2], f), lerp(keys[i - 1][3], keys[i][3], f)};
            }
        }
        float[] last = keys[keys.length - 1];
        return new float[] {last[1], last[2], last[3]};
    }

    private static void keyframes(ModelPart p, float[][] rot, float[][] pos, float t, float baseX, float baseY, float baseZ, float d) {
        float[] r = sample(rot, t);
        rot(p, r[0], r[1], r[2], d);
        float[] o = sample(pos, t);
        pos(p, baseX + o[0], baseY - o[1], baseZ + o[2], d);
    }

    /** Marvel Legacy {@code web_zip_1}: arms throw forward and yank back, legs kick out and tuck. */
    private static void applyPull(PlayerModel m, float t, float d, float air) {
        keyframes(m.rightArm, R_ARM_ROT, R_ARM_POS, t, -5.0F, 2.0F, 0.0F, d);
        keyframes(m.leftArm, L_ARM_ROT, L_ARM_POS, t, 5.0F, 2.0F, 0.0F, d);
        keyframes(m.rightLeg, R_LEG_ROT, R_LEG_POS, t, -1.9F, 12.0F, 0.0F, d * air);
        keyframes(m.leftLeg, L_LEG_ROT, L_LEG_POS, t, 1.9F, 12.0F, 0.0F, d * air);
        m.head.xRot = lerp(m.head.xRot, m.head.xRot - 10.0F * DEG, d * air);  // Powerborne: chin up into the pull
    }

    /** Powerborne {@code web_zip_burst}: right arm flung out back, left arm in, legs tucked, chin up. */
    private static void applyBurst(PlayerModel m, float d, float air) {
        rot(m.rightArm, 80.0F, 30.0F, 90.0F, d);
        pos(m.rightArm, -4.0F, 2.0F, 4.0F, d);
        rot(m.leftArm, 35.0F, 20.0F, -16.0F, d);
        pos(m.leftArm, 4.5F, 2.0F, -1.0F, d);
        float legs = d * air;
        rot(m.rightLeg, 27.0F, 40.0F, 5.0F, legs);
        pos(m.rightLeg, -5.0F, 6.0F, 1.0F, legs);
        rot(m.leftLeg, 62.0F, -20.0F, 0.0F, legs);
        pos(m.leftLeg, 5.0F, 10.0F, -1.0F, legs);
        m.head.xRot = lerp(m.head.xRot, m.head.xRot - 10.0F * DEG, legs);
    }

    /** Superman reach: the right arm stays stretched along the line, the left swept back, legs trailing. */
    private static void applyReach(PlayerModel m, float d, float air) {
        rot(m.rightArm, -165.0F, -10.0F, 10.0F, d);
        pos(m.rightArm, -5.0F, 2.0F, -1.0F, d);
        rot(m.leftArm, 40.0F, 10.0F, -25.0F, d);
        float legs = d * air;
        rot(m.rightLeg, 15.0F, 0.0F, 4.0F, legs);
        rot(m.leftLeg, 30.0F, 0.0F, -4.0F, legs);
        pos(m.leftLeg, 1.9F, 11.0F, 1.5F, legs);
        m.head.xRot = lerp(m.head.xRot, m.head.xRot - 25.0F * DEG, legs);
    }

    /** Fisk's {@code superhero_landing}: one knee down, a hand on the ground. */
    private static void applyLanding(PlayerModel m, float w) {
        float d = curve(w);
        float k = Mth.sin(Mth.PI * d);
        rot(m.head, -45.0F, -35.0F, 0.0F, d);
        pos(m.head, 0.5F, 0.0F, -1.0F, d);
        m.body.xRot = lerp(m.body.xRot, -5.0F * DEG, d);
        rot(m.rightArm, -40.0F, 30.0F, 60.0F, d);
        pos(m.rightArm, -3.0F, 5.0F, 2.5F, d);
        rot(m.leftArm, -60.0F, -10.0F, -30.0F, d);
        pos(m.leftArm, 4.0F, 3.0F, -1.0F, d);
        rot(m.rightLeg, -60.0F, -5.0F, -10.0F, d);
        pos(m.rightLeg, -3.0F, 8.0F, -0.5F, d);
        m.rightLeg.xRot += 40.0F * DEG * k;
        m.rightLeg.y -= 2.0F * k;
        m.rightLeg.z -= 4.0F * k;
        rot(m.leftLeg, 5.0F, -30.0F, 0.0F, d);
        pos(m.leftLeg, 4.0F, 10.0F, -4.0F, d);
        m.leftLeg.xRot += 25.0F * DEG * k;
        m.leftLeg.z -= 2.0F * k;
    }

    /** Limbs of the release flips. */
    private static void applyReleaseLimbs(PlayerModel m, int kind, float progress, float w) {
        switch (kind) {
            case 0, 1, 2, 3 -> { // Fisk: swing_roll, swing_roll2, swing_roll5, swing_springboard
                float d = w * (float) Math.pow(Mth.sin(Mth.PI * (1 - progress)), kind == 1 ? 3 : 2);
                switch (kind) {
                    case 0 -> {
                        m.head.xRot = lerp(m.head.xRot, -0.7F, d);
                        rot(m.rightArm, -1.0F / DEG, 1.5F / DEG, 0.4F / DEG, d);
                        rot(m.leftArm, -1.0F / DEG, -1.5F / DEG, -0.4F / DEG, d);
                        m.rightLeg.xRot = lerp(m.rightLeg.xRot, 0.4F, d);
                        m.leftLeg.xRot = lerp(m.leftLeg.xRot, 0.6F, d);
                    }
                    case 1 -> {
                        m.head.xRot = lerp(m.head.xRot, Math.max(m.head.xRot - 0.4F, -90 * DEG), d);
                        rot(m.rightArm, 1.0F / DEG, -0.8F / DEG, 0.5F / DEG, d);
                        rot(m.leftArm, 1.0F / DEG, 0.9F / DEG, -0.3F / DEG, d);
                        rot(m.rightLeg, 1.5F / DEG, 0.0F, 0.4F / DEG, d);
                        rot(m.leftLeg, 1.8F / DEG, 0.0F, 0.3F / DEG, d);
                    }
                    case 2 -> {
                        m.head.xRot = lerp(m.head.xRot, 0.7F, d);
                        rot(m.rightArm, -0.5F / DEG, 0.0F, -0.1F / DEG, d);
                        rot(m.leftArm, -0.5F / DEG, 0.0F, 0.1F / DEG, d);
                        m.rightLeg.y = lerp(m.rightLeg.y, 0.5F, d);
                        m.rightLeg.x = lerp(m.rightLeg.x, -1.5F, d);
                        m.rightLeg.xRot = lerp(m.rightLeg.xRot, -0.6F, d);
                        m.leftLeg.y = lerp(m.leftLeg.y, -0.5F, d);
                        m.leftLeg.x = lerp(m.leftLeg.x, 1.5F, d);
                        m.leftLeg.xRot = lerp(m.leftLeg.xRot, -0.6F, d);
                    }
                    default -> {
                        m.head.xRot = lerp(m.head.xRot, 0.7F, d);
                        rot(m.rightArm, 0.6F / DEG, 0.3F / DEG, 0.8F / DEG, d);
                        rot(m.leftArm, 0.6F / DEG, -0.3F / DEG, -0.8F / DEG, d);
                        m.rightLeg.xRot = lerp(m.rightLeg.xRot, -0.5F, d);
                        m.leftLeg.xRot = lerp(m.leftLeg.xRot, -0.8F, d);
                    }
                }
            }
            case 4, 5 -> { // Powerborne: spin around, arms out
                float d = w * outCubic(Math.min(progress * 2.0F, 1.0F)) * (1.0F - outCubic(Math.max(progress * 2.0F - 1.0F, 0.0F)));
                float s = kind == 4 ? 1.0F : -1.0F;
                m.head.yRot = rotLerp(m.head.yRot, -30.0F * DEG * s, d);
                rot(m.rightArm, 6.0F, 30.0F, -15.0F, d);
                rot(m.leftArm, -1.0F, 30.0F, 15.0F, d);
                rot(m.rightLeg, -15.0F, 0.0F, -5.0F, d);
                rot(m.leftLeg, 15.0F, -15.0F, 5.0F, d);
            }
            case 7 -> { // Fisk swing_roll3: half flip with a twist
                float d = w * (float) Math.pow(Mth.sin(Mth.PI * (1 - progress)), 2);
                m.head.xRot = lerp(m.head.xRot, 0.8F, d);
                rot(m.rightArm, 1.5F / DEG, 0.5F / DEG, 0.4F / DEG, d);
                rot(m.leftArm, -1.5F / DEG, 0.5F / DEG, -0.4F / DEG, d);
                m.rightLeg.xRot = lerp(m.rightLeg.xRot, -0.4F, d);
                m.leftLeg.xRot = lerp(m.leftLeg.xRot, 0.2F, d);
            }
            case 8, 9 -> { // Fisk swing_rotate_right / swing_rotate_right1: corkscrew / cartwheel
                float d = w * (float) Math.pow(Mth.sin(Mth.PI * (1 - progress)), 3);
                m.head.xRot = lerp(m.head.xRot, Math.max(m.head.xRot - 0.4F, -90 * DEG), d);
                if (kind == 8) {
                    rot(m.rightArm, 0.0F, 0.5F / DEG, 0.5F / DEG, d);
                    rot(m.leftArm, 0.0F, -0.4F / DEG, -0.3F / DEG, d);
                    rot(m.rightLeg, 0.2F / DEG, 0.0F, 0.4F / DEG, d);
                    rot(m.leftLeg, 0.4F / DEG, 0.0F, 0.3F / DEG, d);
                } else {
                    rot(m.rightArm, 1.0F / DEG, -0.8F / DEG, 0.4F / DEG, d);
                    rot(m.leftArm, 1.0F / DEG, 0.9F / DEG, -0.3F / DEG, d);
                    rot(m.rightLeg, 0.3F / DEG, 0.0F, 1.2F / DEG, d);
                    rot(m.leftLeg, 0.5F / DEG, 0.0F, 1.0F / DEG, d);
                }
            }
            case 10 -> { // Fisk falcon_dive_roll: tucked forward roll
                float d = w * (float) Math.pow(Mth.sin(Mth.PI * (1 - progress)), 2);
                m.head.y += d;
                m.head.z -= d;
                m.head.xRot = lerp(m.head.xRot, 60 * DEG, d);
                rot(m.rightArm, -55.0F, -30.0F, -7.0F, d);
                rot(m.leftArm, -65.0F, 40.0F, -20.0F, d);
                pos(m.rightLeg, -2.25F, 3.5F, -1.5F, d);
                m.rightLeg.xRot = lerp(m.rightLeg.xRot, -30 * DEG, d);
                m.rightLeg.zRot = lerp(m.rightLeg.zRot, -2 * DEG, d);
                pos(m.leftLeg, 2.25F, 2.5F, -1.5F, d);
                m.leftLeg.xRot = lerp(m.leftLeg.xRot, -30 * DEG, d);
                m.leftLeg.zRot = lerp(m.leftLeg.zRot, 5 * DEG, d);
            }
            default -> { // Powerborne: spread out
                float d = w * (float) Math.sin(Mth.PI * progress);
                rot(m.rightArm, 1.0F, 24.0F, 73.0F, d);
                rot(m.leftArm, 1.0F, -24.0F, -73.0F, d);
                rot(m.rightLeg, 47.0F, 1.0F, 12.0F, d);
                rot(m.leftLeg, 47.0F, -1.0F, -12.0F, d);
            }
        }
    }

    private static void applyDive(PlayerModel m, int kind, float data, Vec3 mot) {
        float horVel = (float) Math.sqrt(mot.x * mot.x + mot.z * mot.z);
        float pitch = (float) Math.atan2(mot.y, horVel);
        data = curve(data);
        float mm = curve(Mth.clamp((float) -mot.y, 0.0F, 2.0F) / 2.0F);
        float x = data * curve(Mth.clamp((float) -mot.y, 0.0F, 1.0F));
        switch (kind) {
            case 0 -> { // Fisk swing_dive
                m.head.xRot = lerp(m.head.xRot, Math.max(0.6F * pitch, -90 * DEG), x);
                m.rightArm.xRot = lerp(m.rightArm.xRot, 0.1F + 0.5F * mm, x);
                m.rightArm.yRot = rotLerp(m.rightArm.yRot, 0.5F, mm * data);
                m.rightArm.zRot = lerp(m.rightArm.zRot, 0.6F, mm * data);
                m.leftArm.xRot = lerp(m.leftArm.xRot, 0.1F + 0.5F * mm, x);
                m.leftArm.yRot = rotLerp(m.leftArm.yRot, -0.4F, mm * data);
                m.leftArm.zRot = lerp(m.leftArm.zRot, -0.6F, mm * data);
                m.rightLeg.xRot = lerp(m.rightLeg.xRot, 0.05F + 0.6F * mm, x);
                m.rightLeg.zRot = lerp(m.rightLeg.zRot, 0.1F, mm * data);
                m.leftLeg.xRot = lerp(m.leftLeg.xRot, 0.1F + 0.4F * mm, x);
                m.leftLeg.zRot = lerp(m.leftLeg.zRot, -0.1F, mm * data);
            }
            case 1 -> { // Fisk swing_dive2
                m.head.xRot = lerp(m.head.xRot, Math.min(-0.3F * pitch, 90 * DEG), x);
                rot(m.rightArm, 20 + 45 * mm, -35 + 20 * mm, 25 * mm, x);
                rot(m.leftArm, 20 + 45 * mm, 35 - 20 * mm, -25 * mm, x);
                rot(m.rightLeg, 45.0F, 16.0F, 13.0F, x);
                rot(m.leftLeg, 40.0F, -18.0F, -8.0F, x);
                pos(m.rightLeg, -3.0F, 9.0F, -4.9F, x);
                pos(m.leftLeg, 3.0F, 11.0F, -2.9F, x);
            }
            default -> { // Powerborne dive: arms forward-down, legs trailing
                float tilt = -Mth.clamp((float) -mot.y, 0.0F, 2.5F) * 50.0F;
                float d = outCubic(data);
                rot(m.rightArm, 60.0F - tilt * 0.4F, -15.0F, 15.0F, d);
                rot(m.leftArm, 60.0F - tilt * 0.4F, 15.0F, -15.0F, d);
                rot(m.rightLeg, 50.0F, 15.0F, 0.0F, d);
                pos(m.rightLeg, -4.0F, 9.0F, -1.0F, d);
                rot(m.leftLeg, 30.0F, -20.0F, 0.0F, d);
                pos(m.leftLeg, 4.0F, 9.0F, -1.0F, d);
            }
        }
    }

    // ---- whole body ----

    /** Whole body rotations, in the old {@code rotateCorpse} space (after the body yaw, y up, pivot y * 1.8 blocks). */
    public static void applyBody(PoseStack poseStack, int entityId, float partialTick, float scale) {
        State s = STATES.get(entityId);
        if (s == null) {
            return;
        }
        Vec3 mot = s.motion(partialTick);
        float horVel = (float) Math.sqrt(mot.x * mot.x + mot.z * mot.z);
        float pitchDeg = (float) Math.toDegrees(Math.atan2(mot.y, horVel));
        float air = 1.0F - s.ground(partialTick);

        float dive = s.dive(partialTick) * air;
        if (dive > 0.0F) {
            float data = curve(dive);
            float mm = curve(Mth.clamp((float) -mot.y, 0.0F, 2.0F) / 2.0F);
            float angle = switch (s.diveKind) {
                case 0 -> 0.8F * mm * data * (pitchDeg - 90.0F);
                case 1 -> 0.2F * mm * data * (pitchDeg - 90.0F);
                default -> -10.0F * data;
            };
            rotateX(poseStack, scale, 0.5F, angle);
        }
        float pull = s.pull(partialTick) * air;
        float shoot = s.shoot(partialTick) * air;
        if (pull > 0.0F || shoot > 0.0F) {
            // Powerborne's slight forward tilt while shooting; pulled: Fisk's swing_zip lean by speed, toned down
            // towards Marvel Legacy's (up to 65 degrees instead of lying flat)
            float vel = curve(Math.min(2.0F * horVel, 1.0F));
            rotateX(poseStack, scale, 0.5F, -10.0F * outCubic(Math.max(shoot, pull)) - 55.0F * vel * outCubic(pull));
        }
        float release = s.release(partialTick);
        if (s.releaseKind >= 0 && release < 1.0F) {
            // landing mid-flip: the flip unwinds over a few ticks instead of snapping upright
            float w = s.releaseWeight(partialTick);
            float x = curve(release);
            float bell = (float) Math.pow(Mth.sin(Mth.PI * release), 2);
            switch (s.releaseKind) {
                case 0, 1 -> rotateX(poseStack, scale, 0.5F, w * 360.0F * x);
                case 2 -> rotateX(poseStack, scale, 0.6F, w * 360.0F * x);
                case 3 -> rotateX(poseStack, scale, 0.3F + 0.2F * bell, w * -360.0F * x);
                case 4 -> rotateY(poseStack, w * 360.0F * outSine(release));
                case 5 -> rotateY(poseStack, w * -360.0F * outSine(release));
                case 7 -> {
                    float x1 = curve((float) Math.pow(Mth.sin(Mth.PI * (1 - release)), 1.5));
                    float x2 = release > 0.3F && release <= 0.8F ? curve(Math.min((release - 0.3F) / 0.5F, 1.0F)) : 0.0F;
                    rotateX(poseStack, scale, 0.5F, w * 160.0F * x1);
                    rotate(poseStack, scale, 0.5F, w * -360.0F * x2, 0.0F, 1.0F, 0.0F);
                }
                case 8 -> rotate(poseStack, scale, 0.5F, w * -360.0F * x, -1.0F, 1.0F, 0.0F);
                case 9 -> rotate(poseStack, scale, 0.5F, w * -360.0F * x, 0.4F, 0.0F, 1.0F);
                case 10 -> {
                    rotateX(poseStack, scale, 0.6F, w * -360.0F * x);
                    poseStack.translate(0.0F, -0.15F * bell * w * scale, 0.1F * bell * w * scale);
                }
                default -> rotateX(poseStack, scale, 0.55F, w * -10.0F * (float) Math.sin(Mth.PI * release));
            }
        }
        float land = s.land(partialTick);
        if (land > 0.0F) {
            // Fisk superhero_landing
            float d = curve(land);
            poseStack.translate(0.0F, -0.35F * d * scale, 0.25F * d * scale);
            rotate(poseStack, scale, 0.56F, 25.0F * d, 0.0F, 0.0F, 1.0F);
            rotate(poseStack, scale, 0.56F, -20.0F * d, 0.0F, 1.0F, 0.0F);
            rotate(poseStack, scale, 0.56F, -75.0F * d, 1.0F, 0.0F, 0.0F);
        }
    }

    private static void rotate(PoseStack poseStack, float scale, float pivotY, float angleDeg, float ax, float ay, float az) {
        float l = Mth.sqrt(ax * ax + ay * ay + az * az);
        if (angleDeg == 0.0F || l < 1.0E-6F) {
            return;
        }
        float py = pivotY * 1.8F * scale;
        poseStack.translate(0.0F, py, 0.0F);
        poseStack.rotate(new Quaternionf().rotationAxis(angleDeg * DEG, ax / l, ay / l, az / l));
        poseStack.translate(0.0F, -py, 0.0F);
    }

    private static float outSine(float t) {
        return Mth.sin(Mth.clamp(t, 0.0F, 1.0F) * Mth.PI / 2.0F);
    }

    private static void rotateX(PoseStack poseStack, float scale, float pivotY, float angleDeg) {
        if (angleDeg == 0.0F) {
            return;
        }
        float py = pivotY * 1.8F * scale;
        poseStack.translate(0.0F, py, 0.0F);
        poseStack.rotate(new Quaternionf().rotationX(angleDeg * DEG));
        poseStack.translate(0.0F, -py, 0.0F);
    }

    private static void rotateY(PoseStack poseStack, float angleDeg) {
        poseStack.rotate(new Quaternionf().rotationY(angleDeg * DEG));
    }

    // ---- state ----

    private static final class State {
        boolean frozen;
        LivingEntity entity;
        boolean lineOut;
        boolean pulling;
        boolean wasPulling;
        float shoot;
        float prevShoot;
        float pull;
        float prevPull;
        int pullAge;
        float ground;
        float prevGround;
        int releaseKind = -1;
        int releaseAge;
        float releaseWeight = 1.0F;
        float prevReleaseWeight = 1.0F;
        int pullKind;
        int landAge = -1;
        boolean wasOnGround = true;
        float dive;
        float prevDive;
        int diveKind;
        Vec3 motion = Vec3.ZERO;
        Vec3 prevMotion = Vec3.ZERO;
        int idleTicks;

        void tick(Entity e) {
            this.entity = e instanceof LivingEntity living ? living : null;
            this.prevMotion = this.motion;
            this.motion = e.position().subtract(e.xo, e.yo, e.zo);
            boolean onGround = e.onGround();
            this.prevShoot = this.shoot;
            this.prevPull = this.pull;
            this.prevDive = this.dive;
            this.prevGround = this.ground;
            this.ground = Mth.clamp(this.ground + (onGround ? 0.2F : -0.25F), 0.0F, 1.0F);
            this.prevReleaseWeight = this.releaseWeight;
            // landing after a fast fall from a zip / flip / dive: superhero landing
            if (onGround && !this.wasOnGround && this.prevMotion.y < -0.35) {
                this.landAge = 0;
            } else if (this.landAge >= 0 && (++this.landAge > LAND_TICKS || !onGround && this.landAge > 2)) {
                this.landAge = -1;
            }

            // shoot: the line is flying (Powerborne: 6 ticks in)
            boolean shooting = this.lineOut && !this.pulling;
            this.shoot = Mth.clamp(this.shoot + (shooting ? 1.0F / 3.0F : -1.0F / 3.0F), 0.0F, 1.0F);
            // pull: Marvel Legacy keyframes from the moment it caught, faded in fast, out over 5 ticks
            if (this.pulling) {
                if (!this.wasPulling) {
                    this.pullAge = 0;
                    this.pullKind = RANDOM.nextInt(PULL_VARIANTS);
                }
                this.pullAge++;
                this.pull = Math.min(this.pull + 0.5F, 1.0F);
            } else {
                this.pull = Math.max(this.pull - 0.2F, 0.0F);
                if (this.pull > 0.0F) {
                    this.pullAge++;
                }
            }
            // letting go in the air: a flip / spin
            if (this.wasPulling && !this.pulling && !onGround) {
                this.releaseKind = RANDOM.nextInt(RELEASE_VARIANTS);
                this.releaseAge = 0;
                this.releaseWeight = this.prevReleaseWeight = 1.0F;
            }
            if (this.releaseKind >= 0) {
                if (onGround) {
                    this.releaseWeight = Math.max(this.releaseWeight - 0.25F, 0.0F);  // landed: unwind smoothly
                }
                if (++this.releaseAge > RELEASE_TICKS || this.releaseWeight <= 0.0F && this.prevReleaseWeight <= 0.0F) {
                    this.releaseKind = -1;
                }
            }
            // falling afterwards: dive until landing
            boolean diving = !onGround && !this.lineOut && this.releaseKind < 0 && this.motion.y < -0.12;
            if (diving) {
                if (this.dive == 0.0F) {
                    this.diveKind = RANDOM.nextInt(DIVE_VARIANTS);
                }
                this.dive = Math.min(this.dive + 0.12F, 1.0F);
            } else {
                this.dive = Math.max(this.dive - (onGround ? 0.2F : 0.1F), 0.0F);
            }
            this.wasPulling = this.pulling;
            this.wasOnGround = onGround;
            this.idleTicks = this.lineOut || !onGround ? 0 : this.idleTicks + 1;
            this.lineOut = false;
            this.pulling = false;
        }

        boolean idle() {
            return this.idleTicks > 10 && this.shoot == 0.0F && this.pull == 0.0F && this.dive == 0.0F
                    && this.prevDive == 0.0F && this.releaseKind < 0 && this.landAge < 0;
        }

        Vec3 motion(float pt) {
            return this.prevMotion.lerp(this.motion, pt);
        }

        float shoot(float pt) {
            return Mth.lerp(pt, this.prevShoot, this.shoot);
        }

        float pull(float pt) {
            return Mth.lerp(pt, this.prevPull, this.pull);
        }

        float pullTime(float pt) {
            return Math.min(this.pullAge - 1 + pt, PULL_TICKS);
        }

        float ground(float pt) {
            return Mth.lerp(pt, this.prevGround, this.ground);
        }

        float dive(float pt) {
            return Mth.lerp(pt, this.prevDive, this.dive);
        }

        float releaseWeight(float pt) {
            return Mth.lerp(pt, this.prevReleaseWeight, this.releaseWeight);
        }

        /** 0..1 of the landing pose: in over 3 ticks, held, out over the last 8. */
        float land(float pt) {
            if (this.landAge < 0) {
                return 0.0F;
            }
            float t = this.landAge + pt;
            return Mth.clamp(Math.min(t / 3.0F, (LAND_TICKS - t) / 8.0F), 0.0F, 1.0F);
        }

        float release(float pt) {
            return Mth.clamp((this.releaseAge + pt) / RELEASE_TICKS, 0.0F, 1.0F);
        }
    }
}
