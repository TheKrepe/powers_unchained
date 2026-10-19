package lucraft.mods.lucraftcore.util.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.util.entity.vehicle.EntityVehicle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageTurn implements IMessage {

    private EntityVehicle.TurnDirection direction;

    public MessageTurn() {
    }

    public MessageTurn(EntityVehicle.TurnDirection direction) {
        this.direction = direction;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.direction = EntityVehicle.TurnDirection.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(direction.ordinal());
    }

    public static class Handler extends AbstractServerMessageHandler<MessageTurn> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageTurn message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                Entity riding = player.getRidingEntity();
                if (riding instanceof EntityVehicle) {
                    ((EntityVehicle) riding).setTurnDirection(message.direction);
                }
            });
            return null;
        }
    }
}