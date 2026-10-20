package com.thekrepe.powersunchained.entity.web;

import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.web.WebSlowness;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Sticky web: sticks to blocks or to the living entity it hits and stays on it (visible to everyone, the target
 * included) until it dissolves. A web stuck around the legs slows the entity down; more webs, slower.
 */
public class StickyWeb extends AbstractWebProjectile {

    private static final EntityDataAccessor<Integer> HIT_ENTITY = SynchedEntityData.defineId(StickyWeb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3fc> HIT_OFFSET = SynchedEntityData.defineId(StickyWeb.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> YAW_OFFSET = SynchedEntityData.defineId(StickyWeb.class, EntityDataSerializers.FLOAT);

    /** Ticks until the web dissolves. */
    public static final int DISSOLVE_TIME = 120;

    public StickyWeb(EntityType<? extends StickyWeb> type, Level level) {
        super(type, level);
    }

    public StickyWeb(Level level, LivingEntity shooter, int side) {
        super(PUEntities.STICKY_WEB.get(), level, shooter, side);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HIT_ENTITY, 0);
        builder.define(HIT_OFFSET, new Vector3f());
        builder.define(YAW_OFFSET, 0.0F);
    }

    /** The entity the web sticks to, or {@code null}. */
    public @Nullable LivingEntity getHitEntity() {
        int id = this.entityData.get(HIT_ENTITY) - 1;
        return id >= 0 && this.level().getEntity(id) instanceof LivingEntity living ? living : null;
    }

    /** Where on the hit entity the web sits: horizontal direction (rotated by {@link #getYawOffset()}) and height. */
    public Vec3 getHitOffset() {
        Vector3fc v = this.entityData.get(HIT_OFFSET);
        return new Vec3(v.x(), v.y(), v.z());
    }

    public float getYawOffset() {
        return this.entityData.get(YAW_OFFSET);
    }

    @Override
    protected boolean isStuck() {
        return this.inGround || this.entityData.get(HIT_ENTITY) > 0;
    }

    @Override
    public int getLifespan() {
        return DISSOLVE_TIME;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.entityData.get(HIT_ENTITY) > 0) {
            LivingEntity target = this.getHitEntity();
            if (target != null && target.isAlive()) {
                this.setPos(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0F, target.getZ());
                if (!this.level().isClientSide() && this.getHitOffset().y < 0.5) {
                    WebSlowness.add(target);
                }
            } else if (!this.level().isClientSide()) {
                this.discard();
            }
        }
    }

    @Override
    protected void updatePosition() {
        if (this.entityData.get(HIT_ENTITY) > 0) {
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            super.updatePosition();
        }
    }

    @Override
    public boolean canImpactEntity(Entity entity) {
        return true;
    }

    @Override
    protected void onImpactEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        if (this.level().isClientSide() || this.entityData.get(HIT_ENTITY) > 0 || target == this.getShooter()
                || !(target instanceof LivingEntity living)) {
            return;
        }
        float yawOffset = living.yBodyRot;
        // hit point relative to the target's feet (1.7.10's Vec3.subtract was "other - this"), in body space
        Vec3 v = hit.getLocation().subtract(target.getX(), target.getBoundingBox().minY, target.getZ())
                .yRot((float) Math.toRadians(yawOffset));
        double height = v.y;
        v = new Vec3(v.x, 0, v.z).normalize();
        Vec3 offset = new Vec3(v.x, Math.max(Math.min(height, target.getBbHeight() - 0.4F), 0.0), v.z);
        this.entityData.set(YAW_OFFSET, yawOffset);
        this.entityData.set(HIT_OFFSET, offset.toVector3f());
        this.entityData.set(HIT_ENTITY, target.getId() + 1);
    }
}
