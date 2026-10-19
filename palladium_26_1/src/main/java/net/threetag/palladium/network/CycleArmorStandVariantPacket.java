package net.threetag.palladium.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.List;

public record CycleArmorStandVariantPacket(int entityId,
                                           List<SlotVariant> updates) implements CustomPacketPayload {

    public static final Type<CycleArmorStandVariantPacket> TYPE = new Type<>(Palladium.id("cycle_armor_stand_variant"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleArmorStandVariantPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CycleArmorStandVariantPacket::entityId,
            SlotVariant.STREAM_CODEC.apply(ByteBufCodecs.list()), CycleArmorStandVariantPacket::updates,
            CycleArmorStandVariantPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CycleArmorStandVariantPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.hasInfiniteMaterials()) {
                return;
            }

            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof ArmorStand stand) || player.distanceToSqr(stand) > 64.0) {
                return;
            }

            for (SlotVariant update : packet.updates()) {
                ItemStack stack = stand.getItemBySlot(update.slot());
                var category = stack.get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get());
                if (category == null) {
                    continue;
                }

                MultiversalItemVariant variant = MultiversalItemVariant.get(player.registryAccess(), update.variant());
                if (variant == null || variant.forCategory(category) == null) {
                    continue;
                }

                MultiversalItemVariant.apply(player.registryAccess(), stack, update.variant());
            }
        });
    }

    public record SlotVariant(EquipmentSlot slot, ResourceKey<MultiversalItemVariant> variant) {

        public static final StreamCodec<RegistryFriendlyByteBuf, SlotVariant> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(EquipmentSlot.class), SlotVariant::slot,
                ResourceKey.streamCodec(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT), SlotVariant::variant,
                SlotVariant::new
        );
    }
}
