package net.threetag.palladium.entity.flight;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.beam.PalladiumBeams;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.entity.SwingAnchor;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.network.SyncSwingAnchorPacket;
import net.threetag.palladium.network.SyncSwingAnchorStatePacket;
import net.threetag.palladium.util.PalladiumCodecs;
import net.threetag.palladium.util.PlayerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SwingingFlightType extends FlightType {

    private static final Codec<Vec2> RADIUS_CODEC = Codec.either(Codec.FLOAT, Vec2.CODEC).xmap(
            either -> either.map(
                    f -> new Vec2(f, f),
                    vec2 -> vec2
            ),
            vec2 -> vec2.x == vec2.y ? Either.left(vec2.x) : Either.right(vec2)
    );

    public static final BeamRendererValue DEFAULT_BEAM = new BeamRendererValue(PalladiumBeams.SWINGING_WEB_RIGHT, PalladiumBeams.SWINGING_WEB_LEFT);

    public static final MapCodec<SwingingFlightType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_anchor_height").forGetter(f -> f.maxHeight),
            MaxHeightType.CODEC.optionalFieldOf("max_anchor_height_type", MaxHeightType.RELATIVE).forGetter(f -> f.maxHeightType),
            RADIUS_CODEC.fieldOf("radius").forGetter(f -> f.radiusRange),
            PalladiumCodecs.TIME.optionalFieldOf("swing_interval", 10).forGetter(f -> f.swingInterval),
            BeamRendererValue.CODEC.optionalFieldOf("beam_renderer", DEFAULT_BEAM).forGetter(f -> f.beamRendererId),
            Identifier.CODEC.optionalFieldOf("attach_sound").forGetter(f -> f.attachSound),
            Identifier.CODEC.optionalFieldOf("swing_sound").forGetter(f -> f.swingSound)
    ).apply(instance, SwingingFlightType::new));

    final int maxHeight;
    final MaxHeightType maxHeightType;
    final Vec2 radiusRange;
    final int swingInterval;
    public final BeamRendererValue beamRendererId;
    final Optional<Identifier> attachSound;
    final Optional<Identifier> swingSound;

    public SwingingFlightType(int maxHeight, MaxHeightType maxHeightType, Vec2 radiusRange, int swingInterval, BeamRendererValue beamRendererId, Optional<Identifier> attachSound, Optional<Identifier> swingSound) {
        this.maxHeight = maxHeight;
        this.maxHeightType = maxHeightType;
        this.radiusRange = new Vec2(Math.min(radiusRange.x, radiusRange.y), Math.max(radiusRange.x, radiusRange.y));
        this.swingInterval = swingInterval;
        this.beamRendererId = beamRendererId;
        this.attachSound = attachSound;
        this.swingSound = swingSound;
    }

    public float getAvgRadius() {
        return (this.radiusRange.x + this.radiusRange.y) / 2F;
    }

    @Override
    public FlightTypeSerializer<?> getSerializer() {
        return FlightTypeSerializers.SWINGING.get();
    }

    @Override
    public FlightController<?> createController() {
        return new Controller();
    }

    @Override
    public FlightAnimationHandler<?> createAnimationHandler() {
        return new AnimationHandler(this);
    }

    @Override
    @Nullable
    public Identifier getFlightSound() {
        return this.swingSound.orElse(null);
    }

    public enum MaxHeightType implements StringRepresentable {

        ABSOLUTE("absolute"),
        RELATIVE("relative");

        public static final Codec<MaxHeightType> CODEC = StringRepresentable.fromEnum(MaxHeightType::values);

        private final String name;

        MaxHeightType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static class Controller extends FlightController<SwingingFlightType> {

        private static final double LEVEL_ADJUST_SPEED = 0.3;
        private static final double CLIMB_SPEED = 0.25;
        private static final double MAX_DESCEND_SPEED = 0.35;
        private static final double MIN_ROPE_LENGTH = 2;
        private static final double HANGING_SPEED = 0.25;
        private static final double SETTLE_SPEED = 0.35;
        private static final double HANG_DAMPING = 0.94;
        private static final double FORWARD_ACCEL = 0.045;
        private static final double STEER_ACCEL = 0.06;
        private static final double AIR_FORWARD = 0.03;
        private static final double AIR_STEER = 0.03;
        private static final double RELEASE_FORWARD = 0.25;
        private static final double RELEASE_UP = 0.35;
        private static final double EXIT_FORWARD = 0.5;
        private static final double EXIT_UP = 0.6;
        private static final double LAUNCH_FORWARD = 0.35;
        private static final double LAUNCH_UP = 0.85;
        private static final double GROUND_CLEARANCE = 2.5;
        private static final int MIN_SWING_TICKS = 8;
        private static final double ASCEND_THRESHOLD = 0.05;
        private static final double RELEASE_SWING_PROGRESS = 0.55;
        private static final int MAX_SWING_TICKS = 80;
        private static final double SWING_GRAVITY_BOOST = 0.05;
        private static final Vec3 UP = new Vec3(0, 1, 0);
        private static final double DRAG_COMPENSATION_XZ = 1.10;
        private static final double DRAG_COMPENSATION_Y = 1.02;
        private static final double MAX_SWING_SPEED = 2.2;

        private static final int GROUND_GRACE_TICKS = 5;
        private static final int MIN_FREE_TICKS = 6;
        private static final double DIVE_ACCEL = 0.06;
        private static final int LANDING_DURATION = 15;
        private static final int LANDING_CANCEL_TICKS = 4;

        @Nullable
        private Vec3 anchor;
        private Phase phase = Phase.NONE;
        private double ropeLength = -1;
        private double targetRope = -1;
        private double swingLevel = 0;
        private double lastVelY = 0;
        private boolean hanging = false;
        private int attachedTicks = 0;
        private int freeTicks = 0;
        private int lifeTicks = 0;
        private int landingTicks = 0;
        private boolean landingInputReleased = false;
        private int landingMoveTicks = 0;
        public boolean rightArm = true;
        private boolean anchorIsBlock = false;

        @Override
        public void tick(LivingEntity entity, SwingingFlightType flightType) {
            if (entity.level().isClientSide() && entity != Palladium.PROXY.getClientPlayer()) {
                return;
            }

            this.lifeTicks++;

            if (entity.getVehicle() != null) {
                this.stopFlight(entity);
                return;
            }

            boolean controlling = !entity.level().isClientSide()
                    || entity == Palladium.PROXY.getClientPlayer()
                    || !(entity instanceof Player);

            if (this.phase == Phase.DIVING && !entity.onGround() && isApproachingGround(entity)) {
                this.setPhase(entity, Phase.LANDING);
                this.landingTicks = 0;
                this.landingInputReleased = false;
                this.landingMoveTicks = 0;
            }

            if (this.phase == Phase.LANDING) {
                if (entity.onGround()) {
                    Vec3 cur = entity.getDeltaMovement();
                    entity.setDeltaMovement(0, cur.y, 0);

                    if (controlling && entity instanceof Player player) {
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

                    if (++this.landingTicks >= LANDING_DURATION && controlling) {
                        this.stopFlight(entity);
                    }
                } else if (isApproachingGround(entity)) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, -DIVE_ACCEL, 0));
                    entity.fallDistance = 0F;
                } else {
                    this.setPhase(entity, Phase.DIVING);
                }
                return;
            }

            if (controlling && entity.onGround() && this.lifeTicks > GROUND_GRACE_TICKS && entity.getDeltaMovement().y <= 0.05) {
                if (this.phase == Phase.DIVING) {
                    this.setPhase(entity, Phase.LANDING);
                    this.landingTicks = 0;
                    this.landingInputReleased = false;
                    this.landingMoveTicks = 0;
                } else {
                    this.stopFlight(entity);
                }
                return;
            }

            if (!EntityFlightHandler.get(entity).isFlying()) {
                return;
            }

            if (controlling && this.phase != Phase.DIVING && entity.isUsingItem()) {
                this.stopFlight(entity);
                return;
            }

            if (entity != Palladium.PROXY.getClientPlayer() || !(entity instanceof Player player)) {
                return;
            }

            var input = PlayerUtil.getMovementInput(player);
            if (input == null) {
                return;
            }

            switch (this.phase) {
                case SWINGING -> this.tickAttached(player, flightType, input);
                case DIVING -> this.tickDive(player, input);
                default -> this.tickFree(player, flightType, input);
            }
        }

        private void tickDive(Player player, Input input) {
            Vec3 vel = player.getDeltaMovement().add(0, -DIVE_ACCEL, 0);
            Vec3 lookH = horizontal(player.getLookAngle());
            if (input.forward() && lookH.lengthSqr() > 1.0E-4) {
                vel = vel.add(lookH.normalize().scale(AIR_FORWARD * 0.5));
            }
            player.setDeltaMovement(vel);
            player.fallDistance = 0F;
        }

        private void tickAttached(Player player, SwingingFlightType flightType, Input input) {
            Vec3 pos = player.position();
            Vec3 vel = player.getDeltaMovement();
            Vec3 toAnchor = this.anchor.subtract(pos);
            Vec3 ropeDir = toAnchor.normalize();
            boolean pumping = input.forward();
            boolean descending = false;

            if (pumping) {
                this.hanging = false;
            } else if (vel.lengthSqr() < HANGING_SPEED * HANGING_SPEED) {
                this.hanging = true;
            }

            if (this.hanging && (input.jump() || input.shift())) {
                if (input.jump()) {
                    this.ropeLength = Math.max(MIN_ROPE_LENGTH, this.ropeLength - CLIMB_SPEED);
                } else {
                    double groundY = player.level().getHeight(Heightmap.Types.MOTION_BLOCKING,
                            (int) Math.floor(player.getX()), (int) Math.floor(player.getZ()));
                    double maxRope = this.anchor.y - (groundY + GROUND_CLEARANCE);
                    double lengthened = Math.min(this.ropeLength + CLIMB_SPEED, Math.max(this.ropeLength, maxRope));

                    if (lengthened > this.ropeLength) {
                        this.ropeLength = lengthened;
                        descending = true;
                        vel = new Vec3(vel.x, Math.max(vel.y, -MAX_DESCEND_SPEED), vel.z);
                    }
                }

                this.targetRope = this.ropeLength;
                this.swingLevel = player.getY();
            } else {
                this.adjustSwingLevel(input);

                double ropeDiff = Math.min(0, this.targetRope - this.ropeLength);
                this.ropeLength += Math.max(ropeDiff, -SETTLE_SPEED);
            }

            Vec3 lookH = horizontal(player.getLookAngle());
            if (pumping && lookH.lengthSqr() > 1.0E-4) {
                vel = vel.add(lookH.normalize().scale(FORWARD_ACCEL));
            }
            if (input.left() != input.right()) {
                Vec3 side = ropeDir.cross(UP);
                if (side.lengthSqr() > 1.0E-4) {
                    vel = vel.add(side.normalize().scale(input.left() ? -STEER_ACCEL : STEER_ACCEL));
                }
            }

            if (player.horizontalCollision) {
                player.setDeltaMovement(vel);
                this.anchor = null;
                this.phase = Phase.RELEASED;
                this.freeTicks = 0;
                Palladium.PROXY.sendPacketToServer(new SyncSwingAnchorPacket(Optional.empty(), this.rightArm, false));
                return;
            }

            double dist = toAnchor.length();
            if (!descending) {
                this.ropeLength = Math.min(this.ropeLength, dist);
            }

            if (dist >= this.ropeLength) {
                if (pumping && vel.y < 0) {
                    vel = vel.add(0, -SWING_GRAVITY_BOOST, 0);
                }

                double radial = vel.dot(ropeDir);
                if (radial < 0) {
                    vel = vel.subtract(ropeDir.scale(radial));
                }

                Vec3 corrected = this.anchor.subtract(ropeDir.scale(this.ropeLength));
                var boxAt = player.getBoundingBox().move(corrected.subtract(pos));
                if (player.level().noCollision(player, boxAt)) {
                    player.setPos(corrected.x, corrected.y, corrected.z);
                }

                if (pumping) {
                    vel = vel.multiply(DRAG_COMPENSATION_XZ, DRAG_COMPENSATION_Y, DRAG_COMPENSATION_XZ);
                    if (vel.lengthSqr() > MAX_SWING_SPEED * MAX_SWING_SPEED) {
                        vel = vel.normalize().scale(MAX_SWING_SPEED);
                    }
                } else {
                    vel = vel.scale(HANG_DAMPING);
                }
            }

            player.setDeltaMovement(vel);
            player.fallDistance = 0F;
            this.attachedTicks++;

            if (pumping) {
                Vec3 velH = horizontal(vel);
                Vec3 flat = new Vec3(pos.x - this.anchor.x, 0, pos.z - this.anchor.z);
                double fwdOffset = velH.lengthSqr() > 1.0E-4 ? flat.dot(velH.normalize()) : 0;
                double swingProgress = fwdOffset / this.ropeLength;
                boolean apexReached = swingProgress >= RELEASE_SWING_PROGRESS && vel.y > ASCEND_THRESHOLD;
                boolean apexTipped = swingProgress > 0.3 && this.lastVelY > 0 && vel.y <= 0;
                this.lastVelY = vel.y;

                if (this.attachedTicks >= MIN_SWING_TICKS && (apexReached || apexTipped || this.attachedTicks >= MAX_SWING_TICKS)) {
                    this.release(player, velH, RELEASE_FORWARD, RELEASE_UP);
                }
            } else {
                this.lastVelY = vel.y;
                if (!this.hanging && this.attachedTicks >= MAX_SWING_TICKS) {
                    this.release(player, horizontal(vel), RELEASE_FORWARD, RELEASE_UP);
                }
            }
        }

        private void adjustSwingLevel(Input input) {
            if (input.jump()) {
                this.swingLevel += LEVEL_ADJUST_SPEED;
            } else if (input.shift()) {
                this.swingLevel -= LEVEL_ADJUST_SPEED;
            }
        }

        private void tickFree(Player player, SwingingFlightType flightType, Input input) {
            this.freeTicks++;
            this.adjustSwingLevel(input);

            Vec3 vel = player.getDeltaMovement();
            Vec3 lookH = horizontal(player.getLookAngle());
            if (lookH.lengthSqr() > 1.0E-4) {
                lookH = lookH.normalize();
                if (input.forward()) {
                    vel = vel.add(lookH.scale(AIR_FORWARD));
                }
                if (input.left() != input.right()) {
                    vel = vel.add(lookH.cross(UP).normalize().scale(input.left() ? -AIR_STEER : AIR_STEER));
                }
            }
            player.setDeltaMovement(vel);
            player.fallDistance = 0F;

            boolean launchApex = this.phase == Phase.LAUNCHING && vel.y <= 0;
            boolean belowLevel = this.freeTicks >= MIN_FREE_TICKS && player.getY() < this.swingLevel;
            if (launchApex || belowLevel || this.freeTicks >= flightType.swingInterval) {
                Vec3 anchorPos = this.findNewAnchor(player, flightType);
                if (anchorPos != null) {
                    this.attach(player, flightType, anchorPos);
                }
            }
        }

        private void attach(LivingEntity entity, SwingingFlightType flightType, Vec3 pos) {
            if (this.phase == Phase.LAUNCHING) {
                this.swingLevel = entity.getY();
            }

            this.anchor = pos;
            double dist = entity.position().distanceTo(pos);

            double target = Math.min(dist, flightType.radiusRange.y);

            int gx = (int) Math.floor(entity.getX());
            int gz = (int) Math.floor(entity.getZ());
            double groundY = entity.level().getHeight(Heightmap.Types.MOTION_BLOCKING, gx, gz);
            double maxRope = pos.y - (groundY + GROUND_CLEARANCE);
            if (maxRope >= 2) {
                target = Math.min(target, maxRope);
            }

            this.ropeLength = dist;
            this.targetRope = target;
            this.rightArm = !this.rightArm;
            this.attachedTicks = 0;
            this.lastVelY = 0;
            this.hanging = false;
            this.phase = Phase.SWINGING;
            Palladium.PROXY.sendPacketToServer(new SyncSwingAnchorPacket(Optional.of(pos), this.rightArm, this.anchorIsBlock));
        }

        private void release(LivingEntity entity, Vec3 swingDir, double forward, double up) {
            Vec3 dir = horizontal(swingDir.lengthSqr() > 1.0E-4 ? swingDir : entity.getLookAngle());
            if (dir.lengthSqr() > 1.0E-4) {
                dir = dir.normalize();
                entity.setDeltaMovement(entity.getDeltaMovement().add(dir.scale(forward)).add(0, up, 0));
            } else {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, up, 0));
            }
            this.anchor = null;
            this.phase = Phase.RELEASED;
            this.freeTicks = 0;
            this.hanging = false;
            Palladium.PROXY.sendPacketToServer(new SyncSwingAnchorPacket(Optional.empty(), this.rightArm, false));
        }

        @Override
        public void start(LivingEntity entity, SwingingFlightType flightType) {
            this.anchor = null;
            this.phase = Phase.LAUNCHING;
            this.ropeLength = flightType.getAvgRadius();
            this.targetRope = flightType.getAvgRadius();
            this.swingLevel = entity.getY();
            this.attachedTicks = 0;
            this.lifeTicks = 0;
            this.landingTicks = 0;
            this.freeTicks = 0;

            if (entity == Palladium.PROXY.getClientPlayer() && isNearGround(entity)) {
                Vec3 vel = entity.getDeltaMovement();
                Vec3 launched = new Vec3(vel.x, Math.max(vel.y, LAUNCH_UP), vel.z);
                Vec3 dir = horizontal(entity.getLookAngle());
                if (dir.lengthSqr() > 1.0E-4) {
                    launched = launched.add(dir.normalize().scale(LAUNCH_FORWARD));
                }
                entity.setDeltaMovement(launched);
            }
        }

        private static boolean isNearGround(LivingEntity entity) {
            var hit = entity.level().clip(new ClipContext(entity.position(),
                    entity.position().add(0, -3, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, entity));
            return hit.getType() == HitResult.Type.BLOCK;
        }

        @Override
        public boolean interceptStopFlight(LivingEntity entity, SwingingFlightType flightType) {
            if (this.phase == Phase.DIVING || this.phase == Phase.LANDING
                    || entity.onGround() || entity.getVehicle() != null) {
                return false;
            }

            this.enterDive(entity);
            return true;
        }

        private void enterDive(LivingEntity entity) {
            boolean owner = entity == Palladium.PROXY.getClientPlayer();

            if (this.anchor != null) {
                this.anchor = null;
                if (owner) {
                    Palladium.PROXY.sendPacketToServer(new SyncSwingAnchorPacket(Optional.empty(), this.rightArm, false));
                }
            }

            if (owner) {
                Vec3 dir = horizontal(entity.getDeltaMovement());
                if (dir.lengthSqr() < 1.0E-4) {
                    dir = horizontal(entity.getLookAngle());
                }
                Vec3 vel = entity.getDeltaMovement().add(0, EXIT_UP, 0);
                if (dir.lengthSqr() > 1.0E-4) {
                    vel = vel.add(dir.normalize().scale(EXIT_FORWARD));
                }
                entity.setDeltaMovement(vel);
            }

            this.setPhase(entity, Phase.DIVING);
        }

        private void setPhase(LivingEntity entity, Phase phase) {
            this.phase = phase;

            if (!entity.level().isClientSide()) {
                EntityFlightHandler.get(entity).broadcastFlightState();
            }
        }

        @Override
        public void setSyncedPhase(int phaseId) {
            this.phase = Phase.byId(phaseId);
        }

        @Override
        public void stop(LivingEntity entity, SwingingFlightType flightType) {
            if (this.anchor != null && entity == Palladium.PROXY.getClientPlayer()) {
                Palladium.PROXY.sendPacketToServer(new SyncSwingAnchorPacket(Optional.empty(), this.rightArm, false));
            }

            this.anchor = null;
            this.phase = Phase.NONE;
        }

        @Nullable
        private Vec3 findNewAnchor(LivingEntity entity, SwingingFlightType flightType) {
            if (this.phase != Phase.LAUNCHING && entity.getY() > this.swingLevel + 2) {
                return null;
            }

            Vec3 eye = entity.getEyePosition();
            Vec3 fwd = horizontal(entity.getLookAngle());
            if (fwd.lengthSqr() < 1.0E-4) {
                fwd = horizontal(entity.getDeltaMovement());
            }
            if (fwd.lengthSqr() < 1.0E-4) {
                return null;
            }
            fwd = fwd.normalize();

            double r = flightType.getAvgRadius();
            double groundY = entity.level().getHeight(Heightmap.Types.MOTION_BLOCKING,
                    (int) Math.floor(entity.getX()), (int) Math.floor(entity.getZ()));
            double clearance = this.swingLevel - groundY - GROUND_CLEARANCE;
            double minSin = 1 - clearance / r;
            double sin = Mth.clamp(Math.max(0.65, minSin), 0.65, 0.98);
            double cos = Math.sqrt(1 - sin * sin);

            double anchorY = this.swingLevel + entity.getEyeHeight() + r * sin;
            Vec3 ideal = clampMaxHeight(entity.level(), new Vec3(eye.x + fwd.x * r * cos, anchorY, eye.z + fwd.z * r * cos), flightType);
            if (ideal.y <= entity.getY() + 1) {
                return null;
            }

            double minBlockY = entity.getY() + r * 0.5;
            Vec3 right = new Vec3(-fwd.z, 0, fwd.x);
            double[] pitchSins = {0.707, 0.819, 0.906, 0.966}; // 45°, 55°, 65°, 75°
            double[] pitchCoss = {0.707, 0.574, 0.423, 0.259};
            int[] yawSteps = {0, -1, 1, -2, 2};   // 0°, ±20°, ±40°
            Vec3 bestBlock = null;
            double bestDist = Double.MAX_VALUE;
            for (int pi = 0; pi < pitchSins.length; pi++) {
                double ps = pitchSins[pi];
                double pc = pitchCoss[pi];
                for (int ys : yawSteps) {
                    double yawRad = Math.toRadians(ys * 20);
                    Vec3 fwdYaw = fwd.scale(Math.cos(yawRad)).add(right.scale(Math.sin(yawRad)));
                    Vec3 dir = fwdYaw.scale(pc).add(new Vec3(0, ps, 0));
                    Vec3 target = clampMaxHeight(entity.level(), eye.add(dir.scale(r)), flightType);
                    if (target.y <= minBlockY) continue;
                    var clip = new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
                    BlockHitResult hit = entity.level().clip(clip);
                    if (hit.getType() == HitResult.Type.BLOCK && hit.getLocation().y > minBlockY) {
                        Vec3 loc = hit.getLocation();
                        double hDist = Math.sqrt((loc.x - ideal.x) * (loc.x - ideal.x) + (loc.z - ideal.z) * (loc.z - ideal.z));
                        double yDiff = Math.abs(loc.y - ideal.y);
                        double score = hDist + yDiff * 5.0;
                        if (score < bestDist) {
                            bestDist = score;
                            bestBlock = loc;
                        }
                    }
                }
            }
            if (bestBlock != null) {
                this.anchorIsBlock = true;
                return bestBlock;
            }

            this.anchorIsBlock = false;
            return ideal;
        }

        private Vec3 clampMaxHeight(Level level, Vec3 pos, SwingingFlightType flightType) {
            if (flightType.maxHeightType == MaxHeightType.ABSOLUTE) {
                return new Vec3(pos.x, Math.min(pos.y, flightType.maxHeight), pos.z);
            } else {
                int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(pos.x), (int) Math.floor(pos.z));
                return new Vec3(pos.x, Math.min(pos.y, ground + flightType.maxHeight), pos.z);
            }
        }

        public void handleClientAnchor(@Nullable Vec3 pos, boolean rightArm) {
            this.anchor = pos;
            this.rightArm = rightArm;
        }

        public void handleServerAnchor(LivingEntity entity, @Nullable Vec3 pos, boolean rightArm, boolean blockAnchor, SwingingFlightType flightType) {
            for (SwingAnchor old : entity.level().getEntitiesOfClass(SwingAnchor.class, entity.getBoundingBox().inflate(256),
                    a -> a.getOwner() == entity && !a.isMarkedToDespawn())) {
                old.markToDespawn();
            }

            this.anchor = pos;
            this.rightArm = rightArm;

            if (pos != null) {
                this.setPhase(entity, Phase.SWINGING);
            } else if (this.phase != Phase.DIVING && this.phase != Phase.LANDING) {
                this.setPhase(entity, Phase.RELEASED);
            }

            if (pos != null && !entity.level().isClientSide()) {
                SwingAnchor anchorEntity = new SwingAnchor(entity, pos, flightType.beamRendererId.get(rightArm));
                anchorEntity.setBlockAnchor(blockAnchor);
                entity.level().addFreshEntity(anchorEntity);
                flightType.attachSound.ifPresent(sound ->
                        PlayerUtil.playSoundToAll(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), 64, sound, entity.getSoundSource()));
            }

            if (!entity.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntity(entity,
                        new SyncSwingAnchorStatePacket(entity.getId(), Optional.ofNullable(pos), rightArm));
            }
        }

        @Nullable
        public Vec3 getAnchor() {
            return this.anchor;
        }

        public Phase getPhase() {
            return this.phase;
        }

        @Override
        public int getPhaseId() {
            return this.phase.getId();
        }

        public boolean isHanging() {
            return this.hanging;
        }

        public boolean isDiving() {
            return this.phase == Phase.DIVING;
        }

        public boolean isLanding() {
            return this.phase == Phase.LANDING;
        }

        @Override
        public boolean isFlightSoundActive(LivingEntity entity, SwingingFlightType flightType) {
            return (this.phase == Phase.SWINGING && this.anchor != null) || this.phase == Phase.DIVING;
        }

        private static Vec3 horizontal(Vec3 v) {
            return new Vec3(v.x, 0, v.z);
        }
    }

    public enum Phase {

        NONE(0),
        LAUNCHING(1),
        SWINGING(2),
        RELEASED(3),
        DIVING(4),
        LANDING(5);

        private final int id;

        Phase(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static Phase byId(int id) {
            for (Phase phase : values()) {
                if (phase.id == id) {
                    return phase;
                }
            }
            return NONE;
        }
    }

    public record BeamRendererValue(Identifier rightArm, Identifier leftArm) {

        public static final Codec<BeamRendererValue> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("right_arm").forGetter(BeamRendererValue::rightArm),
                Identifier.CODEC.fieldOf("left_arm").forGetter(BeamRendererValue::leftArm)
        ).apply(instance, BeamRendererValue::new));

        public static final Codec<BeamRendererValue> CODEC = Codec.either(DIRECT_CODEC, Identifier.CODEC)
                .xmap(either -> either.map(
                                beamRendererValue -> beamRendererValue,
                                Identifier -> new BeamRendererValue(Identifier, Identifier)),
                        beamRendererValue ->
                                beamRendererValue.rightArm.equals(beamRendererValue.leftArm) ?
                                        Either.right(beamRendererValue.rightArm) : Either.left(beamRendererValue));

        public Identifier get(boolean rightArm) {
            return rightArm ? this.rightArm : this.leftArm;
        }

    }

    public static class AnimationHandler extends FlightAnimationHandler<SwingingFlightType> {

        private static final Value DEFAULT_LIMB_STIFFNESS = new StaticValue(22F);
        private static final float ALIGN_SPEED = 0.5F;
        private static final float LIMB_DAMPING_RATIO = 0.85F;
        private static final float ATTACH_ARM_KICK = 500F;

        private final SwingingFlightType flightType;
        public final Value limbStiffness;
        private float momentum = 0F;
        public float limbPitchVel, prevLimbPitchVel, limbRollVel;
        public float rightArmPitch, prevRightArmPitch, rightArmVel;
        public float leftArmPitch, prevLeftArmPitch, leftArmVel;
        private boolean hadAnchor = false;

        public AnimationHandler(SwingingFlightType flightType) {
            this.flightType = flightType;
            this.limbStiffness = DEFAULT_LIMB_STIFFNESS;
        }

        @Override
        public float getCameraRoll(float partialTick) {
            return this.getRoll(partialTick) * 0.2F;
        }

        @Override
        public void tick(LivingEntity entity, boolean flightStopped) {
            super.tick(entity, flightStopped);

            var context = DataContext.forEntity(entity);
            var swingController = EntityFlightHandler.get(entity).getController() instanceof SwingingFlightType.Controller c ? c : null;
            Vec3 anchorPos = swingController != null ? swingController.getAnchor() : null;

            if (anchorPos != null) {
                Vec3 rope = anchorPos.subtract(entity.position());

                float targetPitch = 0F;
                float targetRoll = 0F;

                if (rope.lengthSqr() > 1.0E-4) {
                    rope = rope.normalize();
                    Vec3 fwd = Vec3.directionFromRotation(0, entity.yBodyRot);
                    Vec3 right = fwd.cross(new Vec3(0, 1, 0));

                    double fwdComp = rope.dot(fwd);
                    double sideComp = rope.dot(right);
                    double upComp = Math.max(rope.y, 0.05);

                    targetPitch = (float) Math.toDegrees(Math.atan2(fwdComp, upComp));
                    targetRoll = (float) Math.toDegrees(Math.atan2(sideComp, upComp));
                }

                double speed = getVelocity(entity).length();
                this.momentum = Mth.lerp(0.3F, this.momentum, Mth.clamp(((float) speed - 0.2F) / 0.5F, 0F, 1F));
                targetPitch *= this.momentum;
                targetRoll *= this.momentum;

                this.pitch = Mth.lerp(ALIGN_SPEED, this.pitch, targetPitch);
                this.roll = Mth.lerp(ALIGN_SPEED, this.roll, targetRoll);
            } else {
                Vec3 vel = getVelocity(entity);
                float targetPitch = 0F;
                double speed = vel.length();
                if (speed > 0.01) {
                    Vec3 fwd = Vec3.directionFromRotation(0, entity.yBodyRot);
                    double fwdComp = vel.normalize().dot(fwd);
                    float momentumScale = Mth.clamp(((float) speed - 0.1F) / 0.3F, 0F, 1F);
                    targetPitch = (float) (fwdComp * 25.0 * momentumScale);
                }
                boolean diving = swingController != null && swingController.isDiving();
                this.pitch = Mth.lerp(diving ? 0.12F : 0.25F, this.pitch, targetPitch);
                this.roll *= 0.85F;
                this.momentum = 0F;
            }

            boolean rightArm = anchorPos != null && swingController.rightArm;
            boolean leftArm = anchorPos != null && !swingController.rightArm;

            if (anchorPos != null && !this.hadAnchor) {
                if (rightArm) {
                    this.rightArmVel -= ATTACH_ARM_KICK;
                } else {
                    this.leftArmVel -= ATTACH_ARM_KICK;
                }
            }
            this.hadAnchor = anchorPos != null;

            this.updateLimbs(this.limbStiffness.getAsFloat(context), this.pitch, this.roll, rightArm, leftArm);
        }

        private void updateLimbs(float stiffness, float targetPitch, float targetRoll, boolean rightArm, boolean leftArm) {
            float deltaTime = 1F / 20F;
            stiffness = stiffness * 2F;
            float damping = 2F * LIMB_DAMPING_RATIO * (float) Math.sqrt(stiffness);

            this.prevLimbPitchVel = this.limbPitchVel;
            float acc = stiffness * (targetPitch - this.limbPitch) - damping * this.limbPitchVel;
            this.limbPitchVel += acc * deltaTime;
            this.limbPitch += this.limbPitchVel * deltaTime;

            acc = stiffness * (targetRoll - this.limbRoll) - damping * this.limbRollVel;
            this.limbRollVel += acc * deltaTime;
            this.limbRoll += this.limbRollVel * deltaTime;

            this.prevRightArmPitch = this.rightArmPitch;
            this.prevLeftArmPitch = this.leftArmPitch;
            var rightArmTarget = rightArm ? -175 : targetPitch;
            var leftArmTarget = leftArm ? -175 : targetPitch;

            acc = stiffness * (rightArmTarget - this.rightArmPitch) - damping * this.rightArmVel;
            this.rightArmVel += acc * deltaTime;
            this.rightArmPitch += this.rightArmVel * deltaTime;

            acc = stiffness * (leftArmTarget - this.leftArmPitch) - damping * this.leftArmVel;
            this.leftArmVel += acc * deltaTime;
            this.leftArmPitch += this.leftArmVel * deltaTime;
        }

        public float getLimbPitchVel(float partialTick) {
            return Mth.lerp(partialTick, this.prevLimbPitchVel, this.limbPitchVel);
        }

        public float getRightArmPitch(float partialTick) {
            return Mth.lerp(partialTick, this.prevRightArmPitch, this.rightArmPitch);
        }

        public float getLeftArmPitch(float partialTick) {
            return Mth.lerp(partialTick, this.prevLeftArmPitch, this.leftArmPitch);
        }
    }

    public static class Serializer extends FlightTypeSerializer<SwingingFlightType> {

        @Override
        public MapCodec<SwingingFlightType> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<FlightType, SwingingFlightType> builder, HolderLookup.Provider provider) {
            builder.setName("Swinging")
                    .setDescription("Allows the entity to swing from anchor points, similar to a grappling hook.")
                    .add("max_anchor_height", TYPE_INT, "The maximum height for anchor points.")
                    .addOptional("max_anchor_height_type", TYPE_SWINGING_HEIGHT_TYPE, "Determines if the maximum height for anchors are relative to the ground, or a fixed y-level.", MaxHeightType.RELATIVE)
                    .add("radius", SettingType.combined(TYPE_FLOAT, TYPE_VECTOR2), "The web length range: how far ahead anchors are placed and the maximum rope length. A single number or an array of 2 numbers (min/max). While flying, holding jump raises and holding shift lowers the height that new anchors are placed at.")
                    .addOptional("swing_interval", TYPE_TIME, "The airborne gap (in ticks) between releasing one web and firing the next. The first web after starting the flight always fires at the apex of the launch boost.", 10)
                    .addOptional("beam_renderer", TYPE_SWINGING_BEAM_RENDERER, "The beam renderer(s) used for the swinging rope. Can be a single identifier or separate right/left arm identifiers.")
                    .addOptional("attach_sound", TYPE_IDENTIFIER, "Sound played for nearby players each time a new anchor attaches.")
                    .addOptional("swing_sound", TYPE_IDENTIFIER, "Looping sound played while the entity is swinging on an anchor. Volume scales with movement speed.")
                    .addExampleObject(new SwingingFlightType(
                            20,
                            MaxHeightType.RELATIVE,
                            new Vec2(5F, 10F),
                            10,
                            new BeamRendererValue(
                                    Identifier.fromNamespaceAndPath("namespace", "beam_right_id"),
                                    Identifier.fromNamespaceAndPath("namespace", "beam_left_id")
                            ),
                            Optional.empty(),
                            Optional.empty()
                    ));
        }
    }
}
