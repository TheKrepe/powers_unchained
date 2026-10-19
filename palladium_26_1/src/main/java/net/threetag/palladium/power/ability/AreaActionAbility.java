package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.CrouchingCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.Collections;
import java.util.List;

public class AreaActionAbility extends Ability{

    public static final MapCodec<AreaActionAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Action.LIST_CODEC.optionalFieldOf("first_tick_action", Collections.emptyList()).forGetter(a -> a.firstTickAction),
                    Action.LIST_CODEC.optionalFieldOf("action", Collections.emptyList()).forGetter(a -> a.action),
                    Action.LIST_CODEC.optionalFieldOf("last_tick_action", Collections.emptyList()).forGetter(a -> a.lastTickAction),
                    Condition.CODEC.optionalFieldOf("entity_condition", TrueCondition.INSTANCE).forGetter(a -> a.entityCondition),
                    FloatValueHolder.CODEC.optionalFieldOf("radius", FloatValueHolder.FOUR).forGetter(ab -> ab.radius),
                    Codec.BOOL.optionalFieldOf("affect_self", false).forGetter(ab -> ab.affectSelf),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, AreaActionAbility::new));

    public final List<Action> firstTickAction;
    public final List<Action> action;
    public final List<Action> lastTickAction;
    public final Condition entityCondition;
    public final FloatValueHolder radius;
    public final Boolean affectSelf;

    public AreaActionAbility(List<Action> firstTickAction, List<Action> action, List<Action> lastTickAction, Condition entityCondition, FloatValueHolder radius, Boolean affectSelf, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.firstTickAction = firstTickAction;
        this.action = action;
        this.lastTickAction = lastTickAction;
        this.entityCondition = entityCondition;
        this.radius = radius;
        this.affectSelf = affectSelf;
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (!this.firstTickAction.isEmpty()) {
            var context = DataContext.forAbility(entity, abilityInstance);
            var targets = entity.level().getEntities(this.affectSelf ? null : entity, AABB.ofSize(entity.position(), this.radius.get(context, 2), this.radius.get(context, 2), this.radius.get(context, 2)));

            for (Entity target : targets) {
                if (target instanceof LivingEntity targetEntity) {
                    var targetContext = DataContext.forAbility(targetEntity, abilityInstance);

                    if (this.entityCondition.test(targetContext)) {
                        for (Action action : this.firstTickAction) {
                            action.run(targetContext);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        boolean result = false;

        if (enabled && !this.action.isEmpty()) {
            var context = DataContext.forAbility(entity, abilityInstance);
            var targets = entity.level().getEntities(this.affectSelf ? null : entity, AABB.ofSize(entity.position(), this.radius.get(context, 2), this.radius.get(context, 2), this.radius.get(context, 2)));

            for (Entity target : targets) {
                if (target instanceof LivingEntity targetEntity) {
                    var targetContext = DataContext.forAbility(targetEntity, abilityInstance);

                    if (this.entityCondition.test(targetContext)) {
                        for (Action action : this.action) {
                            result |= action.run(targetContext);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (!this.lastTickAction.isEmpty()) {
            var context = DataContext.forAbility(entity, abilityInstance);
            var targets = entity.level().getEntities(this.affectSelf ? null : entity, AABB.ofSize(entity.position(), this.radius.get(context, 2), this.radius.get(context, 2), this.radius.get(context, 2)));

            for (Entity target : targets) {
                if (target instanceof LivingEntity targetEntity) {
                    var targetContext = DataContext.forAbility(targetEntity, abilityInstance);

                    if (this.entityCondition.test(targetContext)) {
                        for (Action action : this.lastTickAction) {
                            action.run(targetContext);
                        }
                    }
                }
            }
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.AREA_ACTION.get();
    }

    public static class Serializer extends AbilitySerializer<AreaActionAbility> {

        public MapCodec<AreaActionAbility> codec() {
            return AreaActionAbility.CODEC;
        }

        public void addDocumentation(CodecDocumentationBuilder<Ability, AreaActionAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("An ability that can perform one or many action objects on living entities within a certain range.")
                    .addOptional("action", TYPE_ACTION_LIST, "Action(s) that are performed during each tick of the ability")
                    .addOptional("first_tick_action", TYPE_ACTION_LIST, "Action(s) that are performed during the first tick of the ability")
                    .addOptional("last_tick_action", TYPE_ACTION_LIST, "Action(s) that are performed during the last tick of the ability")
                    .addOptional("entity_condition", TYPE_CONDITION_LIST, "Condition(s) that must be met for an entity to be affected by the ability. If no conditions are specified, then all entities will be affected. If multiple conditions are specified, then only entities that satisfy all conditions will be affected.")
                    .addOptional("radius", TYPE_FLOAT_VALUE, "The area around the player that entities are looked for.", 4F)
                    .addOptional("affect_self", TYPE_BOOLEAN, "Decides if the player executing should also be effected.", false)
                    .addExampleObject(new AreaActionAbility(
                            Collections.emptyList(),
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Hello World"))),
                            Collections.emptyList(),
                            TrueCondition.INSTANCE,
                            FloatValueHolder.FOUR,
                            false,
                            AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()))
                    .addExampleObject(new AreaActionAbility(
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say First Tick!"))),
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Hello World"))),
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Last Tick!"))),
                            CrouchingCondition.INSTANCE,
                            new FloatValueHolder(5),
                            true,
                            AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()));
        }
    }
}
