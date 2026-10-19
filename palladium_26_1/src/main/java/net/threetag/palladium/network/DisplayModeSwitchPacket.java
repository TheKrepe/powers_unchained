package net.threetag.palladium.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.ModeSwitchAbility;

public record DisplayModeSwitchPacket(AbilityReference reference, String mode) implements CustomPacketPayload {

    public static final Type<DisplayModeSwitchPacket> TYPE = new Type<>(Palladium.id("display_mode_switch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DisplayModeSwitchPacket> STREAM_CODEC = StreamCodec.composite(
            AbilityReference.STREAM_CODEC, DisplayModeSwitchPacket::reference,
            ByteBufCodecs.STRING_UTF8, DisplayModeSwitchPacket::mode,
            DisplayModeSwitchPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DisplayModeSwitchPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var abilityInstance = packet.reference.getInstance(context.player());

            if (abilityInstance != null && abilityInstance.getAbility() instanceof ModeSwitchAbility ability) {
                var mode = ability.getMode(packet.mode);

                if (mode != null) {
                    Palladium.PROXY.displayModeSwitch(context.player(), mode);
                }
            }
        });
    }
}
