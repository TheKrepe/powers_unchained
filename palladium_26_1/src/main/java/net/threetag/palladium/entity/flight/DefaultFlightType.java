package net.threetag.palladium.entity.flight;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.shake.ScreenShake;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.AnimationTimer;
import net.threetag.palladium.power.ability.AnimationTimerSetting;
import net.threetag.palladium.sound.PalladiumSoundEvents;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.PlayerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.IntFunction;

public class DefaultFlightType extends FlightType {

    public static final MapCodec<DefaultFlightType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.fieldOf("speed").forGetter(f -> f.speed),
            Value.CODEC.optionalFieldOf("sprint_speed").forGetter(f -> Optional.ofNullable(f.sprintSpeed)),
            ScreenShake.CODEC.optionalFieldOf("screen_shake").forGetter(f -> Optional.ofNullable(f.screenShake)),
            Identifier.CODEC.optionalFieldOf("sound").forGetter(f -> Optional.ofNullable(f.sound)),
            Identifier.CODEC.optionalFieldOf("propelled_sound").forGetter(f -> Optional.ofNullable(f.propelledSound)),
            Identifier.CODEC.optionalFieldOf("propelled_start_sound").forGetter(f -> Optional.ofNullable(f.propelledStartSound))
    ).apply(instance, (s, ss, sh, a, so, ps) -> new DefaultFlightType(s, ss.orElse(null), sh.orElse(null), a.orElse(null), so.orElse(null), ps.orElse(null))));

    private final Value speed;
    @Nullable
    private final Value sprintSpeed;
    @Nullable
    private final ScreenShake screenShake;
    @Nullable
    private final Identifier sound;
    @Nullable
    private final Identifier propelledSound;
    @Nullable
    private final Identifier propelledStartSound;

    public DefaultFlightType(Value speed, Value sprintSpeed, ScreenShake screenShake, Identifier sound, Identifier propelledSound, Identifier propelledStartSound) {
        this.speed = speed;
        this.sprintSpeed = sprintSpeed;
        this.screenShake = screenShake;
        this.sound = sound;
        this.propelledSound = propelledSound;
        this.propelledStartSound = propelledStartSound;
    }

    @Override
    public FlightTypeSerializer<?> getSerializer() {
        return FlightTypeSerializers.FLIGHT.get();
    }

    @Override
    public FlightController<?> createController() {
        return new Controller();
    }

    @Override
    public @Nullable FlightAnimationHandler<?> createAnimationHandler() {
        return new AnimationHandler(this);
    }

    public boolean isPropelling(LivingEntity entity) {
        return this.sprintSpeed != null && entity.isSprinting() && !entity.isUsingItem();
    }

    @Override
    @Nullable
    public ScreenShake getScreenShake(LivingEntity entity) {
        if (this.screenShake != null && this.isPropelling(entity)) {
            return this.screenShake;
        }
        return null;
    }

    @Override
    @Nullable
    public Identifier getFlightSound() {
        return this.sprintSpeed != null ? this.propelledSound : null;
    }

    @Override
    @Nullable
    public Identifier getAmbientFlightSound() {
        return this.sound;
    }

    public enum Phase {

        NONE(0),
        HOVERING(1),
        MOVING(2),
        PROPELLED(3),
        DESCENDING(4),
        LANDING(5);

        public static final IntFunction<Phase> BY_ID = ByIdMap.continuous(Phase::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        private final int id;

        Phase(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static Phase byId(int id) {
            return BY_ID.apply(id);
        }
    }

    public static class Controller extends FlightController<DefaultFlightType> {

        private static final double DESCEND_ACCEL = 0.04;
        private static final double DESCEND_AIR_CONTROL = 0.02;
        private static final int LANDING_DURATION = 15;
        private static final int LANDING_CANCEL_TICKS = 4;
        private static final float MAX_TURN_ANGLE = 85F;

        public Vec3 flightVector = Vec3.ZERO;
        private Phase phase = Phase.NONE;
        private int landingTicks = 0;
        private boolean landingInputReleased = false;
        private int landingMoveTicks = 0;

        @Override
        public void tick(LivingEntity entity, DefaultFlightType flightType) {
            if (entity.level().isClientSide() && entity != Palladium.PROXY.getClientPlayer()) {
                return;
            }

            if (entity.getVehicle() != null) {
                this.stopFlight(entity);
                return;
            }

            entity.fallDistance = 0F;

            if (this.phase == Phase.LANDING) {
                this.tickLanding(entity);
                return;
            }

            if (this.phase == Phase.DESCENDING) {
                this.tickDescent(entity);
                return;
            }

            if (entity instanceof Player player) {
                var input = PlayerUtil.getMovementInput(player);

                if (input != null) {
                    this.updateMovement(player, flightType, input);
                    this.updatePhase(entity, flightType, input);
                }
            }

            super.tick(entity, flightType);
        }

        private void updateMovement(Player player, DefaultFlightType flightType, Input input) {
            var lookAngle = player.getLookAngle().normalize();

            if (input.forward() || input.backward() || input.left() || input.right() || input.jump() || input.shift()) {
                var moveVector = input.forward() && input.backward() ? Vec3.ZERO :
                        input.forward() ? lookAngle :
                                input.backward() ? lookAngle.scale(-1) : Vec3.ZERO;
                moveVector = moveVector.add(0, input.jump() && input.shift() ? 0 : input.jump() ? 1 : input.shift() ? -1 : 0, 0);
                var context = DataContext.forEntity(player);

                if (input.right() || input.left()) {
                    var sideVec = lookAngle.yRot((float) Math.toRadians(
                            input.left() && input.right() ? 0 : input.left() ? 90F : -90F
                    ));
                    moveVector = moveVector.add(sideVec.x, 0, sideVec.z);
                }

                var sprintSpeed = flightType.sprintSpeed != null ? flightType.sprintSpeed : flightType.speed;
                var max = moveVector.normalize()
                        .scale((player.isSprinting() && !player.isUsingItem() ? sprintSpeed.getAsFloat(context) : flightType.speed.getAsFloat(context))
                                * flightType.getSpeedMultiplier(player) * 0.5F);
                var diff = max.subtract(this.flightVector);
                this.flightVector = this.flightVector.add(diff.scale(0.2F));
            } else if (this.flightVector.length() > 0.001F) {
                this.flightVector = this.flightVector.scale(0.8);
            } else {
                this.flightVector = Vec3.ZERO;
            }

            player.setDeltaMovement(new Vec3(this.flightVector.x, this.flightVector.y + Math.sin(player.tickCount / 10F) / 100F, this.flightVector.z));
        }

        private void updatePhase(LivingEntity entity, DefaultFlightType flightType, Input input) {
            Vec3 velocity = entity.getDeltaMovement();

            if (velocity.y < -0.1 && -velocity.y > velocity.horizontalDistance() && isApproachingGround(entity)) {
                this.setPhase(entity, Phase.LANDING);
                this.landingTicks = 0;
                this.landingInputReleased = false;
                this.landingMoveTicks = 0;
                return;
            }

            boolean propulsion = flightType.isPropelling(entity);
            boolean moving = this.flightVector.lengthSqr() > 0.01
                    || input.forward() || input.backward() || input.left() || input.right() || input.jump() || input.shift();

            Phase newPhase = propulsion ? Phase.PROPELLED : moving ? Phase.MOVING : Phase.HOVERING;

            if (newPhase == Phase.PROPELLED && this.phase != Phase.PROPELLED
                    && flightType.propelledStartSound != null && !entity.level().isClientSide()) {
                PlayerUtil.playSoundToAll(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), 64,
                        flightType.propelledStartSound, entity.getSoundSource());
            }

            this.setPhase(entity, newPhase);
        }

        private void tickDescent(LivingEntity entity) {
            if (entity.onGround() || isApproachingGround(entity)) {
                this.setPhase(entity, Phase.LANDING);
                this.landingTicks = 0;
                this.landingInputReleased = false;
                this.landingMoveTicks = 0;
                return;
            }

            if (entity instanceof Player player) {
                var input = PlayerUtil.getMovementInput(player);

                if (input != null && input.forward()) {
                    var look = player.getLookAngle();
                    Vec3 lookH = new Vec3(look.x, 0, look.z);

                    if (lookH.lengthSqr() > 1.0E-4) {
                        entity.setDeltaMovement(entity.getDeltaMovement().add(lookH.normalize().scale(DESCEND_AIR_CONTROL)));
                    }
                }
            }
        }

        private void tickLanding(LivingEntity entity) {
            if (entity.onGround()) {
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4, 1, 0.4));

                if (entity instanceof Player player) {
                    if (wantsToMove(PlayerUtil.getMovementInput(player))) {
                        if (this.landingInputReleased && ++this.landingMoveTicks >= LANDING_CANCEL_TICKS) {
                            this.stopFlight(entity);
                            return;
                        }
                    } else {
                        this.landingInputReleased = true;
                        this.landingMoveTicks = 0;
                    }
                }

                if (++this.landingTicks >= LANDING_DURATION) {
                    this.stopFlight(entity);
                }
            } else if (isApproachingGround(entity)) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, -DESCEND_ACCEL, 0));
            } else {
                this.setPhase(entity, Phase.DESCENDING);
            }
        }

        private void setPhase(LivingEntity entity, Phase phase) {
            if (this.phase != phase) {
                boolean hitboxChanges = (this.phase == Phase.PROPELLED) != (phase == Phase.PROPELLED);
                this.phase = phase;

                if (hitboxChanges) {
                    entity.refreshDimensions();
                }

                if (!entity.level().isClientSide()) {
                    EntityFlightHandler.get(entity).broadcastFlightState();
                }
            }
        }

        @Override
        public void start(LivingEntity entity, DefaultFlightType flightType) {
            this.flightVector = entity.getDeltaMovement();
            this.phase = Phase.HOVERING;
            this.landingTicks = 0;
        }

        @Override
        public void stop(LivingEntity entity, DefaultFlightType flightType) {
            this.phase = Phase.NONE;
        }

        @Override
        public int getPhaseId() {
            return this.phase.getId();
        }

        @Override
        public void setSyncedPhase(int phaseId) {
            this.phase = Phase.byId(phaseId);
        }

        public Phase getPhase() {
            return this.phase;
        }

        @Override
        public void clampRotation(LivingEntity entity, DefaultFlightType flightType) {
            if (flightType.isPropelling(entity)
                    && entity instanceof Player player && PlayerUtil.getMovementInput(player) != null) {
                float yaw = (float) (Mth.atan2(-this.flightVector.x, this.flightVector.z) * (180F / Math.PI));
                float f = Mth.wrapDegrees(entity.getYRot() - yaw);
                float g = Mth.clamp(f, -MAX_TURN_ANGLE, MAX_TURN_ANGLE);
                entity.setYRot(entity.getYRot() + g - f);
                entity.setYHeadRot(entity.getYRot());
            }
        }

        @Override
        public boolean hasSmallHitbox(LivingEntity entity) {
            return this.phase == Phase.PROPELLED;
        }

        @Override
        public boolean isFlightSoundActive(LivingEntity entity, DefaultFlightType flightType) {
            return this.phase == Phase.PROPELLED;
        }
    }

    public static class AnimationHandler extends FlightAnimationHandler<DefaultFlightType> {

        public final DefaultFlightType flightType;
        public float forwardMotion, prevForwardMotion;
        public float sidewaysMotion, prevSidewaysMotion;
        public float yawDelta, prevYawDelta;
        private float lastTrackedYaw = Float.NaN;
        private float lastVelocityYaw = Float.NaN;
        public AnimationTimer propulsionScale = new AnimationTimerSetting(0, 14, AnimationTimerSetting.Behaviour.FOLLOW, Easing.OUTSINE).create();

        public AnimationHandler(DefaultFlightType flightType) {
            this.flightType = flightType;
        }

        @Override
        public void tick(LivingEntity entity, boolean flightStopped) {
            this.prevForwardMotion = this.forwardMotion;
            this.prevSidewaysMotion = this.sidewaysMotion;
            this.prevYawDelta = this.yawDelta;
            float rawYawDelta = Float.isNaN(this.lastTrackedYaw) ? 0F : Mth.wrapDegrees(entity.getYRot() - this.lastTrackedYaw);
            this.lastTrackedYaw = entity.getYRot();
            Vec3 vel = getVelocity(entity);
            double totalSpeed = vel.length();
            float pitchFactor = totalSpeed > 0.01
                    ? 1F - (float) Mth.clamp((Math.abs(vel.y) / totalSpeed - 0.3) / 0.4, 0.0, 1.0)
                    : 1F;
            this.yawDelta = Mth.lerp(0.3F, this.yawDelta, flightStopped ? 0F : rawYawDelta * pitchFactor);
            super.tick(entity, flightStopped);

            boolean propulsion = !flightStopped
                    && EntityFlightHandler.get(entity).getController() instanceof Controller controller
                    && controller.getPhase() == Phase.PROPELLED;
            this.propulsionScale.tickAndUpdate(propulsion);

            float targetForward = 0F;
            float targetSideways = 0F;
            float targetRoll = 0F;

            if (!flightStopped) {
                double horizSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

                if (vel.lengthSqr() > 1.0E-3 && horizSpeed > 0.05D) {
                    var v = vel.normalize();
                    var lookAngle = entity.getLookAngle();
                    var f = lookAngle.subtract(0, lookAngle.y, 0);

                    var vHoriz = new Vec3(v.x, 0, v.z).normalize();
                    var fHoriz = new Vec3(f.x, 0, f.z).normalize();

                    var right = fHoriz.cross(new Vec3(0, 1, 0)).normalize();
                    double side = vHoriz.dot(right);
                    double forward = vHoriz.dot(fHoriz);

                    float velocityYaw = (float) (Mth.atan2(-vHoriz.x, vHoriz.z) * (180F / Math.PI));
                    float headingDelta = Float.isNaN(this.lastVelocityYaw) ? 0F : Mth.wrapDegrees(velocityYaw - this.lastVelocityYaw);
                    this.lastVelocityYaw = velocityYaw;
                    targetRoll = Mth.clamp(headingDelta * 6F * (float) Math.min(horizSpeed * 2, 1), -35F, 35F);

                    float maxSpeed = this.flightType.speed.getAsFloat(DataContext.forEntity(entity))
                            * this.flightType.getSpeedMultiplier(entity) * 0.5F;
                    targetForward = Mth.clamp((float) (forward * horizSpeed / maxSpeed), -1F, 1F);
                    targetSideways = Mth.clamp((float) (side * horizSpeed / maxSpeed), -1F, 1F);
                } else {
                    this.lastVelocityYaw = Float.NaN;
                }
            } else {
                this.lastVelocityYaw = Float.NaN;
            }

            this.forwardMotion = Mth.lerp(Math.abs(targetForward) > Math.abs(this.forwardMotion) ? 0.4F : 0.15F, this.forwardMotion, targetForward);
            this.sidewaysMotion = Mth.lerp(Math.abs(targetSideways) > Math.abs(this.sidewaysMotion) ? 0.4F : 0.15F, this.sidewaysMotion, targetSideways);
            this.roll = Mth.lerp(Math.abs(targetRoll) > Math.abs(this.roll) ? 0.4F : 0.08F, this.roll, targetRoll);
        }

        @Override
        public float getCameraRoll(float partialTick) {
            return -this.getRoll(partialTick) * 0.25F;
        }

        @Override
        public float getForwardMotion(float partialTick) {
            return Mth.lerp(partialTick, this.prevForwardMotion, this.forwardMotion);
        }

        @Override
        public float getSidewaysMotion(float partialTick) {
            return Mth.lerp(partialTick, this.prevSidewaysMotion, this.sidewaysMotion);
        }

        @Override
        public float getPropulsionScale(float partialTick) {
            return this.propulsionScale.eased(partialTick);
        }

        @Override
        public float getYawDelta(float partialTick) {
            return Mth.lerp(partialTick, this.prevYawDelta, this.yawDelta);
        }
    }

    public static class Serializer extends FlightTypeSerializer<DefaultFlightType> {

        @Override
        public MapCodec<DefaultFlightType> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<FlightType, DefaultFlightType> builder, HolderLookup.Provider provider) {
            builder.setName("Default Flight")
                    .setDescription("Default flying type. If the sprint speed is equal or less than the normal speed, this flight will be levitation-only.")
                    .add("speed", TYPE_FLOAT, "The normal flight speed.")
                    .addOptional("sprint_speed", TYPE_FLOAT, "The sprinting flight speed. If not set, it will be equal to the normal speed.")
                    .addOptional("screen_shake", SettingType.simple("Screen Shake"), "Screen shake applied while the entity is in propelled flight.")
                    .addOptional("sound", TYPE_IDENTIFIER, "Looping sound played constantly while flying. If propelled_sound is set as well, the two sounds fade between each other depending on whether the entity is in propelled flight.")
                    .addOptional("propelled_sound", TYPE_IDENTIFIER, "Looping sound played while the entity is fast-flying (sprinting). Volume scales with flight speed.")
                    .addOptional("propelled_start_sound", TYPE_IDENTIFIER, "Sound played once when the entity enters propelled flight.")
                    .addExampleObject(new DefaultFlightType(new StaticValue(1F), new StaticValue(2F),  new ScreenShake(
                            new ScreenShake.ShakeIntensity(0.8F, 0.8F, 0.2F),
                            0.35F, 12, 15,
                            new ScreenShake.EasingPair(Easing.OUTSINE, Easing.OUTSINE)
                    ), null, PalladiumSoundEvents.PLAYER_FLIGHT_PROPELLED.getId(), null));

        }
    }
}
