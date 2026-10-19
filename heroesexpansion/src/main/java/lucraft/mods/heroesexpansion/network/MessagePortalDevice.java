package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.tileentities.TileEntityPortalDevice;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePortalDevice implements IMessage {

    public BlockPos pos;

    public MessagePortalDevice() {
    }

    public MessagePortalDevice(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
    }

    public static class Handler extends AbstractServerMessageHandler<MessagePortalDevice> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessagePortalDevice message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    if (player.world.getTileEntity(message.pos) != null && player.world.getTileEntity(message.pos) instanceof TileEntityPortalDevice) {
                        TileEntityPortalDevice tileEntity = (TileEntityPortalDevice) player.world.getTileEntity(message.pos);
                        tileEntity.opened = !tileEntity.opened;
                        tileEntity.markDirty();
                    }
                }

            });

            return null;
        }

    }
}