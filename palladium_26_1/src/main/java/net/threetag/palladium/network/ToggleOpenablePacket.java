package net.threetag.palladium.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.entity.PlayerSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ToggleOpenablePacket() implements CustomPacketPayload {

    public static final Type<ToggleOpenablePacket> TYPE = new Type<>(Palladium.id("toggle_openable"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleOpenablePacket> STREAM_CODEC = StreamCodec.unit(new ToggleOpenablePacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleOpenablePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();

            List<ItemStack> openableStacks = new ArrayList<>();
            for (PlayerSlot slot : PlayerSlot.values(player.level())) {
                for (ItemStack stack : slot.getItems(player)) {
                    if (!stack.isEmpty() && stack.has(PalladiumDataComponents.Items.OPENABLE)) {
                        openableStacks.add(stack);
                    }
                }
            }

            if (openableStacks.isEmpty()) {
                return;
            }

            boolean allClosed = openableStacks.stream().noneMatch(stack ->
                    stack.getOrDefault(PalladiumDataComponents.Items.OPENED, false));

            for (ItemStack stack : openableStacks) {
                var openable = Objects.requireNonNull(stack.get(PalladiumDataComponents.Items.OPENABLE));
                boolean currentlyOpen = stack.getOrDefault(PalladiumDataComponents.Items.OPENED, false);
                if (currentlyOpen != allClosed) {
                    openable.setOpened(player, stack, allClosed);
                }
            }
        });
    }
}
