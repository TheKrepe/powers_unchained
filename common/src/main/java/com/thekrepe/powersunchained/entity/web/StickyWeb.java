package com.thekrepe.powersunchained.entity.web;

import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.web.WebSlowness;
import com.thekrepe.powersunchained.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
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

    /** Server: when sticky webs recently hit each entity (for the cocoon). */
    private static final Map<LivingEntity, List<Long>> RECENT_HITS = new WeakHashMap<>();

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
    }

    /** The entity the web sticks to, or {@code null}. */
    public @Nullable LivingEntity getHitEntity() {
        int id = this.entityData.get(HIT_ENTITY) - 1;
        return id >= 0 && this.level().getEntity(id) instanceof LivingEntity living ? living : null;
    }

    /**
     * Where on the hit entity the web sits, in the entity's body space at the moment of the hit (forward = +Z, left =
     * +X): x / z = horizontal unit direction from the body's centre, y = height above the feet. The web turns with
     * the body, so it stays on the spot it hit.
     */
    public Vec3 getHitOffset() {
        Vector3fc v = this.entityData.get(HIT_OFFSET);
        return new Vec3(v.x(), v.y(), v.z());
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

    /** Sticky webs stick to every living thing, players too, whatever the PvP setting (they do no damage). */
    @Override
    public boolean canImpactEntity(Entity entity) {
        return true;
    }

    /** Humanoid-shaped (much taller than wide). */
    public static boolean isHumanoid(LivingEntity entity) {
        return entity.getBbHeight() > entity.getBbWidth() * 2.0F;
    }

    /** Whether a web at this height above the feet sits on the head of a humanoid (and turns with the head). */
    public static boolean isOnHead(LivingEntity entity, double height) {
        return isHumanoid(entity) && height > headPivot(entity);
    }

    /** Height of a humanoid's neck (the head's pivot) above its feet. */
    public static float headPivot(LivingEntity entity) {
        return entity.getEyeHeight() - 0.2F * entity.getBbWidth() / 0.6F;
    }

    @Override
    protected void onImpactEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        if (this.level().isClientSide() || this.entityData.get(HIT_ENTITY) > 0 || target == this.getShooter()
                || !(target instanceof LivingEntity living)) {
            return;
        }
        // horizontal direction from the body's centre to the hit point (from where the web came if it hit the top)
        Vec3 rel = hit.getLocation().subtract(target.getX(), target.getBoundingBox().minY, target.getZ());
        Vec3 dir = new Vec3(rel.x, 0.0, rel.z);
        if (dir.lengthSqr() < 0.01) {
            Vec3 m = this.getDeltaMovement();
            dir = new Vec3(-m.x, 0.0, -m.z);
        }
        dir = dir.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : dir.normalize();
        double height = Mth.clamp(rel.y, 0.15, Math.max(0.15, target.getBbHeight() - 0.12));
        // into body space (Vec3.yRot by the yaw turns the forward direction onto +Z); webs on the head turn with it
        float yaw = isOnHead(living, height) ? living.yHeadRot : living.yBodyRot;
        dir = dir.yRot((float) Math.toRadians(yaw));
        this.entityData.set(HIT_OFFSET, new Vector3f((float) dir.x, (float) height, (float) dir.z));
        this.entityData.set(HIT_ENTITY, target.getId() + 1);

        // enough webs on it: they merge into a cocoon that holds it in place
        long now = this.level().getGameTime();
        List<Long> hits = RECENT_HITS.computeIfAbsent(living, e -> new ArrayList<>());
        hits.removeIf(time -> now - time > DISSOLVE_TIME);
        hits.add(now);
        if (Boolean.getBoolean("powers_unchained.ci")) {
            Constants.LOG.info("[CI] sticky web on {} ({} recent hits)", target.getType().getDescriptionId(), hits.size());
        }
        if (hits.size() >= WebCocoon.WEBS_NEEDED) {
            hits.clear();
            WebCocoon.wrap(living);
            this.level().getEntitiesOfClass(StickyWeb.class, target.getBoundingBox().inflate(3.0), web -> web.getHitEntity() == target)
                    .forEach(Entity::discard);
        }
    }
}
