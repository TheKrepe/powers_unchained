package lucraft.mods.lucraftcore.extendedinventory.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MessageSyncExtendedInventory implements IMessage {

    public UUID playerUUID;
    public NBTTagCompound nbt;

    public MessageSyncExtendedInventory() {
    }

    public MessageSyncExtendedInventory(EntityPlayer player) {
        this.playerUUID = player.getPersistentID();

        nbt = (NBTTagCompound) CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP.getStorage().writeNBT(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null), null);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncExtendedInventory> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncExtendedInventory message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    if (message != null && ctx != null) {
                        EntityPlayer en = LucraftCore.proxy.getPlayerEntity(ctx).world.getPlayerEntityByUUID(message.playerUUID);

                        if (en != null) {
                            CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP.getStorage().readNBT(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, en.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null), null, message.nbt);
                        }
                    }
                }

            });

            return null;
        }

    }

}
