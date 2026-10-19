package lucraft.mods.lucraftcore.utilities.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityConstructionTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSetSelectedRecipe implements IMessage {

    public BlockPos pos = BlockPos.ORIGIN;
    public int selected = -1;

    public MessageSetSelectedRecipe() {
    }

    public MessageSetSelectedRecipe(BlockPos pos, int selected) {
        this.pos = pos;
        this.selected = selected;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.selected = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(selected);
    }

    public static class Handler extends AbstractServerMessageHandler<MessageSetSelectedRecipe> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageSetSelectedRecipe message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    TileEntity tileEntity = player.world.getTileEntity(message.pos);

                    if (tileEntity != null && tileEntity instanceof TileEntityConstructionTable) {
                        ((TileEntityConstructionTable) tileEntity).setSelectedRecipe(message.selected);
                    }
                }

            });

            return null;
        }

    }

}
