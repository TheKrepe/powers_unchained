package net.threetag.palladium.logic.value.holder;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;

public class StringValueHolder {

    public static final Codec<StringValueHolder> CODEC = Codec.either(Codec.STRING, Value.CODEC)
            .xmap(either -> either.map(
                    StringValueHolder::new,
                    StringValueHolder::new
            ), holder -> holder.value != null ? Either.right(holder.value) : Either.left(holder.raw));

    private final String raw;
    private final Value value;

    public StringValueHolder(String raw) {
        this.raw = raw;
        this.value = null;
    }

    public StringValueHolder(Value value) {
        this.raw = "";
        this.value = value;
    }

    public String get(DataContext context) {
        if (this.value != null) {
            return this.value.getAsString(context);
        } else {
            return this.raw;
        }
    }
}
