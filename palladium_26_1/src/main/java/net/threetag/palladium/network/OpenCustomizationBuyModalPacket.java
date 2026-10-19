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
import net.threetag.palladium.logic.cost.CostDisplay;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.NotNull;

public record OpenCustomizationBuyModalPacket(Holder<Customization> customization, CostDisplay costDisplay,
                                              boolean available) implements CustomPacketPayload {

    public static final Type<OpenCustomizationBuyModalPacket> TYPE = new Type<>(Palladium.id("open_customization_buy_modal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCustomizationBuyModalPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(PalladiumRegistryKeys.CUSTOMIZATION), OpenCustomizationBuyModalPacket::customization,
            CostDisplay.STREAM_CODEC, OpenCustomizationBuyModalPacket::costDisplay,
            ByteBufCodecs.BOOL, OpenCustomizationBuyModalPacket::available,
            OpenCustomizationBuyModalPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCustomizationBuyModalPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            var cost = packet.customization.value().getUnlockCost();

            if (cost != null && !EntityCustomizationHandler.get(player).isUnlocked(packet.customization)) {
                Palladium.PROXY.openCustomizationBuyScreen(packet.customization, packet.costDisplay, packet.available, context.player().registryAccess());
            }
        });
    }

}
