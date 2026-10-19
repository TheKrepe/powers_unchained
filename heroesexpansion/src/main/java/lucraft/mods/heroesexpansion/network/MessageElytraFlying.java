package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.items.ItemSuitSetElytra;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageElytraFlying implements IMessage {

    public MessageElytraFlying() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler extends AbstractServerMessageHandler<MessageElytraFlying> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageElytraFlying message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {

                if (player instanceof EntityPlayerMP) {
                    EntityPlayerMP p = (EntityPlayerMP) player;
                    boolean b = false;
                    if (!player.onGround && !player.isRiding() && !player.isInWater() && !player.capabilities.isFlying) {
                        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                        if (stack.getItem() instanceof ItemSuitSetElytra && ((ItemSuitSetElytra) stack.getItem()).canUseElytra(player, stack)) {
                            b = true;
                        }
                    }

                    ItemSuitSetElytra.EventHandler.setFlying(p, b);
                }

            });

            return null;
        }

    }

}
