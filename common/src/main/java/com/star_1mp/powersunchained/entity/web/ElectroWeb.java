package com.star_1mp.powersunchained.entity.web;

import com.star_1mp.powersunchained.registry.PUDamageTypes;
import com.star_1mp.powersunchained.registry.PUEntities;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Electro-Web (web-shooter module): a sticky web charged with electricity.
 * <ul>
 *   <li>sticks to what it hits like a sticky web (and slows it the same way);</li>
 *   <li>the hit shocks: {@link #HIT_DAMAGE} at once, then {@link #PULSE_DAMAGE} every {@link #PULSE_TICKS} ticks for
 *   {@link #PULSES} pulses while it sits on the target;</li>
 *   <li>stuns: strong slowness + weakness for {@link #STUN_TICKS} ticks, mobs lose their path;</li>
 *   <li>arcs to up to {@link #ARCS} other living things within {@link #ARC_RANGE} blocks ({@link #ARC_DAMAGE} each); the
 *   range doubles when the target stands in water or in the rain;</li>
 *   <li>sparks while flying and on blocks; uses 2 portions of web fluid, 6 shots, recharge 60 ticks.</li>
 * </ul>
 */
public class ElectroWeb extends StickyWeb {

    public static final float HIT_DAMAGE = 3.0F;
    public static final float PULSE_DAMAGE = 1.0F;
    public static final int PULSE_TICKS = 10;
    public static final int PULSES = 4;
    public static final int STUN_TICKS = 40;
    public static final int ARCS = 2;
    public static final double ARC_RANGE = 4.0;
    public static final float ARC_DAMAGE = 2.0F;

    private int hitTick = -1;

    public ElectroWeb(EntityType<? extends ElectroWeb> type, Level level) {
        super(type, level);
    }

    public ElectroWeb(Level level, LivingEntity shooter, int side) {
        super(PUEntities.ELECTRO_WEB.get(), level, shooter, side);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            if (this.random.nextInt(this.isStuck() ? 4 : 1) == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.4, this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                        0.0, 0.0, 0.0);
            }
            return;
        }
        LivingEntity target = this.getHitEntity();
        if (this.hitTick >= 0 && target != null && target.isAlive()) {
            int since = this.tickCount - this.hitTick;
            if (since > 0 && since % PULSE_TICKS == 0 && since <= PULSE_TICKS * PULSES) {
                this.shock(target, PULSE_DAMAGE);
            }
        }
    }

    @Override
    protected void onImpactEntity(EntityHitResult hit) {
        boolean wasFree = this.getHitEntity() == null;
        super.onImpactEntity(hit);
        if (this.level().isClientSide() || !wasFree || !(hit.getEntity() instanceof LivingEntity target) || target == this.getShooter()) {
            return;
        }
        this.hitTick = this.tickCount;
        this.shock(target, HIT_DAMAGE);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, STUN_TICKS, 3, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, STUN_TICKS, 1, false, true));
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        this.arc(target);
    }

    @Override
    protected void onImpactBlock(BlockHitResult hit) {
        super.onImpactBlock(hit);
        if (this.level() instanceof ServerLevel level) {
            Vec3 p = hit.getLocation();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 12, 0.2, 0.2, 0.2, 0.15);
            level.playSound(null, p.x, p.y, p.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.25F, 2.0F);
        }
    }

    private void shock(LivingEntity target, float damage) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        target.setInvulnerableTime(0);
        target.hurtServer(level, PUDamageTypes.web(this, this.getShooter()), damage);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                10, target.getBbWidth() * 0.4, target.getBbHeight() * 0.35, target.getBbWidth() * 0.4, 0.1);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS,
                0.3F, 1.8F + this.random.nextFloat() * 0.3F);
    }

    /** Lightning arcs from the target to the nearest other living things. */
    private void arc(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        boolean wet = target.isInWaterOrRain();
        double range = wet ? ARC_RANGE * 2.0 : ARC_RANGE;
        Entity shooter = this.getShooter();
        List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(range),
                e -> e != target && e != shooter && e.isAlive() && !e.isSpectator() && this.canImpactEntity(e)
                        && e.distanceTo(target) <= range);
        near.sort(Comparator.comparingDouble(e -> e.distanceToSqr(target)));
        Vec3 from = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        for (int i = 0; i < Math.min(ARCS, near.size()); i++) {
            LivingEntity e = near.get(i);
            Vec3 to = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0);
            // a jagged line of sparks
            int steps = Math.max(4, (int) (from.distanceTo(to) * 4));
            for (int s = 0; s <= steps; s++) {
                Vec3 p = from.lerp(to, s / (double) steps);
                double j = s == 0 || s == steps ? 0.0 : 0.15;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x + (this.random.nextDouble() - 0.5) * j,
                        p.y + (this.random.nextDouble() - 0.5) * j, p.z + (this.random.nextDouble() - 0.5) * j, 1, 0, 0, 0, 0);
            }
            e.setInvulnerableTime(0);
            e.hurtServer(level, PUDamageTypes.web(this, shooter), ARC_DAMAGE);
            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, STUN_TICKS / 2, 1, false, true));
        }
    }
}
