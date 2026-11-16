package com.star_1mp.powersunleashed.network;

import com.star_1mp.powersunleashed.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> clients: a player fired a web from the given wrist (for the arm animations of everyone watching,
 * including the shooter). Sent by the server so the animation never depends on the client catching the web
 * projectile in flight (after a pause, lag, or a web that hits right away).
 */
public record WebShotAnimPayload(int entityId, int side) implements CustomPacketPayload {

    public static final Type<WebShotAnimPayload> TYPE = new Type<>(Constants.id("web_shot_anim"));
    public static final StreamCodec<ByteBuf, WebShotAnimPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WebShotAnimPayload::entityId, ByteBufCodecs.VAR_INT, WebShotAnimPayload::side,
            WebShotAnimPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
