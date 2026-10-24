package com.star_1mp.powersunchained.network;

import com.star_1mp.powersunchained.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: web swinging. {@code action}: {@link #SHOOT} a rope, {@link #RELEASE} (attack key), {@link #DROP}. */
public record WebSwingPayload(int action) implements CustomPacketPayload {

    public static final int SHOOT = 0;
    public static final int RELEASE = 1;
    public static final int DROP = 2;

    public static final Type<WebSwingPayload> TYPE = new Type<>(Constants.id("web_swing"));
    public static final StreamCodec<ByteBuf, WebSwingPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(WebSwingPayload::new, WebSwingPayload::action);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
