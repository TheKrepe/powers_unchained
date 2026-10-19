package net.threetag.palladium.client.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record PackAnimationControllerProperties(Optional<Identifier> parent, Map<String, List<Identifier>> animations, Optional<Integer> blendTransition) {

    public static final PackAnimationControllerProperties DEFAULT = new PackAnimationControllerProperties(Optional.empty(), Collections.emptyMap(), Optional.empty());

    public static final MapCodec<PackAnimationControllerProperties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("parent").forGetter(PackAnimationControllerProperties::parent),
            Codec.unboundedMap(Codec.STRING, ExtraCodecs.compactListCodec(Identifier.CODEC)).optionalFieldOf("animations", Collections.emptyMap()).forGetter(PackAnimationControllerProperties::animations),
            PalladiumCodecs.TIME.optionalFieldOf("blend_transition").forGetter(PackAnimationControllerProperties::blendTransition)
    ).apply(instance, PackAnimationControllerProperties::new));

    public PackAnimationControllerProperties merge(PackAnimationControllerProperties parent) {
        Map<String, List<Identifier>> animations = new HashMap<>(parent.animations);
        animations.putAll(this.animations);
        return new PackAnimationControllerProperties(Optional.empty(), animations, this.blendTransition.isPresent() ? this.blendTransition : parent.blendTransition);
    }

}
