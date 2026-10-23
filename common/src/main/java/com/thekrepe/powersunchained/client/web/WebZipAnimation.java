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
import org.joml.Vector3f;

/**
 * Web Zip and web swinging body animations for every player in view. Our own poses and timings, made in the spirit of the classic
 * Spider-Man mods (Fisk's Superheroes, Powerborne Heroes, Marvel Legacy): the same ideas (aim and throw the line,
 * yank yourself in, flip when letting go, dive, land like a superhero), drawn and timed anew.
 * <ol>
 *   <li><b>Shoot</b> (the line flies): the right arm points the line where you look, the left arm swings out
 *       behind, in the air one knee comes up.</li>
 *   <li><b>Pull</b> (the line caught), one of three: <i>double yank</i> (both hands grab high and haul down and back
 *       while the legs split and tuck), <i>burst</i> (one arm flung back, the other guarding, legs tucked) or
 *       <i>reach</i> (the arm stays stretched along the line, superman style). The whole body leans into the flight
 *       by speed.</li>
 *   <li><b>Release</b> in the air: one of eight flips / spins (tucked front flip, layout back flip, twist, spin,
 *       corkscrew, cartwheel, forward roll, star jump).</li>
 *   <li><b>Dive</b> while falling afterwards, one of three (arrow, skydiver, swan), until landing.</li>
 *   <li><b>Landing</b> after a fast fall, one of three: superhero landing (one knee, a fist on the ground), spider
 *       crouch (deep squat, both hands on the ground between the feet), skid (sideways lunge, a hand trailing on the
 *       ground, the other arm out for balance).</li>
 * </ol>
 * Web swinging (same ideas as Fisk's web swinging, own poses): <b>swing shoot</b> (the arm reaches where you look
 * while the rope flies), <b>hang</b> (both hands or one hand on the rope, the free arm out for balance; the whole
 * body hangs along the rope and leans into the motion, the legs trail on the way down and kick forward on the way
 * up), <b>trick</b> when letting go in the air (the flips above, one-handed swings favour the twisting ones; faster
 * = quicker flip), then the dive, a <b>springboard</b> flip when jumping right after landing from a swing, and the
 * landings.
 * Every pose is keyframed with smooth (ease in-out) interpolation and every phase fades in and out, so phases blend
 * instead of snapping; landing mid-flip unwinds the flip. Limbs are set after the vanilla pose
 * ({@code PlayerModelMixin}), the whole body right after the body yaw ({@code LivingEntityRendererMixin}).
 */
public final class WebZipAnimation {

    private static final Map<Integer, State> STATES = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    private static final float DEG = Mth.DEG_TO_RAD;

    private static final float PULL_TICKS = 14.0F;
    private static final int PULL_VARIANTS = 3;
    private static final int RELEASE_TICKS = 16;
    private static final int RELEASE_VARIANTS = 8;
    private static final int DIVE_VARIANTS = 3;
    private static final int LAND_TICKS = 20;
    private static final int LAND_VARIANTS = 3;
    private static final float SWING_IN_TICKS = 6.0F;

    private WebZipAnimation() {
    }

    /** Client, every tick while the entity has a web zip line out: {@code pulling} once it caught. */
    public static void zip(int entityId, boolean pulling) {
        State s = STATES.computeIfAbsent(entityId, id -> new State());
        s.lineOut = true;
        s.pulling |= pulling;
    }

    /**
     * Client, every tick while the entity has a swing rope out: {@code kind} (0 both hands, 1 right, 2 left), the
     * rope's anchor, {@code attached} once it holds the entity.
     */
    public static void swing(int entityId, int kind, Vec3 anchor, boolean attached) {
        State s = STATES.computeIfAbsent(entityId, id -> new State());
        s.swingKind = kind;
        s.anchor = anchor;
        if (attached) {
            s.swingAttached = true;
        } else {
            s.swingOut = true;
        }
    }

    /** Whether the entity hangs on a swing rope (client). */
    public static boolean isHanging(int entityId) {
        State s = STATES.get(entityId);
        return s != null && s.wasSwingAttached;
    }

