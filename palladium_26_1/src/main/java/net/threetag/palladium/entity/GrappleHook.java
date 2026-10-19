package net.threetag.palladium.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.util.PlayerUtil;
import org.jetbrains.annotations.Nullable;

public class GrappleHook extends ThrowableProjectile {

    public static final byte STATE_FLYING = 0;
    public static final byte STATE_BLOCK = 1;
    public static final byte STATE_ENTITY = 2;

    private static final double ARRIVAL_DISTANCE = 1.5;

    private static final EntityDataAccessor<String> BEAM_RENDERER_ID = SynchedEntityData.defineId(GrappleHook.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> STATE = SynchedEntityData.defineId(GrappleHook.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(GrappleHook.class, EntityDataSerializers.INT);

    private double range;
    private double pullSpeed;
    private boolean attachToEntity;
    private boolean attachToBlock;
    private int pullTicks = 0;
    @Nullable
    private Identifier detachedSound;

    public GrappleHook(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public GrappleHook(LivingEntity owner, Vec3 direction, double range, double flightSpeed, double pullSpeed,
                       boolean attachToEntity, boolean attachToBlock, Identifier beamRendererId, @Nullable Identifier detachedSound) {
        super(PalladiumEntityTypes.GRAPPLE_HOOK.get(), owner.level());
        this.setOwner(owner);
        this.snapTo(owner.getEyePosition());
        this.shoot(direction.x, direction.y, direction.z, (float) flightSpeed, 0F);
        this.entityData.set(BEAM_RENDERER_ID, beamRendererId.toString());
        this.range = range;
        this.pullSpeed = pullSpeed;
        this.attachToEntity = attachToEntity;
        this.attachToBlock = attachToBlock;
        this.detachedSound = detachedSound;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BEAM_RENDERER_ID, "");
        builder.define(STATE, STATE_FLYING);
        builder.define(TARGET_ID, -1);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            if (!(this.getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
                this.endGrapple();
                return;
            }

            switch (this.getState()) {
                case STATE_ENTITY -> this.tickEntityPull(owner);
                case STATE_BLOCK -> this.tickBlockPull(owner);
                default -> {
                    super.tick();
                    if (owner.getEyePosition().distanceTo(this.position()) > this.range) {
                        this.discard();
                    }
                    return;
                }
            }
        }

        super.tick();
    }

    private void tickEntityPull(LivingEntity owner) {
        if (!(this.level().getEntity(this.entityData.get(TARGET_ID)) instanceof LivingEntity target) || !target.isAlive()) {
            this.endGrapple();
            return;
        }

        this.snapTo(target.getBoundingBox().getCenter());
        this.setDeltaMovement(Vec3.ZERO);

        if (this.pullTowards(target, owner.position()) || this.pullTimedOut()) {
            this.endGrapple();
        }
    }

    private void tickBlockPull(LivingEntity owner) {
        this.setDeltaMovement(Vec3.ZERO);

        if (this.pullTowards(owner, this.position()) || this.pullTimedOut()) {
            this.endGrapple();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.getState() != STATE_FLYING) {
            return;
        }

        if (this.attachToEntity && result.getEntity() instanceof LivingEntity target && target != this.getOwner()) {
            this.snapTo(target.getBoundingBox().getCenter());
            this.setDeltaMovement(Vec3.ZERO);

            if (!this.level().isClientSide()) {
                this.entityData.set(TARGET_ID, target.getId());
                this.setState(STATE_ENTITY);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.getState() != STATE_FLYING) {
            return;
        }

        if (this.attachToBlock) {
            this.snapTo(result.getLocation());
            this.setDeltaMovement(Vec3.ZERO);

            if (!this.level().isClientSide()) {
                this.setState(STATE_BLOCK);
            }
        } else if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    private boolean pullTowards(LivingEntity mover, Vec3 target) {
        Vec3 dir = target.subtract(mover.position());
        double dist = dir.length();

        if (dist <= ARRIVAL_DISTANCE) {
            return true;
        }

        mover.setDeltaMovement(dir.scale(this.pullSpeed / dist));
        mover.hurtMarked = true;
        mover.fallDistance = 0F;
        return false;
    }

    private boolean pullTimedOut() {
        return ++this.pullTicks > this.range / Math.max(this.pullSpeed, 1.0E-3) + 40;
    }

    public byte getState() {
        return this.entityData.get(STATE);
    }

    private void setState(byte state) {
        this.entityData.set(STATE, state);
    }

    public boolean isAttached() {
        return this.getState() != STATE_FLYING;
    }

    public void endGrapple() {
        if (this.isAttached() && this.detachedSound != null && !this.level().isClientSide()) {
            PlayerUtil.playSoundToAll(this.level(), this.getX(), this.getEyeY(), this.getZ(), 100, this.detachedSound, this.getSoundSource(), 1F, 1F);
        }

        this.discard();
    }

    @Nullable
    public LivingEntity getOwnerLiving() {
        return this.getOwner() instanceof LivingEntity living ? living : null;
    }

    @Nullable
    public Identifier getBeamRendererId() {
        var id = this.entityData.get(BEAM_RENDERER_ID);
        return id.isEmpty() ? null : Identifier.parse(id);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
    }
}
