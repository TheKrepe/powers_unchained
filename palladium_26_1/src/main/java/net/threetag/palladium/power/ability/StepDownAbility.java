package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class StepDownAbility extends Ability {

    public static final MapCodec<StepDownAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("fall_speed").forGetter(ab -> ab.fallSpeed),
                    FloatValueHolder.CODEC.fieldOf("fall_distance").forGetter(ab -> ab.fallDistance),
                    Codec.BOOL.optionalFieldOf("safety_checks", true).forGetter(ab -> ab.safetyChecks),
                    Codec.BOOL.optionalFieldOf("allow_vehicles", false).forGetter(ab -> ab.allowVehicles),
                    Codec.BOOL.optionalFieldOf("allow_fluids", false).forGetter(ab -> ab.allowFluids),
                    RegistryCodecs.homogeneousList(Registries.FLUID).optionalFieldOf("allowed_fluids", HolderSet.emptyNamed(BuiltInRegistries.FLUID, FluidTags.WATER)).forGetter(ab -> ab.allowedFluids),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, StepDownAbility::new));

    public final float fallSpeed;
    public final FloatValueHolder fallDistance;
    public final boolean safetyChecks;
    public final boolean allowVehicles;
    public final boolean allowFluids;
    public final HolderSet<Fluid> allowedFluids;

    public StepDownAbility(float fallSpeed, FloatValueHolder fallDistance, boolean safetyChecks, boolean allowVehicles, boolean allowFluids, HolderSet<Fluid> allowedFluids, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.fallSpeed = fallSpeed;
        this.fallDistance = fallDistance;
        this.safetyChecks = safetyChecks;
        this.allowFluids = allowFluids;
        this.allowedFluids = allowedFluids;
        this.allowVehicles = allowVehicles;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.STEP_DOWN.get();
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (!enabled) return false;

        LivingEntity target = entity;
        if (this.allowVehicles && entity.getVehicle() instanceof LivingEntity livingVehicle) {
            target = livingVehicle;
        }

        if (this.safetyChecks) {
            if (!target.onGround() || target.isSwimming() || target.isSpectator() || target.isFallFlying()) {
                return false;
            }
        }

        if (target.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4)
            return false;

        final FluidState fluidState = target.level().getFluidState(target.blockPosition());
        if (!fluidState.isEmpty() && !this.allowFluids) {
            if (!fluidState.is(this.allowedFluids))
                return false;
        }

        final float fallDistance = this.fallDistance.get(DataContext.forAbility(entity, abilityInstance)) * abilityInstance.getAnimationTimerProgress(0);
        final float fallSpeed = this.fallSpeed;

        if (!target.level().noCollision(target.getBoundingBox().move(0.0, -(fallDistance + 0.01F), 0.0))) {
            target.setDeltaMovement(target.getDeltaMovement().x, -fallSpeed, target.getDeltaMovement().z);
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    public static class Serializer extends AbilitySerializer<StepDownAbility> {

        @Override
        public MapCodec<StepDownAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, StepDownAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows for stepping down blocks quickly.")
                    .add("fall_speed", TYPE_FLOAT, "Speed at which you fall off the block.")
                    .add("fall_distance", TYPE_FLOAT_VALUE, "Fall distance it will enable at.")
                    .addOptional("safety_checks", TYPE_BOOLEAN, "If false, checks for if the ability SHOULD be enabled such as the entity being in spectator, fall flying, swimming, or on the ground will be disabled.", true)
                    .addOptional("allow_vehicles", TYPE_BOOLEAN, "If true, it will work for living vehicles.", false)
                    .addOptional("allow_fluids", TYPE_BOOLEAN, "If true, it will work in fluids.", false)
                    .addOptional("allowed_fluids", TYPE_FLUID_HOLDER_SET, "The fluid the entity can walk down when in.", "#minecraft:water")
                    .addExampleObject(new StepDownAbility(1.0F, FloatValueHolder.ONE, true, false, false, HolderSet.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }
}