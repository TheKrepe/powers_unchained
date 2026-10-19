package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.entity.flight.FlightType;
import net.threetag.palladium.entity.flight.PalladiumFlightTypes;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.PalladiumHolderSet;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IsUsingFlightTypeCondition implements Condition {

    public static final MapCodec<IsUsingFlightTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PalladiumHolderSet.codec(PalladiumRegistryKeys.FLIGHT_TYPE).optionalFieldOf("flight_type").forGetter(c -> Optional.ofNullable(c.flightType))
    ).apply(instance, opt -> new IsUsingFlightTypeCondition(opt.orElse(null))));

    @Nullable
    private final PalladiumHolderSet<FlightType> flightType;

    public IsUsingFlightTypeCondition(@Nullable PalladiumHolderSet<FlightType> flightType) {
        this.flightType = flightType;
    }

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity != null) {
            var handler = EntityFlightHandler.get(entity);

            if (handler.isFlying() && handler.getFlightType() != null) {
                if (this.flightType == null) {
                    return true;
                }
                var holder = entity.registryAccess().lookupOrThrow(PalladiumRegistryKeys.FLIGHT_TYPE).wrapAsHolder(handler.getFlightType());
                return this.flightType.contains(holder);
            }
        }

        return false;
    }

    @Override
    public ConditionSerializer<?> getSerializer() {
        return ConditionSerializers.IS_USING_FLIGHT_TYPE.get();
    }

    public static class Serializer extends ConditionSerializer<IsUsingFlightTypeCondition> {

        @Override
        public MapCodec<IsUsingFlightTypeCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsUsingFlightTypeCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is using Flight Type")
                    .setDescription("Checks if the given entity is currently flying. Optionally checks against specific flight type(s).")
                    .addOptional("flight_type", TYPE_FLIGHT_TYPE_HOLDER_SET, "ID(s) or tag(s) of the required flight type. If omitted, any active flight type passes.", null)
                    .addExampleObject(new IsUsingFlightTypeCondition(null))
                    .addExampleObject(new IsUsingFlightTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(PalladiumFlightTypes.PROPELLED)))))
                    .addExampleObject(new IsUsingFlightTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(
                            provider.holderOrThrow(PalladiumFlightTypes.PROPELLED),
                            provider.holderOrThrow(PalladiumFlightTypes.WEB_SWINGING)
                    ))));

        }
    }
}
