package net.threetag.palladium.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.entity.flight.SwingingFlightType;

import java.util.Optional;

public record SyncSwingAnchorPacket(Optional<Vec3> anchor, boolean rightArm, boolean blockAnchor) implements CustomPacketPayload {

    public static final Type<SyncSwingAnchorPacket> TYPE = new Type<>(Palladium.id("sync_swinging_anchor"));

    private static final StreamCodec<ByteBuf, Vec3> VEC3 = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSwingAnchorPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(VEC3), SyncSwingAnchorPacket::anchor,
            ByteBufCodecs.BOOL, SyncSwingAnchorPacket::rightArm,
            ByteBufCodecs.BOOL, SyncSwingAnchorPacket::blockAnchor,
            SyncSwingAnchorPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSwingAnchorPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                var flightHandler = EntityFlightHandler.get(player);

                if (flightHandler.getFlightType() instanceof SwingingFlightType swinging &&
                        flightHandler.getController() instanceof SwingingFlightType.Controller controller) {
                    controller.handleServerAnchor(player, packet.anchor.orElse(null), packet.rightArm, packet.blockAnchor, swinging);
                }
            }
        });
    }
}
