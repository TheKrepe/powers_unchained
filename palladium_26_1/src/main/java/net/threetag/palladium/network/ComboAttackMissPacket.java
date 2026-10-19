package net.threetag.palladium.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.power.ability.ComboAttackAbility;
import org.jetbrains.annotations.NotNull;

public record ComboAttackMissPacket() implements CustomPacketPayload {

    public static final Type<ComboAttackMissPacket> TYPE = new Type<>(Palladium.id("combo_attack_miss"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboAttackMissPacket> STREAM_CODEC = StreamCodec.unit(new ComboAttackMissPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ComboAttackMissPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var instance = ComboAttackAbility.getTopPriorityInstance(context.player());
            if (instance != null) {
                ComboAttackAbility.resetCombo(instance);
            }
        });
    }

}
