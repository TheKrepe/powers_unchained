package lucraft.mods.lucraftcore.superpowers.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChooseSuperpower implements IMessage {

    public String superpower = "";

    public MessageChooseSuperpower() {
    }

    public MessageChooseSuperpower(String superpower) {
        this.superpower = superpower;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.superpower = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, superpower);
    }

    public static class Handler extends AbstractServerMessageHandler<MessageChooseSuperpower> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageChooseSuperpower message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    if (!player.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null).hasPlayedBefore()) {
                        Superpower superpower = SuperpowerHandler.SUPERPOWER_REGISTRY.getValue(new ResourceLocation(message.superpower));

                        if (superpower != null)
                            SuperpowerHandler.giveSuperpower(player, superpower);
                    }

                    player.getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null).setHasPlayedBefore(true);
                }

            });

            return null;
        }

    }

}
