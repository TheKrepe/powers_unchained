package net.threetag.palladium.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public record CycleMultiversalVariantPacket(int slotId, boolean playerInventory,
                                            ResourceKey<MultiversalItemVariant> variant) implements CustomPacketPayload {

    public static final Type<CycleMultiversalVariantPacket> TYPE = new Type<>(Palladium.id("cycle_multiversal_variant"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleMultiversalVariantPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CycleMultiversalVariantPacket::slotId,
            ByteBufCodecs.BOOL, CycleMultiversalVariantPacket::playerInventory,
            ResourceKey.streamCodec(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT), CycleMultiversalVariantPacket::variant,
            CycleMultiversalVariantPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CycleMultiversalVariantPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.hasInfiniteMaterials()) {
                return;
            }

            ItemStack stack = packet.getStack(player);
            if (stack == null) {
                return;
            }

            var category = stack.get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get());
            if (category == null) {
                return;
            }

            MultiversalItemVariant variant = MultiversalItemVariant.get(player.registryAccess(), packet.variant());
            if (variant == null || variant.forCategory(category) == null) {
                return;
            }

            MultiversalItemVariant.apply(player.registryAccess(), stack, packet.variant());

            if (packet.playerInventory()) {
                player.getInventory().setChanged();
            } else {
                player.containerMenu.getSlot(packet.slotId()).setChanged();
            }
        });
    }

    private ItemStack getStack(ServerPlayer player) {
        if (this.playerInventory) {
            var inventory = player.getInventory();
            return this.slotId < 0 || this.slotId >= inventory.getContainerSize() ? null : inventory.getItem(this.slotId);
        }

        AbstractContainerMenu menu = player.containerMenu;
        return this.slotId < 0 || this.slotId >= menu.slots.size() ? null : menu.getSlot(this.slotId).getItem();
    }
}
