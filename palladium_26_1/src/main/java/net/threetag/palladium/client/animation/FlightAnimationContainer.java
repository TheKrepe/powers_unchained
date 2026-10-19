package net.threetag.palladium.client.animation;

import net.minecraft.world.entity.Avatar;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.Objects;

public class FlightAnimationContainer extends PackControllerAnimationContainer {

    public FlightAnimationContainer(PalladiumAnimationLayer layer, Avatar player) {
        super(layer, player);
    }

    @Override
    public PackAnimationController getAvailableAnimationController(Avatar player) {
        var flightHandler = EntityFlightHandler.get(this.getPlayer());
        var flightType = flightHandler.getFlightType();

        if (flightType != null) {
            var id = Objects.requireNonNull(player.registryAccess().lookupOrThrow(PalladiumRegistryKeys.FLIGHT_TYPE).getKey(flightType)).withPrefix("flight/");
            return PackAnimationControllerManager.INSTANCE.get(id);
        }

        return null;
    }
}
