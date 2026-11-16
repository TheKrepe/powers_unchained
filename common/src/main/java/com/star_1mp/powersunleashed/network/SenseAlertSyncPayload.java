package com.star_1mp.powersunleashed.network;

import com.star_1mp.powersunleashed.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> clients: an entity's Spider-Sense went off (the bolts over its head). */
public record SenseAlertSyncPayload(int entityId) implements CustomPacketPayload {

    public static final Type<SenseAlertSyncPayload> TYPE = new Type<>(Constants.id("sense_alert_sync"));
    public static final StreamCodec<ByteBuf, SenseAlertSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SenseAlertSyncPayload::entityId, SenseAlertSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
