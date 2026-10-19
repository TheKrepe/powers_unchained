package net.threetag.palladium.logic.value.holder;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;

public class IntValueHolder {

    public static final IntValueHolder MINUS_ONE = new IntValueHolder(-1);
    public static final IntValueHolder ZERO = new IntValueHolder(0);
    public static final IntValueHolder ONE = new IntValueHolder(1);
    public static final IntValueHolder _256 = new IntValueHolder(256);

    public static final Codec<IntValueHolder> CODEC = codec(Codec.INT);
    public static final StreamCodec<RegistryFriendlyByteBuf, IntValueHolder> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static Codec<IntValueHolder> codec(Codec<Integer> rawCodec) {
        return Codec.either(rawCodec, Value.CODEC)
                .xmap(either -> either.map(
                        IntValueHolder::new,
                        IntValueHolder::new
                ), holder -> holder.value != null ? Either.right(holder.value) : Either.left(holder.raw));
    }

    private final int raw;
    private final Value value;

    public IntValueHolder(int raw) {
        this.raw = raw;
        this.value = null;
    }

    public IntValueHolder(Value value) {
        this.raw = 0;
        this.value = value;
    }

    public int get(DataContext context) {
        if (this.value != null) {
            return this.value.getAsInt(context);
        } else {
            return this.raw;
        }
    }

    public int get(DataContext context, int fallback) {
        if (this.value != null) {
            return this.value.getAsInt(context, fallback);
        } else {
            return this.raw;
        }
    }

    public boolean isDynamic() {
        return this.value != null;
    }

    public int getRaw() {
        return this.raw;
    }
}
