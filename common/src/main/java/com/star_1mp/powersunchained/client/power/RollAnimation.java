package com.star_1mp.powersunchained.client.power;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.star_1mp.powersunchained.power.Rolling;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/**
 * Roll / flip animations of every player (the local one starts its own, others come from the server): the body turns
 * to the roll's direction, tucks in and goes once round forwards — low over the ground for the dodge roll, around
 * the middle for the air flip — then unfolds.
 */
public final class RollAnimation {

    private static final Map<Integer, Roll> ROLLS = new ConcurrentHashMap<>();
    private static final float DEG = Mth.DEG_TO_RAD;
    private static long clock;
    /** Dev/CI only: holds every roll at this progress (negative = off). */
    private static float debugHold = -1.0F;

    private record Roll(int kind, float yaw, long start) {
        int length() {
            return this.kind == Rolling.KIND_FLIP ? RollClient.FLIP_TICKS : RollClient.ROLL_TICKS;
        }
    }

    private RollAnimation() {
    }

    /** From the server: someone else rolled. */
    public static void onRoll(int entityId, int kind, float yaw) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            return;
        }
        start(entityId, kind, yaw);
    }

    public static void start(int entityId, int kind, float yaw) {
        ROLLS.put(entityId, new Roll(kind, yaw, clock));
    }

    /** Dev/CI only. */
    public static void debugHold(float progress) {
        debugHold = progress;
    }

    public static void tick() {
        clock++;
        if (debugHold >= 0.0F) {
            return;
        }
        Iterator<Roll> it = ROLLS.values().iterator();
        while (it.hasNext()) {
            Roll r = it.next();
            if (clock - r.start > r.length() + 1) {
                it.remove();
            }
        }
        if (Minecraft.getInstance().level == null) {
            ROLLS.clear();
        }
    }

    public static boolean isRolling(int entityId) {
        return ROLLS.containsKey(entityId);
    }

    /** Progress 0..1, or -1 without a roll. */
    private static float progress(Roll r, float partialTick) {
        if (debugHold >= 0.0F) {
            return debugHold;
        }
        float t = (clock - r.start + partialTick) / r.length();
        return t >= 1.0F ? -1.0F : Math.max(t, 0.0F);
    }

    /** How tucked in (0 = stretched, 1 = a ball): in quickly, out at the end. */
    private static float tuck(float p) {
        return smooth(Math.min(p / 0.18F, 1.0F)) * smooth(Math.min((1.0F - p) / 0.22F, 1.0F));
    }

    /** Render: after the body yaw (y up, model forward = -Z). */
    public static void applyBody(PoseStack poseStack, int entityId, float partialTick, float scale, float bodyYaw) {
        Roll r = ROLLS.get(entityId);
        if (r == null) {
            return;
        }
        float p = progress(r, partialTick);
        if (p < 0.0F) {
            return;
        }
        float tuck = tuck(p);
        // face the roll's direction
        float turn = Mth.wrapDegrees(bodyYaw - r.yaw) * smooth(Math.min(p / 0.12F, 1.0F)) * smooth(Math.min((1.0F - p) / 0.12F, 1.0F));
        poseStack.rotateDegrees(Axis.YP, turn);
        if (r.kind == Rolling.KIND_FLIP) {
            float spin = smooth(Mth.clamp((p - 0.05F) / 0.85F, 0.0F, 1.0F));
            rotate(poseStack, scale, 0.55F, -360.0F * spin);
        } else {
            float spin = smooth(Mth.clamp((p - 0.1F) / 0.75F, 0.0F, 1.0F));
            // a ball around the chest (legs tucked up), rolling on the ground: the chest drops to about the
            // ball's radius over the ground and the body turns around it
            poseStack.translate(0.0F, -0.52F * tuck * scale, 0.0F);
            rotate(poseStack, scale, 0.62F, -360.0F * spin);
        }
    }

    /** Limbs: knees to the chest, arms round the shins, head down. */
    public static void applyParts(PlayerModel m, int entityId, float partialTick) {
        Roll r = ROLLS.get(entityId);
        if (r == null) {
            return;
        }
        float p = progress(r, partialTick);
        if (p < 0.0F) {
            return;
        }
        float d = tuck(p);
        boolean flip = r.kind == Rolling.KIND_FLIP;
        rot(m.head, 32.0F, 0.0F, 0.0F, d);
        rot(m.body, 18.0F, 0.0F, 0.0F, d);
        rot(m.rightArm, flip ? -62.0F : -48.0F, 10.0F, 18.0F, d);
        rot(m.leftArm, flip ? -62.0F : -48.0F, -10.0F, -18.0F, d);
        rot(m.rightLeg, -108.0F, 8.0F, 6.0F, d);
        rot(m.leftLeg, -100.0F, -8.0F, -6.0F, d);
        // legs come up to the chest: pull the hips forward a little so they don't stick out of the back
        m.rightLeg.z = Mth.lerp(d, m.rightLeg.z, -2.0F);
        m.leftLeg.z = Mth.lerp(d, m.leftLeg.z, -2.0F);
    }

    private static void rot(ModelPart p, float x, float y, float z, float d) {
        p.xRot = Mth.lerp(d, p.xRot, x * DEG);
        p.yRot = Mth.lerp(d, p.yRot, y * DEG);
        p.zRot = Mth.lerp(d, p.zRot, z * DEG);
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static void rotate(PoseStack poseStack, float scale, float pivot, float angleDeg) {
        float py = pivot * 1.8F * scale;
        poseStack.translate(0.0F, py, 0.0F);
        poseStack.rotate(new Quaternionf().rotationX(angleDeg * DEG));
        poseStack.translate(0.0F, -py, 0.0F);
    }
}
