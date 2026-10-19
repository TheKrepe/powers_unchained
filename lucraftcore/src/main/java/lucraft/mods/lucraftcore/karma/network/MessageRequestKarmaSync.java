package lucraft.mods.lucraftcore.karma.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRequestKarmaSync implements IMessage {

    public boolean openGui;

    public MessageRequestKarmaSync() {
    }

    public MessageRequestKarmaSync(boolean openGui) {
        this.openGui = openGui;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.openGui = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.openGui);
    }

    public static class Handler extends AbstractServerMessageHandler<MessageRequestKarmaSync> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageRequestKarmaSync message, MessageContext ctx) {
            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                if (message != null && ctx != null && player instanceof EntityPlayerMP) {
                    LCPacketDispatcher.sendTo(new MessageSyncKarma(player, message.openGui), (EntityPlayerMP) player);
                }
            });

            return null;
        }
    }

}
