package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.attachment.PalladiumAttachments;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class CarryingCondition implements Condition {

    public static final CarryingCondition INSTANCE = new CarryingCondition();

    public static final MapCodec<CarryingCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, CarryingCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        LivingEntity entity = context.getLivingEntity();
        return entity instanceof Player player && player.getData(PalladiumAttachments.CARRIED_TARGET_ID) != -1;
    }

    @Override
    public ConditionSerializer<CarryingCondition> getSerializer() {
        return ConditionSerializers.CARRYING.get();
    }

    public static class Serializer extends ConditionSerializer<CarryingCondition> {

        @Override
        public MapCodec<CarryingCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, CarryingCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Carrying")
                    .setDescription("Checks if the entity is a player currently holding a target via the carry ability.")
                    .addExampleObject(CarryingCondition.INSTANCE);
        }
    }

}
