package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.client.particles.ParticleKineticEnergy;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Random;

public class MessageKineticEnergyBlast implements IMessage {

    public double posX;
    public double posY;
    public double posZ;
    public int size;
    public double red;
    public double green;
    public double blue;

    public MessageKineticEnergyBlast() {
    }

    public MessageKineticEnergyBlast(double posX, double posY, double posZ, int size, double red, double green, double blue) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.size = size;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.size = buf.readInt();
        this.red = buf.readDouble();
        this.green = buf.readDouble();
        this.blue = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeInt(this.size);
        buf.writeDouble(this.red);
        buf.writeDouble(this.green);
        buf.writeDouble(this.blue);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageKineticEnergyBlast> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageKineticEnergyBlast message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    double d0 = message.posX;
                    double d1 = message.posY;
                    double d2 = message.posZ;
                    int size = message.size;
                    float speed = 0.5F;
                    Random rand = new Random();

                    for (int i = -size; i <= size; ++i) {
                        for (int j = -size; j <= size; ++j) {
                            for (int k = -size; k <= size; ++k) {
                                double d3 = (double) j + (rand.nextDouble() - rand.nextDouble()) * 0.5D;
                                double d4 = (double) i + (rand.nextDouble() - rand.nextDouble()) * 0.5D;
                                double d5 = (double) k + (rand.nextDouble() - rand.nextDouble()) * 0.5D;
                                double d6 = (double) MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5) / speed + rand.nextGaussian() * 0.05D;
                                Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(ParticleKineticEnergy.ID, d0, d1 + player.height / 2F, d2, (float) d3 / (float) d6, (float) d4 / (float) d6, (float) d5 / (float) d6, (int) (message.red * 255), (int) (message.green * 255), (int) (message.blue * 255));
                                if (i != -size && i != size && j != -size && j != size) {
                                    k += size * 2 - 1;
                                }
                            }
                        }
                    }
                }

            });

            return null;
        }

    }
}
