package com.thekrepe.powersunchained.entity.web;

import com.thekrepe.powersunchained.web.WebShotTracker;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Base of the web shots fired by the web-shooters. Ported from TheKrepe's Fisk's Superheroes 26.3 port
 * ({@code SHProjectile} + {@code AbstractEntityWeb}, used with permission), which itself ports the 1.7.10 arrow
 * physics of the original. Differences: loader-independent (the spawn data travels as synched entity data instead
 * of NeoForge's complex spawn), tuned for Powers Unchained.
 * <p>
 * Webs fly very fast with little gravity, are shot alternately from the right and left wrist, stick to blocks
 * (spreading out on the surface) and fade out at the end of their life.
 */
public abstract class AbstractWebProjectile extends Entity {

    private static final EntityDataAccessor<Integer> SHOOTER = SynchedEntityData.defineId(AbstractWebProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> SHOOT_SIDE = SynchedEntityData.defineId(AbstractWebProjectile.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> SHOOT_YAW = SynchedEntityData.defineId(AbstractWebProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SHOOT_PITCH = SynchedEntityData.defineId(AbstractWebProjectile.class, EntityDataSerializers.FLOAT);

    /** Side of the wrist the web was shot from. */
    public static final int RIGHT = 0;
    public static final int LEFT = 1;

    protected @Nullable Entity shooter;
    protected @Nullable BlockPos inPos;
    protected @Nullable BlockState inState;
    protected int ticksInGround;
    protected int ticksInAir;
    public boolean inGround;
    /** The side of the block the web is stuck to (3D data value of the {@link Direction}). */
    public int blockSide;
    private boolean velocityInitialized;
    /** 0 while flying, grows to 1 when the web spreads out on a surface (render only). */
    public float grappleTimer;
    public float prevGrappleTimer;

    protected AbstractWebProjectile(EntityType<? extends AbstractWebProjectile> type, Level level) {
        super(type, level);
    }

    protected AbstractWebProjectile(EntityType<? extends AbstractWebProjectile> type, Level level, LivingEntity shooter, int side) {
        this(type, level);
        this.shooter = shooter;
        this.entityData.set(SHOOTER, shooter.getId() + 1);
        this.entityData.set(SHOOT_SIDE, (byte) side);
        float yaw = shooter.getYRot();
        float pitch = shooter.getXRot();
        this.entityData.set(SHOOT_YAW, yaw);
        this.entityData.set(SHOOT_PITCH, pitch);
        // start at the wrist the web is shot from
        float f = (side == RIGHT ? 1 : -1) * 0.17F;
        double x = shooter.getX() - Mth.cos(yaw * Mth.DEG_TO_RAD) * f;
        double y = shooter.getBoundingBox().minY + shooter.getBbHeight() * 0.8F - 0.1;
        double z = shooter.getZ() - Mth.sin(yaw * Mth.DEG_TO_RAD) * f;
        this.snapTo(x, y, z, yaw, pitch);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SHOOTER, 0);
        builder.define(SHOOT_SIDE, (byte) -1);
        builder.define(SHOOT_YAW, 0.0F);
        builder.define(SHOOT_PITCH, 0.0F);
    }

    public @Nullable Entity getShooter() {
        if (this.shooter == null) {
            int id = this.entityData.get(SHOOTER) - 1;
            this.shooter = id >= 0 ? this.level().getEntity(id) : null;
        }
        return this.shooter;
    }

    public int getShootSide() {
        return this.entityData.get(SHOOT_SIDE);
    }

    // ---- Properties ----

    /** Ticks the web exists in total. */
    public int getLifespan() {
        return 120;
    }

    public float getVelocityFactor() {
        return 7.5F;
    }

    public float getStrayFactor() {
        return 0.25F;
    }

    protected float getGravityVelocity() {
        return 0.02F;
    }

    protected boolean canTargetEntity(Entity entity) {
        return !(entity instanceof Enderman);
    }

    public boolean canImpactEntity(Entity entity) {
        if (entity instanceof Player player) {
            Entity s = this.getShooter();
            return !player.getAbilities().invulnerable && !(s instanceof Player p && !p.canHarmPlayer(player));
        }
        return true;
    }

    protected boolean isStuck() {
        return this.inGround;
    }

    // ---- Shooting ----

    /**
     * Sets the velocity along the shooting direction. The spread is seeded by the entity id, so the server and the
     * clients compute the same trajectory.
     */
    private void initVelocity() {
        if (this.getShootSide() < 0) {
            return;
        }
        Random rand = new Random(this.getId());
        rand.setSeed(rand.nextLong());
        float f = this.getStrayFactor() * 8.0F;
        float yaw = this.entityData.get(SHOOT_YAW) + (0.5F - rand.nextFloat()) * f;
        float pitch = this.entityData.get(SHOOT_PITCH) + (0.5F - rand.nextFloat()) * f;
        double mx = -Mth.sin(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD);
        double mz = Mth.cos(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD);
        double my = -Mth.sin(pitch * Mth.DEG_TO_RAD);
        Vec3 m = new Vec3(mx, my, mz).normalize().scale(this.getVelocityFactor());
        this.setDeltaMovement(m);
        this.setYRot((float) (Math.atan2(m.x, m.z) * 180.0 / Math.PI));
        this.setXRot((float) (Math.atan2(m.y, m.horizontalDistance()) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.ticksInGround = 0;
    }

    // ---- Ticking ----

    @Override
    public void tick() {
        if (!this.velocityInitialized) {
            this.velocityInitialized = true;
            this.initVelocity();
            if (this.level().isClientSide() && this.getShootSide() >= 0) {
                WebShotTracker.onShot(this.entityData.get(SHOOTER) - 1, this.getShootSide());
            }
        }
        super.tick();
        this.checkInGround();
        if (this.inGround) {
            this.updateInGround();
        } else {
            this.updateInAir();
        }
        this.prevGrappleTimer = this.grappleTimer;
        if (this.isStuck() && this.grappleTimer < 1.0F) {
            this.grappleTimer = Math.min(this.grappleTimer + 0.5F, 1.0F);
        }
        if (this.tickCount > this.getLifespan() && !this.level().isClientSide()) {
            this.discard();
        }
    }

    protected void checkInGround() {
        if (this.inPos == null) {
            return;
        }
        BlockState state = this.level().getBlockState(this.inPos);
        if (!state.isAir()) {
            VoxelShape shape = state.getCollisionShape(this.level(), this.inPos);
            if (!shape.isEmpty()) {
                Vec3 pos = this.position();
                for (AABB aabb : shape.toAabbs()) {
                    if (aabb.move(this.inPos).inflate(0.05).contains(pos)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }
    }

    protected void updateInGround() {
        BlockState state = this.inPos != null ? this.level().getBlockState(this.inPos) : null;
        if (state != null && state == this.inState) {
            ++this.ticksInGround;
            this.onTickInGround(state);
        } else {
            // the block is gone: fall
            this.inGround = false;
            this.inPos = null;
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x * this.random.nextFloat() * 0.2F, m.y * this.random.nextFloat() * 0.2F, m.z * this.random.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
        }
    }

    /** Webs stuck in a button press it (like the original's button activators). */
    protected void onTickInGround(BlockState state) {
        if (!this.level().isClientSide() && this.inPos != null && state.getBlock() instanceof ButtonBlock button
                && !state.getValue(ButtonBlock.POWERED)) {
            button.press(state, this.level(), this.inPos, this.getShooter() instanceof Player p ? p : null);
        }
    }

    protected void updateInAir() {
        ++this.ticksInAir;
        HitResult hit = this.checkForImpact(0.3, this.ticksInAir >= 5);
        if (hit != null && hit.getType() != HitResult.Type.MISS) {
            this.onImpact(hit);
        }
        this.updatePosition();
    }

    protected void updatePosition() {
        Vec3 m = this.getDeltaMovement();
        double x = this.getX() + m.x;
        double y = this.getY() + m.y;
        double z = this.getZ() + m.z;
        float yaw = (float) (Math.atan2(m.x, m.z) * 180.0 / Math.PI);
        float pitch = (float) (Math.atan2(m.y, m.horizontalDistance()) * 180.0 / Math.PI);
        while (pitch - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }
        while (pitch - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }
        while (yaw - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }
        while (yaw - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }
        this.setXRot(this.xRotO + (pitch - this.xRotO) * 0.2F);
        this.setYRot(this.yRotO + (yaw - this.yRotO) * 0.2F);

        float factor = 0.99F;
        if (this.isInWater()) {
            if (this.level().isClientSide()) {
                for (int i = 0; i < 4; ++i) {
                    this.level().addParticle(ParticleTypes.BUBBLE, x - m.x * 0.25, y - m.y * 0.25, z - m.z * 0.25, m.x, m.y, m.z);
                }
            }
            factor = 0.8F;
        }
        Vec3 scaled = this.getDeltaMovement().scale(factor);
        this.setDeltaMovement(scaled.x, scaled.y - this.getGravityVelocity(), scaled.z);
        this.setPos(x, y, z);
    }

    /** Raytraces along the current motion for blocks and entities (entities with a slightly inflated hit box). */
    protected @Nullable HitResult checkForImpact(double hitBox, boolean canHitShooter) {
        Vec3 from = this.position();
        Vec3 to = from.add(this.getDeltaMovement());
        HitResult hit = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            to = hit.getLocation();
        }
        Entity shooter = this.getShooter();
        Entity target = null;
        Vec3 hitVec = null;
        double best = 0.0;
        AABB area = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
        for (Entity e : this.level().getEntities(this, area, t -> t.isPickable() && !t.isSpectator())) {
            if (e != shooter || canHitShooter) {
                Optional<Vec3> opt = e.getBoundingBox().inflate(hitBox).clip(from, to);
                if (opt.isPresent()) {
                    double d = from.distanceTo(opt.get());
                    if (d < best || best == 0.0) {
                        target = e;
                        hitVec = opt.get();
                        best = d;
                    }
                }
            }
        }
        if (target != null) {
            return this.canImpactEntity(target) ? new EntityHitResult(target, hitVec) : null;
        }
        return hit;
    }

    protected void onImpact(HitResult hit) {
        if (hit instanceof EntityHitResult ehr) {
            this.onImpactEntity(ehr);
        } else if (hit instanceof BlockHitResult bhr) {
            this.onImpactBlock(bhr);
        }
    }

    protected void onImpactEntity(EntityHitResult hit) {
    }

    protected void onImpactBlock(BlockHitResult hit) {
        this.inPos = hit.getBlockPos();
        this.inState = this.level().getBlockState(this.inPos);
        this.blockSide = hit.getDirection().get3DDataValue();
        Vec3 v = hit.getLocation().subtract(this.position());
        this.setDeltaMovement(v);
        double len = v.length();
        if (len > 0) {
            this.setPos(this.getX() - v.x / len * 0.05, this.getY() - v.y / len * 0.05, this.getZ() - v.z / len * 0.05);
        }
        this.inGround = true;
        this.snapToBlockFace(hit);
    }

    /** Snaps the web flat against the face of the block it hit. */
    protected void snapToBlockFace(BlockHitResult hit) {
        Direction dir = hit.getDirection();
        BlockPos pos = hit.getBlockPos();
        VoxelShape shape = this.level().getBlockState(pos).getCollisionShape(this.level(), pos);
        AABB box = shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();
        Vec3 m = this.getDeltaMovement();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        float w = this.getBbWidth();
        float h = this.getBbHeight();
        if (dir.getStepX() != 0) {
            x = pos.getX() + (dir.getStepX() > 0 ? box.maxX : box.minX) + dir.getStepX() * w / 2.0F;
            m = new Vec3(0, m.y, m.z);
        }
        if (dir.getStepY() != 0) {
            y = pos.getY() + (dir.getStepY() > 0 ? box.maxY : box.minY - h);
            m = new Vec3(m.x, 0, m.z);
        }
        if (dir.getStepZ() != 0) {
            z = pos.getZ() + (dir.getStepZ() > 0 ? box.maxZ : box.minZ) + dir.getStepZ() * w / 2.0F;
            m = new Vec3(m.x, m.y, 0);
        }
        this.setPos(x, y, z);
        this.setDeltaMovement(m);
    }

    // ---- Misc ----

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64.0 * 64.0;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.inPos != null) {
            output.store("inPos", BlockPos.CODEC, this.inPos);
        }
        if (this.inState != null) {
            output.store("inState", BlockState.CODEC, this.inState);
        }
        output.putInt("life", this.ticksInGround);
        output.putBoolean("inGround", this.inGround);
        output.putInt("blockSide", this.blockSide);
        output.putInt("ShootSide", this.getShootSide());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.inPos = input.read("inPos", BlockPos.CODEC).orElse(null);
        this.inState = input.read("inState", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        this.ticksInGround = input.getIntOr("life", 0);
        this.inGround = input.getBooleanOr("inGround", false);
        this.blockSide = input.getIntOr("blockSide", 0);
        this.entityData.set(SHOOT_SIDE, (byte) input.getIntOr("ShootSide", -1));
        this.velocityInitialized = true;
    }
}
