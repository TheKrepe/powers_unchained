package com.thekrepe.powersunchained.entity.web;

import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.web.WebShotTracker;
import com.thekrepe.powersunchained.web.WebShooting;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Web swinging rope, modelled on Fisk's Superheroes {@code EntityWebRope}: shot with the use key while web swinging is
 * on, it flies fast and sticks to the first block it hits. While it holds, the rope keeps the shooter within its
 * length from the anchor (a soft rope: 20% of the overshoot is corrected per tick, so gravity and momentum make a
 * pendulum), and the movement keys steer and pump the swing. The attack key lets go with a boost (speed x1.9 and a
 * lift towards the anchor's height); a new rope replaces the old one.
 * <p>
 * Like every player movement, the swing physics run on the shooter's own client ({@link #applySwing}, called after
 * the player moved); the server only resets the fall distance and keeps the authority over the rope's life.
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
        return SWING_RANGE;
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
            if (!hooked.isAlive() || hooked.distanceTo(player) > SWING_RANGE) {
                this.detach();
            }
            return;
        }
        if (server) {
            player.resetFallDistance();
            WebShooting.markFallSafe(player);
        }
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

    /**
     * Shooter's client, after the player moved: keeps the player on the rope (Fisk's applyMotion with the player's
     * motion as the "proxy") and lets the movement keys pump the swing.
     */
    public void applySwing(Player player) {
        if (!this.holdsShooter() || this.length() <= 0.0F) {
            return;
        }
        Vec3 anchor = this.position();
        Vec3 point = center(player);
        float length = this.length();
        Vec3 v = restrict(anchor, point, length, STIFFNESS);
        if (v == null) {
            return;
        }
        float vel = (float) player.getDeltaMovement().length();
        player.moveRelative(Math.min(vel, 2.0F) / 12.0F, new Vec3(player.xxa, 0.0, player.zza));
        player.move(MoverType.SELF, v);
        player.setDeltaMovement(player.getDeltaMovement().add(v));
        Vec3 v1 = restrict(anchor, point.add(player.getDeltaMovement()), length, STIFFNESS);
        if (v1 != null) {
            player.setDeltaMovement(player.getDeltaMovement().add(v1.subtract(v)));
        }
        Vec3 m = player.getDeltaMovement();
        if (m.lengthSqr() > 9.0) {
            player.setDeltaMovement(m.normalize().scale(3.0));
        }
        player.resetFallDistance();
    }

    /** Shooter's client: the attack key lets go with Fisk's boost (only after the rope held for a moment). */
    public void releaseWithBoost(Player player) {
        if (this.holdsShooter() && this.tickCount > 10 && !player.onGround()) {
            Vec3 m = player.getDeltaMovement();
            double lift = Mth.clamp((this.getY() - player.getY()) * 0.1, -1.0, 1.0);
            Vec3 boosted = new Vec3(m.x * 1.9, m.y * 1.9 + lift, m.z * 1.9);
            double hor = boosted.horizontalDistance();
            if (hor > 2.5) {
                boosted = new Vec3(boosted.x * 2.5 / hor, boosted.y, boosted.z * 2.5 / hor);
            }
            player.setDeltaMovement(boosted.x, Math.min(boosted.y, 1.6), boosted.z);
        }
        this.detach();
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
