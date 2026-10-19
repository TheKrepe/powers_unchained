package lucraft.mods.lucraftcore.utilities.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.utilities.jei.JEIInfoReader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageClearJEIInfo implements IMessage {

    public MessageClearJEIInfo() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler extends AbstractClientMessageHandler<MessageClearJEIInfo> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageClearJEIInfo message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    JEIInfoReader.getInfo().clear();
                }

            });

            return null;
        }

    }

}
