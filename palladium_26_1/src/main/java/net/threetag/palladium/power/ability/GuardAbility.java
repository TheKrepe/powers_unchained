package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.List;
import java.util.Optional;

public class GuardAbility extends Ability {

    public static final Identifier DEFAULT_BLOCK_SOUND = SoundEvents.SHIELD_BLOCK.key().identifier();
    public static final Identifier DEFAULT_BREAK_SOUND = SoundEvents.SHIELD_BREAK.key().identifier();

    public static final MapCodec<GuardAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("guard_threshold").forGetter(a -> a.guardThreshold),
                    Codec.FLOAT.fieldOf("break_damage").forGetter(a -> a.breakDamage),
                    PalladiumCodecs.TIME.optionalFieldOf("break_lockout", 0).forGetter(a -> a.breakLockout),
                    Codec.FLOAT.optionalFieldOf("block_angle", 180F).forGetter(a -> a.blockAngle),
                    RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("bypassed_by").forGetter(a -> a.bypassedBy),
                    Codec.BOOL.optionalFieldOf("disable_attacking", true).forGetter(a -> a.disableAttacking),
                    Codec.FLOAT.optionalFieldOf("knockback_strength", 0.5F).forGetter(a -> a.knockbackStrength),
                    Identifier.CODEC.optionalFieldOf("block_sound", DEFAULT_BLOCK_SOUND).forGetter(a -> a.blockSound),
                    Identifier.CODEC.optionalFieldOf("break_sound", DEFAULT_BREAK_SOUND).forGetter(a -> a.breakSound),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, GuardAbility::new));

    private final float guardThreshold;
    private final float breakDamage;
    private final int breakLockout;
    private final float blockAngle;
    private final Optional<HolderSet<DamageType>> bypassedBy;
    private final boolean disableAttacking;
    private final float knockbackStrength;
    private final Identifier blockSound;
    private final Identifier breakSound;

    public GuardAbility(float guardThreshold, float breakDamage, int breakLockout, float blockAngle, Optional<HolderSet<DamageType>> bypassedBy, boolean disableAttacking, float knockbackStrength, Identifier blockSound, Identifier breakSound, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.guardThreshold = guardThreshold;
        this.breakDamage = breakDamage;
        this.breakLockout = breakLockout;
        this.blockAngle = blockAngle;
        this.bypassedBy = bypassedBy;
        this.disableAttacking = disableAttacking;
        this.knockbackStrength = knockbackStrength;
        this.blockSound = blockSound;
        this.breakSound = breakSound;
    }

    public static boolean isLockedFromBreak(AbilityInstance<?> instance) {
        return instance.getOrDefault(PalladiumDataComponents.Abilities.GUARD_BREAK_LOCKOUT.get(), 0) > 0;
    }

    public static boolean isBlockingActions(LivingEntity entity) {
        for (AbilityInstance<GuardAbility> instance : AbilityUtil.getEnabledInstances(entity, AbilitySerializers.GUARD.get())) {
            if (instance.getAbility().disableAttacking) {
                return true;
            }
        }

        return false;
    }

    public static boolean canBlock(AbilityInstance<GuardAbility> instance, LivingEntity entity, DamageSource source) {
        GuardAbility ability = instance.getAbility();

        if (ability.isBypassedBy(source)) {
            return false;
        }

        float blockAngle = ability.blockAngle;

        if (blockAngle >= 360F) {
            return true;
        }

        Vec3 sourcePos = source.getSourcePosition();

        if (sourcePos == null) {
            return false;
        }

        Vec3 toSource = new Vec3(sourcePos.x - entity.getX(), 0, sourcePos.z - entity.getZ());

        if (toSource.lengthSqr() < 1.0E-4) {
            return true;
        }

        toSource = toSource.normalize();

        Vec3 look = entity.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();

        double angleBetween = Math.toDegrees(Math.acos(Mth.clamp(flatLook.dot(toSource), -1.0, 1.0)));
        return angleBetween <= blockAngle / 2.0;
    }

    private boolean isBypassedBy(DamageSource source) {
        return this.bypassedBy.map(types -> types.contains(source.typeHolder())).orElseGet(() -> source.is(DamageTypeTags.BYPASSES_SHIELD));
    }

    public static boolean absorb(AbilityInstance<GuardAbility> instance, float amount) {
        GuardAbility ability = instance.getAbility();
        float absorbed = instance.getOrDefault(PalladiumDataComponents.Abilities.GUARD_DAMAGE_ABSORBED.get(), 0F) + amount;

        if (absorbed >= ability.guardThreshold) {
            instance.set(PalladiumDataComponents.Abilities.GUARD_DAMAGE_ABSORBED.get(), 0F);
            instance.setSilently(PalladiumDataComponents.Abilities.GUARD_JUST_BROKE.get(), true);

            if (ability.breakLockout > 0) {
                instance.set(PalladiumDataComponents.Abilities.GUARD_BREAK_LOCKOUT.get(), ability.breakLockout);
            }

            return true;
        }

        instance.set(PalladiumDataComponents.Abilities.GUARD_DAMAGE_ABSORBED.get(), absorbed);
        return false;
    }

    public void playBlockSound(LivingEntity entity) {
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvent.createVariableRangeEvent(this.blockSound), entity.getSoundSource(), 1.0F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
    }

    public void knockbackAttacker(LivingEntity defender, LivingEntity attacker) {
        if (this.knockbackStrength <= 0F || attacker == defender) {
            return;
        }

        attacker.knockback(this.knockbackStrength, defender.getX() - attacker.getX(), defender.getZ() - attacker.getZ());
        attacker.hurtMarked = true;
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder components) {
        super.registerDataComponents(components);
        components.set(PalladiumDataComponents.Abilities.GUARD_DAMAGE_ABSORBED.get(), 0F);
        components.set(PalladiumDataComponents.Abilities.GUARD_JUST_BROKE.get(), false);
        components.set(PalladiumDataComponents.Abilities.GUARD_BREAK_LOCKOUT.get(), 0);
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (!entity.level().isClientSide()) {
            if (abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.GUARD_JUST_BROKE.get(), false)) {
                abilityInstance.setSilently(PalladiumDataComponents.Abilities.GUARD_JUST_BROKE.get(), false);
                this.playGuardBreakEffects(entity);
            }

            int lockout = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.GUARD_BREAK_LOCKOUT.get(), 0);
            if (lockout > 0) {
                abilityInstance.setSilently(PalladiumDataComponents.Abilities.GUARD_BREAK_LOCKOUT.get(), lockout - 1);
            }
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (!entity.level().isClientSide()) {
            abilityInstance.setSilently(PalladiumDataComponents.Abilities.GUARD_DAMAGE_ABSORBED.get(), 0F);
        }
    }

    private void playGuardBreakEffects(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (this.breakDamage > 0F && entity.isAlive()) {
                entity.hurtServer(serverLevel, entity.damageSources().generic(), this.breakDamage);
            }

            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, entity.getX(), entity.getY(entity.getBbHeight() * 0.5), entity.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.CRIT, entity.getX(), entity.getY(entity.getBbHeight() * 0.5), entity.getZ(), 20, entity.getBbWidth() * 0.5, entity.getBbHeight() * 0.5, entity.getBbWidth() * 0.5, 0.2);
        }

        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvent.createVariableRangeEvent(this.breakSound), entity.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.GUARD.get();
    }

    public static class Serializer extends AbilitySerializer<GuardAbility> {

        @Override
        public MapCodec<GuardAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, GuardAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Guard")
                    .setDescription("While enabled, blocks incoming damage until a threshold is reached, at which point the guard breaks: damage is dealt to the entity and a break effect plays.")
                    .add("guard_threshold", TYPE_FLOAT, "The cumulative damage the entity can absorb while guarding before the guard breaks.")
                    .add("break_damage", TYPE_FLOAT, "The damage dealt to the entity when the guard breaks.")
                    .addOptional("break_lockout", TYPE_TIME, "How long the guard stays broken after breaking. Using the palladium:guard_broken condition inside a not condition on this ability to enable this lockout.", 0)
                    .addOptional("block_angle", TYPE_FLOAT, "The width, in degrees, of the arc in front of the entity's look direction that can block damage. 360 blocks from all directions, including damage without a source position (e.g. magic). Defaults to 180 (front hemisphere).", 180F)
                    .addOptional("bypassed_by", TYPE_DAMAGE_TYPE_HOLDER_SET, "Damage types that go through the guard. Defaults to the #minecraft:bypasses_shield tag, which also covers fall, fire and void damage.")
                    .addOptional("disable_attacking", TYPE_BOOLEAN, "If true, the entity cannot attack entities or interact with items/blocks/entities while this ability is enabled. Only applies to players. Defaults to true.", true)
                    .addOptional("knockback_strength", TYPE_FLOAT, "How strongly the entity that directly hit the guarding entity is knocked away whenever a hit is blocked. Projectile shooters are not affected. 0 disables this. Defaults to 0.5 (roughly 1 block of push).", 0.5F)
                    .addOptional("block_sound", TYPE_IDENTIFIER, "Sound played whenever a hit is blocked.", DEFAULT_BLOCK_SOUND)
                    .addOptional("break_sound", TYPE_IDENTIFIER, "Sound played when the guard breaks.", DEFAULT_BREAK_SOUND)
                    .addExampleObject(new GuardAbility(20F, 4F, 60, 180F, Optional.empty(), true, 0.5F, DEFAULT_BLOCK_SOUND, DEFAULT_BREAK_SOUND, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
