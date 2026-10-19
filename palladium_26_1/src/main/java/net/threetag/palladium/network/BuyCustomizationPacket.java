package net.threetag.palladium.network;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.customization.Customization;
import net.threetag.palladium.customization.EntityCustomizationHandler;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public record BuyCustomizationPacket(Holder<Customization> customization) implements CustomPacketPayload {

    public static final Type<BuyCustomizationPacket> TYPE = new Type<>(Palladium.id("buy_customization"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyCustomizationPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(PalladiumRegistryKeys.CUSTOMIZATION), BuyCustomizationPacket::customization,
            BuyCustomizationPacket::new
    );

    public static void handle(BuyCustomizationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var handler = EntityCustomizationHandler.get(context.player());
            var cost = packet.customization.value().getUnlockCost();

            if (!handler.isUnlocked(packet.customization) && cost != null && cost.hasEnoughCurrency(context.player())) {
                cost.consumeCurrency(context.player());
                handler.unlock(packet.customization);
            }

            handler.select(packet.customization);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
