package lucraft.mods.lucraftcore.utilities.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.utilities.recipes.InstructionRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageClearInstructionRecipes implements IMessage {

    public MessageClearInstructionRecipes() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler extends AbstractClientMessageHandler<MessageClearInstructionRecipes> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageClearInstructionRecipes message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    InstructionRecipe.getInstructionRecipesMap().clear();
                }

            });

            return null;
        }

    }

}
