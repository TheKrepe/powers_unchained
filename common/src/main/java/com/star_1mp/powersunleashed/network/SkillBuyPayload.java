package com.star_1mp.powersunleashed.network;

import com.star_1mp.powersunleashed.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: buy a skill-tree node of a power (powers menu, Skills tab). */
public record SkillBuyPayload(String power, String node) implements CustomPacketPayload {

    public static final Type<SkillBuyPayload> TYPE = new Type<>(Constants.id("skill_buy"));
    public static final StreamCodec<ByteBuf, SkillBuyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SkillBuyPayload::power, ByteBufCodecs.STRING_UTF8, SkillBuyPayload::node, SkillBuyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
