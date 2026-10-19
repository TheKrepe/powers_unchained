package net.threetag.palladium.logic.value.holder;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;

public class DoubleValueHolder {

    public static final DoubleValueHolder ZERO = new DoubleValueHolder(0D);
    public static final DoubleValueHolder ONE = new DoubleValueHolder(1D);
    public static final DoubleValueHolder FIVE = new DoubleValueHolder(5D);

    public static final Codec<DoubleValueHolder> CODEC = Codec.either(Codec.DOUBLE, Value.CODEC)
            .xmap(either -> either.map(
                    DoubleValueHolder::new,
                    DoubleValueHolder::new
            ), holder -> holder.value != null ? Either.right(holder.value) : Either.left(holder.raw));

    private final double raw;
    private final Value value;

    public DoubleValueHolder(double raw) {
        this.raw = raw;
        this.value = null;
    }

    public DoubleValueHolder(Value value) {
        this.raw = 0D;
        this.value = value;
    }

    public double get(DataContext context) {
        if (this.value != null) {
            return this.value.getAsDouble(context);
        } else {
            return this.raw;
        }
    }
}
