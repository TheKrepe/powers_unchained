package lucraft.mods.lucraftcore.extendedinventory.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.gui.GuiHandlerEntryExtendedInventory;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.util.gui.LCGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOpenExtendedInventory implements IMessage {

    public MessageOpenExtendedInventory() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler extends AbstractServerMessageHandler<MessageOpenExtendedInventory> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageOpenExtendedInventory message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    LCGuiHandler.openGui(player, GuiHandlerEntryExtendedInventory.ID);
                }

            });

            return null;
        }

    }

}
