package net.threetag.palladium.client.model.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.entity.FlightHandler;
import net.threetag.palladium.entity.PalladiumPlayerExtension;
import net.threetag.palladium.util.Easing;
import net.threetag.palladiumcore.event.ViewportEvents;

import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class FlightAnimation extends PalladiumAnimation implements ViewportEvents.ComputeCameraAngles {

    private static final int BODY_X = 0, BODY_Y = 1, BODY_Z = 2;
    private static final int HEAD_X = 3, HEAD_Y = 4, HEAD_Z = 5;
    private static final int RIGHT_ARM_X = 6, RIGHT_ARM_Y = 7, RIGHT_ARM_Z = 8;
    private static final int LEFT_ARM_X = 9, LEFT_ARM_Y = 10, LEFT_ARM_Z = 11;
    private static final int RIGHT_LEG_X = 12, RIGHT_LEG_Y = 13, RIGHT_LEG_Z = 14;
    private static final int LEFT_LEG_X = 15, LEFT_LEG_Y = 16, LEFT_LEG_Z = 17;
    private static final int VALUE_COUNT = 18;

    private static final float ROLL_SPIN_TIME = 0.55F;

    public FlightAnimation(int priority) {
        super(priority);

        ViewportEvents.COMPUTE_CAMERA_ANGLES.register(this);
    }

    @Override
    public void animate(Builder builder, AbstractClientPlayer player, HumanoidModel<?> model, FirstPersonContext firstPersonContext, float partialTicks) {
        if (firstPersonContext.firstPerson() || !(player instanceof PalladiumPlayerExtension extension)) {
            return;
        }

        var flight = extension.palladium$getFlightHandler();
        float fade = flight.getHoveringAnimation(partialTicks);

        if (fade <= 0F) {
            return;
        }

        var context = new Context(flight, player, partialTicks);
        var from = pose(flight.getBlendFromState(), context);
        var to = pose(flight.getAnimationState(), context);
        float blend = Easing.OUTSINE.apply(flight.getBlendProgress(partialTicks));

        this.apply(builder, player, model, Pose.blend(from, to, blend, model), fade, context);
    }

    private void apply(Builder builder, AbstractClientPlayer player, HumanoidModel<?> model, Pose pose, float fade, Context context) {
        float[] v = pose.values;

        if (FlightHandler.getAnimationType(player) == FlightHandler.FlightAnimationType.HEROIC) {
            boolean right = player.getMainArm() == HumanoidArm.RIGHT;
            int arm = right ? RIGHT_ARM_X : LEFT_ARM_X;

            v[arm] = Mth.lerp(context.propulsion, v[arm], -180F);
            v[arm + 1] = Mth.lerp(context.propulsion, v[arm + 1], 0F);
            v[arm + 2] = Mth.lerp(context.propulsion, v[arm + 2], right ? -15F : 15F);
        }

        builder.get(PlayerModelPart.BODY)
                .setXRotDegrees(-v[BODY_X])
                .setYRotDegrees(-v[BODY_Y])
                .setZRotDegrees(v[BODY_Z])
                .setY(0.2F * 16F * pose.bodyOffset)
                .setY2(-1.8F / 2F * 16F * pose.bodyOffset)
                .multiplier(fade);

        part(builder, PlayerModelPart.HEAD, v, HEAD_X, fade);
        part(builder, PlayerModelPart.RIGHT_ARM, v, RIGHT_ARM_X, fade);
        part(builder, PlayerModelPart.LEFT_ARM, v, LEFT_ARM_X, fade);
        part(builder, PlayerModelPart.RIGHT_LEG, v, RIGHT_LEG_X, fade);
        part(builder, PlayerModelPart.LEFT_LEG, v, LEFT_LEG_X, fade);

        this.addAttackAnimation(builder, player, model);
    }

    private void addAttackAnimation(Builder builder, AbstractClientPlayer player, HumanoidModel<?> model) {
        if (model.attackTime <= 0.0F) {
            return;
        }

        HumanoidArm humanoidArm = player.swingingArm == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        PartAnimationData modelPart = builder.get(humanoidArm == HumanoidArm.RIGHT ? PlayerModelPart.RIGHT_ARM : PlayerModelPart.LEFT_ARM);
        var bodyYRot = model.body.yRot;

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setX(-Mth.cos(bodyYRot) * 5.0F)
                .setZ(Mth.sin(bodyYRot) * 5.0F)
                .setYRotShortest(bodyYRot);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setX(Mth.cos(bodyYRot) * 5.0F)
                .setZ(-Mth.sin(bodyYRot) * 5.0F)
                .setXRotShortest(bodyYRot)
                .setYRotShortest(bodyYRot);

        float f = 1.0F - model.attackTime;
        f *= f;
        f *= f;
        f = 1.0F - f;
        float g = Mth.sin(f * (float) Math.PI);
        float h = Mth.sin(model.attackTime * (float) Math.PI) * -(model.head.xRot - 0.7F) * 0.75F;

        modelPart.rotateX(-(g * 1.2F + h)).rotateY(bodyYRot * 2F).rotateZ(Mth.sin(model.attackTime * (float) Math.PI) * -0.4F);
    }

    private static void part(Builder builder, PlayerModelPart modelPart, float[] values, int offset, float fade) {
        builder.get(modelPart)
                .setXRotShortestDegrees(values[offset])
                .setYRotShortestDegrees(values[offset + 1])
                .setZRotShortestDegrees(values[offset + 2])
                .multiplier(fade);
    }

    private static Pose pose(FlightHandler.AnimationState state, Context context) {
        return switch (state) {
            case HOVERING -> hovering(context);
            case MOVING -> levitating(context);
            case PROPELLED -> propelled(context);
            case ROLLING_LEFT -> barrelRoll(context, 1F);
            case ROLLING_RIGHT -> barrelRoll(context, -1F);
        };
    }

    private static Pose hovering(Context c) {
        var pose = new Pose();
        float inverse = 1F - c.propulsion;

        pose.set(BODY_X, c.propulsion * (c.headPitch + 80F) + 30F * c.forward * inverse);
        pose.set(BODY_Y, c.yawDelta * c.propulsion);
        pose.set(BODY_Z, -c.roll * c.propulsion - 30F * c.sideways * inverse);

        float armSwing = 10F + sinDegrees(c.animTime * 100F) * 3F;
        pose.set(RIGHT_ARM_X, 5F).set(RIGHT_ARM_Y, 20F).set(RIGHT_ARM_Z, armSwing);
        pose.set(LEFT_ARM_X, 5F).set(LEFT_ARM_Y, -20F).set(LEFT_ARM_Z, -armSwing);

        pose.set(RIGHT_LEG_X, sinDegrees(c.animTime * 75F) * 6F).set(RIGHT_LEG_Y, 10F).set(RIGHT_LEG_Z, 2F);
        pose.set(LEFT_LEG_X, cosDegrees(c.animTime * 75F) * 6F).set(LEFT_LEG_Y, -10F).set(LEFT_LEG_Z, -2F);

        return pose;
    }

    private static Pose levitating(Context c) {
        var pose = new Pose();
        float forward = c.forward;
        float sideways = c.sideways;

        pose.set(BODY_X, 30F * forward).set(BODY_Y, 0F).set(BODY_Z, -30F * sideways);

        pose.set(HEAD_X, c.headPitch - 30F * forward).set(HEAD_Y, c.headYaw);

        pose.set(RIGHT_ARM_X, 0.2F * forward + 0.1F * sideways).set(RIGHT_ARM_Y, 20F)
                .set(RIGHT_ARM_Z, 15F + 0.1F * Math.abs(forward) + 0.2F * sideways);
        pose.set(LEFT_ARM_X, 0.2F * forward - 0.1F * sideways).set(LEFT_ARM_Y, -20F)
                .set(LEFT_ARM_Z, -15F - 0.1F * Math.abs(forward) + 0.2F * sideways);

        pose.set(RIGHT_LEG_X, 0.3F * forward).set(RIGHT_LEG_Y, 5F)
                .set(RIGHT_LEG_Z, 2F + 0.05F * Math.abs(forward) + 0.4F * sideways);
        pose.set(LEFT_LEG_X, 0.2F * forward).set(LEFT_LEG_Y, -5F)
                .set(LEFT_LEG_Z, -2F - 0.05F * Math.abs(forward) + 0.4F * sideways);

        return pose;
    }

    private static Pose propelled(Context c) {
        var pose = new Pose();
        float propulsion = c.propulsion;
        float inverse = 1F - propulsion;
        float turn = c.forward * c.yawDelta * propulsion / 2.5F;

        pose.set(BODY_X, propulsion * (c.headPitch + 80F) + 30F * c.forward * inverse);
        pose.set(BODY_Y, c.yawDelta * propulsion);
        pose.set(BODY_Z, -c.roll * propulsion - 30F * c.sideways * inverse);
        pose.bodyOffset = propulsion;

        pose.set(HEAD_X, c.headPitch - propulsion * (c.headPitch + 45F) - 30F * c.forward * inverse);
        pose.set(HEAD_Y, c.headYaw * inverse);

        float armX = 8F * propulsion + 0.5F * Math.abs(turn) + 5F * inverse;
        float armSwing = 10F + sinDegrees(c.animTime * 100F) * 3F;
        pose.set(RIGHT_ARM_X, armX).set(RIGHT_ARM_Y, 18F * propulsion + 20F * inverse)
                .set(RIGHT_ARM_Z, 12F * propulsion + armSwing * inverse);
        pose.set(LEFT_ARM_X, armX).set(LEFT_ARM_Y, -18F * propulsion - 20F * inverse)
                .set(LEFT_ARM_Z, -12F * propulsion - armSwing * inverse);

        pose.set(RIGHT_LEG_X, 15F * propulsion + 0.3F * turn + sinDegrees(c.animTime * 75F) * 6F * inverse)
                .set(RIGHT_LEG_Y, 2F * propulsion + 10F * inverse)
                .set(RIGHT_LEG_Z, 3F * propulsion + 2F * inverse);
        pose.set(LEFT_LEG_X, 15F * propulsion - 0.3F * turn + cosDegrees(c.animTime * 75F) * 6F * inverse)
                .set(LEFT_LEG_Y, -2F * propulsion - 10F * inverse)
                .set(LEFT_LEG_Z, -3F * propulsion - 2F * inverse);

        return pose;
    }

    private static Pose barrelRoll(Context c, float direction) {
        var pose = new Pose();
        float progress = Math.min(1F, c.rollTime / ROLL_SPIN_TIME);
        float spin = progress * progress * (3F - 2F * progress) - Mth.floor(progress);

        pose.set(BODY_X, c.headPitch + 80F);
        pose.set(BODY_Z, direction * 360F * spin);
        pose.bodyOffset = 1F;

        pose.set(HEAD_X, -45F).set(HEAD_Y, 0F);

        pose.set(RIGHT_ARM_X, 8F).set(RIGHT_ARM_Y, 5F).set(RIGHT_ARM_Z, 4F);
        pose.set(LEFT_ARM_X, 8F).set(LEFT_ARM_Y, -5F).set(LEFT_ARM_Z, -4F);

        pose.set(RIGHT_LEG_X, 12F).set(RIGHT_LEG_Y, 2F).set(RIGHT_LEG_Z, 3F);
        pose.set(LEFT_LEG_X, 12F).set(LEFT_LEG_Y, -2F).set(LEFT_LEG_Z, -3F);

        return pose;
    }

    private static float sinDegrees(float degrees) {
        return (float) Math.sin(Math.toRadians(degrees));
    }

    private static float cosDegrees(float degrees) {
        return (float) Math.cos(Math.toRadians(degrees));
    }

    public static Vec2 to2D(Vec3 vec) {
        return new Vec2((float) vec.x, (float) vec.z);
    }

    public static double angleBetweenVector(Vec2 vec1, Vec2 vec2) {
        return Math.atan2(vec1.x * vec2.y - vec1.y * vec2.x, vec1.x * vec2.x + vec1.y * vec2.y);
    }

    @Override
    public void computeCameraAngles(GameRenderer gameRenderer, Camera camera, double partialTick, AtomicReference<Float> yaw, AtomicReference<Float> pitch, AtomicReference<Float> roll) {
        if (Minecraft.getInstance().player instanceof PalladiumPlayerExtension extension) {
            var flight = extension.palladium$getFlightHandler();
            float partialTicks = (float) partialTick;
            float fade = flight.getHoveringAnimation(partialTicks);

            if (fade <= 0F) {
                return;
            }

            float propulsion = flight.getPropulsionScale(partialTicks);
            float bodyRoll = -flight.getRoll(partialTicks) * propulsion - 30F * flight.getSidewaysMotion(partialTicks) * (1F - propulsion);

            roll.set(-bodyRoll / 3F * fade);
        }
    }

    private record Context(float propulsion, float forward, float sideways, float yawDelta, float roll,
                           float headPitch, float headYaw, float animTime, float rollTime) {

        Context(FlightHandler flight, AbstractClientPlayer player, float partialTicks) {
            this(flight.getPropulsionScale(partialTicks), flight.getForwardMotion(partialTicks),
                    flight.getSidewaysMotion(partialTicks), flight.getYawDelta(partialTicks),
                    flight.getRoll(partialTicks), player.getViewXRot(partialTicks), netHeadYaw(player, partialTicks),
                    (player.tickCount + partialTicks) / 20F, flight.getRollTime(partialTicks));
        }

        private static float netHeadYaw(AbstractClientPlayer player, float partialTicks) {
            return Mth.wrapDegrees(Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot)
                    - Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot));
        }

    }

    private static class Pose {

        private final float[] values = new float[VALUE_COUNT];
        private final boolean[] present = new boolean[VALUE_COUNT];
        private float bodyOffset = 0F;

        private Pose set(int index, float degrees) {
            this.values[index] = degrees;
            this.present[index] = true;
            return this;
        }

        private float get(int index, float[] vanilla) {
            return this.present[index] ? this.values[index] : vanilla[index];
        }

        private static Pose blend(Pose from, Pose to, float progress, HumanoidModel<?> model) {
            float[] vanilla = vanilla(model);
            var result = new Pose();

            for (int i = 0; i < VALUE_COUNT; i++) {
                result.set(i, Mth.lerp(progress, from.get(i, vanilla), to.get(i, vanilla)));
            }

            result.bodyOffset = Mth.lerp(progress, from.bodyOffset, to.bodyOffset);
            return result;
        }

        private static float[] vanilla(HumanoidModel<?> model) {
            float[] vanilla = new float[VALUE_COUNT];
            put(vanilla, HEAD_X, model.head);
            put(vanilla, RIGHT_ARM_X, model.rightArm);
            put(vanilla, LEFT_ARM_X, model.leftArm);
            put(vanilla, RIGHT_LEG_X, model.rightLeg);
            put(vanilla, LEFT_LEG_X, model.leftLeg);
            return vanilla;
        }

        private static void put(float[] vanilla, int offset, ModelPart part) {
            vanilla[offset] = Mth.wrapDegrees((float) Math.toDegrees(part.xRot));
            vanilla[offset + 1] = Mth.wrapDegrees((float) Math.toDegrees(part.yRot));
            vanilla[offset + 2] = Mth.wrapDegrees((float) Math.toDegrees(part.zRot));
        }

    }
}
