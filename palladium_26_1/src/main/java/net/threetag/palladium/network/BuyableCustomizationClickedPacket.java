package net.threetag.palladium.network;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.customization.Customization;
import net.threetag.palladium.customization.EntityCustomizationHandler;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.NotNull;

public record BuyableCustomizationClickedPacket(Holder<Customization> customization) implements CustomPacketPayload {

    public static final Type<BuyableCustomizationClickedPacket> TYPE = new Type<>(Palladium.id("buyable_customization_clicked"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyableCustomizationClickedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(PalladiumRegistryKeys.CUSTOMIZATION), BuyableCustomizationClickedPacket::customization,
            BuyableCustomizationClickedPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BuyableCustomizationClickedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var cost = packet.customization.value().getUnlockCost();

            if (cost != null && context.player() instanceof ServerPlayer player && !EntityCustomizationHandler.get(player).isUnlocked(packet.customization)) {
                PacketDistributor.sendToPlayer(player, new OpenCustomizationBuyModalPacket(packet.customization, cost.createDisplay(), cost.hasEnoughCurrency(player)));
            }
        });
    }

}
