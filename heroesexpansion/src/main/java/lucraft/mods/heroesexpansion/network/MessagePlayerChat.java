package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePlayerChat implements IMessage {

    public String message;

    public MessagePlayerChat() {
    }

    public MessagePlayerChat(String message) {
        this.message = message;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.message = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.message);
    }

    public static class Handler extends AbstractClientMessageHandler<MessagePlayerChat> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessagePlayerChat message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {

                if (player instanceof EntityPlayerSP)
                    ((EntityPlayerSP) player).sendChatMessage(message.message);

            });

            return null;
        }

    }
}