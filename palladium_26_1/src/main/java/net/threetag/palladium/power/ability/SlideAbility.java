package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class SlideAbility extends Ability {

    public static final MapCodec<SlideAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("speed").forGetter(a -> a.speed),
                    ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(a -> a.duration),
                    Codec.doubleRange(0.0, 1.0).optionalFieldOf("friction", 0.93).forGetter(a -> a.friction),
                    Codec.DOUBLE.fieldOf("knockback_strength").forGetter(a -> a.knockbackStrength),
                    Codec.DOUBLE.optionalFieldOf("range", 1.0).forGetter(a -> a.range),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, SlideAbility::new));

    private final double speed;
    private final int duration;
    private final double friction;
    private final double knockbackStrength;
    private final double range;

    public SlideAbility(double speed, int duration, double friction, double knockbackStrength, double range,
                         AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.speed = speed;
        this.duration = duration;
        this.friction = friction;
        this.knockbackStrength = knockbackStrength;
        this.range = range;
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder components) {
        super.registerDataComponents(components);
        components.set(PalladiumDataComponents.Abilities.SLIDE_TICKS_REMAINING.get(), 0);
        components.set(PalladiumDataComponents.Abilities.SLIDE_WAS_SHIFT_DOWN.get(), false);
        components.set(PalladiumDataComponents.Abilities.SLIDE_WAS_SPRINTING.get(), false);
        components.set(PalladiumDataComponents.Abilities.SLIDE_VELOCITY_X.get(), 0.0);
        components.set(PalladiumDataComponents.Abilities.SLIDE_VELOCITY_Z.get(), 0.0);
        components.set(PalladiumDataComponents.Abilities.SLIDE_LOCKED.get(), false);
        components.set(PalladiumDataComponents.Abilities.SLIDE_WAS_MOVEMENT_INPUT.get(), false);
        components.set(PalladiumDataComponents.Abilities.SLIDE_ACTIVE.get(), false);
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            Input input = serverPlayer.getLastClientInput();
            boolean shiftDown = input.shift();
            boolean sprinting = entity.isSprinting();
            boolean movementInput = input.forward() || input.backward() || input.left() || input.right();
            boolean wasShiftDown = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.SLIDE_WAS_SHIFT_DOWN.get(), false);
            boolean wasSprinting = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.SLIDE_WAS_SPRINTING.get(), false);
            boolean wasMovementInput = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.SLIDE_WAS_MOVEMENT_INPUT.get(), false);
            int slideTicks = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.SLIDE_TICKS_REMAINING.get(), 0);
            boolean locked = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.SLIDE_LOCKED.get(), false);
            boolean wasActive = slideTicks > 0;

            if (enabled && slideTicks <= 0 && shiftDown && !wasShiftDown && wasSprinting && entity.onGround()) {
                float yawRad = entity.getYRot() * ((float) Math.PI / 180F);
                double velX = -Mth.sin(yawRad) * this.speed;
                double velZ = Mth.cos(yawRad) * this.speed;

                entity.setDeltaMovement(velX, Math.min(entity.getDeltaMovement().y, 0.0), velZ);
                entity.hurtMarked = true;
                abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_VELOCITY_X.get(), velX);
                abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_VELOCITY_Z.get(), velZ);
                slideTicks = this.duration;
                locked = true;
                abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_TICKS_REMAINING.get(), slideTicks);
                abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_LOCKED.get(), true);
            }

            if (slideTicks > 0) {
                if (!enabled || !shiftDown || !entity.onGround()) {
                    slideTicks = 0;
                } else {
                    double velX = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.SLIDE_VELOCITY_X.get(), 0.0) * this.friction;
                    double velZ = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.SLIDE_VELOCITY_Z.get(), 0.0) * this.friction;

                    entity.setDeltaMovement(velX, Math.min(entity.getDeltaMovement().y, 0.0), velZ);
                    entity.hurtMarked = true;
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_VELOCITY_X.get(), velX);
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_VELOCITY_Z.get(), velZ);

                    AABB sweep = entity.getBoundingBox().inflate(this.range);
                    Vec3 pushInput = new Vec3(-velX, 0.0, -velZ);

                    for (LivingEntity target : entity.level().getEntitiesOfClass(LivingEntity.class, sweep, e -> e != entity && e.isAlive())) {
                        target.knockback(this.knockbackStrength, pushInput.x, pushInput.z);
                        target.hurtMarked = true;
                    }

                    slideTicks--;
                }

                abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_TICKS_REMAINING.get(), slideTicks);
            } else if (locked) {
                Vec3 delta = entity.getDeltaMovement();

                if (delta.x != 0.0 || delta.z != 0.0) {
                    entity.setDeltaMovement(0.0, delta.y, 0.0);
                    entity.hurtMarked = true;
                }

                if (movementInput && !wasMovementInput) {
                    locked = false;
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_LOCKED.get(), false);
                }
            }

            boolean isActive = slideTicks > 0;

            if (isActive != wasActive) {
                abilityInstance.set(PalladiumDataComponents.Abilities.SLIDE_ACTIVE.get(), isActive);
            }

            abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_WAS_SHIFT_DOWN.get(), shiftDown);
            abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_WAS_SPRINTING.get(), sprinting);
            abilityInstance.setSilently(PalladiumDataComponents.Abilities.SLIDE_WAS_MOVEMENT_INPUT.get(), movementInput);
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    public static boolean isSliding(AbilityInstance<?> instance) {
        return instance.getOrDefault(PalladiumDataComponents.Abilities.SLIDE_ACTIVE.get(), false);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.SLIDE.get();
    }

    public static class Serializer extends AbilitySerializer<SlideAbility> {

        @Override
        public MapCodec<SlideAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, SlideAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("While enabled, pressing crouch during a sprint launches the entity into a ground slide along its facing direction, knocking back any living entities it slides into.")
                    .add("speed", TYPE_DOUBLE, "Horizontal blocks/tick the slide starts at.")
                    .add("duration", TYPE_POSITIVE_INT, "How many ticks the slide lasts (speed decays linearly to 0 over this time).")
                    .addOptional("friction", TYPE_DOUBLE, "Per-tick retention (0-1) of any pre-existing horizontal velocity blended into the slide. Defaults to 0.93.", 0.93)
                    .add("knockback_strength", TYPE_DOUBLE, "Knockback strength applied to entities the slide sweeps into, in the slide's direction.")
                    .addOptional("range", TYPE_DOUBLE, "How far past the entity's own hitbox the knockback sweep reaches each tick. Defaults to 1.0.", 1.0)
                    .addExampleObject(new SlideAbility(0.8, 10, 0.93, 1.2, 1.0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
