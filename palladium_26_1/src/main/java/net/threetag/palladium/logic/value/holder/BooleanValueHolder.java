package net.threetag.palladium.logic.value.holder;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;

public class BooleanValueHolder {

    public static final BooleanValueHolder TRUE = new BooleanValueHolder(true);
    public static final BooleanValueHolder FALSE = new BooleanValueHolder(false);

    public static final Codec<BooleanValueHolder> CODEC = Codec.either(Codec.BOOL, Value.CODEC)
            .xmap(either -> either.map(
                    BooleanValueHolder::new,
                    BooleanValueHolder::new
            ), holder -> holder.value != null ? Either.right(holder.value) : Either.left(holder.raw));

    private final boolean raw;
    private final Value value;

    public BooleanValueHolder(boolean raw) {
        this.raw = raw;
        this.value = null;
    }

    public BooleanValueHolder(Value value) {
        this.raw = false;
        this.value = value;
    }

    public boolean get(DataContext context) {
        if (this.value != null) {
            return this.value.getAsBoolean(context);
        } else {
            return this.raw;
        }
    }
}
