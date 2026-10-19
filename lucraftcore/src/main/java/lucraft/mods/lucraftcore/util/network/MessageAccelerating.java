package lucraft.mods.lucraftcore.util.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.util.entity.vehicle.EntityVehicle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAccelerating implements IMessage {

    private EntityVehicle.AccelerationDirection acceleration;

    public MessageAccelerating() {
    }

    public MessageAccelerating(EntityVehicle.AccelerationDirection acceleration) {
        this.acceleration = acceleration;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.acceleration = EntityVehicle.AccelerationDirection.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(acceleration.ordinal());
    }

    public static class Handler extends AbstractServerMessageHandler<MessageAccelerating> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageAccelerating message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                Entity riding = player.getRidingEntity();
                if (riding instanceof EntityVehicle) {
                    ((EntityVehicle) riding).setAcceleration(message.acceleration);
                }
            });
            return null;
        }
    }
}