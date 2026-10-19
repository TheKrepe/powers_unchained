package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.items.ItemWebShooter;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChangeWebMode implements IMessage {

    public ResourceLocation loc;

    public MessageChangeWebMode() {

    }

    public MessageChangeWebMode(ResourceLocation loc) {
        this.loc = loc;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.loc = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.loc.toString());
    }

    public static class Handler extends AbstractServerMessageHandler<MessageChangeWebMode> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageChangeWebMode message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                ItemWebShooter.WebMode webMode = ItemWebShooter.WEB_MODES.getObject(message.loc);
                ItemStack stack = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory().getStackInSlot(InventoryExtendedInventory.SLOT_WRIST);

                if (webMode != null && !stack.isEmpty() && stack.getItem() instanceof ItemWebShooter) {
                    ItemWebShooter.setWebMode(stack, webMode);
                    player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).syncToAll();
                }
            });

            return null;
        }

    }
}
