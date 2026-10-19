package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum AbilityColor implements StringRepresentable {

    WHITE,
    ORANGE,
    MAGENTA,
    LIGHT_BLUE,
    YELLOW,
    LIME,
    PINK,
    GRAY,
    LIGHT_GRAY,
    CYAN,
    PURPLE,
    BLUE,
    BROWN,
    GREEN,
    RED,
    BLACK;

    public static final Codec<AbilityColor> CODEC = StringRepresentable.fromEnum(AbilityColor::values);
    public static final StreamCodec<ByteBuf, AbilityColor> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> AbilityColor.values()[i], Enum::ordinal);

    public static AbilityColor getByName(String name) {
        for (AbilityColor type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
