package net.threetag.palladium.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.entity.flight.SwingingFlightType;

import java.util.Optional;

public record SyncSwingAnchorStatePacket(int entityId, Optional<Vec3> anchor, boolean rightArm) implements CustomPacketPayload {

    public static final Type<SyncSwingAnchorStatePacket> TYPE = new Type<>(Palladium.id("sync_swinging_anchor_state"));

    private static final StreamCodec<ByteBuf, Vec3> VEC3 = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSwingAnchorStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncSwingAnchorStatePacket::entityId,
            ByteBufCodecs.optional(VEC3), SyncSwingAnchorStatePacket::anchor,
            ByteBufCodecs.BOOL, SyncSwingAnchorStatePacket::rightArm,
            SyncSwingAnchorStatePacket::new
    );

    public static Optional<SyncSwingAnchorStatePacket> create(LivingEntity entity) {
        if (EntityFlightHandler.get(entity).getController() instanceof SwingingFlightType.Controller controller) {
            return Optional.of(new SyncSwingAnchorStatePacket(entity.getId(), Optional.ofNullable(controller.getAnchor()), controller.rightArm));
        }

        return Optional.empty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSwingAnchorStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var entity = context.player().level().getEntity(packet.entityId);

            if (!(entity instanceof LivingEntity living) || living == context.player()) {
                return;
            }

            if (EntityFlightHandler.get(living).getController() instanceof SwingingFlightType.Controller controller) {
                controller.handleClientAnchor(packet.anchor.orElse(null), packet.rightArm);
            }
        });
    }
}
