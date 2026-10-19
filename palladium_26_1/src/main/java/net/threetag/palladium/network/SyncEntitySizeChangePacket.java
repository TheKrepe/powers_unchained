package net.threetag.palladium.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.sizechange.EntitySizeChangeHandler;
import net.threetag.palladium.entity.sizechange.SizeChangeType;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public record SyncEntitySizeChangePacket(
        int entityId,
        float baseSize,
        Map<ResourceKey<SizeChangeType>, EntitySizeChangeHandler.ModifierEntry> modifiers
) implements CustomPacketPayload {

    public static final Type<SyncEntitySizeChangePacket> TYPE = new Type<>(Palladium.id("sync_entity_size_change"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntitySizeChangePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.entityId);
                buf.writeFloat(packet.baseSize);
                buf.writeVarInt(packet.modifiers.size());
                packet.modifiers.forEach((key, entry) -> {
                    buf.writeUtf(key.identifier().toString());
                    buf.writeFloat(entry.getCurrentValue());
                    buf.writeFloat(entry.getStartValue());
                    buf.writeFloat(entry.getTargetValue());
                    buf.writeVarInt(entry.getTransitionTicks());
                    buf.writeVarInt(entry.getMaxTransitionTicks());
                    buf.writeBoolean(entry.isRemoving());
                });
            },
            buf -> {
                int entityId = buf.readVarInt();
                float baseSize = buf.readFloat();
                int count = buf.readVarInt();
                Map<ResourceKey<SizeChangeType>, EntitySizeChangeHandler.ModifierEntry> modifiers = new LinkedHashMap<>();
                for (int i = 0; i < count; i++) {
                    var key = ResourceKey.create(PalladiumRegistryKeys.SIZE_CHANGE_TYPE, Identifier.parse(buf.readUtf()));
                    float currentValue = buf.readFloat();
                    float startValue = buf.readFloat();
                    float targetValue = buf.readFloat();
                    int transitionTicks = buf.readVarInt();
                    int maxTransitionTicks = buf.readVarInt();
                    boolean removing = buf.readBoolean();
                    modifiers.put(key, new EntitySizeChangeHandler.ModifierEntry(currentValue, startValue, targetValue,
                            transitionTicks, maxTransitionTicks, removing));
                }
                return new SyncEntitySizeChangePacket(entityId, baseSize, modifiers);
            }
    );

    @Override
    public @NotNull Type<SyncEntitySizeChangePacket> type() {
        return TYPE;
    }

    public static void handle(SyncEntitySizeChangePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.entityId);
            if (entity instanceof LivingEntity livingEntity) {
                var sizeData = PalladiumEntityData.get(livingEntity, PalladiumEntityDataTypes.SIZE_CHANGE.get());
                if (sizeData != null) {
                    sizeData.setFromPacket(packet.baseSize, packet.modifiers);
                }
            }
        });
    }

    public static SyncEntitySizeChangePacket create(LivingEntity entity) {
        var sizeData = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        if (sizeData != null) {
            return new SyncEntitySizeChangePacket(entity.getId(), sizeData.getBaseSize(), sizeData.getModifiers());
        }
        return new SyncEntitySizeChangePacket(entity.getId(), 1F, Map.of());
    }
}
