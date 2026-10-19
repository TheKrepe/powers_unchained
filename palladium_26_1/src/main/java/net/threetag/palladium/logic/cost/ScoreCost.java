package net.threetag.palladium.logic.cost;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.icon.SpriteIcon;
import net.threetag.palladium.util.ScoreboardUtil;

public class ScoreCost extends Cost {

    public static final MapCodec<ScoreCost> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("objective").forGetter(c -> c.objective),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1).forGetter(c -> c.amount),
            Icon.CODEC.fieldOf("icon").forGetter(c -> c.icon),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(c -> c.description)
    ).apply(instance, ScoreCost::new));

    private final String objective;
    private final int amount;
    private final Icon icon;
    private final Component description;

    public ScoreCost(String objective, int amount, Icon icon, Component description) {
        this.objective = objective;
        this.amount = amount;
        this.icon = icon;
        this.description = description;
    }

    @Override
    public boolean hasEnoughCurrency(LivingEntity entity) {
        if (entity instanceof Player player) {
            return ScoreboardUtil.getScore(player, this.objective) >= this.amount;
        }
        return false;
    }

    @Override
    public void consumeCurrency(LivingEntity entity) {
        if (entity instanceof Player player) {
            int score = ScoreboardUtil.getScore(player, this.objective);
            if (score >= this.amount) {
                ScoreboardUtil.setScore(player, this.objective, score - this.amount);
            }
        }
    }

    @Override
    public CostDisplay createDisplay() {
        return new CostDisplay(this.icon, this.amount, this.description);
    }

    @Override
    public CostSerializer<?> getSerializer() {
        return CostSerializers.SCORE.value();
    }

    public static class Serializer extends CostSerializer<ScoreCost> {

        @Override
        public MapCodec<ScoreCost> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Cost, ScoreCost> builder, HolderLookup.Provider provider) {
            builder.setName("Score Cost").setDescription("Requires the entity to have a certain scoreboard score.")
                    .add("objective", TYPE_STRING, "The scoreboard objective name.")
                    .add("amount", TYPE_POSITIVE_INT, "The score amount to consume. Defaults to 1.")
                    .add("icon", TYPE_ICON, "The icon shown in the UI.")
                    .add("description", TYPE_TEXT_COMPONENT, "The display text shown in the UI.")
                    .addExampleObject(new ScoreCost("my_score", 10, new SpriteIcon(Identifier.parse("example:sprite_path")), Component.literal("My Score")));
        }
    }
}
