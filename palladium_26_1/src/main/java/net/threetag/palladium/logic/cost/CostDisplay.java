package net.threetag.palladium.logic.cost;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.icon.Icon;

public record CostDisplay(Icon icon, int amount, Component description) {

    public static final StreamCodec<RegistryFriendlyByteBuf, CostDisplay> STREAM_CODEC = StreamCodec.composite(
            Icon.STREAM_CODEC, CostDisplay::icon,
            ByteBufCodecs.INT, CostDisplay::amount,
            ComponentSerialization.STREAM_CODEC, CostDisplay::description,
            CostDisplay::new
    );

}
