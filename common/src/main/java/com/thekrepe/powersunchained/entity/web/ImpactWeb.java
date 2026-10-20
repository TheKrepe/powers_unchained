package com.thekrepe.powersunchained.entity.web;

import com.thekrepe.powersunchained.registry.PUDamageTypes;
import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.registry.PUSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Impact web: a hard web ball. Damage scales with its speed (full damage while flying fast), ignores the target's
 * hurt cooldown so rapid shots all land, and bounces off targets it cannot hurt. Sticks to blocks.
 */
public class ImpactWeb extends AbstractWebProjectile {

    public static final float DAMAGE = 5.0F;

    public ImpactWeb(EntityType<? extends ImpactWeb> type, Level level) {
        super(type, level);
    }

    public ImpactWeb(Level level, LivingEntity shooter, int side) {
        super(PUEntities.IMPACT_WEB.get(), level, shooter, side);
    }

    /** 0..1: how fast the web still is compared to its launch speed. */
    protected float speedFactor() {
        return Math.min((float) this.getDeltaMovement().length() / this.getVelocityFactor() / 0.96F, 1.0F);
    }

    @Override
    protected void onImpactEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        float damage = DAMAGE * this.speedFactor();
        if (damage <= 0.0F) {
            return;
        }
        if (target instanceof LivingEntity) {
            target.setInvulnerableTime(0);  // webs do not wait for the hurt cooldown
        }
        if (target.hurtServer(level, PUDamageTypes.web(this, this.getShooter()), damage)) {
            if (this.canTargetEntity(target)) {
                this.discard();
            }
        } else if (target.isAlive()) {
            this.bounceOff();
        }
    }

    protected void bounceOff() {
        Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.scale(-0.1));
        this.setYRot(this.getYRot() + 180.0F);
        this.yRotO += 180.0F;
        this.ticksInAir = 0;
        this.playSound(PUSounds.WEB_BOUNCE.get(), 1.0F, 0.75F + this.random.nextFloat() * 0.5F);
    }
}
