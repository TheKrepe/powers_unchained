package net.threetag.palladium.power.ability.unlocking;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.logic.condition.AbilityUnlockedCondition;
import net.threetag.palladium.logic.condition.AndCondition;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConditionalUnlockingHandler extends UnlockingHandler {

    public static final MapCodec<ConditionalUnlockingHandler> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.fieldOf("conditions").forGetter(h -> h.condition)
    ).apply(instance, ConditionalUnlockingHandler::new));

    public static final ConditionalUnlockingHandler EMPTY = new ConditionalUnlockingHandler(TrueCondition.INSTANCE);

    public final Condition condition;
    private final List<AbilityReference> parentAbilities;

    public ConditionalUnlockingHandler(Condition condition) {
        this.condition = condition;
        this.parentAbilities = new ArrayList<>();

        if (condition instanceof AndCondition(List<Condition> conditions)) {
            for (Condition c : conditions) {
                if (c instanceof AbilityUnlockedCondition(AbilityReference ability)) {
                    this.parentAbilities.add(ability);
                }
            }
        } else if (condition instanceof AbilityUnlockedCondition(AbilityReference ability)) {
            this.parentAbilities.add(ability);
        }
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
    public List<AbilityReference> getParentAbilities() {
        return this.parentAbilities;
    }

    @Override
    public MapCodec<? extends UnlockingHandler> codec() {
        return CODEC;
    }
}
