package net.threetag.palladium.entity.flight;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.FlightAbility;
import net.threetag.palladium.client.gui.shake.ScreenShake;
import net.threetag.palladium.registry.PalladiumRegistries;
import org.jetbrains.annotations.Nullable;

public abstract class FlightType {

    public static final Codec<FlightType> CODEC = PalladiumRegistries.FLIGHT_TYPE_SERIALIZERS.byNameCodec().dispatch(FlightType::getSerializer, FlightTypeSerializer::codec);

    public abstract FlightTypeSerializer<?> getSerializer();

    public abstract FlightController<?> createController();

    public boolean canStart(LivingEntity entity) {
        return true;
    }

    public abstract FlightAnimationHandler<?> createAnimationHandler();

    @Nullable
    public ScreenShake getScreenShake(LivingEntity entity) {
        return null;
    }

    public float getSpeedMultiplier(LivingEntity entity) {
        return FlightAbility.getSpeedMultiplier(entity, this);
    }

    @Nullable
    public Identifier getFlightSound() {
        return null;
    }

    @Nullable
    public Identifier getAmbientFlightSound() {
        return null;
    }

}
