package com.star_1mp.powersunleashed.entity.web;

import com.star_1mp.powersunleashed.registry.PUDamageTypes;
import com.star_1mp.powersunleashed.registry.PUEntities;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
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
 * Electro-Web (web-shooter module), owner's spec: an IMPACT web charged with electricity — it does not stick.
 * <ul>
 *   <li>the hit shocks for {@link #HIT_DAMAGE} (1.5 hearts) at once;</li>
 *   <li>stun for {@link #STUN_TICKS} ticks (3 s): strong slowness + weakness, mobs lose their path (navigation is
 *   stopped every tick of the stun, see {@link #tickStuns()});</li>
 *   <li>chain lightning to up to {@link #ARCS} other living things within {@link #ARC_RANGE} blocks ({@link #ARC_DAMAGE}
 *   each, shorter stun); the range doubles when the target is wet;</li>
 *   <li>sparks in flight and on hit; 5 shots, recharge 60 ticks, 2 portions of web fluid per shot (see WebType).</li>
 * </ul>
 */
public class ElectroWeb extends ImpactWeb {

    public static final float HIT_DAMAGE = 3.0F;
    public static final int STUN_TICKS = 60;
    public static final int ARCS = 2;
    public static final double ARC_RANGE = 4.0;
    public static final float ARC_DAMAGE = 2.0F;

    /** Stunned mobs -> game time the stun ends (server only). */
    private static final Map<Mob, Long> STUNNED = new WeakHashMap<>();

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
        if (level.isClientSide() && this.random.nextInt(this.ticksInAir > 0 ? 1 : 4) == 0) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.4, this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onImpactEntity(EntityHitResult hit) {
        if (!(this.level() instanceof ServerLevel level) || !(hit.getEntity() instanceof LivingEntity target)
                || target == this.getShooter()) {
            super.onImpactEntity(hit);
            return;
        }
        target.setInvulnerableTime(0);
        if (!target.hurtServer(level, PUDamageTypes.web(this, this.getShooter()), HIT_DAMAGE) && target.isAlive()) {
            this.bounceOff();
            return;
        }
        this.sparks(level, target, 18);
        stun(level, target, STUN_TICKS, 4);
        this.arc(level, target);
        this.discard();
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

    private void sparks(ServerLevel level, LivingEntity target, int count) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                count, target.getBbWidth() * 0.4, target.getBbHeight() * 0.35, target.getBbWidth() * 0.4, 0.1);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS,
                0.3F, 1.8F + this.random.nextFloat() * 0.3F);
    }

    /** Slowness (amplifier {@code slow}) + weakness; a mob also forgets where it was going for the whole stun. */
    private static void stun(ServerLevel level, LivingEntity target, int ticks, int slow) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, slow, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 1, false, true));
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
            STUNNED.merge(mob, level.getGameTime() + ticks, Math::max);
        }
    }

    /** Server tick: stunned mobs keep losing their path. */
    public static void tickStuns() {
        Iterator<Map.Entry<Mob, Long>> it = STUNNED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Mob, Long> e = it.next();
            Mob mob = e.getKey();
            if (mob == null || !mob.isAlive() || mob.level().getGameTime() >= e.getValue()) {
                it.remove();
            } else {
                mob.getNavigation().stop();
            }
        }
    }

    /** Chain lightning from the target to the nearest other living things. */
    private void arc(ServerLevel level, LivingEntity target) {
        double range = target.isInWaterOrRain() ? ARC_RANGE * 2.0 : ARC_RANGE;
        Entity shooter = this.getShooter();
        List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(range),
                e -> e != target && e != shooter && e.isAlive() && !e.isSpectator() && this.canImpactEntity(e)
                        && e.distanceTo(target) <= range);
        near.sort(Comparator.comparingDouble(e -> e.distanceToSqr(target)));
        Vec3 from = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        for (int i = 0; i < Math.min(ARCS, near.size()); i++) {
            LivingEntity e = near.get(i);
            Vec3 to = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0);
            int steps = Math.max(4, (int) (from.distanceTo(to) * 4));
            for (int s = 0; s <= steps; s++) {
                Vec3 p = from.lerp(to, s / (double) steps);
                double j = s == 0 || s == steps ? 0.0 : 0.15;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x + (this.random.nextDouble() - 0.5) * j,
                        p.y + (this.random.nextDouble() - 0.5) * j, p.z + (this.random.nextDouble() - 0.5) * j, 1, 0, 0, 0, 0);
            }
            e.setInvulnerableTime(0);
            e.hurtServer(level, PUDamageTypes.web(this, shooter), ARC_DAMAGE);
            stun(level, e, STUN_TICKS / 2, 2);
        }
    }
}
