package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class IsHardcoreCondition implements Condition {

    public static final IsHardcoreCondition INSTANCE = new IsHardcoreCondition();

    public static final MapCodec<IsHardcoreCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsHardcoreCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        var level = context.getLevel();
        return level != null && level.getLevelData().isHardcore();
    }

    @Override
    public ConditionSerializer<IsHardcoreCondition> getSerializer() {
        return ConditionSerializers.IS_HARDCORE.get();
    }

    public static class Serializer extends ConditionSerializer<IsHardcoreCondition> {

        @Override
        public MapCodec<IsHardcoreCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsHardcoreCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Hardcore")
                    .setDescription("Checks if the world is hardcore.")
                    .addExampleObject(new IsHardcoreCondition());
        }
    }

}