    /**
     * Dev/CI only (screenshot tests): holds an entity in one pose. {@code phase}: 0 shoot, 1 pull {@code variant} at
     * tick {@code value}, 2 release flip {@code variant} at progress {@code value}, 3 dive {@code variant},
     * 4 landing {@code variant} at progress {@code value}, 5 hanging on a swing rope ({@code variant} = kind, rope
     * anchor 8 blocks up and 4 ahead), 6 swing shoot ({@code variant} 0 right, 1 left).
     */
    public static void debugPose(int entityId, int phase, float value, int variant, Vec3 motion) {
        State s = new State();
        s.frozen = true;
        s.motion = s.prevMotion = motion;
        switch (phase) {
            case 0 -> s.shoot = s.prevShoot = 1.0F;
            case 1 -> {
                s.pull = s.prevPull = 1.0F;
                s.pullKind = variant;
                s.pullAge = (int) value + 1;
            }
            case 2 -> {
                s.releaseKind = variant;
                s.releaseAge = (int) (value * RELEASE_TICKS);
            }
            case 3 -> {
                s.dive = s.prevDive = 1.0F;
                s.diveKind = variant;
            }
            case 5 -> {
                s.swing = s.prevSwing = 1.0F;
                s.swingKind = variant;
                s.wasSwingAttached = true;
                Entity e = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(entityId) : null;
                float yaw = e != null ? e.getYRot() * DEG : 0.0F;
                s.anchorRel = s.prevAnchorRel = new Vec3(-Math.sin(yaw) * 4.0, 9.8, Math.cos(yaw) * 4.0);
            }
            case 6 -> {
                s.swingShoot = s.prevSwingShoot = 1.0F;
                s.swingShootSide = variant;
            }
            default -> {
                s.ground = s.prevGround = 1.0F;
                s.landAge = (int) (value * LAND_TICKS);
                s.landKind = variant;
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

    /** Ease in-out (smoothstep). */
    private static float smooth(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float easeOut(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        float u = 1.0F - t;
        return 1.0F - u * u * u;
    }

    /** 0 -> 1 -> 0 over the progress, soft at both ends. */
    private static float bell(float t) {
        return smooth(Mth.sin(Mth.PI * Mth.clamp(t, 0.0F, 1.0F)));
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

    /**
     * Samples keyframes {@code {tick, a, b, c}} at {@code t}, eased in-out between neighbours (so every pose
     * flows into the next), holding the last one.
     */
    private static float[] key(float[][] keys, float t) {
        if (t <= keys[0][0]) {
            return new float[] {keys[0][1], keys[0][2], keys[0][3]};
        }
        for (int i = 1; i < keys.length; i++) {
            if (t <= keys[i][0]) {
                float f = smooth((t - keys[i - 1][0]) / (keys[i][0] - keys[i - 1][0]));
                return new float[] {lerp(keys[i - 1][1], keys[i][1], f), lerp(keys[i - 1][2], keys[i][2], f),
                    lerp(keys[i - 1][3], keys[i][3], f)};
            }
        }
        float[] last = keys[keys.length - 1];
        return new float[] {last[1], last[2], last[3]};
    }

    private static void rotKeys(ModelPart p, float[][] keys, float t, float d) {
        float[] r = key(keys, t);
        rot(p, r[0], r[1], r[2], d);
    }

    private static void posKeys(ModelPart p, float[][] keys, float t, float d) {
        float[] o = key(keys, t);
        pos(p, o[0], o[1], o[2], d);
    }

    // ---- limbs ----

    public static void applyParts(PlayerModel model, int entityId, float partialTick) {
        State s = STATES.get(entityId);
        if (s == null) {
            return;
        }
        Vec3 mot = s.motion(partialTick);
        float air = 1.0F - s.ground(partialTick);
        float dive = s.dive(partialTick) * air;
        if (dive > 0.0F) {
            applyDive(model, s.diveKind, smooth(dive), mot);
        }
        float release = s.release(partialTick);
        if (s.releaseKind >= 0 && release < 1.0F) {
            applyReleaseLimbs(model, s.releaseKind, release, s.releaseWeight(partialTick));
        }
        float land = s.land(partialTick);
        if (land > 0.0F) {
            applyLanding(model, s.landKind, smooth(land));
        }
        float hang = smooth(s.swing(partialTick));
        if (hang > 0.0F) {
            applyHang(model, s.swingKind, hang, mot, s.swingTime(partialTick));
        }
        float swingShoot = s.swingShoot(partialTick);
        if (swingShoot > 0.0F) {
            applyShootSide(model, smooth(swingShoot) * (1.0F - hang), air, s.entity, s.swingShootSide == 1);
        }
        float shoot = s.shoot(partialTick);
        if (shoot > 0.0F) {
            applyShoot(model, smooth(shoot), air, s.entity);
        }
        float pull = s.pull(partialTick);
        if (pull > 0.0F) {
            float t = s.pullTime(partialTick);
            float d = smooth(pull);
            switch (s.pullKind) {
                case 0 -> applyYank(model, t, d, air);
                case 1 -> applyBurst(model, t, d, air);
                default -> applyReach(model, t, d, air);
            }
        }
    }

    /** Swing shoot: the shooting arm reaches where you look, the other one pulls back, a knee comes up. */
    private static void applyShootSide(PlayerModel m, float d, float air, LivingEntity entity, boolean left) {
        if (d <= 0.0F) {
            return;
        }
        float pitch = entity != null ? Mth.clamp(entity.getXRot(), -85.0F, 85.0F) : -30.0F;
        float yaw = entity != null ? Mth.clamp(Mth.wrapDegrees(entity.getYHeadRot() - entity.yBodyRot), -70.0F, 70.0F) : 0.0F;
        float k = left ? -1.0F : 1.0F;
        ModelPart shoot = left ? m.leftArm : m.rightArm;
        ModelPart other = left ? m.rightArm : m.leftArm;
        rot(shoot, pitch - 96.0F, yaw + k * 6.0F, k * 6.0F, d);
        pos(shoot, k * -5.0F, 2.0F, -1.0F, d);
        rot(other, 38.0F, k * -10.0F, k * -30.0F, d);
        float legs = d * air;
        ModelPart up = left ? m.leftLeg : m.rightLeg;
        ModelPart down = left ? m.rightLeg : m.leftLeg;
        rot(up, -62.0F, k * 6.0F, k * 4.0F, legs);
        pos(up, k * -1.9F, 10.0F, -2.0F, legs);
        rot(down, 18.0F, k * -4.0F, k * -6.0F, legs);
    }

    /**
     * Hanging on the swing rope (limbs in the body frame; the body itself is turned along the rope). {@code mot}
     * steers the legs: trailing on the way down, kicking forward and tucking on the way up.
     */
    private static void applyHang(PlayerModel m, int kind, float d, Vec3 mot, float t) {
        float vy = smooth(Mth.clamp((float) -mot.y / 1.2F * 0.5F + 0.5F, 0.0F, 1.0F)) * 2.0F - 1.0F;  // -1 up .. 1 down
        float speed = smooth(Mth.clamp((float) mot.length() / 1.4F, 0.0F, 1.0F));
        float sway = Mth.sin(t * 0.35F) * (1.0F - 0.6F * speed);
        if (kind == 0) {
            // both hands on the rope, right one higher
            rot(m.rightArm, -176.0F, -4.0F, 7.0F + 2.0F * sway, d);
            pos(m.rightArm, -4.6F, 1.6F, -0.3F, d);
            rot(m.leftArm, -162.0F, 6.0F, -11.0F - 2.0F * sway, d);
            pos(m.leftArm, 4.6F, 2.2F, 0.2F, d);
        } else {
            boolean left = kind == 2;
            float k = left ? -1.0F : 1.0F;
            ModelPart hold = left ? m.leftArm : m.rightArm;
            ModelPart free = left ? m.rightArm : m.leftArm;
            rot(hold, -178.0F, k * -3.0F, k * 5.0F, d);
            pos(hold, k * -4.8F, 1.4F, 0.0F, d);
            // the free arm out for balance: forward and wide on the way down, swept back on the way up
            rot(free, -38.0F + 46.0F * (1.0F - vy) * 0.5F + 6.0F * sway, k * 12.0F, k * -(52.0F + 18.0F * speed), d);
        }
        // legs: one knee always a little higher (the classic swing silhouette)
        float trail = vy * (22.0F + 18.0F * speed);
        rot(m.rightLeg, 8.0F + trail - 4.0F * sway, 4.0F, 7.0F, d);
        rot(m.leftLeg, -34.0F + trail * 0.6F + 4.0F * sway - 26.0F * Math.max(-vy, 0.0F), -6.0F, -5.0F, d);
        pos(m.leftLeg, 1.9F, 11.2F, -1.2F, d);
        m.head.xRot = lerp(m.head.xRot, m.head.xRot - 10.0F * DEG * speed, d);
    }

    /** Shoot: point the line where you look, the other arm swings out behind, one knee up in the air. */
    private static void applyShoot(PlayerModel m, float d, float air, LivingEntity entity) {
        float pitch = entity != null ? Mth.clamp(entity.getXRot(), -85.0F, 85.0F) : 0.0F;
        float yaw = entity != null ? Mth.clamp(Mth.wrapDegrees(entity.getYHeadRot() - entity.yBodyRot), -70.0F, 70.0F) : 0.0F;
        rot(m.rightArm, pitch - 92.0F, yaw - 4.0F, 3.0F, d);
        pos(m.rightArm, -5.0F, 2.5F, -0.5F, d);
        rot(m.leftArm, 52.0F, -14.0F, -22.0F, d);
        pos(m.leftArm, 5.0F, 2.0F, 1.5F, d);
        float legs = d * air;
        rot(m.rightLeg, -48.0F, 8.0F, 6.0F, legs);
        pos(m.rightLeg, -1.9F, 10.5F, -2.0F, legs);
        rot(m.leftLeg, 22.0F, -6.0F, -5.0F, legs);
    }

    // Double yank: {tick, x, y, z} angles (degrees) / positions (model pixels)
    private static final float[][] YANK_R_ARM = {{0, -105, -18, -6}, {3, -128, -22, -14}, {6, -30, -26, -24}, {10, 22, -28, -6}, {14, 44, -24, 6}};
    private static final float[][] YANK_L_ARM = {{0, -105, 18, 6}, {3, -128, 22, 14}, {6, -30, 26, 24}, {10, 22, 28, 6}, {14, 44, 24, -6}};
    private static final float[][] YANK_R_ARM_POS = {{0, -5, 2, -2.5F}, {6, -4.5F, 2, -1.5F}, {14, -4.5F, 2, 0.5F}};
    private static final float[][] YANK_L_ARM_POS = {{0, 5, 2, -2.5F}, {6, 4.5F, 2, -1.5F}, {14, 4.5F, 2, 0.5F}};
    private static final float[][] YANK_R_LEG = {{0, -55, 0, 4}, {5, -8, 4, 52}, {9, 6, 4, 10}, {14, 14, 6, -10}};
    private static final float[][] YANK_L_LEG = {{0, -55, 0, -4}, {5, -6, -4, -52}, {9, 16, -4, -10}, {14, 24, -6, 12}};
    private static final float[][] YANK_R_LEG_POS = {{0, -1.9F, 12, -1}, {5, -2.5F, 11, 0}, {14, -3.5F, 6.5F, -3}};
    private static final float[][] YANK_L_LEG_POS = {{0, 1.9F, 12, -1}, {5, 2.5F, 11, 0}, {14, 3.5F, 6, -3.5F}};

    /** Double yank: both hands grab high on the line and haul down and back, legs split then tuck. */
    private static void applyYank(PlayerModel m, float t, float d, float air) {
        rotKeys(m.rightArm, YANK_R_ARM, t, d);
        rotKeys(m.leftArm, YANK_L_ARM, t, d);
        posKeys(m.rightArm, YANK_R_ARM_POS, t, d);
        posKeys(m.leftArm, YANK_L_ARM_POS, t, d);
        float legs = d * air;
        rotKeys(m.rightLeg, YANK_R_LEG, t, legs);
        rotKeys(m.leftLeg, YANK_L_LEG, t, legs);
        posKeys(m.rightLeg, YANK_R_LEG_POS, t, legs);
        posKeys(m.leftLeg, YANK_L_LEG_POS, t, legs);
        m.head.xRot = lerp(m.head.xRot, m.head.xRot - 14.0F * DEG, legs);
    }

    private static final float[][] BURST_R_ARM = {{0, -95, 10, 20}, {4, 66, 28, 84}, {14, 72, 22, 76}};
    private static final float[][] BURST_L_ARM = {{0, -95, -10, -20}, {4, -28, -18, -12}, {14, -22, -12, -18}};
    private static final float[][] BURST_R_LEG = {{0, -20, 0, 6}, {5, 36, 32, 8}, {14, 30, 28, 6}};
    private static final float[][] BURST_L_LEG = {{0, -20, 0, -6}, {5, 74, -24, -4}, {14, 68, -18, -2}};

    /** Burst: one arm flung back and out, the other forward on guard, legs tucked as you fly in. */
    private static void applyBurst(PlayerModel m, float t, float d, float air) {
        float wobble = 4.0F * Mth.sin(t * 0.8F);
        float[] r = key(BURST_R_ARM, t);
        rot(m.rightArm, r[0] + wobble, r[1], r[2], d);
        pos(m.rightArm, -4.5F, 2.0F, 3.0F, d * smooth(t / 4.0F));
        rotKeys(m.leftArm, BURST_L_ARM, t, d);
        pos(m.leftArm, 4.5F, 2.0F, -1.5F, d);
        float legs = d * air;
        rotKeys(m.rightLeg, BURST_R_LEG, t, legs);
        pos(m.rightLeg, -4.0F, 7.0F, 0.5F, legs * smooth(t / 5.0F));
        rotKeys(m.leftLeg, BURST_L_LEG, t, legs);
        pos(m.leftLeg, 4.0F, 9.5F, -1.5F, legs * smooth(t / 5.0F));
        m.head.xRot = lerp(m.head.xRot, m.head.xRot - 12.0F * DEG, legs);
    }

    /** Reach: the shooting arm stays stretched along the line, the other swept back, legs trailing. */
    private static void applyReach(PlayerModel m, float t, float d, float air) {
        float sway = Mth.sin(t * 0.6F);
        rot(m.rightArm, -168.0F, -6.0F, 8.0F + 3.0F * sway, d);
        pos(m.rightArm, -5.0F, 2.0F, -1.0F, d);
        rot(m.leftArm, 34.0F, 14.0F, -32.0F - 4.0F * sway, d);
        float legs = d * air;
        rot(m.rightLeg, 12.0F + 4.0F * sway, 0.0F, 5.0F, legs);
        rot(m.leftLeg, 34.0F - 4.0F * sway, 0.0F, -5.0F, legs);
        pos(m.leftLeg, 1.9F, 11.0F, 1.5F, legs);
        m.head.xRot = lerp(m.head.xRot, m.head.xRot - 22.0F * DEG, legs);
    }

    private static void applyLanding(PlayerModel m, int kind, float d) {
        switch (kind) {
            case 1 -> { // spider crouch: deep wide squat, both hands on the ground between the feet, head up
                float settle = Mth.sin(Mth.PI * d) * 0.5F;
                rot(m.head, -46.0F, 0.0F, 0.0F, d);
                rot(m.rightArm, -16.0F, 10.0F, -12.0F, d);
                pos(m.rightArm, -3.6F, 3.0F, -1.0F, d);
                rot(m.leftArm, -22.0F, -10.0F, 12.0F, d);
                pos(m.leftArm, 3.6F, 3.0F, -1.5F, d);
                rot(m.rightLeg, -86.0F + 10.0F * settle, 28.0F, 18.0F, d);
                pos(m.rightLeg, -3.6F, 9.5F, -2.0F, d);
                rot(m.leftLeg, -86.0F + 10.0F * settle, -28.0F, -18.0F, d);
                pos(m.leftLeg, 3.6F, 9.5F, -2.0F, d);
            }
            case 2 -> { // skid: sideways lunge, one leg out wide, a hand trailing on the ground, the other arm up
                rot(m.head, -18.0F, -28.0F, 6.0F, d);
                rot(m.rightArm, -122.0F, 18.0F, 52.0F, d);
                pos(m.rightArm, -5.0F, 2.0F, 0.0F, d);
                rot(m.leftArm, -24.0F, -6.0F, -40.0F, d);
                pos(m.leftArm, 5.0F, 2.5F, -0.5F, d);
                rot(m.rightLeg, -72.0F, 12.0F, 6.0F, d);
                pos(m.rightLeg, -2.4F, 9.0F, -2.5F, d);
                rot(m.leftLeg, -8.0F, -16.0F, -56.0F, d);
                pos(m.leftLeg, 2.6F, 11.0F, 0.5F, d);
            }
            default -> applySuperheroLanding(m, d);
        }
    }

    /** Superhero landing: down on the right knee, right fist on the ground, left arm out behind, then up. */
    private static void applySuperheroLanding(PlayerModel m, float d) {
        float settle = Mth.sin(Mth.PI * d) * 0.5F;
        rot(m.head, -38.0F, -24.0F, 4.0F, d);
        m.body.xRot = lerp(m.body.xRot, -6.0F * DEG, d);
        rot(m.rightArm, -28.0F, 20.0F, 54.0F, d);
        pos(m.rightArm, -3.5F, 4.5F, 2.0F, d);
        rot(m.leftArm, -52.0F, -16.0F, -38.0F, d);
        pos(m.leftArm, 4.5F, 3.0F, -0.5F, d);
        rot(m.rightLeg, -64.0F + 30.0F * settle, -4.0F, -8.0F, d);
        pos(m.rightLeg, -3.0F, 8.5F - 1.5F * settle, -1.0F - 3.0F * settle, d);
        rot(m.leftLeg, 8.0F + 20.0F * settle, -26.0F, 2.0F, d);
        pos(m.leftLeg, 3.5F, 10.0F, -3.5F - 1.5F * settle, d);
    }

    /** Limbs of the release flips; {@code w} unwinds the pose when landing mid-flip. */
    private static void applyReleaseLimbs(PlayerModel m, int kind, float p, float w) {
        float d = w * bell(p);
        switch (kind) {
            case 0 -> { // tucked front flip: knees to the chest, arms hugging them
                m.head.xRot = lerp(m.head.xRot, 0.6F, d);
                rot(m.rightArm, -62.0F, 18.0F, 16.0F, d);
                rot(m.leftArm, -62.0F, -18.0F, -16.0F, d);
                rot(m.rightLeg, -96.0F, 4.0F, 4.0F, d);
                pos(m.rightLeg, -1.9F, 10.0F, -2.5F, d);
                rot(m.leftLeg, -104.0F, -4.0F, -4.0F, d);
                pos(m.leftLeg, 1.9F, 10.0F, -2.5F, d);
            }
            case 1 -> { // layout back flip: arms overhead, body straight
                m.head.xRot = lerp(m.head.xRot, -0.5F, d);
                rot(m.rightArm, -168.0F, -6.0F, 18.0F, d);
                rot(m.leftArm, -168.0F, 6.0F, -18.0F, d);
                rot(m.rightLeg, 8.0F, 0.0F, 4.0F, d);
                rot(m.leftLeg, 14.0F, 0.0F, -4.0F, d);
            }
            case 2 -> { // half flip with a twist: arms crossed in, legs together
                m.head.xRot = lerp(m.head.xRot, 0.7F, d);
                rot(m.rightArm, 70.0F, 26.0F, 22.0F, d);
                rot(m.leftArm, -70.0F, 26.0F, -22.0F, d);
                rot(m.rightLeg, -18.0F, 0.0F, 3.0F, d);
                rot(m.leftLeg, 14.0F, 0.0F, -3.0F, d);
            }
            case 3 -> { // flat spin: arms in, one knee up
                m.head.yRot = rotLerp(m.head.yRot, -26.0F * DEG, d);
                rot(m.rightArm, -20.0F, 32.0F, -12.0F, d);
                rot(m.leftArm, -26.0F, 34.0F, 12.0F, d);
                rot(m.rightLeg, -38.0F, 0.0F, -4.0F, d);
                rot(m.leftLeg, 12.0F, -12.0F, 4.0F, d);
            }
            case 4 -> { // corkscrew: arms wrapped, legs crossed
                m.head.xRot = lerp(m.head.xRot, Math.max(m.head.xRot - 0.35F, -80 * DEG), d);
                rot(m.rightArm, -8.0F, 30.0F, 30.0F, d);
                rot(m.leftArm, -8.0F, -24.0F, -20.0F, d);
                rot(m.rightLeg, 10.0F, 0.0F, 22.0F, d);
                rot(m.leftLeg, 20.0F, 0.0F, 18.0F, d);
            }
            case 5 -> { // cartwheel: arms and legs wide like a wheel
                m.head.xRot = lerp(m.head.xRot, Math.max(m.head.xRot - 0.3F, -80 * DEG), d);
                rot(m.rightArm, -4.0F, -8.0F, 128.0F, d);
                rot(m.leftArm, -4.0F, 8.0F, -128.0F, d);
                rot(m.rightLeg, 4.0F, 0.0F, 44.0F, d);
                rot(m.leftLeg, 4.0F, 0.0F, -44.0F, d);
            }
            case 6 -> { // forward roll in a ball, head tucked
                m.head.y += 0.8F * d;
                m.head.z -= 0.8F * d;
                m.head.xRot = lerp(m.head.xRot, 52.0F * DEG, d);
                rot(m.rightArm, -50.0F, -26.0F, -10.0F, d);
                rot(m.leftArm, -60.0F, 34.0F, -16.0F, d);
                rot(m.rightLeg, -36.0F, 0.0F, -3.0F, d);
                pos(m.rightLeg, -2.2F, 4.0F, -1.5F, d);
                rot(m.leftLeg, -32.0F, 0.0F, 4.0F, d);
                pos(m.leftLeg, 2.2F, 3.0F, -1.5F, d);
            }
            default -> { // star jump: everything spread out wide
                rot(m.rightArm, -8.0F, 20.0F, 82.0F, d);
                rot(m.leftArm, -8.0F, -20.0F, -82.0F, d);
                rot(m.rightLeg, 40.0F, 2.0F, 16.0F, d);
                rot(m.leftLeg, 40.0F, -2.0F, -16.0F, d);
            }
        }
    }

    private static void applyDive(PlayerModel m, int kind, float d, Vec3 mot) {
        float fall = smooth(Mth.clamp((float) -mot.y / 1.5F, 0.0F, 1.0F));
        switch (kind) {
            case 0 -> { // arrow: arms forward along the dive, legs together
                m.head.xRot = lerp(m.head.xRot, -55.0F * DEG * fall, d);
                rot(m.rightArm, -150.0F + 20.0F * (1 - fall), -8.0F, 10.0F, d);
                rot(m.leftArm, -150.0F + 20.0F * (1 - fall), 8.0F, -10.0F, d);
                rot(m.rightLeg, 6.0F, 0.0F, 2.0F, d);
                rot(m.leftLeg, 14.0F, 0.0F, -2.0F, d);
            }
            case 1 -> { // skydiver: arms and legs spread, knees bent
                m.head.xRot = lerp(m.head.xRot, -40.0F * DEG * fall, d);
                rot(m.rightArm, -70.0F, -30.0F, 70.0F, d);
                rot(m.leftArm, -70.0F, 30.0F, -70.0F, d);
                rot(m.rightLeg, 38.0F, 10.0F, 22.0F, d);
                rot(m.leftLeg, 38.0F, -10.0F, -22.0F, d);
            }
            default -> { // swan: arms swept back like wings, legs trailing
                m.head.xRot = lerp(m.head.xRot, -50.0F * DEG * fall, d);
                rot(m.rightArm, 30.0F + 20.0F * fall, -12.0F, 34.0F, d);
                rot(m.leftArm, 30.0F + 20.0F * fall, 12.0F, -34.0F, d);
                rot(m.rightLeg, 24.0F, 6.0F, 6.0F, d);
                pos(m.rightLeg, -2.5F, 11.0F, -0.5F, d);
                rot(m.leftLeg, 10.0F, -6.0F, -6.0F, d);
            }
        }
    }

    // ---- whole body ----

    /** Whole body rotations, after the body yaw (y up; pivots as a fraction of the 1.8 block body height). */
    public static void applyBody(PoseStack poseStack, int entityId, float partialTick, float scale, float bodyYaw) {
        State s = STATES.get(entityId);
        if (s == null) {
            return;
        }
        float hang = smooth(s.swing(partialTick));
        if (hang > 0.0F) {
            hangAlongRope(poseStack, s, partialTick, scale, bodyYaw, hang);
        }
        Vec3 mot = s.motion(partialTick);
        float horVel = (float) Math.sqrt(mot.x * mot.x + mot.z * mot.z);
        float air = 1.0F - s.ground(partialTick);

        float dive = smooth(s.dive(partialTick) * air);
        if (dive > 0.0F) {
            float fall = smooth(Mth.clamp((float) -mot.y / 1.5F, 0.0F, 1.0F));
            float angle = switch (s.diveKind) {
                case 0 -> -(25.0F + 50.0F * fall);
                case 1 -> -(55.0F + 20.0F * fall);
                default -> -(20.0F + 45.0F * fall);
            };
            rotate(poseStack, scale, 0.5F, angle * dive, 1.0F, 0.0F, 0.0F);
        }
        float pull = smooth(s.pull(partialTick)) * air;
        float shoot = smooth(s.shoot(partialTick)) * air;
        if (pull > 0.0F || shoot > 0.0F) {
            // lean into the flight: a little while shooting, up to 60 degrees by speed while pulled
            float vel = smooth(Math.min(1.8F * horVel, 1.0F));
            float roll = s.pullKind == 2 ? 6.0F * Mth.sin(s.pullTime(partialTick) * 0.6F) : 0.0F;
            rotate(poseStack, scale, 0.5F, -8.0F * Math.max(shoot, pull) - 52.0F * vel * pull, 1.0F, 0.0F, 0.0F);
            rotate(poseStack, scale, 0.5F, roll * pull, 0.0F, 0.0F, 1.0F);
        }
        float release = s.release(partialTick);
        if (s.releaseKind >= 0 && release < 1.0F) {
            // landing mid-flip: the flip unwinds over a few ticks instead of snapping upright
            float w = s.releaseWeight(partialTick);
            float x = smooth(release);
            float b = bell(release);
            switch (s.releaseKind) {
                case 0 -> rotate(poseStack, scale, 0.52F, w * -360.0F * x, 1.0F, 0.0F, 0.0F);
                case 1 -> rotate(poseStack, scale, 0.5F, w * 360.0F * x, 1.0F, 0.0F, 0.0F);
                case 2 -> {
                    rotate(poseStack, scale, 0.5F, w * 150.0F * b, 1.0F, 0.0F, 0.0F);
                    rotate(poseStack, scale, 0.5F, w * -360.0F * smooth((release - 0.25F) / 0.55F), 0.0F, 1.0F, 0.0F);
                }
                case 3 -> rotate(poseStack, scale, 0.5F, w * 360.0F * easeOut(release), 0.0F, 1.0F, 0.0F);
                case 4 -> rotate(poseStack, scale, 0.5F, w * -360.0F * x, -0.8F, 1.0F, 0.0F);
                case 5 -> rotate(poseStack, scale, 0.5F, w * -360.0F * x, 0.3F, 0.0F, 1.0F);
                case 6 -> {
                    rotate(poseStack, scale, 0.58F, w * -360.0F * x, 1.0F, 0.0F, 0.0F);
                    poseStack.translate(0.0F, -0.12F * b * w * scale, 0.08F * b * w * scale);
                }
                default -> rotate(poseStack, scale, 0.55F, w * -8.0F * b, 1.0F, 0.0F, 0.0F);
            }
        }
        float land = smooth(s.land(partialTick));
        if (land > 0.0F) {
            switch (s.landKind) {
                case 1 -> { // spider crouch: low and level, chest over the knees
                    poseStack.translate(0.0F, -0.5F * land * scale, 0.12F * land * scale);
                    rotate(poseStack, scale, 0.56F, -34.0F * land, 1.0F, 0.0F, 0.0F);
                }
                case 2 -> { // skid: low, turned sideways, leaning over the bent knee
                    poseStack.translate(0.0F, -0.34F * land * scale, 0.1F * land * scale);
                    rotate(poseStack, scale, 0.56F, 34.0F * land, 0.0F, 1.0F, 0.0F);
                    rotate(poseStack, scale, 0.56F, 16.0F * land, 0.0F, 0.0F, 1.0F);
                    rotate(poseStack, scale, 0.56F, -24.0F * land, 1.0F, 0.0F, 0.0F);
                }
                default -> { // superhero landing: drop low, lean forward over the knee, a slight twist
                    poseStack.translate(0.0F, -0.38F * land * scale, 0.2F * land * scale);
                    rotate(poseStack, scale, 0.56F, 14.0F * land, 0.0F, 0.0F, 1.0F);
                    rotate(poseStack, scale, 0.56F, -16.0F * land, 0.0F, 1.0F, 0.0F);
                    rotate(poseStack, scale, 0.56F, -62.0F * land, 1.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    /**
     * Hanging: the body turns (around its middle) so it hangs along the rope, its top leaning a little further into
     * the motion (the legs trail), limited to 120 degrees. Local frame after the body yaw: +X right, -Z forward.
     */
    private static void hangAlongRope(PoseStack poseStack, State s, float pt, float scale, float bodyYaw, float hang) {
        Vec3 rel = s.prevAnchorRel.lerp(s.anchorRel, pt).subtract(0.0, 0.9, 0.0);
        if (rel.lengthSqr() < 0.04) {
            return;
        }
        Vec3 up = rel.normalize().add(s.motion(pt).scale(0.35));
        if (up.lengthSqr() < 1.0E-6) {
            return;
        }
        float yaw = bodyYaw * DEG;
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        Vector3f target = new Vector3f((float) (up.x * rx + up.z * rz), (float) up.y, (float) -(up.x * fx + up.z * fz)).normalize();
        float angle = (float) Math.acos(Mth.clamp(target.y, -1.0F, 1.0F));
        float max = 120.0F * DEG;
        if (angle > max) {
            // keep the direction, limit the tilt
            float h = Mth.sqrt(target.x * target.x + target.z * target.z);
            if (h > 1.0E-4F) {
                target.set(target.x / h * Mth.sin(max), Mth.cos(max), target.z / h * Mth.sin(max));
            }
        }
        Quaternionf q = new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), target);
        q = new Quaternionf().slerp(q, hang);
        float py = 0.5F * 1.8F * scale;
        poseStack.translate(0.0F, py, 0.0F);
        poseStack.rotate(q);
        poseStack.translate(0.0F, -py, 0.0F);
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
        int pullKind;
        float ground;
        float prevGround;
        int releaseKind = -1;
        int releaseAge;
        float releaseWeight = 1.0F;
        float prevReleaseWeight = 1.0F;
        float dive;
        float prevDive;
        int diveKind;
        int landAge = -1;
        int landKind;
        boolean wasOnGround = true;
        // web swinging
        boolean swingOut;
        boolean swingAttached;
        boolean wasSwingAttached;
        int swingKind;
        Vec3 anchor = Vec3.ZERO;
        Vec3 anchorRel = Vec3.ZERO;
        Vec3 prevAnchorRel = Vec3.ZERO;
        float swing;
        float prevSwing;
        int swingAge;
        float swingShoot;
        float prevSwingShoot;
        int swingShootSide;
        int releaseTicks = RELEASE_TICKS;
        /** Ticks since landing after swinging (springboard jump window), -1 = none. */
        int sinceSwingLand = -1;
        boolean fromSwing;
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
            this.prevReleaseWeight = this.releaseWeight;
            this.prevSwing = this.swing;
            this.prevSwingShoot = this.swingShoot;
            this.ground = Mth.clamp(this.ground + (onGround ? 0.2F : -0.25F), 0.0F, 1.0F);

            // web swinging: hanging weight (in over 6 ticks, only in the air), the rope shot, sounds
            boolean hanging = this.swingAttached && !onGround;
            this.swing = Mth.clamp(this.swing + (hanging ? 1.0F : -1.0F) / SWING_IN_TICKS, 0.0F, 1.0F);
            this.swingAge = this.swingAttached ? this.swingAge + 1 : 0;
            Vec3 rel = this.swingAttached || this.swingOut ? this.anchor.subtract(e.position()) : this.anchorRel;
            this.prevAnchorRel = this.anchorRel == Vec3.ZERO ? rel : this.anchorRel;
            this.anchorRel = rel;
            boolean flying = this.swingOut && !this.swingAttached;
            if (flying && this.swingShoot == 0.0F) {
                this.swingShootSide = this.swingKind == 2 ? 1 : 0;
            }
            this.swingShoot = Mth.clamp(this.swingShoot + (flying ? 0.4F : -0.2F), 0.0F, 1.0F);
            if (this.swingAttached || this.swingOut) {
                this.fromSwing = true;
            }
            if (this.swingAttached && !this.wasSwingAttached && this.entity != null) {
                Sounds.startSwing(this.entity);
            }
            // letting go of the swing rope in the air (no new rope on the way): a trick, faster = quicker
            if (this.wasSwingAttached && !this.swingAttached && !this.swingOut && !onGround && this.motion.length() > 0.25) {
                double hor = this.motion.horizontalDistance();
                this.releaseTicks = (int) (28.0F - Math.min((float) hor + 0.5F, 3.0F) * 4.0F);
                this.releaseKind = this.swingKind != 0 && RANDOM.nextFloat() < 0.7F
                        ? new int[] {2, 4, 5}[RANDOM.nextInt(3)]
                        : new int[] {0, 1, 3, 6, 7}[RANDOM.nextInt(5)];
                this.releaseAge = 0;
                this.releaseWeight = this.prevReleaseWeight = 1.0F;
                if (this.entity != null) {
                    Sounds.startFall(this.entity);
                }
            }
            // springboard: jumping right after landing from a swing
            if (this.sinceSwingLand >= 0 && !onGround && this.wasOnGround && this.motion.y > 0.2 && this.sinceSwingLand <= 5) {
                this.releaseKind = RANDOM.nextBoolean() ? 0 : 6;
                this.releaseTicks = 17;
                this.releaseAge = 0;
                this.releaseWeight = this.prevReleaseWeight = 1.0F;
                this.landAge = -1;
                this.sinceSwingLand = -1;
            }
            if (onGround && !this.wasOnGround && this.fromSwing && !this.swingAttached) {
                this.sinceSwingLand = 0;
            } else if (this.sinceSwingLand >= 0 && ++this.sinceSwingLand > 5) {
                this.sinceSwingLand = -1;
            }

            // landing after a fast fall (only while this state lives, i.e. after zipping / swinging)
            if (onGround && !this.wasOnGround && this.prevMotion.y < -0.35) {
                this.landAge = 0;
                this.landKind = RANDOM.nextInt(LAND_VARIANTS);
            } else if (this.landAge >= 0 && (++this.landAge > LAND_TICKS || !onGround && this.landAge > 2)) {
                this.landAge = -1;
            }
            // shoot: the line is flying
            boolean shooting = this.lineOut && !this.pulling;
            this.shoot = Mth.clamp(this.shoot + (shooting ? 0.34F : -0.25F), 0.0F, 1.0F);
            // pull: keyframes from the moment it caught, in over 2 ticks, out over 5
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
                this.releaseTicks = RELEASE_TICKS;
                this.releaseKind = RANDOM.nextInt(RELEASE_VARIANTS);
                this.releaseAge = 0;
                this.releaseWeight = this.prevReleaseWeight = 1.0F;
            }
            if (this.releaseKind >= 0) {
                if (onGround) {
                    this.releaseWeight = Math.max(this.releaseWeight - 0.25F, 0.0F);
                }
                if (++this.releaseAge > this.releaseTicks || this.releaseWeight <= 0.0F && this.prevReleaseWeight <= 0.0F) {
                    this.releaseKind = -1;
                }
            }
            // falling afterwards: dive until landing
            boolean diving = !onGround && !this.lineOut && !this.swingOut && !this.swingAttached && this.releaseKind < 0
                    && this.motion.y < -0.12;
            if (diving) {
                if (this.dive == 0.0F) {
                    this.diveKind = RANDOM.nextInt(DIVE_VARIANTS);
                }
                this.dive = Math.min(this.dive + 0.1F, 1.0F);
            } else {
                this.dive = Math.max(this.dive - (onGround ? 0.2F : 0.1F), 0.0F);
            }
            this.wasPulling = this.pulling;
            this.wasOnGround = onGround;
            this.wasSwingAttached = this.swingAttached;
            boolean anyLine = this.lineOut || this.swingOut || this.swingAttached;
            this.idleTicks = anyLine || !onGround ? 0 : this.idleTicks + 1;
            if (this.idleTicks > 10) {
                this.fromSwing = false;
            }
            this.lineOut = false;
            this.pulling = false;
            this.swingOut = false;
            this.swingAttached = false;
        }

        boolean idle() {
            return this.idleTicks > 10 && this.shoot == 0.0F && this.pull == 0.0F && this.dive == 0.0F
                    && this.prevDive == 0.0F && this.releaseKind < 0 && this.landAge < 0 && this.swing == 0.0F
                    && this.prevSwing == 0.0F && this.swingShoot == 0.0F && this.prevSwingShoot == 0.0F && this.sinceSwingLand < 0;
        }

        float swing(float pt) {
            return Mth.lerp(pt, this.prevSwing, this.swing);
        }

        float swingShoot(float pt) {
            return Mth.lerp(pt, this.prevSwingShoot, this.swingShoot);
        }

        float swingTime(float pt) {
            return this.swingAge + pt;
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

        float release(float pt) {
            return Mth.clamp((this.releaseAge + pt) / this.releaseTicks, 0.0F, 1.0F);
        }

        /** 0..1 of the landing pose: in over 3 ticks, held, out over the last 9. */
        float land(float pt) {
            if (this.landAge < 0) {
                return 0.0F;
            }
            float t = this.landAge + pt;
            return Mth.clamp(Math.min(t / 3.0F, (LAND_TICKS - t) / 9.0F), 0.0F, 1.0F);
        }
    }

    /** Looping swing sounds for the entity (wind + rope stress while hanging, wind while falling after a trick). */
    private static final class Sounds {
        static void startSwing(LivingEntity entity) {
            var manager = Minecraft.getInstance().getSoundManager();
            manager.play(WebSwingSound.wind(entity, e -> isHanging(e.getId())));
            manager.play(WebSwingSound.stress(entity, e -> isHanging(e.getId())));
        }

        static void startFall(LivingEntity entity) {
            Minecraft.getInstance().getSoundManager().play(WebSwingSound.fall(entity, e -> {
                State s = STATES.get(e.getId());
                return s != null && !e.onGround() && !s.wasSwingAttached && (s.releaseKind >= 0 || s.dive > 0.0F);
            }));
        }
    }
}
