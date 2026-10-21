package com.thekrepe.powersunchained.entity.web;

import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.web.WebShotTracker;
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

    public static final float RANGE = 48.0F;
    /** Ticks the line stays (fading) after letting go. */
    public static final int FADE_TICKS = 10;

    private int pullTicks;
    private boolean detachedHere;
    public int ticksDetached;

    public WebZipLine(EntityType<? extends WebZipLine> type, Level level) {
        super(type, level);
    }

    public WebZipLine(Level level, LivingEntity shooter) {
        super(PUEntities.WEB_ZIP_LINE.get(), level, shooter, RIGHT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HOOKED, 0);
        builder.define(DETACHED, false);
    }

    public @Nullable Entity getHooked() {
        int id = this.entityData.get(HOOKED) - 1;
        return id >= 0 ? this.level().getEntity(id) : null;
    }

    public boolean isDetached() {
        return this.detachedHere || this.entityData.get(DETACHED);
    }

    private void detach() {
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
        if (this.isDetached()) {
            if (++this.ticksDetached > FADE_TICKS && server) {
                this.discard();
            }
            return;
        }
        if (!server && this.getShootSide() >= 0) {
            WebShotTracker.hold(player.getId(), this.getShootSide());
        }
        if (!this.isStuck()) {
            if (this.distanceToSqr(player) > RANGE * RANGE) {
                this.detach();
            }
            return;
        }
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
                    target.hurtMarked = true;
                    player.setDeltaMovement(motion.scale(1.0 - knockback / 2.0));
                    player.hurtMarked = true;
                }
            }
            this.detach();
        }
    }
}
