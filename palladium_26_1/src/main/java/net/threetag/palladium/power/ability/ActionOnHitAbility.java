package net.threetag.palladium.power.ability;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.CrouchingCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.Collections;
import java.util.List;

public class ActionOnHitAbility extends Ability {

    public static final MapCodec<ActionOnHitAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Action.LIST_CODEC.optionalFieldOf("action", Collections.emptyList()).forGetter(a -> a.action),
                    Condition.CODEC.optionalFieldOf("entity_condition", TrueCondition.INSTANCE).forGetter(a -> a.entityCondition),
                    Codec.BOOL.optionalFieldOf("direct_only", true).forGetter(ab -> ab.directOnly),
                    propertiesCodec(),
                    stateCodec(),
                    energyBarUsagesCodec()
            ).apply(instance, ActionOnHitAbility::new)
    );

    public final List<Action> action;
    public final Condition entityCondition;
    public final boolean directOnly;

    public ActionOnHitAbility(List<Action> action, Condition entityCondition, boolean directOnly, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.action = action;
        this.entityCondition = entityCondition;
        this.directOnly = directOnly;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.ACTION_ON_HIT.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnHitAbility> {

        public Serializer() {
        }

        public MapCodec<ActionOnHitAbility> codec() {
            return ActionOnHitAbility.CODEC;
        }

        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnHitAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions to a victim when the entity with the ability damages them.")
                    .addOptional("action", TYPE_ACTION_LIST, "Action(s) that are performed when an entity is attacked")
                    .addOptional("entity_condition", TYPE_CONDITION_LIST, "Condition(s) that must be met for an entity to be affected by the ability. If no conditions are specified, then all entities will be affected. If multiple conditions are specified, then only entities that satisfy all conditions will be affected.")
                    .addOptional("direct_only", TYPE_BOOLEAN, "If false, then projectiles and other non-direct damage will also trigger the Action(s).", true)
                    .addExampleObject(new ActionOnHitAbility(
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Hello World"))),
                            CrouchingCondition.INSTANCE,
                            true,
                            AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }
}