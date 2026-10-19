package net.threetag.palladium.client.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.AnyAnimationFinishedCondition;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.NotCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TriggeredPackAnimationController extends PackAnimationController {

    public static final MapCodec<TriggeredPackAnimationController> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Condition.CODEC).optionalFieldOf("triggers", Collections.emptyMap()).forGetter(c -> c.triggers),
            propertiesCodec()
    ).apply(instance, TriggeredPackAnimationController::new));

    private static final String IDLE = "idle";
    private static final String PLAYING = "playing/";
    private static final String FINISHED = "finished/";

    private final Map<String, Condition> triggers;
    private final Map<String, State> states;

    public TriggeredPackAnimationController(Map<String, Condition> triggers, PackAnimationControllerProperties properties) {
        super(properties);
        this.triggers = triggers;
        this.states = buildStates(triggers, properties);
    }

    private static Map<String, State> buildStates(Map<String, Condition> triggers, PackAnimationControllerProperties properties) {
        Map<String, State> states = new LinkedHashMap<>();
        List<Transition> fromIdle = new ArrayList<>();

        for (Map.Entry<String, Condition> trigger : triggers.entrySet()) {
            fromIdle.add(new Transition(PLAYING + trigger.getKey(), trigger.getValue()));
        }

        states.put(IDLE, new State(List.of(PalladiumAnimationManager.EMPTY_ANIMATION.toString()), Optional.empty(), fromIdle));

        for (Map.Entry<String, Condition> trigger : triggers.entrySet()) {
            String animation = trigger.getKey();
            List<Transition> fromFinished = new ArrayList<>();

            for (Transition other : fromIdle) {
                if (!other.state().equals(PLAYING + animation)) {
                    fromFinished.add(other);
                }
            }

            fromFinished.add(new Transition(IDLE, new NotCondition(List.of(trigger.getValue()))));

            states.put(PLAYING + animation, new State(resolveAnimations(List.of(animation), properties.animations()), Optional.empty(), List.of(new Transition(FINISHED + animation, AnyAnimationFinishedCondition.INSTANCE))));
            states.put(FINISHED + animation, new State(List.of(PalladiumAnimationManager.EMPTY_ANIMATION.toString()), Optional.empty(), fromFinished));
        }

        return states;
    }

    @Override
    public Optional<String> initialState() {
        return Optional.of(IDLE);
    }

    @Override
    public Map<String, State> states() {
        return this.states;
    }

    @Override
    public PackAnimationController merge(PackAnimationController parent) {
        Map<String, Condition> triggers = new LinkedHashMap<>(((TriggeredPackAnimationController) parent).triggers);
        triggers.putAll(this.triggers);
        return new TriggeredPackAnimationController(triggers, this.properties.merge(parent.getProperties()));
    }

    @Override
    public PackAnimationController resolve() {
        return this;
    }

    @Override
    public PackAnimationControllerSerializer<?> getSerializer() {
        return PackAnimationControllerSerializers.TRIGGERED;
    }

    public static class Serializer extends PackAnimationControllerSerializer<TriggeredPackAnimationController> {

        @Override
        public MapCodec<TriggeredPackAnimationController> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<PackAnimationController, TriggeredPackAnimationController> builder, HolderLookup.Provider provider) {
            builder.setName("Triggered").setDescription("Plays one-shot animations when a condition becomes true. Each trigger plays its animation once, then waits until its condition is false again before it can fire again. No states or transitions needed.")
                    .addOptional("triggers", SettingType.simple("Map<Animation (ID or #key), Condition>"), "Animations to play, keyed by animation ID or \"#key\" reference, with the condition that triggers them. Earlier entries win when several conditions are true at once.");
        }
    }
}
