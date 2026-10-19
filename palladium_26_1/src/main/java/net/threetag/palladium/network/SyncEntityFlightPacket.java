package net.threetag.palladium.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.flight.EntityFlightHandler;

public record SyncEntityFlightPacket(int entityId, boolean flying, int phase) implements CustomPacketPayload {

    public static final Type<SyncEntityFlightPacket> TYPE = new Type<>(Palladium.id("sync_entity_flight"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityFlightPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncEntityFlightPacket::entityId,
            ByteBufCodecs.BOOL, SyncEntityFlightPacket::flying,
            ByteBufCodecs.VAR_INT, SyncEntityFlightPacket::phase,
            SyncEntityFlightPacket::new
    );

    public static SyncEntityFlightPacket create(LivingEntity entity) {
        var handler = EntityFlightHandler.get(entity);
        int phase = handler.getController() != null ? handler.getController().getPhaseId() : 0;
        return new SyncEntityFlightPacket(entity.getId(), handler.isFlying(), phase);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncEntityFlightPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var entity = context.player().level().getEntity(packet.entityId);

            if (!(entity instanceof LivingEntity living)) {
                return;
            }

            var flightHandler = EntityFlightHandler.get(living);

            if (living == context.player()) {
                if (!packet.flying) {
                    flightHandler.forceStopFlight();
                }

                return;
            }

            if (packet.flying) {
                if (!flightHandler.isFlying()) {
                    flightHandler.startFlight();
                }

                if (packet.phase > 0) {
                    flightHandler.setSyncedPhase(packet.phase);
                }
            } else {
                flightHandler.forceStopFlight();
            }
        });
    }
}
