package com.thekrepe.powersunchained.entity;

import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.registry.PUItems;
import com.thekrepe.powersunchained.web.WebType;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A shot from the web-shooters. Impact web hurts what it hits; sticky web sticks to living targets (slows them and
 * covers them in web for a few seconds) without damage. The web type comes from the entity type.
 */
public class WebProjectile extends ThrowableItemProjectile {

    /** How long sticky web stays on a target, in ticks. */
    public static final int STICKY_DURATION = 100;

    public WebProjectile(EntityType<? extends WebProjectile> type, Level level) {
        super(type, level);
    }

    public WebProjectile(EntityType<? extends WebProjectile> type, Level level, LivingEntity owner, ItemStack stack) {
        super(type, owner, level, stack);
    }

    public WebType getWebType() {
        return this.getType() == PUEntities.STICKY_WEB.get() ? WebType.STICKY : WebType.IMPACT;
    }

    @Override
    protected Item getDefaultItem() {
        return this.getWebType() == WebType.STICKY ? PUItems.STICKY_WEB.get() : PUItems.IMPACT_WEB.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ItemStack item = this.getItem();
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(item)),
                        this.getX(), this.getY(), this.getZ(),
                        (this.random.nextDouble() - 0.5) * 0.15, this.random.nextDouble() * 0.1, (this.random.nextDouble() - 0.5) * 0.15);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity target = hitResult.getEntity();
        WebType type = this.getWebType();
        if (type == WebType.IMPACT) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), type.damage());
        } else if (target instanceof LivingEntity living && !this.level().isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, STICKY_DURATION, 3, false, false, true), this.getOwner());
            Services.GAMEPLAY.setWebbedUntil(living, this.level().getGameTime() + STICKY_DURATION);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
}
