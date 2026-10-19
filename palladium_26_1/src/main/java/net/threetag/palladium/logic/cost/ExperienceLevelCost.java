package net.threetag.palladium.logic.cost;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.icon.ExperienceIcon;

public class ExperienceLevelCost extends Cost {

    public static final MapCodec<ExperienceLevelCost> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("xp_level").forGetter(c -> c.xpLevel)
    ).apply(instance, ExperienceLevelCost::new));

    private final int xpLevel;

    public ExperienceLevelCost(int xpLevel) {
        this.xpLevel = xpLevel;
    }

    @Override
    public boolean hasEnoughCurrency(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.experienceLevel >= this.xpLevel;
        }
        return false;
    }

    @Override
    public void consumeCurrency(LivingEntity entity) {
        if (entity instanceof Player player) {
            player.giveExperienceLevels(-this.xpLevel);
        }
    }

    @Override
    public CostDisplay createDisplay() {
        return new CostDisplay(
                new ExperienceIcon(1, true),
                this.xpLevel,
                Component.translatable("gui.palladium.powers.buy_ability.experience_level" + (this.xpLevel > 1 ? "_plural" : ""), this.xpLevel)
        );
    }

    @Override
    public CostSerializer<?> getSerializer() {
        return CostSerializers.EXPERIENCE_LEVEL.value();
    }

    public static class Serializer extends CostSerializer<ExperienceLevelCost> {

        @Override
        public MapCodec<ExperienceLevelCost> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Cost, ExperienceLevelCost> builder, HolderLookup.Provider provider) {
            builder.setName("Experience Level Cost").setDescription("Requires the entity to have a certain number of experience levels.")
                    .add("xp_level", TYPE_POSITIVE_INT, "The number of experience levels to consume.")
                    .addExampleObject(new ExperienceLevelCost(5));
        }
    }
}
