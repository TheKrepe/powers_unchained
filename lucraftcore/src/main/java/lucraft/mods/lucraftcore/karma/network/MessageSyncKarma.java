package lucraft.mods.lucraftcore.karma.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.karma.capabilities.CapabilityKarma;
import lucraft.mods.lucraftcore.karma.gui.GuiKarma;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MessageSyncKarma implements IMessage {

    public UUID playerUUID;
    public NBTTagCompound nbt;
    public boolean openGui;

    public MessageSyncKarma() {
    }

    public MessageSyncKarma(EntityPlayer player) {
        this.playerUUID = player.getPersistentID();

        this.nbt = (NBTTagCompound) CapabilityKarma.KARMA_CAP.getStorage().writeNBT(CapabilityKarma.KARMA_CAP, player.getCapability(CapabilityKarma.KARMA_CAP, null), null);

        this.openGui = false;
    }

    public MessageSyncKarma(EntityPlayer player, boolean openGui) {
        this(player);
        this.openGui = openGui;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        this.nbt = ByteBufUtils.readTag(buf);
        this.openGui = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
        ByteBufUtils.writeTag(buf, this.nbt);
        buf.writeBoolean(this.openGui);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncKarma> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncKarma message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    if (message != null && ctx != null) {
                        EntityPlayer en = LucraftCore.proxy.getPlayerEntity(ctx).world.getPlayerEntityByUUID(message.playerUUID);

                        if (en != null) {
                            CapabilityKarma.KARMA_CAP.getStorage().readNBT(CapabilityKarma.KARMA_CAP, en.getCapability(CapabilityKarma.KARMA_CAP, null), null, message.nbt);

                            if (message.openGui) {
                                Minecraft.getMinecraft().displayGuiScreen(new GuiKarma());
                            }
                        }
                    }
                }

            });

            return null;
        }

    }

}
