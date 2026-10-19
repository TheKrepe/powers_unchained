package net.threetag.palladium.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.sound.FlightSound;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.property.PalladiumProperties;
import net.threetag.palladiumcore.util.Platform;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FlightHandler {

    public static Object CACHED_SOUND = null;

    private static final float MAX_TURN_ANGLE = 85F;
    private static final float MAX_TURN_CORRECTION = 15F;
    private static final int PROPULSION_TIMER_LENGTH = 14;
    private static final int IN_FLIGHT_TIMER_LENGTH = 10;
    private static final int MOVING_TIMER_LENGTH = 10;
    private static final float SPRINT_SPEED_FALLBACK_FACTOR = 3F;

    private static final int PROPELLED_BLEND_LENGTH = 20;
    private static final int LEVITATION_BLEND_LENGTH = 10;
    private static final int ROLL_BLEND_LENGTH = 5;
    private static final int ROLL_LENGTH = 12;
    private static final float ROLL_TRIGGER_YAW = 18F;
    private static final float ROLL_EXIT_YAW = 6F;

    private final Player player;
    private FlightHandler.FlightType flightType = FlightHandler.FlightType.NONE;
    private Phase phase = Phase.NONE;

    private Vec3 flightVector = Vec3.ZERO;
    private Vec3 prevFlightVector = Vec3.ZERO;
    private Vec3 prevMovementDelta = Vec3.ZERO;
    private Vec3 prevLookAngle = Vec3.ZERO;
    private boolean hasMovementInput = false;

    private float speed = 0F;
    private float prevSpeed = 0F;
    private float horizontalSpeed = 0F;
    private float prevHorizontalSpeed = 0F;

    private float forwardMotion = 0F, prevForwardMotion = 0F;
    private float sidewaysMotion = 0F, prevSidewaysMotion = 0F;
    private float yawDelta = 0F, prevYawDelta = 0F;
    private float roll = 0F, prevRoll = 0F;
    private float lastTrackedYaw = Float.NaN;

    private int propulsionTimer = 0, prevPropulsionTimer = 0;
    private int inFlightTimer = 0, prevInFlightTimer = 0;
    private int movingTimer = 0, prevMovingTimer = 0;

    private AnimationState animationState = AnimationState.HOVERING;
    private AnimationState blendFrom = AnimationState.HOVERING;
    private int blendLength = 1;
    private int blendTimer = 1, prevBlendTimer = 1;
    private int rollTimer = 0, prevRollTimer = 0;

    public float flightBoost = 0F;
    private float prevFlightBoost = 0F;

    @Nullable
    private Pose hitboxPose = null;
    private boolean ignoreHitboxPose = false;

    public FlightHandler(Player player) {
        this.player = player;
    }

    public void tick() {
        this.prevMovementDelta = this.player.getDeltaMovement();
        this.prevFlightVector = this.flightVector;
        this.prevLookAngle = this.player.getLookAngle();
        this.prevSpeed = this.speed;
        this.prevHorizontalSpeed = this.horizontalSpeed;
        this.speed = (float) this.prevMovementDelta.length();
        this.horizontalSpeed = (float) this.prevMovementDelta.horizontalDistance();
        this.prevFlightBoost = this.flightBoost;

        // Reset flight type if on ground
        if (this.flightType.isNotNull() && (this.player.onGround() || this.player.isFallFlying() || this.player.isSwimming()
                || this.player.getAttributeValue(this.flightType.getAttribute()) <= 0D)) {
            this.flightType = FlightHandler.FlightType.NONE;
        }

        if (this.flightType.isNotNull()) {
            this.updateMovement();
            this.updatePhase();
            this.clampRotation();
            this.player.fallDistance = 0F;
        } else {
            this.flightVector = Vec3.ZERO;
            this.hasMovementInput = false;
            this.phase = Phase.NONE;
        }

        this.tickAnimationValues();
        this.tickAnimationState();
        this.updateHitbox();

        if (this.flightBoost > 1F && this.prevFlightBoost <= 1F && Platform.isClient()) {
            FlightHandler.startSound(this.player);
        }
    }

    private void updateHitbox() {
        var pose = this.getDesiredHitboxPose();

        if (pose == this.hitboxPose) {
            return;
        }

        var dimensions = this.getDimensions(pose);

        if (dimensions.height > this.player.getBbHeight() && !this.canFit(dimensions)) {
            return;
        }

        this.hitboxPose = pose;
        this.player.refreshDimensions();
    }

    @Nullable
    private Pose getDesiredHitboxPose() {
        if (this.flightBoost > 1F) {
            return Pose.FALL_FLYING;
        }

        if (this.getHoveringAnimation(0F) > 0F || this.getLevitationAnimation(0F) > 0F || this.getFlightAnimation(0F) > 0F) {
            return Pose.STANDING;
        }

        return null;
    }

    private EntityDimensions getDimensions(@Nullable Pose pose) {
        this.ignoreHitboxPose = true;

        try {
            return this.player.getDimensions(pose == null ? this.player.getPose() : pose);
        } finally {
            this.ignoreHitboxPose = false;
        }
    }

    private boolean canFit(EntityDimensions dimensions) {
        return this.player.level().noCollision(this.player, dimensions.makeBoundingBox(this.player.position()).deflate(1.0E-6));
    }

    public Pose getHitboxPose(Pose pose) {
        return this.ignoreHitboxPose || this.hitboxPose == null ? pose : this.hitboxPose;
    }

    private void updateMovement() {
        var lookAngle = this.player.getLookAngle().normalize();
        boolean forward = PalladiumProperties.FORWARD_KEY_DOWN.get(this.player);
        boolean backward = PalladiumProperties.BACKWARDS_KEY_DOWN.get(this.player);
        boolean left = PalladiumProperties.LEFT_KEY_DOWN.get(this.player);
        boolean right = PalladiumProperties.RIGHT_KEY_DOWN.get(this.player);
        boolean jump = PalladiumProperties.JUMP_KEY_DOWN.get(this.player);
        boolean shift = this.player.isShiftKeyDown();
        this.hasMovementInput = forward || backward || left || right || jump || shift;

        if (this.hasMovementInput) {
            var moveVector = forward && backward ? Vec3.ZERO : forward ? lookAngle : backward ? lookAngle.scale(-1) : Vec3.ZERO;
            moveVector = moveVector.add(0, jump && shift ? 0 : jump ? 1 : shift ? -1 : 0, 0);

            if (right || left) {
                var sideVec = lookAngle.yRot((float) Math.toRadians(left && right ? 0 : left ? 90F : -90F));
                moveVector = moveVector.add(sideVec.x, 0, sideVec.z);
            }

            var max = moveVector.normalize().scale(this.getCurrentSpeed() * 0.5F);
            this.flightVector = this.flightVector.add(max.subtract(this.flightVector).scale(0.2F));
        } else if (this.flightVector.length() > 0.001F) {
            this.flightVector = this.flightVector.scale(0.8);
        } else {
            this.flightVector = Vec3.ZERO;
        }

        this.player.setDeltaMovement(new Vec3(this.flightVector.x,
                this.flightVector.y + Math.sin(this.player.tickCount / 10F) / 100F, this.flightVector.z));
    }

    private void updatePhase() {
        boolean moving = this.flightVector.lengthSqr() > 0.01 || this.hasMovementInput;
        this.phase = this.isPropelling() ? Phase.PROPELLED : moving ? Phase.MOVING : Phase.HOVERING;
    }

    private void clampRotation() {
        if (!this.isPropelling() || !this.player.isLocalPlayer() || this.flightVector.horizontalDistanceSqr() <= 1.0E-4) {
            return;
        }

        var lookAngle = this.player.getLookAngle();

        if (this.flightVector.x * lookAngle.x + this.flightVector.z * lookAngle.z <= 0D) {
            return;
        }

        float yaw = (float) (Mth.atan2(-this.flightVector.x, this.flightVector.z) * (180F / Math.PI));
        float f = Mth.wrapDegrees(this.player.getYRot() - yaw);
        float correction = f - Mth.clamp(f, -MAX_TURN_ANGLE, MAX_TURN_ANGLE);

        if (correction != 0F) {
            this.player.setYRot(this.player.getYRot() - Mth.clamp(correction, -MAX_TURN_CORRECTION, MAX_TURN_CORRECTION));
            this.player.setYHeadRot(this.player.getYRot());
        }
    }

    private void tickAnimationValues() {
        boolean flying = this.flightType.isNotNull();

        this.prevForwardMotion = this.forwardMotion;
        this.prevSidewaysMotion = this.sidewaysMotion;
        this.prevYawDelta = this.yawDelta;
        this.prevRoll = this.roll;
        this.prevPropulsionTimer = this.propulsionTimer;
        this.prevInFlightTimer = this.inFlightTimer;
        this.prevMovingTimer = this.movingTimer;

        float rawYawDelta = Float.isNaN(this.lastTrackedYaw) ? 0F : Mth.wrapDegrees(this.player.getYRot() - this.lastTrackedYaw);
        this.lastTrackedYaw = this.player.getYRot();

        Vec3 vel = this.player.getDeltaMovement();
        double totalSpeed = vel.length();
        float pitchFactor = totalSpeed > 0.01
                ? 1F - (float) Mth.clamp((Math.abs(vel.y) / totalSpeed - 0.3) / 0.4, 0.0, 1.0)
                : 1F;
        this.yawDelta = Mth.lerp(0.3F, this.yawDelta, flying ? rawYawDelta * pitchFactor : 0F);

        this.inFlightTimer = tickTimer(this.inFlightTimer, flying, IN_FLIGHT_TIMER_LENGTH);
        this.propulsionTimer = tickTimer(this.propulsionTimer, flying && this.isPropelling(), PROPULSION_TIMER_LENGTH);
        this.movingTimer = tickTimer(this.movingTimer, this.phase == Phase.MOVING || this.phase == Phase.PROPELLED, MOVING_TIMER_LENGTH);

        float targetForward = 0F;
        float targetSideways = 0F;
        float targetRoll = 0F;

        if (flying) {
            Vec3 v = this.player.getDeltaMovement();
            double horizSpeed = v.horizontalDistance();

            if (v.lengthSqr() > 1.0E-3 && horizSpeed > 0.05D) {
                v = v.normalize();
                var lookAngle = this.player.getLookAngle();
                var f = lookAngle.subtract(0, lookAngle.y, 0);

                var vHoriz = new Vec3(v.x, 0, v.z).normalize();
                var fHoriz = new Vec3(f.x, 0, f.z).normalize();

                var right = fHoriz.cross(new Vec3(0, 1, 0)).normalize();
                double side = vHoriz.dot(right);
                double forward = vHoriz.dot(fHoriz);

                targetRoll = (float) Mth.clamp(-side * 80F * horizSpeed, -35F, 35F);

                float maxSpeed = this.getBaseSpeed() * 0.5F;

                if (maxSpeed > 0F) {
                    targetForward = Mth.clamp((float) (forward * horizSpeed / maxSpeed), -1F, 1F);
                    targetSideways = Mth.clamp((float) (side * horizSpeed / maxSpeed), -1F, 1F);
                }
            }
        }

        this.forwardMotion = Mth.lerp(Math.abs(targetForward) > Math.abs(this.forwardMotion) ? 0.4F : 0.15F, this.forwardMotion, targetForward);
        this.sidewaysMotion = Mth.lerp(Math.abs(targetSideways) > Math.abs(this.sidewaysMotion) ? 0.4F : 0.15F, this.sidewaysMotion, targetSideways);
        this.roll = Mth.lerp(Math.abs(targetRoll) > Math.abs(this.roll) ? 0.4F : 0.08F, this.roll, targetRoll);

        this.flightBoost = this.getFlightAnimation(1F);
    }

    private void tickAnimationState() {
        this.prevBlendTimer = this.blendTimer;
        this.prevRollTimer = this.rollTimer;

        if (this.flightType.isNull() && this.inFlightTimer <= 0) {
            this.animationState = this.blendFrom = AnimationState.HOVERING;
            this.blendLength = 1;
            this.blendTimer = this.prevBlendTimer = 1;
            this.rollTimer = this.prevRollTimer = 0;
            return;
        }

        if (this.animationState.isRolling()) {
            this.rollTimer++;
        }

        var next = this.getNextAnimationState();

        if (next != null) {
            this.blendFrom = this.animationState;
            this.animationState = next;
            this.blendLength = next.isRolling() ? ROLL_BLEND_LENGTH
                    : this.flightType.allowsPropulsion() ? PROPELLED_BLEND_LENGTH : LEVITATION_BLEND_LENGTH;
            this.blendTimer = this.prevBlendTimer = 0;

            if (next.isRolling()) {
                this.rollTimer = this.prevRollTimer = 0;
            }
        } else if (this.blendTimer < this.blendLength) {
            this.blendTimer++;
        }
    }

    private AnimationState getNextAnimationState() {
        if (this.flightType.isNull()) {
            return null;
        }

        float forward = Math.abs(this.forwardMotion);
        float sideways = Math.abs(this.sidewaysMotion);
        boolean idle = forward < 0.1F && sideways < 0.1F;

        if (!this.flightType.allowsPropulsion()) {
            if (this.animationState == AnimationState.HOVERING) {
                return this.phase == Phase.MOVING ? AnimationState.MOVING : null;
            } else if (this.animationState == AnimationState.MOVING) {
                return this.phase == Phase.HOVERING && idle ? AnimationState.HOVERING : null;
            }

            return AnimationState.HOVERING;
        }

        return switch (this.animationState) {
            case HOVERING -> this.phase == Phase.PROPELLED ? AnimationState.PROPELLED
                    : this.phase == Phase.MOVING ? AnimationState.MOVING : null;
            case MOVING -> this.phase == Phase.PROPELLED ? AnimationState.PROPELLED
                    : this.phase == Phase.HOVERING && idle ? AnimationState.HOVERING : null;
            case PROPELLED -> {
                if (this.phase == Phase.PROPELLED && this.yawDelta < -ROLL_TRIGGER_YAW) {
                    yield AnimationState.ROLLING_LEFT;
                } else if (this.phase == Phase.PROPELLED && this.yawDelta > ROLL_TRIGGER_YAW) {
                    yield AnimationState.ROLLING_RIGHT;
                } else if (this.phase == Phase.HOVERING && this.propulsionTimer <= 0 && idle) {
                    yield AnimationState.HOVERING;
                } else if (this.phase == Phase.MOVING && this.propulsionTimer <= 0) {
                    yield AnimationState.MOVING;
                }

                yield null;
            }
            case ROLLING_LEFT, ROLLING_RIGHT ->
                    this.rollTimer >= ROLL_LENGTH && Math.abs(this.yawDelta) < ROLL_EXIT_YAW ? AnimationState.PROPELLED : null;
        };
    }

    private static int tickTimer(int value, boolean active, int max) {
        return Mth.clamp(value + (active ? 1 : -1), 0, max);
    }

    private static float easedTimer(int prev, int current, int max, Easing easing, float partialTicks) {
        return easing.apply(Mth.clamp(Mth.lerp(partialTicks, prev, current) / (float) max, 0F, 1F));
    }

    private float getBaseSpeed() {
        return this.flightType.isNull() ? 0F : (float) this.player.getAttributeValue(this.flightType.getAttribute());
    }

    private float getSprintSpeed() {
        if (!this.flightType.allowsPropulsion()) {
            return this.getBaseSpeed();
        }

        double sprint = this.player.getAttributes().hasAttribute(PalladiumAttributes.FLIGHT_SPRINT_SPEED.get())
                ? this.player.getAttributeValue(PalladiumAttributes.FLIGHT_SPRINT_SPEED.get()) : 0D;
        return sprint > 0D ? (float) sprint : this.getBaseSpeed() * SPRINT_SPEED_FALLBACK_FACTOR;
    }

    private float getCurrentSpeed() {
        return this.isPropelling() ? this.getSprintSpeed() : this.getBaseSpeed();
    }

    public boolean isPropelling() {
        return this.player.isSprinting() && this.flightType.allowsPropulsion() && !this.player.isUsingItem();
    }

    public Vec3 getPrevMovementDelta() {
        return this.prevMovementDelta;
    }

    public FlightHandler.FlightType getFlightType() {
        return this.flightType;
    }

    public Phase getPhase() {
        return this.phase;
    }

    public void setFlightType(FlightHandler.FlightType flightType) {
        boolean wasFlying = this.flightType.isNotNull();
        this.flightType = flightType;

        if (flightType.isNotNull()) {
            if (!wasFlying) {
                this.flightVector = this.player.getDeltaMovement();
            }

            this.phase = Phase.HOVERING;
        } else {
            this.phase = Phase.NONE;
        }

        this.player.refreshDimensions();
    }

    public float getFlightAnimation(float partialTicks) {
        return this.getMovingAnimation(partialTicks) + 2F * this.getPropulsionScale(partialTicks);
    }

    public float getMovingAnimation(float partialTicks) {
        return easedTimer(this.prevMovingTimer, this.movingTimer, MOVING_TIMER_LENGTH, Easing.INOUTCUBIC, partialTicks);
    }

    public float getLevitationAnimation(float partialTicks) {
        return this.getMovingAnimation(partialTicks) * (1F - this.getPropulsionScale(partialTicks));
    }

    public float getHoveringAnimation(float partialTicks) {
        return easedTimer(this.prevInFlightTimer, this.inFlightTimer, IN_FLIGHT_TIMER_LENGTH, Easing.INOUTCUBIC, partialTicks);
    }

    public float getPropulsionScale(float partialTicks) {
        return easedTimer(this.prevPropulsionTimer, this.propulsionTimer, PROPULSION_TIMER_LENGTH, Easing.OUTSINE, partialTicks);
    }

    public float getForwardMotion(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevForwardMotion, this.forwardMotion);
    }

    public float getSidewaysMotion(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevSidewaysMotion, this.sidewaysMotion);
    }

    public float getYawDelta(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevYawDelta, this.yawDelta);
    }

    public float getRoll(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevRoll, this.roll);
    }

    public AnimationState getAnimationState() {
        return this.animationState;
    }

    public AnimationState getBlendFromState() {
        return this.blendFrom;
    }

    public float getBlendProgress(float partialTicks) {
        return Mth.clamp(Mth.lerp(partialTicks, this.prevBlendTimer, this.blendTimer) / (float) this.blendLength, 0F, 1F);
    }

    public float getRollTime(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevRollTimer, this.rollTimer) / 20F;
    }

    public Vec3 getFlightVector(float partialTicks) {
        return this.prevFlightVector.lerp(this.flightVector, partialTicks);
    }

    public Vec3 getLookAngle(float partialTicks) {
        return this.prevLookAngle.lerp(this.player.getLookAngle(), partialTicks);
    }

    public float getSpeed(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevSpeed, this.speed);
    }

    public float getHorizontalSpeed(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevHorizontalSpeed, this.horizontalSpeed);
    }

    public static FlightType getAvailableFlightType(LivingEntity entity) {
        if (entity.getAttributes().hasAttribute(PalladiumAttributes.FLIGHT_SPEED.get()) && entity.getAttributeValue(PalladiumAttributes.FLIGHT_SPEED.get()) > 0D) {
            return FlightType.NORMAL;
        }

        if (entity.getAttributes().hasAttribute(PalladiumAttributes.LEVITATION_SPEED.get()) && entity.getAttributeValue(PalladiumAttributes.LEVITATION_SPEED.get()) > 0D) {
            return FlightType.LEVITATION;
        }

        return FlightType.NONE;
    }

    public static FlightAnimationType getAnimationType(LivingEntity entity) {
        if (entity.getAttributes().hasAttribute(PalladiumAttributes.HEROIC_FLIGHT_TYPE.get()) && entity.getAttributeValue(PalladiumAttributes.HEROIC_FLIGHT_TYPE.get()) > 0D) {
            return FlightAnimationType.HEROIC;
        }

        return FlightAnimationType.NORMAL;
    }

    @Environment(EnvType.CLIENT)
    public static void startSound(Player player) {
        if (player == Minecraft.getInstance().player) {
            if (CACHED_SOUND instanceof FlightSound sound) {
                sound.stop = true;
            }

            Minecraft.getInstance().getSoundManager().play((SoundInstance) (CACHED_SOUND = new FlightSound(player, SoundEvents.ELYTRA_FLYING, player.getSoundSource())));
        }
    }

    public enum Phase {

        NONE, HOVERING, MOVING, PROPELLED

    }

    public enum AnimationState {

        HOVERING, MOVING, PROPELLED, ROLLING_LEFT, ROLLING_RIGHT;

        public boolean isRolling() {
            return this == ROLLING_LEFT || this == ROLLING_RIGHT;
        }

    }

    public enum FlightType {

        NONE(() -> null, false), NORMAL(PalladiumAttributes.FLIGHT_SPEED, true), LEVITATION(PalladiumAttributes.LEVITATION_SPEED, false);

        FlightType(Supplier<Attribute> attributeSupplier, boolean allowsPropulsion) {
            this.attributeSupplier = attributeSupplier;
            this.allowsPropulsion = allowsPropulsion;
        }

        private final Supplier<Attribute> attributeSupplier;
        private final boolean allowsPropulsion;

        public Attribute getAttribute() {
            return this.attributeSupplier.get();
        }

        public boolean allowsPropulsion() {
            return this.allowsPropulsion;
        }

        public boolean isNotNull() {
            return this != NONE;
        }

        public boolean isNull() {
            return this == NONE;
        }

        public boolean isNormal() {
            return this == NORMAL;
        }

        public boolean isLevitation() {
            return this == LEVITATION;
        }

    }

    public enum FlightAnimationType {

        NORMAL,
        HEROIC

    }

}
