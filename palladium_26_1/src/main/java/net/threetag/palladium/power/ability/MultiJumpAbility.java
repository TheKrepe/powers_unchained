package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;
import java.util.Optional;

public class MultiJumpAbility extends Ability {

    public static final int MULTI_JUMP_TIME = 5;

    public static final MapCodec<MultiJumpAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_jumps").forGetter(a -> a.maxJumps),
                    Codec.FLOAT.optionalFieldOf("jump_velocity", LivingEntity.BASE_JUMP_POWER).forGetter(a -> a.jumpVelocity),
                    Codec.FLOAT.optionalFieldOf("forward_velocity", 0F).forGetter(a -> a.forwardVelocity),
                    Identifier.CODEC.optionalFieldOf("sound").forGetter(a -> a.sound),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, MultiJumpAbility::new));

    private final int maxJumps;
    private final float jumpVelocity;
    private final float forwardVelocity;
    private final Optional<Identifier> sound;

    public MultiJumpAbility(int maxJumps, float jumpVelocity, float forwardVelocity, Optional<Identifier> sound, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.maxJumps = maxJumps;
        this.jumpVelocity = jumpVelocity;
        this.forwardVelocity = forwardVelocity;
        this.sound = sound;
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder components) {
        super.registerDataComponents(components);
        components.set(PalladiumDataComponents.Abilities.MULTI_JUMPS_REMAINING.get(), this.maxJumps);
        components.set(PalladiumDataComponents.Abilities.WAS_JUMPING.get(), false);
        components.set(PalladiumDataComponents.Abilities.MULTI_JUMP_ARMED.get(), false);
        components.set(PalladiumDataComponents.Abilities.MULTI_JUMP_TIMER.get(), 0);
    }

    public static int getRemainingJumps(AbilityInstance<?> instance) {
        return instance.getAbility() instanceof MultiJumpAbility ability
                ? instance.getOrDefault(PalladiumDataComponents.Abilities.MULTI_JUMPS_REMAINING.get(), ability.maxJumps)
                : 0;
    }

    public static boolean isMultiJumping(AbilityInstance<?> instance) {
        return instance.getAbility() instanceof MultiJumpAbility
                && instance.getOrDefault(PalladiumDataComponents.Abilities.MULTI_JUMP_TIMER.get(), 0) > 0;
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (!entity.level().isClientSide()) {
            int timer = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.MULTI_JUMP_TIMER.get(), 0);

            if (timer > 0) {
                abilityInstance.set(PalladiumDataComponents.Abilities.MULTI_JUMP_TIMER.get(), timer - 1);
            }
        }

        if (!entity.level().isClientSide() && enabled && entity instanceof ServerPlayer serverPlayer) {
            boolean isJumping = serverPlayer.getLastClientInput().jump();
            boolean wasJumping = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.WAS_JUMPING.get(), false);

            if (entity.onGround() && entity.getDeltaMovement().y <= 0.0) {
                abilityInstance.setSilently(PalladiumDataComponents.Abilities.MULTI_JUMPS_REMAINING.get(), this.maxJumps);
                abilityInstance.setSilently(PalladiumDataComponents.Abilities.MULTI_JUMP_ARMED.get(), false);
            } else if (!entity.onGround()) {
                boolean armed = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.MULTI_JUMP_ARMED.get(), false);

                if (!armed) {
                    if (!isJumping && wasJumping) {
                        abilityInstance.setSilently(PalladiumDataComponents.Abilities.MULTI_JUMP_ARMED.get(), true);
                    }
                } else if (isJumping && !wasJumping) {
                    int remaining = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.MULTI_JUMPS_REMAINING.get(), this.maxJumps);

                    if (remaining > 0) {
                        Vec3 delta = entity.getDeltaMovement();
                        float yawRad = entity.getYRot() * ((float) Math.PI / 180F);
                        entity.setDeltaMovement(delta.x - Mth.sin(yawRad) * this.forwardVelocity, this.jumpVelocity, delta.z + Mth.cos(yawRad) * this.forwardVelocity);
                        entity.hurtMarked = true;
                        ignoreFallDamageBelowJump(entity);
                        this.sound.ifPresent(sound -> entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvent.createVariableRangeEvent(sound), entity.getSoundSource(), 1.0F, 1.0F));
                        abilityInstance.setSilently(PalladiumDataComponents.Abilities.MULTI_JUMPS_REMAINING.get(), remaining - 1);
                        abilityInstance.set(PalladiumDataComponents.Abilities.MULTI_JUMP_TIMER.get(), MULTI_JUMP_TIME);
                    }
                }
            }

            abilityInstance.setSilently(PalladiumDataComponents.Abilities.WAS_JUMPING.get(), isJumping);
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    @Override
    public void onComponentsSynced(LivingEntity entity, AbilityInstance<?> abilityInstance, DataComponentPatch patch) {
        var timer = patch.getPatch(PalladiumDataComponents.Abilities.MULTI_JUMP_TIMER.get());

        if (timer != null && timer.orElse(0) == MULTI_JUMP_TIME) {
            ignoreFallDamageBelowJump(entity);
        }
    }

    private static void ignoreFallDamageBelowJump(LivingEntity entity) {
        entity.resetFallDistance();
        entity.setIgnoreFallDamageFromCurrentImpulse(true, entity.position());
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.MULTI_JUMP.get();
    }

    public static class Serializer extends AbilitySerializer<MultiJumpAbility> {

        @Override
        public MapCodec<MultiJumpAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, MultiJumpAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("While enabled, lets the entity jump again in mid-air up to a set number of times before it needs to touch the ground again to refill its extra jumps. Fall damage is only taken for the distance fallen below the position of the last extra jump.")
                    .add("max_jumps", TYPE_NON_NEGATIVE_INT, "How many extra mid-air jumps the entity can perform before landing again.")
                    .addOptional("jump_velocity", TYPE_FLOAT, "The vertical velocity applied per extra jump. Defaults to the same strength as a normal jump.", LivingEntity.BASE_JUMP_POWER)
                    .addOptional("forward_velocity", TYPE_FLOAT, "The horizontal velocity added in the direction the entity is facing per extra jump.", 0F)
                    .addOptional("sound", TYPE_IDENTIFIER, "Sound played whenever an extra jump is performed.")
                    .addExampleObject(new MultiJumpAbility(1, LivingEntity.BASE_JUMP_POWER, 0.5F, Optional.of(SoundEvents.BREEZE_JUMP.location()), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
