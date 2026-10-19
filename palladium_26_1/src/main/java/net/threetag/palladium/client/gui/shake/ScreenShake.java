package net.threetag.palladium.client.gui.shake;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.PalladiumCodecs;

public record ScreenShake(ShakeIntensity intensity, float frequency, int fadeInDuration, int fadeOutDuration, EasingPair easing) {

    public static final Codec<ScreenShake> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShakeIntensity.CODEC.fieldOf("intensity").forGetter(ScreenShake::intensity),
            Codec.FLOAT.optionalFieldOf("frequency", 0.3F).forGetter(ScreenShake::frequency),
            PalladiumCodecs.TIME.optionalFieldOf("fade_in", 0).forGetter(ScreenShake::fadeInDuration),
            PalladiumCodecs.TIME.optionalFieldOf("fade_out", 10).forGetter(ScreenShake::fadeOutDuration),
            EasingPair.CODEC.optionalFieldOf("easing", new EasingPair(Easing.LINEAR, Easing.OUTSINE)).forGetter(ScreenShake::easing)
    ).apply(instance, ScreenShake::new));

    public record ShakeIntensity(float yaw, float pitch, float roll) {

        public static final Codec<ShakeIntensity> CODEC = Codec.withAlternative(
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.FLOAT.optionalFieldOf("yaw", 0F).forGetter(ShakeIntensity::yaw),
                        Codec.FLOAT.optionalFieldOf("pitch", 0F).forGetter(ShakeIntensity::pitch),
                        Codec.FLOAT.optionalFieldOf("roll", 0F).forGetter(ShakeIntensity::roll)
                ).apply(instance, ShakeIntensity::new)),
                Codec.FLOAT.xmap(v -> new ShakeIntensity(v, v, 0F), ShakeIntensity::yaw)
        );
    }

    public record EasingPair(Easing in, Easing out) {

        public static final Codec<EasingPair> CODEC = Codec.withAlternative(
                RecordCodecBuilder.create(instance -> instance.group(
                        Easing.CODEC.fieldOf("in").forGetter(EasingPair::in),
                        Easing.CODEC.fieldOf("out").forGetter(EasingPair::out)
                ).apply(instance, EasingPair::new)),
                Easing.CODEC.xmap(e -> new EasingPair(e, e), EasingPair::in)
        );
    }
}
