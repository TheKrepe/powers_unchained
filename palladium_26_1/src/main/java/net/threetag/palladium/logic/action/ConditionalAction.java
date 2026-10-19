package net.threetag.palladium.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.CrouchingCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.ParsedCommands;

import java.util.Collections;
import java.util.List;

public class ConditionalAction extends Action {

    public static final MapCodec<ConditionalAction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Condition.CODEC.fieldOf("condition").forGetter(a -> a.condition),
                    Action.LIST_CODEC.optionalFieldOf("on_true", Collections.emptyList()).forGetter(a -> a.ifTrue),
                    Action.LIST_CODEC.optionalFieldOf("on_false", Collections.emptyList()).forGetter(a -> a.ifFalse)
            ).apply(instance, ConditionalAction::new));

    private final Condition condition;
    private final List<Action> ifTrue;
    private final List<Action> ifFalse;

    public ConditionalAction(Condition condition, List<Action> ifTrue, List<Action> ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public boolean run(DataContext context) {
        if (this.condition.test(context)) {
            return Action.runList(this.ifTrue, context);
        } else {
            return Action.runList(this.ifFalse, context);
        }
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.CONDITIONAL.get();
    }

    public static class Serializer extends ActionSerializer<ConditionalAction> {

        @Override
        public MapCodec<ConditionalAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, ConditionalAction> builder, HolderLookup.Provider provider) {
            builder.setName("Conditional Action").setDescription("Runs an action based on the outcome of a condition")
                    .add("condition", TYPE_CONDITION_LIST, "The condition(s) that define the action that will run.")
                    .addOptional("on_true", TYPE_ACTION_LIST, "The action(s) that are being run when the condition is true")
                    .addOptional("on_false", TYPE_ACTION_LIST, "The action(s) that are being run when the condition is false")
                    .addExampleObject(new ConditionalAction(
                            CrouchingCondition.INSTANCE,
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Is crouching!"))),
                            Collections.singletonList(new RunCommandAction(new ParsedCommands("say Is not crouching!")))
                    ));
        }
    }
}