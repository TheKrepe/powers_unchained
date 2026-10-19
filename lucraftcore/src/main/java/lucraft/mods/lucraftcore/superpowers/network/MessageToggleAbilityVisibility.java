package lucraft.mods.lucraftcore.superpowers.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageToggleAbilityVisibility implements IMessage {

    public String ability;

    public MessageToggleAbilityVisibility() {
    }

    public MessageToggleAbilityVisibility(String ability) {
        this.ability = ability;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ability = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.ability);
    }

    public static class Handler extends AbstractServerMessageHandler<MessageToggleAbilityVisibility> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageToggleAbilityVisibility message, MessageContext ctx) {
            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                // TODO Ability visibility for ALL contexts
                Ability ability = Ability.getAbilityContainer(Ability.EnumAbilityContext.SUPERPOWER, player).getAbility(message.ability);

                if (ability != null) {
                    ability.setHidden(!ability.isHidden());
                }
            });
            return null;
        }

    }

}