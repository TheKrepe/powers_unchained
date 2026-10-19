package lucraft.mods.lucraftcore.utilities.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.utilities.recipes.InstructionRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSyncInstructionRecipe implements IMessage {

    public InstructionRecipe recipe;

    public MessageSyncInstructionRecipe() {

    }

    public MessageSyncInstructionRecipe(InstructionRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ResourceLocation loc = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        ItemStack output = ByteBufUtils.readItemStack(buf);
        int amount = buf.readInt();
        ItemStack[] requirements = new ItemStack[amount];
        for (int i = 0; i < amount; i++) {
            requirements[i] = ByteBufUtils.readItemStack(buf);
        }
        this.recipe = new InstructionRecipe(output, requirements).setRegistryName(loc);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, recipe.getRegistryName().toString());
        ByteBufUtils.writeItemStack(buf, recipe.getOutput());
        buf.writeInt(recipe.getRequirements().size());
        for (int i = 0; i < recipe.getRequirements().size(); i++) {
            ByteBufUtils.writeItemStack(buf, recipe.getRequirements().get(i));
        }
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncInstructionRecipe> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncInstructionRecipe message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    InstructionRecipe.getInstructionRecipesMap().put(message.recipe.getRegistryName(), message.recipe);
                }

            });

            return null;
        }

    }

}
