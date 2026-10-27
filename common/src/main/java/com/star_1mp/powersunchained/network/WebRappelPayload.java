package com.star_1mp.powersunchained.network;

import com.star_1mp.powersunchained.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: the player turned upside down on its web (or back); server -> watchers with its entity id. */
public record WebRappelPayload(int entityId, boolean on) implements CustomPacketPayload {

    public static final Type<WebRappelPayload> TYPE = new Type<>(Constants.id("web_rappel"));
    public static final StreamCodec<ByteBuf, WebRappelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WebRappelPayload::entityId, ByteBufCodecs.BOOL, WebRappelPayload::on, WebRappelPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
