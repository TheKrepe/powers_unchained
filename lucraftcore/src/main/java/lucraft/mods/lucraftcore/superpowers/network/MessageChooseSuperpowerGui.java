package lucraft.mods.lucraftcore.superpowers.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.superpowers.gui.GuiChooseSuperpower;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChooseSuperpowerGui implements IMessage {

    public String[] superpowers = new String[0];

    public MessageChooseSuperpowerGui() {
    }

    public MessageChooseSuperpowerGui(String[] superpowers) {
        this.superpowers = superpowers;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.superpowers = new String[buf.readInt()];
        for (int i = 0; i < this.superpowers.length; i++)
            superpowers[i] = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(superpowers.length);
        for (int i = 0; i < superpowers.length; i++)
            ByteBufUtils.writeUTF8String(buf, superpowers[i]);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageChooseSuperpowerGui> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageChooseSuperpowerGui message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiChooseSuperpower(message.superpowers));
                }

            });

            return null;
        }

    }

}
