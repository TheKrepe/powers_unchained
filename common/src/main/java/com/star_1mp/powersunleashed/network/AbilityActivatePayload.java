package com.star_1mp.powersunleashed.network;

import com.star_1mp.powersunleashed.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AbilityActivatePayload(String ability) implements CustomPacketPayload {

    public static final Type<AbilityActivatePayload> TYPE = new Type<>(Constants.id("ultimate_activate"));
    public static final StreamCodec<ByteBuf, AbilityActivatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128), AbilityActivatePayload::ability, AbilityActivatePayload::new);

    @Override
    public Type<AbilityActivatePayload> type() {
        return TYPE;
    }
}
