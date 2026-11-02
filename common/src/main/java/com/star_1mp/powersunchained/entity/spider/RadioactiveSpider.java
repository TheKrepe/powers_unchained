package com.star_1mp.powersunchained.entity.spider;

import com.star_1mp.powersunchained.irradiation.RadioactiveBite;
import com.star_1mp.powersunchained.power.Superpower;
import com.star_1mp.powersunchained.power.Superpowers;
import com.star_1mp.powersunchained.registry.PUDamageTypes;
import com.star_1mp.powersunchained.registry.PUItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Radioactive Spider (owner's design): a cave spider turned by the Irradiator. Cave spider model, size, health, damage
 * and sounds (the vanilla spider sounds), its own dark-blue texture with slowly glowing red patterns; NO poison.
 * <ul>
 *   <li>Hunts the nearest player within {@value #TARGET_RANGE} blocks who has no Spider-Man power yet (re-picks every
 *   second, so a player who comes closer "steals" the powers); climbs walls like every spider; leaves players who
 *   already have the power, mobs, mannequins and armour stands alone.</li>
 *   <li>Its bite ({@link RadioactiveBite}) hurts like a spider's, starts the 30 s fever, then the spider dies at once
 *   (cave spider loot, green sparks).</li>
 *   <li>Without anyone to bite for 5 minutes it dies of the radiation. Killed by a player before it bit anyone: also a
 *   Radioactive Spider Eye.</li>
 * </ul>
 */
public class RadioactiveSpider extends Spider {

    public static final int TARGET_RANGE = 12;
    /** 5 minutes without a target -> dies of the radiation. */
    public static final int LONELY_TICKS = 6000;
    /** Green sparks (and the death burst). */
    public static final DustParticleOptions SPARK = new DustParticleOptions(0x58F05A, 0.9F);

    private final TargetingConditions biteable = TargetingConditions.forCombat().range(TARGET_RANGE)
            .selector((target, level) -> canBite(target));
    private int lonelyTicks;
    private boolean bit;

    public RadioactiveSpider(EntityType<? extends RadioactiveSpider> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return CaveSpider.createCaveSpider().add(Attributes.FOLLOW_RANGE, TARGET_RANGE);
    }

    /** Only players without the Spider-Man power. */
    public static boolean canBite(LivingEntity target) {
        return target instanceof Player player && !player.isSpectator() && !Superpowers.data(player).has(Superpower.SPIDERMAN);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        // a plain melee goal: unlike vanilla spiders it does not give up in daylight
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 5, false, false,
                (target, level) -> canBite(target)));
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            if (this.isAlive() && this.random.nextFloat() < 0.06F) {
                level.addParticle(SPARK, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.02, 0.0);
            }
            return;
        }
        if (!(level instanceof ServerLevel server) || !this.isAlive()) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && !canBite(target)) {
            this.setTarget(null);  // got the powers some other way meanwhile
            target = null;
        }
        // owner: in multiplayer someone closer can "steal" the bite
        if (this.tickCount % 20 == 0) {
            Player nearest = server.getNearestPlayer(this.biteable, this, this.getX(), this.getEyeY(), this.getZ());
            if (nearest != null && nearest != target) {
                this.setTarget(nearest);
                target = nearest;
            }
        }
        this.lonelyTicks = target == null ? this.lonelyTicks + 1 : 0;
        if (this.lonelyTicks >= LONELY_TICKS) {
            this.dieOfRadiation(server);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        // a spider's hit (Mob), without the cave spider's poison
        boolean hit = super.doHurtTarget(level, target);
        if (hit && !this.bit && target instanceof ServerPlayer player
                && canBite(player)) {
            this.bit = true;
            RadioactiveBite.bite(player, this);
            this.dieOfRadiation(level);
        }
        return hit;
    }

    /** Dies for real (death animation, cave spider loot) in a burst of green sparks. */
    public void dieOfRadiation(ServerLevel level) {
        level.sendParticles(SPARK, this.getX(), this.getY() + 0.3, this.getZ(), 30, 0.35, 0.25, 0.35, 0.02);
        this.hurtServer(level, PUDamageTypes.radiation(level), Float.MAX_VALUE);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (killedByPlayer && !this.bit) {
            this.spawnAtLocation(level, new ItemStack(PUItems.RADIOACTIVE_SPIDER_EYE.get()));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }
}
