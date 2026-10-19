package net.threetag.palladium.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;

public record PlayOpenableAnimationPacket(int entityId, Identifier animation) implements CustomPacketPayload {

    public static final Type<PlayOpenableAnimationPacket> TYPE = new Type<>(Palladium.id("play_openable_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayOpenableAnimationPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayOpenableAnimationPacket::entityId,
            Identifier.STREAM_CODEC, PlayOpenableAnimationPacket::animation,
            PlayOpenableAnimationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayOpenableAnimationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.entityId) instanceof LivingEntity living) {
                Palladium.PROXY.playOpenableAnimation(living, packet.animation);
            }
        });
    }
}
