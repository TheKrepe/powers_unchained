package lucraft.mods.lucraftcore.superpowers.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MessageSyncSuperpower implements IMessage {

    public UUID entityUUID;
    public NBTTagCompound nbt;

    public MessageSyncSuperpower() {
    }

    public MessageSyncSuperpower(EntityLivingBase entity) {
        this.entityUUID = entity.getPersistentID();
        nbt = (NBTTagCompound) CapabilitySuperpower.SUPERPOWER_CAP.getStorage()
                .writeNBT(CapabilitySuperpower.SUPERPOWER_CAP, entity.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null), null);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.entityUUID.toString());
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncSuperpower> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncSuperpower message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                if (message != null && ctx != null) {
                    EntityLivingBase en = null;
                    for (Entity entity : LucraftCore.proxy.getPlayerEntity(ctx).world.getLoadedEntityList()) {
                        if (entity instanceof EntityLivingBase && entity.getPersistentID().equals(message.entityUUID))
                            en = (EntityLivingBase) entity;
                    }
                    if (en != null) {
                        CapabilitySuperpower.SUPERPOWER_CAP.getStorage().readNBT(CapabilitySuperpower.SUPERPOWER_CAP, en.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null), null, message.nbt);
                    }
                }
            });

            return null;
        }

    }

}
