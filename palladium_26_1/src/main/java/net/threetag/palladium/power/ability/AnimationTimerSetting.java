package net.threetag.palladium.power.ability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.PalladiumCodecs;

public record AnimationTimerSetting(int min, int max, Behaviour behaviour, Easing easing) {

    private static final Codec<AnimationTimerSetting> DIRECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PalladiumCodecs.TIME.optionalFieldOf("min", 0).forGetter(AnimationTimerSetting::min),
                    PalladiumCodecs.TIME.fieldOf("max").forGetter(AnimationTimerSetting::max),
                    Behaviour.CODEC.optionalFieldOf("behaviour", Behaviour.FOLLOW).forGetter(AnimationTimerSetting::behaviour),
                    Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(AnimationTimerSetting::easing)
            ).apply(instance, AnimationTimerSetting::new));

    public static final Codec<AnimationTimerSetting> CODEC = Codec.either(DIRECT_CODEC, Codec.INT).xmap(
            either -> either.map(
                    left -> left,
                    right -> new AnimationTimerSetting(0, right, Behaviour.FOLLOW, Easing.LINEAR)
            ),
            setting -> (setting.min == 0 && setting.behaviour == Behaviour.FOLLOW && setting.easing == Easing.LINEAR) ? Either.right(setting.max) : Either.left(setting)
    );

    public AnimationTimer create() {
        return new AnimationTimer(this, this.min);
    }

    public enum Behaviour implements StringRepresentable {

        FOLLOW("follow"),
        LOOP("once"),
        RESET("reset");

        public static final Codec<Behaviour> CODEC = StringRepresentable.fromEnum(Behaviour::values);
        public static final StreamCodec<ByteBuf, Behaviour> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> Behaviour.values()[i], Enum::ordinal);

        private final String name;

        Behaviour(String name) {
            this.name = name;
        }


        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

}
