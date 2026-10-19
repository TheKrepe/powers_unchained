package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.logic.context.DataContext;

public class IsFlyingCondition implements Condition {

    public static final IsFlyingCondition INSTANCE = new IsFlyingCondition();

    public static final MapCodec<IsFlyingCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsFlyingCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        if (EntityFlightHandler.get(entity).isFlying()) {
            return true;
        }

        return entity instanceof Player player && player.getAbilities().flying;
    }

    @Override
    public ConditionSerializer<IsFlyingCondition> getSerializer() {
        return ConditionSerializers.IS_FLYING.get();
    }

    public static class Serializer extends ConditionSerializer<IsFlyingCondition> {

        @Override
        public MapCodec<IsFlyingCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsFlyingCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Flying")
                    .setDescription("Checks if the entity is currently flying via any flight type, or is creative flying.")
                    .addExampleObject(new IsFlyingCondition());
        }
    }
}
