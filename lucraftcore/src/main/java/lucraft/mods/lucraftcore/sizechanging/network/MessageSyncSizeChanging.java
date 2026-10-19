package lucraft.mods.lucraftcore.sizechanging.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSyncSizeChanging implements IMessage {

    public int entityId;
    public NBTTagCompound nbt;

    public MessageSyncSizeChanging() {
    }

    public MessageSyncSizeChanging(Entity entity) {
        this.entityId = entity.getEntityId();

        nbt = (NBTTagCompound) CapabilitySizeChanging.SIZE_CHANGING_CAP.getStorage().writeNBT(CapabilitySizeChanging.SIZE_CHANGING_CAP, entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null), null);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncSizeChanging> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncSizeChanging message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    if (message != null && ctx != null) {
                        Entity en = LucraftCore.proxy.getPlayerEntity(ctx).world.getEntityByID(message.entityId);

                        if (en != null) {
                            CapabilitySizeChanging.SIZE_CHANGING_CAP.getStorage().readNBT(CapabilitySizeChanging.SIZE_CHANGING_CAP, en.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null), null, message.nbt);
                        }
                    }
                }

            });

            return null;
        }

    }

}
