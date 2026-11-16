package com.star_1mp.powersunleashed.network;

import com.star_1mp.powersunleashed.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> clients: a player turned upside down on its web (or back). */
public record WebRappelSyncPayload(int entityId, boolean on) implements CustomPacketPayload {

    public static final Type<WebRappelSyncPayload> TYPE = new Type<>(Constants.id("web_rappel_sync"));
    public static final StreamCodec<ByteBuf, WebRappelSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WebRappelSyncPayload::entityId, ByteBufCodecs.BOOL, WebRappelSyncPayload::on, WebRappelSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
