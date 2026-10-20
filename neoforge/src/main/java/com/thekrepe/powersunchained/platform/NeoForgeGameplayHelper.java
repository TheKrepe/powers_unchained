package com.thekrepe.powersunchained.platform;

import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.compat.curios.CuriosCompat;
import com.thekrepe.powersunchained.platform.services.IGameplayHelper;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class NeoForgeGameplayHelper implements IGameplayHelper {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    /** Game time until which the entity is covered in sticky web. Synced to clients, not saved. */
    private static final Supplier<AttachmentType<Long>> WEBBED_UNTIL = ATTACHMENTS.register("webbed_until",
            () -> AttachmentType.builder(() -> 0L).sync(ByteBufCodecs.VAR_LONG).build());

    private static final boolean CURIOS = ModList.get().isLoaded("curios");

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }

    @Override
    public boolean isAccessoryEquipped(Player player, Item item) {
        if (CURIOS) {
            return CuriosCompat.isEquipped(player, item);
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getWebbedUntil(Entity entity) {
        return entity.getData(WEBBED_UNTIL.get());
    }

    @Override
    public void setWebbedUntil(Entity entity, long gameTime) {
        entity.setData(WEBBED_UNTIL.get(), gameTime);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
