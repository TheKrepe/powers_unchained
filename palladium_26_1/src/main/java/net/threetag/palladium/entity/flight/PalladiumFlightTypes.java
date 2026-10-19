package net.threetag.palladium.entity.flight;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec2;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.sound.PalladiumSoundEvents;

import java.util.Optional;
import java.util.function.BiConsumer;

public class PalladiumFlightTypes {

    public static final ResourceKey<FlightType> LEVITATION = create("levitation");
    public static final ResourceKey<FlightType> PROPELLED = create("propelled");
    public static final ResourceKey<FlightType> WEB_SWINGING = create("web_swinging");

    private static ResourceKey<FlightType> create(String id) {
        return ResourceKey.create(PalladiumRegistryKeys.FLIGHT_TYPE, Palladium.id(id));
    }

    public static void bootstrap(BiConsumer<ResourceKey<FlightType>, FlightType> consumer) {
        consumer.accept(LEVITATION, new DefaultFlightType(
                new StaticValue(1F), null, null, null, null, null)
        );

        consumer.accept(PROPELLED, new DefaultFlightType(
                new StaticValue(1F), new StaticValue(3F), null, null, PalladiumSoundEvents.PLAYER_FLIGHT_PROPELLED.getId(), null)
        );

        consumer.accept(WEB_SWINGING, new SwingingFlightType(
                120, SwingingFlightType.MaxHeightType.ABSOLUTE, new Vec2(10, 30), 40, SwingingFlightType.DEFAULT_BEAM,
                Optional.of(PalladiumSoundEvents.PLAYER_FLIGHT_WEB_SHOOT.getId()), Optional.of(PalladiumSoundEvents.PLAYER_FLIGHT_SWING.getId()))
        );
    }

}
