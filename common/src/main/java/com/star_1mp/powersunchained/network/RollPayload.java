package com.star_1mp.powersunchained.network;

import com.star_1mp.powersunchained.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Both ways: a roll ({@code kind} 0 = dodge roll on the ground, 1 = front flip in the air) towards {@code yaw}.
 * Client -> server when the local player rolls (entity id unused); server -> everyone watching with the roller's
 * entity id, for the animation.
 */
public record RollPayload(int entityId, int kind, float yaw) implements CustomPacketPayload {

    public static final Type<RollPayload> TYPE = new Type<>(Constants.id("roll"));
    public static final StreamCodec<ByteBuf, RollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RollPayload::entityId, ByteBufCodecs.VAR_INT, RollPayload::kind,
            ByteBufCodecs.FLOAT, RollPayload::yaw, RollPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
