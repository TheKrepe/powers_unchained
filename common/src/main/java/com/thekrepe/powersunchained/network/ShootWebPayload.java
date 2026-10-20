package com.thekrepe.powersunchained.network;

import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.web.WebType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: the player fires the web-shooters with the selected web type. */
public record ShootWebPayload(WebType webType) implements CustomPacketPayload {

    public static final Type<ShootWebPayload> TYPE = new Type<>(Constants.id("shoot_web"));
    public static final StreamCodec<ByteBuf, ShootWebPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
            id -> new ShootWebPayload(WebType.byId(id)), payload -> payload.webType().ordinal());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
