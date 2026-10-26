package com.star_1mp.powersunchained.network;

import com.star_1mp.powersunchained.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Spider-Sense went off: client -> server for the local player (entity id unused), server -> everyone watching it
 * with its entity id (the bolts over its head).
 */
public record SenseAlertPayload(int entityId) implements CustomPacketPayload {

    public static final Type<SenseAlertPayload> TYPE = new Type<>(Constants.id("sense_alert"));
    public static final StreamCodec<ByteBuf, SenseAlertPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SenseAlertPayload::entityId, SenseAlertPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
