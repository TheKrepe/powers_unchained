package lucraft.mods.lucraftcore.util.events;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerEmptyClickEvent extends PlayerEvent {

    private EnumHand hand;

    public PlayerEmptyClickEvent(EntityPlayer player, EnumHand hand) {
        super(player);
        this.hand = hand;
    }

    public EnumHand getHand() {
        return hand;
    }

    public static class LeftClick extends PlayerEmptyClickEvent {

        public LeftClick(EntityPlayer player, EnumHand hand) {
            super(player, hand);
        }

    }

    public static class RightClick extends PlayerEmptyClickEvent {

        public RightClick(EntityPlayer player, EnumHand hand) {
            super(player, hand);
        }

    }

    @EventBusSubscriber(modid = LucraftCore.MODID, value = Side.CLIENT)
    public static class EventHandler {

        @SubscribeEvent
        public static void leftClick(PlayerInteractEvent.LeftClickEmpty e) {
            if (e.getEntityPlayer() == Minecraft.getMinecraft().player)
                LCPacketDispatcher.sendToServer(new MessagePlayerEmptyClickEvent(false, e.getHand() == EnumHand.MAIN_HAND));
        }

        @SubscribeEvent
        public static void rightClick(PlayerInteractEvent.RightClickEmpty e) {
            if (e.getEntityPlayer() == Minecraft.getMinecraft().player)
                LCPacketDispatcher.sendToServer(new MessagePlayerEmptyClickEvent(true, e.getHand() == EnumHand.MAIN_HAND));
        }

    }

    public static class MessagePlayerEmptyClickEvent implements IMessage {

        public boolean right;
        public boolean mainHand;

        public MessagePlayerEmptyClickEvent() {
        }

        public MessagePlayerEmptyClickEvent(boolean right, boolean mainHand) {
            this.right = right;
            this.mainHand = mainHand;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.right = buf.readBoolean();
            this.mainHand = buf.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(right);
            buf.writeBoolean(mainHand);
        }

        public static class Handler extends AbstractServerMessageHandler<MessagePlayerEmptyClickEvent> {

            @Override
            public IMessage handleServerMessage(EntityPlayer player, MessagePlayerEmptyClickEvent message, MessageContext ctx) {

                LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                    @Override
                    public void run() {
                        if (message.right)
                            MinecraftForge.EVENT_BUS.post(new PlayerEmptyClickEvent.RightClick(player, message.mainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
                        else
                            MinecraftForge.EVENT_BUS.post(new PlayerEmptyClickEvent.LeftClick(player, message.mainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
                    }

                });

                return null;
            }

        }

    }

}