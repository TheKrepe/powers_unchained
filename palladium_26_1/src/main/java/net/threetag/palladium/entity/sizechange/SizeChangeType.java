package net.threetag.palladium.entity.sizechange;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.List;
import java.util.Optional;

public record SizeChangeType(int duration, float minSize, float maxSize, float speedScaling, float jumpScaling,
                             float gravityScaling, float damageScaling, boolean destructive, Easing easing,
                             Optional<Value> shrinkSound, Optional<Value> enlargeSound, List<Identifier> trails) {

    public static final Codec<SizeChangeType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PalladiumCodecs.TIME.optionalFieldOf("duration", 20).forGetter(SizeChangeType::duration),
            Codec.FLOAT.optionalFieldOf("min_size", 0.0625F).forGetter(SizeChangeType::minSize),
            Codec.FLOAT.optionalFieldOf("max_size", 16F).forGetter(SizeChangeType::maxSize),
            Codec.FLOAT.optionalFieldOf("speed_scaling", 1F).forGetter(SizeChangeType::speedScaling),
            Codec.FLOAT.optionalFieldOf("jump_scaling", 1F).forGetter(SizeChangeType::jumpScaling),
            Codec.FLOAT.optionalFieldOf("gravity_scaling", 1F).forGetter(SizeChangeType::gravityScaling),
            Codec.FLOAT.optionalFieldOf("damage_scaling", 1F).forGetter(SizeChangeType::damageScaling),
            Codec.BOOL.optionalFieldOf("destructive", false).forGetter(SizeChangeType::destructive),
            Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(SizeChangeType::easing),
            Value.CODEC.optionalFieldOf("shrink_sound").forGetter(SizeChangeType::shrinkSound),
            Value.CODEC.optionalFieldOf("enlarge_sound").forGetter(SizeChangeType::enlargeSound),
            ExtraCodecs.compactListCodec(Identifier.CODEC).optionalFieldOf("trails", List.of()).forGetter(SizeChangeType::trails)
    ).apply(instance, SizeChangeType::new));

    public float clamp(float size) {
        return Math.clamp(size, this.minSize, this.maxSize);
    }

    public float speedContribution(float sizeContribution) {
        return 1F + (sizeContribution - 1F) * this.speedScaling;
    }

    public float jumpContribution(float sizeContribution) {
        return 1F + (sizeContribution - 1F) * this.jumpScaling;
    }

    public float gravityContribution(float sizeContribution) {
        return 1F + (sizeContribution - 1F) * this.gravityScaling;
    }

    public float damageContribution(float sizeContribution) {
        return 1F + (sizeContribution - 1F) * this.damageScaling;
    }
}
