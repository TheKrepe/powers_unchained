package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.gui.GuiOpenPortal;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageSendInfoToClient implements IMessage {

    public ClientMessageType type = ClientMessageType.NONE;
    public int data = 0;

    public MessageSendInfoToClient() {
    }

    public MessageSendInfoToClient(ClientMessageType type) {
        this(type, 0);
    }

    public MessageSendInfoToClient(ClientMessageType type, int data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = ClientMessageType.values()[buf.readInt()];
        this.data = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.type.ordinal());
        buf.writeInt(this.data);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSendInfoToClient> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSendInfoToClient message, MessageContext ctx) {

            HeroesExpansion.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                switch (message.type) {
                    case PORTAL_GUI:
                        Minecraft.getMinecraft().displayGuiScreen(new GuiOpenPortal());
                        break;
                }

            });

            return null;
        }
    }

    public enum ClientMessageType {

        NONE, PORTAL_GUI

    }

}
