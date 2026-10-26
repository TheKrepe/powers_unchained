package com.star_1mp.powersunchained.network;

import com.star_1mp.powersunchained.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> clients: an entity clings to a wall / ceiling now (for its climbing pose). */
public record ClimbSyncPayload(int entityId, int mode, int direction) implements CustomPacketPayload {

    public static final Type<ClimbSyncPayload> TYPE = new Type<>(Constants.id("climb_sync"));
    public static final StreamCodec<ByteBuf, ClimbSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClimbSyncPayload::entityId, ByteBufCodecs.VAR_INT, ClimbSyncPayload::mode,
            ByteBufCodecs.VAR_INT, ClimbSyncPayload::direction, ClimbSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
