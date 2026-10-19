package net.threetag.palladium.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum NumberOperation implements StringRepresentable {

    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%");

    public static final Codec<NumberOperation> CODEC = StringRepresentable.fromEnum(NumberOperation::values);
    public static final StreamCodec<ByteBuf, NumberOperation> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> NumberOperation.values()[i], Enum::ordinal);

    private final String symbol;

    NumberOperation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.symbol;
    }

    public Number apply(Number base, Number operand) {
        double result = switch (this) {
            case ADD -> base.doubleValue() + operand.doubleValue();
            case SUBTRACT -> base.doubleValue() - operand.doubleValue();
            case MULTIPLY -> base.doubleValue() * operand.doubleValue();
            case DIVIDE -> base.doubleValue() / operand.doubleValue();
            case MODULO -> base.doubleValue() % operand.doubleValue();
        };

        return switch (base) {
            case Integer _ -> (int) result;
            case Long _ -> (long) result;
            case Float _ -> (float) result;
            default -> result;
        };
    }
}
