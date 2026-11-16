package com.star_1mp.powersunleashed.entity.web;

import com.star_1mp.powersunleashed.registry.PUEntities;
import com.star_1mp.powersunleashed.web.WebShotTracker;
import com.star_1mp.powersunleashed.web.WebShooting;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Web swinging rope (Fisk's Superheroes {@code EntityWebRope} idea, swing feel of Powerborne Heroes / Marvel Legacy):
 * while web swinging is on, holding the use key shoots a rope (retried while it misses); it flies fast and sticks to
 * the first block it hits, and the player swings on it as long as the key is held. Letting go of the key (or the
 * attack key) releases it with a launch; a new rope replaces the old one.
 * <p>
 * Like every player movement, the swing physics run on the shooter's own client ({@link #applySwing}, called before
 * the player moves); the server only resets the fall distance and keeps the authority over the rope's life.
 * When the rope catches a mob instead, the mob is held on the rope, and letting go yanks it to the shooter.
 * <p>
 * {@link #getKind() Kind}: 0 = hangs with both hands (rope from the right wrist), 1 = right hand, 2 = left hand —
 * picked like Fisk's (one-handed more often when the head is turned away from the body).
 */
public class WebSwingLine extends WebZipLine {

    private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(WebSwingLine.class, EntityDataSerializers.BYTE);

    public static final float SWING_RANGE = 64.0F;
    public static final int BOTH_HANDS = 0;
    public static final int RIGHT_HAND = 1;
    public static final int LEFT_HAND = 2;
    /** Rope stiffness (part of the overshoot pulled back each tick). */
    private static final double STIFFNESS = 0.2;

    /** Newest rope of every shooter, per side (client / server). */
    private static final Map<Integer, WebSwingLine> CLIENT = new ConcurrentHashMap<>();
    private static final Map<Integer, WebSwingLine> SERVER = new ConcurrentHashMap<>();

    /** Client: told every tick which player has a swing rope out (animations, sounds). */
    public static SwingListener swingListener = (id, kind, anchor, attached) -> {
    };

    @FunctionalInterface
    public interface SwingListener {
        void swing(int entityId, int kind, Vec3 anchor, boolean attached);
    }

    /** Length measured by this side when the rope caught (the synced one arrives a bit later on the client). */
    private float localLength = -1.0F;
    public int groundTicks;

    public WebSwingLine(EntityType<? extends WebSwingLine> type, Level level) {
        super(type, level);
    }

    public WebSwingLine(Level level, LivingEntity shooter, int kind) {
        super(PUEntities.WEB_SWING_LINE.get(), level, shooter, kind == LEFT_HAND ? LEFT : RIGHT);
        this.entityData.set(KIND, (byte) kind);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIND, (byte) 0);
    }

    public int getKind() {
        return this.entityData.get(KIND);
    }

    /** The shooter's rope that currently holds (caught on a block or a mob, not let go), on this side. */
    public static @Nullable WebSwingLine of(Player player) {
        WebSwingLine line = (player.level().isClientSide() ? CLIENT : SERVER).get(player.getId());
        if (line == null || line.isRemoved() || line.isDetached() || line.getShooter() != player) {
            return null;
        }
        return line;
    }

    @Override
    protected float range() {
        return SWING_RANGE * this.speedMultiplier();
    }

    @Override
    public int getLifespan() {
        return 6000;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved() || this.isDetached() || !(this.getShooter() instanceof Player player)) {
            return;
        }
        Map<Integer, WebSwingLine> map = this.level().isClientSide() ? CLIENT : SERVER;
        WebSwingLine current = map.get(player.getId());
        // the newest rope wins (an older one may still tick until its "let go" arrives)
        if (current == null || current == this || current.isRemoved() || current.isDetached() || current.tickCount >= this.tickCount) {
            map.put(player.getId(), this);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (this.getShooter() instanceof Player player) {
            (this.level().isClientSide() ? CLIENT : SERVER).remove(player.getId(), this);
        }
    }

    @Override
    protected void clientTick(Player player) {
        int kind = this.getKind();
        boolean caught = this.isStuck();
        if (!caught) {
            WebShotTracker.hold(player.getId(), this.getShootSide());
        } else if (kind == BOTH_HANDS) {
            WebShotTracker.hold(player.getId(), RIGHT);
            WebShotTracker.hold(player.getId(), LEFT);
        } else {
            WebShotTracker.hold(player.getId(), kind == LEFT_HAND ? LEFT : RIGHT);
        }
        swingListener.swing(player.getId(), kind, this.position(), caught && this.getHooked() == null);
    }

    @Override
    protected void whileCaught(Player player, @Nullable Entity hooked, boolean server) {
        if (this.localLength < 0.0F) {
            this.localLength = (float) this.position().distanceTo(center(player));
        }
        if (hooked != null) {
            if (server && !(hooked instanceof Player)) {
                // the mob is held on the rope
                Vec3 v = restrict(center(player), center(hooked), this.length(), STIFFNESS);
                if (v != null) {
                    hooked.move(MoverType.SELF, v);
                    hooked.setDeltaMovement(hooked.getDeltaMovement().add(v));
                    hooked.resetFallDistance();
                }
            }
            if (!hooked.isAlive() || hooked.distanceTo(player) > SWING_RANGE * com.star_1mp.powersunleashed.tech.WebModules.range(player)) {
                this.detach();
            }
            return;
        }
        if (server) {
            player.resetFallDistance();
            WebShooting.markFallSafe(player);
        }
    }

    /**
     * Whether a rope shot now could catch anything: a block (or a mob) within reach along the look (like Powerborne's
     * check before firing), so no web fluid is wasted shooting into the sky.
     */
    public static boolean canReachAnything(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(SWING_RANGE * com.star_1mp.powersunleashed.tech.WebModules.range(player)));
        HitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.MISS && eye.distanceTo(hit.getLocation()) >= 1.5) {
            return true;
        }
        AABB box = new AABB(eye, end).inflate(1.0);
        for (Entity e : player.level().getEntities(player, box, t -> t instanceof LivingEntity && t.isPickable() && !t.isSpectator())) {
            if (e.getBoundingBox().inflate(0.3).clip(eye, end).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** Shooter's client: a new rope length (rappelling with the mouse wheel). */
    public void setLocalLength(float length) {
        this.localLength = length;
    }

    /** Rope length: measured when it caught. */
    public float length() {
        return this.localLength > 0.0F ? this.localLength : this.getLength();
    }

    private static Vec3 center(Entity e) {
        return new Vec3(e.getX(), e.getBoundingBox().minY + e.getBbHeight() / 2.0F, e.getZ());
    }

    /** Fisk's restrictMotion: the move pulling {@code point} back within {@code length} of {@code anchor}, or null. */
    private static @Nullable Vec3 restrict(Vec3 anchor, Vec3 point, double length, double stiffness) {
        Vec3 d = anchor.subtract(point);
        double dist = Math.max(length, 1.0);
        double l = d.length();
        return l > dist ? d.scale((l - dist) / l * stiffness) : null;
    }

    /** Whether the rope caught on a block or a mob. */
    public boolean isCaught() {
        return this.isStuck();
    }

    /** Whether the rope holds the shooter (caught on a block). */
    public boolean holdsShooter() {
        return this.inGround && this.getHooked() == null && !this.isDetached();
    }

    // ---- swing physics (shooter's client, before the player moves) ----

    /** Rope pull back per block of overshoot (the rope is a little elastic). */
    private static final double CONSTRAINT = 0.15;
    /** Vanilla air drag is strong; while swinging most of it is given back so the swing keeps its momentum. */
    private static final double DRAG_COMP = 1.05;
    private static final double PUMP = 0.06;
    private static final double STRAFE_PUMP = 0.03;
    private static final double SPRINT_PUMP = 1.5;
    private static final double TANGENTIAL_PUMP = 0.004;
    private static final double REEL_SPEED = 0.38;
    private static final double AUTO_REEL_SPEED = 0.06;
    private static final double MIN_LENGTH = 1.5;
    private static final double SOFT_CAP = 2.4;
    private static final double HARD_CAP = 3.4;
    /** Overshoot that snaps the rope (pulled through a wall, teleported). */
    private static final double SNAP = 10.0;
    private int airTicks;

    /**
     * Rope physics on the shooter's own client, before the player moves (like Powerborne's / Marvel Legacy's swing,
     * with Fisk's rope): the next position is predicted from the motion; if it would leave the rope's reach, the
     * motion away from the anchor is removed and the overshoot pulled back, so the speed along the swing stays and
     * gravity turns it into a pendulum. W pumps the swing towards where you look (stronger sprinting), A/D steer,
     * jump reels the rope in (also lifts you off the ground), sprinting forward reels it in slowly.
     *
     * @return {@code false} if the rope snapped
     */
    public boolean applySwing(Player player, boolean reel) {
        if (!this.holdsShooter() || this.length() <= 0.0F) {
            return true;
        }
        Vec3 anchor = this.position();
        Vec3 pos = center(player);
        Vec3 v = player.getDeltaMovement();
        boolean onGround = player.onGround();
        this.airTicks = onGround ? 0 : this.airTicks + 1;
        double length = this.length();
        if ((onGround || this.airTicks > 4) && length > MIN_LENGTH) {
            double amount = (reel ? REEL_SPEED : 0.0) + (player.isSprinting() && player.zza > 0.0F ? AUTO_REEL_SPEED : 0.0);
            if (amount > 0.0) {
                double shorter = Math.max(MIN_LENGTH, length - amount);
                v = v.scale(Math.min(1.03, length / shorter));
                length = shorter;
                this.localLength = (float) length;
            }
        }
        // pumping / steering with the movement keys
        float forward = Math.signum(player.zza);
        float strafe = -Math.signum(player.xxa);
        if (!onGround && (forward != 0.0F || strafe != 0.0F)) {
            Vec3 look = player.getLookAngle();
            double lh = Math.sqrt(look.x * look.x + look.z * look.z);
            if (lh > 1.0E-6) {
                double fx = look.x / lh;
                double fz = look.z / lh;
                double mx = fx * forward + -fz * strafe;
                double mz = fz * forward + fx * strafe;
                double ml = Math.sqrt(mx * mx + mz * mz);
                double strength = (forward != 0.0F ? PUMP : STRAFE_PUMP) * (player.isSprinting() ? SPRINT_PUMP : 1.0);
                v = v.add(mx / ml * strength, 0.0, mz / ml * strength);
            }
            if (forward > 0.0F) {
                Vec3 out = pos.subtract(anchor);
                if (out.lengthSqr() > 1.0E-6) {
                    Vec3 ur = out.normalize();
                    Vec3 t = v.subtract(ur.scale(v.dot(ur)));
                    if (t.lengthSqr() > 1.0E-6) {
                        v = v.add(t.normalize().scale(TANGENTIAL_PUMP));
                    }
                }
            }
        }
        if (!onGround) {
            v = new Vec3(v.x * DRAG_COMP, v.y * 1.015, v.z * DRAG_COMP);
        }
        // the rope: no moving away from the anchor beyond its length
        Vec3 next = pos.add(v);
        Vec3 r = next.subtract(anchor);
        double d = r.length();
        if (d > length && d > 1.0E-6) {
            Vec3 ur = r.scale(1.0 / d);
            double out = v.dot(ur);
            if (out > 0.0) {
                v = v.subtract(ur.scale(out));
            }
            double excess = d - length;
            if (excess > SNAP) {
                return false;
            }
            v = v.subtract(ur.scale(excess * CONSTRAINT));
        }
        double speed = v.length();
        if (speed > SOFT_CAP) {
            v = v.scale(1.0 - Math.min(0.1 * (speed - SOFT_CAP), 0.12));
        }
        speed = v.length();
        if (speed > HARD_CAP) {
            v = v.scale(HARD_CAP / speed);
        }
        player.setDeltaMovement(v);
        player.resetFallDistance();
        return true;
    }

    /**
     * Shooter's client: lets go with a launch (Fisk's / Powerborne's): a push towards where you look and a lift
     * towards the anchor's height, if moving.
     *
     * @return whether it launched (moving in the air)
     */
    public boolean releaseWithBoost(Player player) {
        boolean launched = false;
        if (this.holdsShooter() && !player.onGround()) {
            Vec3 m = player.getDeltaMovement();
            if (m.length() > 0.2) {
                Vec3 look = player.getLookAngle();
                double lh = Math.sqrt(look.x * look.x + look.z * look.z);
                double x = m.x;
                double z = m.z;
                if (lh > 1.0E-6) {
                    x += look.x / lh * 0.5;
                    z += look.z / lh * 0.5;
                }
                double y = m.y + Mth.clamp((this.getY() - player.getY()) * 0.1, -1.0, 1.0);
                Vec3 v = new Vec3(x, y, z);
                if (v.length() > HARD_CAP) {
                    v = v.scale(HARD_CAP / v.length());
                }
                player.setDeltaMovement(v);
                launched = true;
            }
        }
        this.detach();
        return launched;
    }

    /** Lets go without a boost (server, or the client when web swinging is turned off / after landing). */
    public void letGo() {
        this.detach();
    }

    /** Server: the shooter let go while the rope held a mob: yank the mob towards the shooter (Fisk's punch). */
    public void yankHooked(Player player) {
        Entity hooked = this.getHooked();
        if (hooked != null && this.tickCount > 10) {
            Vec3 d = player.position().subtract(hooked.position());
            double l = d.length();
            if (l > 1.0E-4) {
                hooked.setDeltaMovement(hooked.getDeltaMovement().add(d.x / l * 2.0, d.y / l * 1.5 + 0.2, d.z / l * 2.0));
                hooked.syncVelocity = true;
            }
        }
        this.detach();
    }
}
