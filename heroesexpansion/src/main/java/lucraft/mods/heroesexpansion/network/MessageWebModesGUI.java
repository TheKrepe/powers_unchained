package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.client.gui.GuiWebModes;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageWebModesGUI implements IMessage {

    public int tier;

    public MessageWebModesGUI() {

    }

    public MessageWebModesGUI(int tier) {
        this.tier = tier;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.tier = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.tier);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageWebModesGUI> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageWebModesGUI message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {

                Minecraft.getMinecraft().displayGuiScreen(new GuiWebModes(message.tier));

            });

            return null;
        }

    }
}
