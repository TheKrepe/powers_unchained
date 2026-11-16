package com.star_1mp.powersunleashed.network;

import com.star_1mp.powersunleashed.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: switch a superpower on / off (ability wheel). */
public record PowerTogglePayload(String power) implements CustomPacketPayload {

    public static final Type<PowerTogglePayload> TYPE = new Type<>(Constants.id("power_toggle"));
    public static final StreamCodec<ByteBuf, PowerTogglePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PowerTogglePayload::power, PowerTogglePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
