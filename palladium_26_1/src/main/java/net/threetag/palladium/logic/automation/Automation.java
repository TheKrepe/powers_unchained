package net.threetag.palladium.logic.automation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;

import java.util.List;

public record Automation(Criterion<?> trigger, Condition condition, List<Action> actions) {

    public static final Codec<Automation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Criterion.CODEC.fieldOf("trigger").forGetter(Automation::trigger),
            Condition.CODEC.optionalFieldOf("condition", TrueCondition.INSTANCE).forGetter(Automation::condition),
            ExtraCodecs.compactListCodec(Action.CODEC).fieldOf("actions").forGetter(Automation::actions)
    ).apply(instance, Automation::new));

}
