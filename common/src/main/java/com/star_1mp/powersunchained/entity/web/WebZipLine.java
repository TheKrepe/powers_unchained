package com.star_1mp.powersunchained.entity.web;

import com.star_1mp.powersunchained.registry.PUEntities;
import com.star_1mp.powersunchained.web.WebShotTracker;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Web Zip (like Fisk's Superheroes {@code EntityWebRope} as a pull web): a fast web line that yanks the shooter
 * towards the block it hits (up and forward, 5 ticks), or towards the entity it hits (10 ticks), ramming it for
 * damage based on the speed. The pull is applied where the player is simulated (on the shooter's own client), the
 * server handles the hit. Once done, the line lets go and fades away.
 */
public class WebZipLine extends AbstractWebProjectile {

    private static final EntityDataAccessor<Integer> HOOKED = SynchedEntityData.defineId(WebZipLine.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DETACHED = SynchedEntityData.defineId(WebZipLine.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(WebZipLine.class, EntityDataSerializers.FLOAT);

    public static final float RANGE = 48.0F;
    /** Client: told every tick which entity has a web zip line out, and whether it pulls yet (zip animations). */
    public static ZipListener zipListener = (id, pulling) -> {
    };

    /** See {@link #zipListener}. */
    @FunctionalInterface
    public interface ZipListener {
        void zip(int entityId, boolean pulling);
    }
    /** Ticks the rope stays (falling slack, fading) after letting go. */
    public static final int FADE_TICKS = 60;

    private int pullTicks;
    private boolean detachedHere;
    public int ticksDetached;
    /** 1 = tight rope, less = slack (it sags), like Fisk's rope (render only). */
    public float tension = 1.0F;
    public float prevTension = 1.0F;
    /** After letting go: the loose end of the rope, falling where the shooter was (render only). */
    public Vec3 end = Vec3.ZERO;
    public Vec3 prevEnd = Vec3.ZERO;
    private Vec3 endMotion = Vec3.ZERO;

    public WebZipLine(EntityType<? extends WebZipLine> type, Level level) {
        super(type, level);
    }

    public WebZipLine(Level level, LivingEntity shooter) {
        super(PUEntities.WEB_ZIP_LINE.get(), level, shooter, RIGHT);
    }

    protected WebZipLine(EntityType<? extends WebZipLine> type, Level level, LivingEntity shooter, int side) {
        super(type, level, shooter, side);
    }

    /** How far the line flies before it gives up. */
    protected float range() {
        return RANGE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HOOKED, 0);
        builder.define(DETACHED, false);
        builder.define(LENGTH, -1.0F);
    }

    public @Nullable Entity getHooked() {
        int id = this.entityData.get(HOOKED) - 1;
        return id >= 0 ? this.level().getEntity(id) : null;
    }

    /** Rope length when it caught on something (-1 before). */
    public float getLength() {
        return this.entityData.get(LENGTH);
    }

    public boolean isDetached() {
        return this.detachedHere || this.entityData.get(DETACHED);
    }

    protected void detach() {
        this.detachedHere = true;
        if (!this.level().isClientSide()) {
            this.entityData.set(DETACHED, true);
        }
    }

    @Override
    public float getVelocityFactor() {
        return 12.0F;
    }

    @Override
    public float getStrayFactor() {
        return 0.0F;
    }

    @Override
    protected float getGravityVelocity() {
        return 0.01F;
    }

    @Override
    public int getLifespan() {
        return 100;
    }

    @Override
    protected boolean isStuck() {
        return this.inGround || this.entityData.get(HOOKED) > 0;
    }

    @Override
    public boolean canImpactEntity(Entity entity) {
        return entity instanceof LivingEntity && entity != this.getShooter();
    }

    @Override
    protected void onImpactEntity(EntityHitResult hit) {
        if (!this.level().isClientSide() && this.entityData.get(HOOKED) == 0 && !this.isDetached()
                && hit.getEntity() instanceof LivingEntity target && target != this.getShooter()) {
            this.entityData.set(HOOKED, target.getId() + 1);
        }
    }

    @Override
    protected void updatePosition() {
        if (this.entityData.get(HOOKED) > 0) {
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            super.updatePosition();
        }
    }

    @Override
    public void tick() {
        super.tick();
        boolean server = !this.level().isClientSide();
        Entity shooter = this.getShooter();
        if (!(shooter instanceof Player player) || !shooter.isAlive()) {
            if (server) {
                this.discard();
            }
            return;
        }
        Entity hooked = this.getHooked();
        if (hooked != null) {
            this.setPos(hooked.getX(), hooked.getY() + hooked.getBbHeight() / 2.0F - this.getBbHeight() / 2.0F, hooked.getZ());
        }
        this.updateRope(player, server);
        if (this.isDetached()) {
            if (++this.ticksDetached > FADE_TICKS && server) {
                this.discard();
            }
            return;
        }
        if (!server) {
            this.clientTick(player);
        }
        if (!this.isStuck()) {
            if (this.distanceToSqr(player) > this.range() * this.range()) {
                this.detach();
            }
            return;
        }
        this.whileCaught(player, hooked, server);
    }

    /** Client, every tick while the line is out (not let go). */
    protected void clientTick(Player player) {
        if (this.getShootSide() >= 0 && !this.isStuck()) {
            WebShotTracker.hold(player.getId(), this.getShootSide());  // arm aimed while the line flies
        }
        zipListener.zip(player.getId(), this.isStuck());
    }

    /** Every tick once the line caught on a block or an entity (and was not let go): the zip pull. */
    protected void whileCaught(Player player, @Nullable Entity hooked, boolean server) {
        boolean simulated = !server && player.isLocalPlayer();  // players move on their own client
        if (hooked != null) {
            this.pullToEntity(player, hooked, simulated, server);
        } else if (this.inGround) {
            if (++this.pullTicks <= 5) {
                Vec3 v = this.position().subtract(player.position()).normalize().scale(0.4);
                if (simulated) {
                    player.setDeltaMovement(player.getDeltaMovement().add(v.x, v.y + 0.15, v.z));
                }
                player.resetFallDistance();
            } else {
                this.detach();
            }
        }
    }

    /** Fisk's rope: length when it catches, tension (slack when the shooter gets closer), loose end after letting go. */
    private void updateRope(Player player, boolean server) {
        Vec3 shooterCenter = player.position().add(0.0, player.getBbHeight() / 2.0F, 0.0);
        if (server && this.getLength() < 0.0F && this.isStuck()) {
            this.entityData.set(LENGTH, (float) this.position().distanceTo(shooterCenter));
        }
        if (this.isDetached()) {
            if (this.ticksDetached == 0) {
                this.end = this.prevEnd = player.position().add(0.0, player.getBbHeight() * 0.75F, 0.0);
                this.endMotion = this.isStuck() ? Vec3.ZERO : player.getDeltaMovement();
            }
            this.prevEnd = this.end;
            this.endMotion = this.endMotion.scale(0.7).add(0.0, this.isStuck() ? -0.02 : 0.0, 0.0);
            this.end = this.end.add(this.endMotion);
            Vec3 m = this.getDeltaMovement();
            if (!this.isStuck()) {
                this.setDeltaMovement(m.x * 0.7, m.y * 0.7 - 0.01, m.z * 0.7);
            }
        }
        double length = this.position().distanceTo(this.isDetached() ? this.end : shooterCenter);
        this.prevTension = this.tension;
        float goal = this.isStuck() && this.getLength() > 0.0F ? (float) Math.min((length + 1.0) / this.getLength(), 1.0) : 1.0F;
        this.tension -= (this.tension - goal) / 4.0F;
    }

    private void pullToEntity(Player player, Entity target, boolean simulated, boolean server) {
        if (++this.pullTicks > 10 || !target.isAlive()) {
            this.detach();
            return;
        }
        Vec3 from = player.getBoundingBox().getCenter();
        Vec3 v = target.getBoundingBox().getCenter().subtract(from).normalize().scale(0.5);
        if (simulated) {
            player.setDeltaMovement(player.getDeltaMovement().add(v));
        }
        player.resetFallDistance();
        if (server && player.getBoundingBox().inflate(0.4).intersects(target.getBoundingBox())) {
            // rammed it: damage from the speed (the client moves the player, the server sees the position change)
            Vec3 motion = player.position().subtract(player.xo, player.yo, player.zo);
            float damage = (float) motion.length() * 3.0F - 1.0F;
            if (damage > 0.0F) {
                damage += 3.0F;
                ServerLevel level = (ServerLevel) this.level();
                if (target.hurtServer(level, player.damageSources().playerAttack(player), damage)) {
                    double knockback = 0.75;
                    target.setDeltaMovement(target.getDeltaMovement().add(motion.x * knockback, motion.y * knockback + 0.1, motion.z * knockback));
                    target.syncVelocity = true;
                    player.setDeltaMovement(motion.scale(1.0 - knockback / 2.0));
                    player.syncVelocity = true;
                }
            }
            this.detach();
        }
    }
}
