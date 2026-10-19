package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.Collections;
import java.util.List;

public class ActionAbility extends Ability {

    public static final MapCodec<ActionAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Action.LIST_CODEC.optionalFieldOf("first_tick_action", Collections.emptyList()).forGetter(a -> a.firstTickAction),
                    Action.LIST_CODEC.optionalFieldOf("action", Collections.emptyList()).forGetter(a -> a.action),
                    Action.LIST_CODEC.optionalFieldOf("last_tick_action", Collections.emptyList()).forGetter(a -> a.lastTickAction),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, ActionAbility::new));

    private final List<Action> firstTickAction;
    private final List<Action> action;
    private final List<Action> lastTickAction;

    public ActionAbility(List<Action> firstTickAction, List<Action> action, List<Action> lastTickAction, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.firstTickAction = firstTickAction;
        this.action = action;
        this.lastTickAction = lastTickAction;
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (!this.firstTickAction.isEmpty()) {
            var context = DataContext.forAbility(entity, abilityInstance);

            for (Action action : this.firstTickAction) {
                action.run(context);
            }
        }
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (enabled && !this.action.isEmpty()) {
            boolean result = false;
            var context = DataContext.forAbility(entity, abilityInstance);

            for (Action action : this.action) {
                result |= action.run(context);
            }

            return result;
        }

        return false;
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (!this.lastTickAction.isEmpty()) {
            var context = DataContext.forAbility(entity, abilityInstance);

            for (Action action : this.lastTickAction) {
                action.run(context);
            }
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.ACTION.get();
    }

    public static class Serializer extends AbilitySerializer<ActionAbility> {

        @Override
        public MapCodec<ActionAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("An ability that can perform one or many action objects.")
                    .addOptional("action", TYPE_ACTION_LIST, "Action(s) that are performed during each tick of the ability")
                    .addOptional("first_tick_action", TYPE_ACTION_LIST, "Action(s) that are performed during the first tick of the ability")
                    .addOptional("last_tick_action", TYPE_ACTION_LIST, "Action(s) that are performed during the last tick of the ability")
                    .addExampleObject(new ActionAbility(
                            Collections.emptyList(),
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Hello World"))),
                            Collections.emptyList(),
                            AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()))
                    .addExampleObject(new ActionAbility(
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say First Tick!"))),
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Hello World"))),
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Last Tick!"))),
                            AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()));
        }
    }
}
