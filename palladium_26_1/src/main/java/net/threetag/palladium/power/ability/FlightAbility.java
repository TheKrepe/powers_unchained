package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.entity.flight.FlightType;
import net.threetag.palladium.entity.flight.PalladiumFlightTypes;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlightAbility extends Ability {

    public static final MapCodec<FlightAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(PalladiumRegistryKeys.FLIGHT_TYPE).optionalFieldOf("flight_type", PalladiumFlightTypes.PROPELLED).forGetter(a -> a.flightTypeKey),
            PalladiumCodecs.POSITIVE_FLOAT_DYNAMIC.optionalFieldOf("speed_multiplier", FloatValueHolder.ONE).forGetter(a -> a.speedMultiplier),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, FlightAbility::new));

    private final ResourceKey<FlightType> flightTypeKey;
    private final FloatValueHolder speedMultiplier;
    private Holder<FlightType> flightType;

    public FlightAbility(ResourceKey<FlightType> flightTypeKey, FloatValueHolder speedMultiplier, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.flightTypeKey = flightTypeKey;
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (enabled) {
            if (this.flightType == null) {
                this.flightType = entity.registryAccess().get(this.flightTypeKey).orElse(null);
            }
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        var flightHandler = EntityFlightHandler.get(entity);
        flightHandler.reevaluateFlightType();
    }

    @Nullable
    public FlightType getFlightType() {
        return this.flightType != null ? this.flightType.value() : null;
    }

    public float getSpeedMultiplier(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        return this.speedMultiplier.get(DataContext.forAbility(entity, abilityInstance));
    }

    public static float getSpeedMultiplier(LivingEntity entity, FlightType flightType) {
        for (AbilityInstance<FlightAbility> instance : AbilityUtil.getEnabledInstances(entity, AbilitySerializers.FLIGHT.get())) {
            if (instance.getAbility().getFlightType() == flightType) {
                return instance.getAbility().getSpeedMultiplier(entity, instance);
            }
        }

        return 1F;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.FLIGHT.get();
    }

    public static class Serializer extends AbilitySerializer<FlightAbility> {

        @Override
        public MapCodec<FlightAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, FlightAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows the player to fly using a pre-defined flight type")
                    .addOptional("flight_type", TYPE_FLIGHT_TYPE, "The ID of the flight type that will be used.", PalladiumFlightTypes.PROPELLED.identifier())
                    .addOptional("speed_multiplier", TYPE_FLOAT_VALUE, "Multiplier applied to the speeds defined in the flight type.", 1F)
                    .addExampleObject(new FlightAbility(PalladiumFlightTypes.PROPELLED, FloatValueHolder.ONE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }
}
