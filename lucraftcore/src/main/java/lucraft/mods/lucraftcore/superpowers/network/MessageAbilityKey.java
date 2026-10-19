package lucraft.mods.lucraftcore.superpowers.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.superpowers.events.AbilityKeyEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAbilityKey implements IMessage {

    public boolean pressed;
    public String ability;
    public Ability.EnumAbilityContext context;

    public MessageAbilityKey() {
    }

    public MessageAbilityKey(boolean pressed, String ability, Ability.EnumAbilityContext context) {
        this.pressed = pressed;
        this.ability = ability;
        this.context = context;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pressed = buf.readBoolean();
        this.ability = ByteBufUtils.readUTF8String(buf);
        this.context = Ability.EnumAbilityContext.fromString(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.pressed);
        ByteBufUtils.writeUTF8String(buf, this.ability);
        ByteBufUtils.writeUTF8String(buf, this.context.toString());
    }

    public static class Handler extends AbstractServerMessageHandler<MessageAbilityKey> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageAbilityKey message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                if (message != null && ctx != null) {
                    AbilityContainer container = Ability.getAbilityContainer(message.context, player);
                    if (container == null)
                        return;
                    Ability ability = container.getAbility(message.ability);
                    if (ability == null || MinecraftForge.EVENT_BUS.post(new AbilityKeyEvent.Server(ability, player, message.pressed)))
                        return;

                    if (message.pressed) {
                        ability.onKeyPressed();
                    } else {
                        ability.onKeyReleased();
                    }
                }
            });

            return null;
        }

    }

}