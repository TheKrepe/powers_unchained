package net.threetag.palladium.logic.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Difficulty;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;

public record DifficultyCondition(Difficulty difficulty, boolean locked) implements Condition {

    public static final MapCodec<DifficultyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Difficulty.CODEC.fieldOf("difficulty").forGetter(DifficultyCondition::difficulty),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(DifficultyCondition::locked)
    ).apply(instance, DifficultyCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DifficultyCondition> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Difficulty.class), DifficultyCondition::difficulty,
            ByteBufCodecs.BOOL, DifficultyCondition::locked,
            DifficultyCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var level = context.getLevel();
        if (level == null) return false;

        var levelData = level.getLevelData();
        if (locked && !levelData.isDifficultyLocked()) return false;
        return levelData.getDifficulty() == difficulty;
    }

    @Override
    public ConditionSerializer<DifficultyCondition> getSerializer() {
        return ConditionSerializers.DIFFICULTY.get();
    }

    public static class Serializer extends ConditionSerializer<DifficultyCondition> {

        @Override
        public MapCodec<DifficultyCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, DifficultyCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Difficulty")
                    .setDescription("Checks which difficulty the world is on.")
                    .add("difficulty", SettingType.enumList(Difficulty.values()), "Difficulty to check.")
                    .addOptional("locked", TYPE_BOOLEAN, "Checks if the difficulty is also locked.", false)
                    .addExampleObject(new DifficultyCondition(Difficulty.HARD, true));
        }
    }

}