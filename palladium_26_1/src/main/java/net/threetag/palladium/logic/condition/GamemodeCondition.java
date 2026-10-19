package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;

public record GamemodeCondition(GameType gameType) implements Condition {

    public static final MapCodec<GamemodeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GameType.CODEC.fieldOf("gamemode").forGetter(GamemodeCondition::gameType)
    ).apply(instance, GamemodeCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GamemodeCondition> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(GameType.class), GamemodeCondition::gameType,
            GamemodeCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var player = context.getPlayer();
        if (player == null) return false;

        var current = player.gameMode();
        if (current == null) return false;

        return current == gameType;
    }

    @Override
    public ConditionSerializer<GamemodeCondition> getSerializer() {
        return ConditionSerializers.GAMEMODE.get();
    }

    public static class Serializer extends ConditionSerializer<GamemodeCondition> {

        @Override
        public MapCodec<GamemodeCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, GamemodeCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Gamemode")
                    .setDescription("Checks if the player is in the specified gamemode.")
                    .add("gamemode", SettingType.enumList(GameType.values()), "Gamemode to check.")
                    .addExampleObject(new GamemodeCondition(GameType.SURVIVAL));
        }
    }

}