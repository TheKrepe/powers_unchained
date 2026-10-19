package net.threetag.palladium.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;

public class LeapAction extends Action {

    public static final MapCodec<LeapAction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    FloatValueHolder.CODEC.fieldOf("velocity_modifier").forGetter(a -> a.velocityModifier)
            ).apply(instance, LeapAction::new));

    private final FloatValueHolder velocityModifier;

    public LeapAction(FloatValueHolder velocityModifier) {
        this.velocityModifier = velocityModifier;
    }

    @Override
    public boolean run(DataContext context) {
        LivingEntity entity = context.getLivingEntity();

        if (entity == null || entity.level().isClientSide()) {
            return false;
        }

        Vec3 look = entity.getLookAngle();
        entity.setDeltaMovement(entity.getDeltaMovement().add(look.scale(this.velocityModifier.get(context))));
        entity.hurtMarked = true;
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.LEAP.get();
    }

    public static class Serializer extends ActionSerializer<LeapAction> {
        @Override
        public MapCodec<LeapAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, LeapAction> builder, HolderLookup.Provider provider) {
            builder.setName("Leap")
                    .setDescription("Launches the entity along its current look direction, scaled by a velocity modifier.")
                    .add("velocity_modifier", TYPE_FLOAT_VALUE, "The multiplier applied to the entity's look direction vector when this action runs.")
                    .addExampleObject(new LeapAction(new FloatValueHolder(1.5F)));
        }
    }
}
