package net.threetag.palladium.power.ability.enabling;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import org.jetbrains.annotations.Nullable;

public class ConditionalEnablingHandler extends EnablingHandler {

    public static final MapCodec<ConditionalEnablingHandler> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.fieldOf("conditions").forGetter(handler -> handler.condition)
    ).apply(instance, ConditionalEnablingHandler::new));

    public static final ConditionalEnablingHandler EMPTY = new ConditionalEnablingHandler(TrueCondition.INSTANCE);

    public final Condition condition;

    public ConditionalEnablingHandler(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean requiresContext() {
        return !(this.condition instanceof TrueCondition);
    }

    @Override
    public boolean check(LivingEntity entity, AbilityInstance<?> abilityInstance, @Nullable DataContext context) {
        return this.condition.test(context);
    }

    @Override
    public MapCodec<? extends EnablingHandler> codec() {
        return CODEC;
    }
}
