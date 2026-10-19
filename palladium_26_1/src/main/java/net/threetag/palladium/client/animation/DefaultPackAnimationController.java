package net.threetag.palladium.client.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultPackAnimationController extends PackAnimationController {

    public static final MapCodec<DefaultPackAnimationController> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("initial_state").forGetter(c -> c.initialState),
            Codec.unboundedMap(Codec.STRING, State.CODEC).optionalFieldOf("states", Collections.emptyMap()).forGetter(c -> c.states),
            propertiesCodec()
    ).apply(instance, DefaultPackAnimationController::new));

    private final Optional<String> initialState;
    private final Map<String, State> states;

    public DefaultPackAnimationController(Optional<String> initialState, Map<String, State> states, PackAnimationControllerProperties properties) {
        super(properties);
        this.initialState = initialState;
        this.states = states;
    }

    @Override
    public Optional<String> initialState() {
        return this.initialState;
    }

    @Override
    public Map<String, State> states() {
        return this.states;
    }

    @Override
    public PackAnimationController merge(PackAnimationController parent) {
        Map<String, State> states = new HashMap<>(parent.states());
        states.putAll(this.states);
        return new DefaultPackAnimationController(this.initialState.isPresent() ? this.initialState : parent.initialState(), states, this.properties.merge(parent.getProperties()));
    }

    @Override
    public PackAnimationController resolve() {
        Map<String, State> states = new HashMap<>();
        this.states.forEach((name, state) -> states.put(name, state.resolve(this.animations())));
        return new DefaultPackAnimationController(this.initialState, states, this.properties);
    }

    @Override
    public PackAnimationControllerSerializer<?> getSerializer() {
        return PackAnimationControllerSerializers.DEFAULT;
    }

    public static class Serializer extends PackAnimationControllerSerializer<DefaultPackAnimationController> {

        @Override
        public MapCodec<DefaultPackAnimationController> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<PackAnimationController, DefaultPackAnimationController> builder, HolderLookup.Provider provider) {
            builder.setName("Default").setDescription("State machine: every state plays an animation and lists conditions that move the controller into other states. Used when no \"type\" is given.")
                    .addOptional("initial_state", TYPE_STRING, "State the controller starts in.")
                    .addOptional("states", SettingType.simple("Map<String, State (animation, blend_transition, transitions)>"), "States of this controller, keyed by name. Transitions are evaluated in order every tick.");
        }
    }
}
