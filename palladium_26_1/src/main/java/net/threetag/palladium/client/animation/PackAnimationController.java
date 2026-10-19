package net.threetag.palladium.client.animation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class PackAnimationController {

    protected final PackAnimationControllerProperties properties;

    protected PackAnimationController(PackAnimationControllerProperties properties) {
        this.properties = properties;
    }

    public final PackAnimationControllerProperties getProperties() {
        return this.properties;
    }

    public Optional<Identifier> parent() {
        return this.properties.parent();
    }

    public Map<String, List<Identifier>> animations() {
        return this.properties.animations();
    }

    public Optional<Integer> blendTransition() {
        return this.properties.blendTransition();
    }

    public abstract Optional<String> initialState();

    public abstract Map<String, State> states();

    public abstract PackAnimationController merge(PackAnimationController parent);

    public abstract PackAnimationController resolve();

    public abstract PackAnimationControllerSerializer<?> getSerializer();

    protected static <B extends PackAnimationController> RecordCodecBuilder<B, PackAnimationControllerProperties> propertiesCodec() {
        return PackAnimationControllerProperties.CODEC.forGetter(c -> c.properties);
    }

    protected static List<String> resolveAnimations(List<String> references, Map<String, List<Identifier>> animations) {
        if (references.stream().noneMatch(reference -> reference.startsWith("#"))) {
            return references;
        }

        List<String> resolved = new ArrayList<>();

        for (String reference : references) {
            if (reference.startsWith("#")) {
                String key = reference.substring(1);
                List<Identifier> ids = animations.get(key);

                if (ids == null) {
                    AddonPackLog.error("Animation key '#" + key + "' not found in animations map");
                    continue;
                }

                ids.forEach(id -> resolved.add(id.toString()));
            } else {
                resolved.add(reference);
            }
        }

        if (resolved.isEmpty()) {
            resolved.add(PalladiumAnimationManager.EMPTY_ANIMATION.toString());
        }

        return resolved;
    }

    public record State(List<String> animations, Optional<Integer> blendTransition, List<Transition> transitions) {

        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.compactListCodec(Codec.STRING).optionalFieldOf("animation", List.of(PalladiumAnimationManager.EMPTY_ANIMATION.toString())).forGetter(State::animations),
                PalladiumCodecs.TIME.optionalFieldOf("blend_transition").forGetter(State::blendTransition),
                Transition.LIST_CODEC.optionalFieldOf("transitions", Collections.emptyList()).forGetter(State::transitions)
        ).apply(instance, State::new));

        public String pickAnimation(RandomSource random) {
            if (this.animations.isEmpty()) {
                return PalladiumAnimationManager.EMPTY_ANIMATION.toString();
            }

            if (this.animations.size() == 1) {
                return this.animations.getFirst();
            }

            return this.animations.get(random.nextInt(this.animations.size()));
        }

        public State resolve(Map<String, List<Identifier>> animations) {
            List<String> resolved = resolveAnimations(this.animations, animations);
            return resolved == this.animations ? this : new State(resolved, this.blendTransition, this.transitions);
        }

    }

    public record Transition(String state, Condition condition) {

        public static final Codec<List<Transition>> LIST_CODEC = Codec.unboundedMap(Codec.STRING, Condition.CODEC)
                .xmap(map -> map.entrySet().stream().map(e -> new Transition(e.getKey(), e.getValue())).toList(),
                        transitions -> transitions.stream().collect(Collectors.toMap(Transition::state, Transition::condition)));

    }

    public static class Codecs {

        public static final Codec<PackAnimationController> DIRECT_CODEC = PackAnimationControllerSerializer.TYPE_CODEC.dispatch(PackAnimationController::getSerializer, PackAnimationControllerSerializer::codec);

        public static final Codec<PackAnimationController> SIMPLE_CODEC = NeoForgeExtraCodecs.<PackAnimationControllerSerializer<?>, PackAnimationController, DefaultPackAnimationController>dispatchMapOrElse(
                PackAnimationControllerSerializer.TYPE_CODEC, PackAnimationController::getSerializer, PackAnimationControllerSerializer::codec, DefaultPackAnimationController.CODEC
        ).codec().xmap(
                either -> either.map(Function.identity(), Function.identity()),
                controller -> controller instanceof DefaultPackAnimationController defaultController ? Either.right(defaultController) : Either.left(controller)
        );

    }
}
