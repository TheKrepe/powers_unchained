package net.threetag.palladium.logic.value.holder;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;

public class FloatValueHolder {

    public static final FloatValueHolder ZERO = new FloatValueHolder(0F);
    public static final FloatValueHolder ONE = new FloatValueHolder(1F);
    public static final FloatValueHolder FOUR = new FloatValueHolder(4F);

    public static final Codec<FloatValueHolder> CODEC = codec(Codec.FLOAT);
    public static final StreamCodec<RegistryFriendlyByteBuf, FloatValueHolder> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static Codec<FloatValueHolder> codec(Codec<Float> rawCodec) {
        return Codec.either(rawCodec, Value.CODEC)
                .xmap(either -> either.map(
                        FloatValueHolder::new,
                        FloatValueHolder::new
                ), holder -> holder.value != null ? Either.right(holder.value) : Either.left(holder.raw));
    }

    private final float raw;
    private final Value value;

    public FloatValueHolder(float raw) {
        this.raw = raw;
        this.value = null;
    }

    public FloatValueHolder(Value value) {
        this.raw = 0F;
        this.value = value;
    }

    public float get(DataContext context) {
        if (this.value != null) {
            return this.value.getAsFloat(context);
        } else {
            return this.raw;
        }
    }

    public float get(DataContext context, float fallback) {
        if (this.value != null) {
            return this.value.getAsFloat(context, fallback);
        } else {
            return this.raw;
        }
    }

    public boolean isDynamic() {
        return this.value != null;
    }

    public float getRaw() {
        return this.raw;
    }
}
