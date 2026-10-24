package com.star_1mp.powersunchained.network;

import com.star_1mp.powersunchained.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: the player uses Web Zip. */
public record WebZipPayload() implements CustomPacketPayload {

    public static final WebZipPayload INSTANCE = new WebZipPayload();
    public static final Type<WebZipPayload> TYPE = new Type<>(Constants.id("web_zip"));
    public static final StreamCodec<ByteBuf, WebZipPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
