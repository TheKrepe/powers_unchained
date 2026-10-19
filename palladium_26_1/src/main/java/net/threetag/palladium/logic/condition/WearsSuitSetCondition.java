package net.threetag.palladium.logic.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.item.suitset.EntitySuitSetHandler;
import net.threetag.palladium.item.suitset.SuitSet;
import net.threetag.palladium.item.suitset.SuitSets;
import net.threetag.palladium.logic.context.DataContext;

public record WearsSuitSetCondition(SuitSet suitSet) implements Condition {

    public static final MapCodec<WearsSuitSetCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(SuitSets.BY_NAME_CODEC.fieldOf("suit_set").forGetter(WearsSuitSetCondition::suitSet))
            .apply(instance, WearsSuitSetCondition::new)
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();
        return entity != null && EntitySuitSetHandler.get(entity).isWearing(this.suitSet);
    }

    @Override
    public ConditionSerializer<WearsSuitSetCondition> getSerializer() {
        return ConditionSerializers.WEARS_SUIT_SET.get();
    }

    public static class Serializer extends ConditionSerializer<WearsSuitSetCondition> {

        @Override
        public MapCodec<WearsSuitSetCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, WearsSuitSetCondition> builder, HolderLookup.Provider provider) {
            JsonObject example = new JsonObject();
            example.addProperty("type", ConditionSerializers.WEARS_SUIT_SET.getId().toString());
            example.add("suit_set", new JsonPrimitive("example:suit_set"));

            builder.setName("Wears Suit Set").setDescription("Checks if the entity is wearing the specified suit set")
                    .add("suit_set", TYPE_SUIT_SET, "ID of the needed suit set")
                    .addExampleJson(example);
        }

    }
}
