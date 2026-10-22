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
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Fisk's Superheroes web zip body animations, translated from its {@code .fsk} scripts (TheKrepe's port, used with
 * permission), for every player in view:
 * <ul>
 *   <li>{@code swing_zip}: while the web line pulls you through the air, the body leans into the flight
 *       (up to 80 degrees, by horizontal speed).</li>
 *   <li>{@code swing_roll} / {@code swing_roll2} / {@code swing_roll5} / {@code swing_springboard}: when the line
 *       lets go in the air, often a flip.</li>
 *   <li>{@code swing_dive} / {@code swing_dive2}: falling afterwards, the body dives head first with spread arms,
 *       until landing.</li>
 * </ul>
 * Fed by the web zip lines (client side) and ticked every client tick; applied to the model parts after the vanilla
 * pose ({@code PlayerModelMixin}) and to the whole body right after the body yaw ({@code LivingEntityRendererMixin}).
 */
public final class WebZipAnimation {

    private static final Map<Integer, State> STATES = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    /** Ticks of a flip after letting go. */
    private static final int TRICK_TICKS = 20;
    private static final float DEG = Mth.DEG_TO_RAD;

    private WebZipAnimation() {
    }

    /** Client: the entity's web zip line is pulling it this tick. */
    public static void pull(int entityId) {
        STATES.computeIfAbsent(entityId, id -> new State()).pulling = true;
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
            State s = e.getValue();
            if (entity == null || !entity.isAlive()) {
                it.remove();
                continue;
            }
            s.tick(entity);
            if (s.idle()) {
                it.remove();
            }
        }
    }

    private static float curve(float d) {
        return (Mth.sin((d * 2.0F - 1.0F) * Mth.PI / 2.0F) + 1.0F) / 2.0F;
    }

    private static float lerp(float from, float to, float d) {
        return from + (to - from) * d;
    }

    /** Fisk's {@code -'>}: rotation lerp the short way round. */
    private static float rotLerp(float from, float to, float d) {
        return from + d * Mth.wrapDegrees((to - from) / DEG) * DEG;
    }

    /** Part angles (after the vanilla pose and the web-shooter arm poses). */
    public static void applyParts(PlayerModel model, int entityId, float partialTick) {
        State s = STATES.get(entityId);
        if (s == null) {
            return;
        }
        Vec3 mot = s.motion(partialTick);
        float dive = s.dive(partialTick);
        if (dive > 0.0F) {
            applyDive(model, s.diveKind, dive, mot);
        }
        if (s.trick >= 0) {
            applyTrick(model, s.trick, s.trickProgress(partialTick));
        }
    }

    private static void applyDive(PlayerModel m, int kind, float data, Vec3 mot) {
        float horVel = (float) Math.sqrt(mot.x * mot.x + mot.z * mot.z);
        float pitch = (float) Math.atan2(mot.y, horVel);
        data = curve(data);
        float mm = curve(Mth.clamp((float) -mot.y, 0.0F, 2.0F) / 2.0F);
        float x = data * curve(Mth.clamp((float) -mot.y, 0.0F, 1.0F));
        if (kind == 0) {
            // swing_dive
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
        } else {
            // swing_dive2
            m.head.xRot = lerp(m.head.xRot, Math.min(-0.3F * pitch, 90 * DEG), x);
            m.rightArm.xRot = lerp(m.rightArm.xRot, (20 + 45 * mm) * DEG, x);
            m.rightArm.yRot = rotLerp(m.rightArm.yRot, (-35 + 20 * mm) * DEG, x);
            m.rightArm.zRot = lerp(m.rightArm.zRot, 25 * DEG, mm * data);
            m.leftArm.xRot = lerp(m.leftArm.xRot, (20 + 45 * mm) * DEG, x);
            m.leftArm.yRot = rotLerp(m.leftArm.yRot, (35 - 20 * mm) * DEG, x);
            m.leftArm.zRot = lerp(m.leftArm.zRot, -25 * DEG, mm * data);
            m.rightLeg.xRot = lerp(m.rightLeg.xRot, 45 * DEG, x);
            m.rightLeg.yRot = rotLerp(m.rightLeg.yRot, 16 * DEG, x);
            m.rightLeg.zRot = lerp(m.rightLeg.zRot, 13 * DEG, x);
            m.leftLeg.xRot = lerp(m.leftLeg.xRot, 40 * DEG, x);
            m.leftLeg.yRot = rotLerp(m.leftLeg.yRot, -18 * DEG, x);
            m.leftLeg.zRot = lerp(m.leftLeg.zRot, -8 * DEG, x);
            move(m.rightLeg, -3.0F, 9.0F, -4.9F, x);
            move(m.leftLeg, 3.0F, 11.0F, -2.9F, x);
        }
    }

    private static void move(ModelPart part, float x, float y, float z, float d) {
        part.x = lerp(part.x, x, d);
        part.y = lerp(part.y, y, d);
        part.z = lerp(part.z, z, d);
    }

    private static void applyTrick(PlayerModel m, int kind, float progress) {
        switch (kind) {
            case 0 -> { // swing_roll
                float d = (float) Math.pow(Mth.sin(Mth.PI * (1 - progress)), 2);
                m.head.xRot = lerp(m.head.xRot, -0.7F, d);
                m.rightArm.xRot = lerp(m.rightArm.xRot, -1.0F, d);
                m.rightArm.yRot = rotLerp(m.rightArm.yRot, 1.5F, d);
                m.rightArm.zRot = lerp(m.rightArm.zRot, 0.4F, d);
                m.leftArm.xRot = lerp(m.leftArm.xRot, -1.0F, d);
                m.leftArm.yRot = rotLerp(m.leftArm.yRot, -1.5F, d);
                m.leftArm.zRot = lerp(m.leftArm.zRot, -0.4F, d);
                m.rightLeg.xRot = lerp(m.rightLeg.xRot, 0.4F, d);
                m.leftLeg.xRot = lerp(m.leftLeg.xRot, 0.6F, d);
            }
            case 1 -> { // swing_roll2
                float d = (float) Math.pow(Mth.sin(Mth.PI * (1 - progress)), 3);
                m.head.xRot = lerp(m.head.xRot, Math.max(m.head.xRot - 0.4F, -90 * DEG), d);
                m.rightArm.xRot = lerp(m.rightArm.xRot, 1.0F, d);
                m.rightArm.yRot = rotLerp(m.rightArm.yRot, -0.8F, d);
                m.rightArm.zRot = lerp(m.rightArm.zRot, 0.5F, d);
                m.leftArm.xRot = lerp(m.leftArm.xRot, 1.0F, d);
                m.leftArm.yRot = rotLerp(m.leftArm.yRot, 0.9F, d);
                m.leftArm.zRot = lerp(m.leftArm.zRot, -0.3F, d);
                m.rightLeg.xRot = lerp(m.rightLeg.xRot, 1.5F, d);
                m.rightLeg.zRot = lerp(m.rightLeg.zRot, 0.4F, d);
                m.leftLeg.xRot = lerp(m.leftLeg.xRot, 1.8F, d);
                m.leftLeg.zRot = lerp(m.leftLeg.zRot, 0.3F, d);
            }
            case 2 -> { // swing_roll5
                float d = (float) Math.pow(Mth.sin(Mth.PI * (1 - progress)), 2);
                m.head.xRot = lerp(m.head.xRot, 0.7F, d);
                m.rightArm.xRot = lerp(m.rightArm.xRot, -0.5F, d);
                m.rightArm.yRot = rotLerp(m.rightArm.yRot, 0.0F, d);
                m.rightArm.zRot = lerp(m.rightArm.zRot, -0.1F, d);
                m.leftArm.xRot = lerp(m.leftArm.xRot, -0.5F, d);
                m.leftArm.yRot = rotLerp(m.leftArm.yRot, 0.0F, d);
                m.leftArm.zRot = lerp(m.leftArm.zRot, 0.1F, d);
                m.rightLeg.y = lerp(m.rightLeg.y, 0.5F, d);
                m.rightLeg.x = lerp(m.rightLeg.x, -1.5F, d);
                m.rightLeg.xRot = lerp(m.rightLeg.xRot, -0.6F, d);
                m.leftLeg.y = lerp(m.leftLeg.y, -0.5F, d);
                m.leftLeg.x = lerp(m.leftLeg.x, 1.5F, d);
                m.leftLeg.xRot = lerp(m.leftLeg.xRot, -0.6F, d);
            }
            default -> { // swing_springboard
                float d = (float) Math.pow(Mth.sin(Mth.PI * (1 - progress)), 2);
                m.head.xRot = lerp(m.head.xRot, 0.7F, d);
                m.rightArm.xRot = lerp(m.rightArm.xRot, 0.6F, d);
                m.rightArm.yRot = rotLerp(m.rightArm.yRot, 0.3F, d);
                m.rightArm.zRot = lerp(m.rightArm.zRot, 0.8F, d);
                m.leftArm.xRot = lerp(m.leftArm.xRot, 0.6F, d);
                m.leftArm.yRot = rotLerp(m.leftArm.yRot, -0.3F, d);
                m.leftArm.zRot = lerp(m.leftArm.zRot, -0.8F, d);
                m.rightLeg.xRot = lerp(m.rightLeg.xRot, -0.5F, d);
                m.leftLeg.xRot = lerp(m.leftLeg.xRot, -0.8F, d);
            }
        }
    }

    /**
     * Whole body rotations, in the coordinate system of the old {@code rotateCorpse} (after the body yaw, y up,
     * pivots in body units: x/z * 0.6, y * 1.8 blocks).
     */
    public static void applyBody(PoseStack poseStack, int entityId, float partialTick, float scale) {
        State s = STATES.get(entityId);
        if (s == null) {
            return;
        }
        Vec3 mot = s.motion(partialTick);
        float horVel = (float) Math.sqrt(mot.x * mot.x + mot.z * mot.z);
        float pitchDeg = (float) Math.toDegrees(Math.atan2(mot.y, horVel));

        float dive = s.dive(partialTick);
        if (dive > 0.0F) {
            float data = curve(dive);
            float mm = curve(Mth.clamp((float) -mot.y, 0.0F, 2.0F) / 2.0F);
            float factor = s.diveKind == 0 ? 0.8F : 0.2F;
            rotate(poseStack, scale, 0.5F, factor * mm * data * (pitchDeg - 90.0F), 1.0F);
        }
        float zip = s.zip(partialTick);
        if (zip > 0.0F) {
            // swing_zip: lean into the pull
            float vel = curve(Math.min(2.0F * horVel, 1.0F));
            rotate(poseStack, scale, 0.5F, -80.0F * vel * zip, 1.0F);
        }
        if (s.trick >= 0) {
            float x = curve(s.trickProgress(partialTick));
            switch (s.trick) {
                case 0 -> rotate(poseStack, scale, 0.5F, 360.0F * x, 1.0F);
                case 1, 2 -> rotate(poseStack, scale, s.trick == 2 ? 0.6F : 0.5F, -360.0F * x, -1.0F);
                default -> rotate(poseStack, scale, 0.3F + 0.2F * (float) Math.pow(Mth.sin(Mth.PI * (1 - x)), 2), -360.0F * x, 1.0F);
            }
        }
    }

    private static void rotate(PoseStack poseStack, float scale, float pivotY, float angleDeg, float axisX) {
        if (angleDeg == 0.0F) {
            return;
        }
        float py = pivotY * 1.8F * scale;
        poseStack.translate(0.0F, py, 0.0F);
        poseStack.rotate(new Quaternionf().rotationAxis(angleDeg * DEG, axisX, 0.0F, 0.0F));
        poseStack.translate(0.0F, -py, 0.0F);
    }

    private static final class State {
        boolean pulling;
        boolean wasPulling;
        float zip;
        float prevZip;
        float dive;
        float prevDive;
        int diveKind;
        int trick = -1;
        int trickAge;
        Vec3 motion = Vec3.ZERO;
        Vec3 prevMotion = Vec3.ZERO;
        int idleTicks;

        void tick(Entity entity) {
            this.prevMotion = this.motion;
            this.motion = entity.position().subtract(entity.xo, entity.yo, entity.zo);
            boolean onGround = entity.onGround();
            this.prevZip = this.zip;
            this.prevDive = this.dive;

            // swing_zip: up quickly while pulled through the air, eases off after
            if (this.pulling && !onGround) {
                this.zip = Math.min(this.zip + 0.25F, 1.0F);
            } else {
                this.zip = Math.max(this.zip - 0.2F, 0.0F);
            }
            // letting go in the air: often a flip
            if (this.wasPulling && !this.pulling && !onGround && RANDOM.nextFloat() < 0.7F) {
                this.trick = RANDOM.nextInt(4);
                this.trickAge = 0;
            }
            if (this.trick >= 0 && (++this.trickAge > TRICK_TICKS || onGround)) {
                this.trick = -1;
            }
            // falling afterwards: dive
            boolean diving = !onGround && !this.pulling && this.trick < 0 && this.motion.y < -0.15;
            if (diving) {
                if (this.dive == 0.0F) {
                    this.diveKind = RANDOM.nextInt(2);
                }
                this.dive = Math.min(this.dive + 0.1F, 1.0F);
            } else {
                this.dive = Math.max(this.dive - (onGround ? 0.34F : 0.1F), 0.0F);
            }
            this.wasPulling = this.pulling;
            this.idleTicks = this.pulling || !onGround ? 0 : this.idleTicks + 1;
            this.pulling = false;
        }

        boolean idle() {
            return this.idleTicks > 10 && this.zip == 0.0F && this.dive == 0.0F && this.prevDive == 0.0F && this.trick < 0;
        }

        Vec3 motion(float partialTick) {
            return this.prevMotion.lerp(this.motion, partialTick);
        }

        float zip(float partialTick) {
            return Mth.lerp(partialTick, this.prevZip, this.zip);
        }

        float dive(float partialTick) {
            return Mth.lerp(partialTick, this.prevDive, this.dive);
        }

        float trickProgress(float partialTick) {
            return Mth.clamp((this.trickAge + partialTick) / TRICK_TICKS, 0.0F, 1.0F);
        }
    }
}
