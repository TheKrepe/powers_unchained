package com.star_1mp.powersunchained.network;

import com.star_1mp.powersunchained.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: where the local player clings now ({@code WallClimbing} mode, wall direction 3d id). */
public record ClimbPayload(int mode, int direction) implements CustomPacketPayload {

    public static final Type<ClimbPayload> TYPE = new Type<>(Constants.id("climb"));
    public static final StreamCodec<ByteBuf, ClimbPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClimbPayload::mode, ByteBufCodecs.VAR_INT, ClimbPayload::direction, ClimbPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
