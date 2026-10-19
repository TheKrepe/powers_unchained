package lucraft.mods.lucraftcore.utilities.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.utilities.jei.JEIInfoReader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSyncJEIInfo implements IMessage {

    public ResourceLocation loc;
    public JEIInfoReader.JEIInfo info;

    public MessageSyncJEIInfo() {

    }

    public MessageSyncJEIInfo(ResourceLocation loc, JEIInfoReader.JEIInfo info) {
        this.loc = loc;
        this.info = info;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.loc = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        ITextComponent text = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
        ItemStack[] stacks = new ItemStack[buf.readInt()];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = ByteBufUtils.readItemStack(buf);
        }
        this.info = new JEIInfoReader.JEIInfo(text, stacks);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.loc.toString());
        ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(this.info.getText()));
        buf.writeInt(this.info.getItemStacks().length);
        for (int i = 0; i < this.info.getItemStacks().length; i++) {
            ByteBufUtils.writeItemStack(buf, this.info.getItemStacks()[i]);
        }
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncJEIInfo> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncJEIInfo message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    JEIInfoReader.getInfo().put(message.loc, message.info);
                }

            });

            return null;
        }

    }

}
