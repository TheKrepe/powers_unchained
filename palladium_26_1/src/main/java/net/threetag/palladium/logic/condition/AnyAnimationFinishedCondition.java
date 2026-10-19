package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;

public class AnyAnimationFinishedCondition implements Condition {

    public static final AnyAnimationFinishedCondition INSTANCE = new AnyAnimationFinishedCondition();

    public static final MapCodec<AnyAnimationFinishedCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, AnyAnimationFinishedCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        return context.getOrDefault(DataContextKeys.ANY_ANIMATION_FINISHED, false);
    }

    @Override
    public ConditionSerializer<AnyAnimationFinishedCondition> getSerializer() {
        return ConditionSerializers.ANY_ANIMATION_FINISHED.get();
    }

    public static class Serializer extends ConditionSerializer<AnyAnimationFinishedCondition> {

        @Override
        public MapCodec<AnyAnimationFinishedCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, AnyAnimationFinishedCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Any Animation Finished")
                    .setDescription("Checks if any animation has finished in the current animation controller. Note: this condition is only effective when used inside an animation controller.")
                    .addExampleObject(new AnyAnimationFinishedCondition());
        }
    }

}
