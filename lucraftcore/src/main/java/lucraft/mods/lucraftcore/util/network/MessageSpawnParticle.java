package lucraft.mods.lucraftcore.util.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageSpawnParticle implements IMessage {

    public int particleId;
    public double xCoord;
    public double yCoord;
    public double zCoord;
    public double xSpeed;
    public double ySpeed;
    public double zSpeed;
    public int[] parameters;

    public MessageSpawnParticle() {

    }

    public MessageSpawnParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        this.particleId = particleId;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.zSpeed = zSpeed;
        this.parameters = parameters;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.particleId = buf.readInt();
        this.xCoord = buf.readDouble();
        this.yCoord = buf.readDouble();
        this.zCoord = buf.readDouble();
        this.xSpeed = buf.readDouble();
        this.ySpeed = buf.readDouble();
        this.zSpeed = buf.readDouble();

        this.parameters = new int[buf.readInt()];
        for (int i = 0; i < parameters.length; i++) {
            this.parameters[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.particleId);
        buf.writeDouble(this.xCoord);
        buf.writeDouble(this.yCoord);
        buf.writeDouble(this.zCoord);
        buf.writeDouble(this.xSpeed);
        buf.writeDouble(this.ySpeed);
        buf.writeDouble(this.zSpeed);
        buf.writeInt(this.parameters == null ? 0 : this.parameters.length);

        if (this.parameters != null && this.parameters.length > 0) {
            for (int i = 0; i < this.parameters.length; i++) {
                buf.writeInt(this.parameters[i]);
            }
        }
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSpawnParticle> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSpawnParticle message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                LucraftCore.proxy.spawnParticle(message.particleId, message.xCoord, message.yCoord, message.zCoord, message.xSpeed, message.ySpeed, message.zSpeed, message.parameters);
            });

            return null;
        }
    }

}