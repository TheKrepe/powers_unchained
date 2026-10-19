package lucraft.mods.lucraftcore.util.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSwingArm implements IMessage {

    public int entityId;
    public EnumHand hand;

    public MessageSwingArm() {
    }

    public MessageSwingArm(EntityPlayer player, EnumHand hand) {
        this.entityId = player.getEntityId();
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(hand == EnumHand.MAIN_HAND);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSwingArm> {

        @Override
        public IMessage handleClientMessage(final EntityPlayer player, final MessageSwingArm message, final MessageContext ctx) {

            EntityPlayer en = (EntityPlayer) LucraftCore.proxy.getPlayerEntity(ctx).world.getEntityByID(message.entityId);

            if (en != null)
                en.swingArm(message.hand);

            return null;
        }

    }

}
