package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityGrabEntity;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSendInfoToServer implements IMessage {

    public ServerMessageType type = ServerMessageType.NONE;
    public int data = 0;

    public MessageSendInfoToServer() {
    }

    public MessageSendInfoToServer(ServerMessageType type) {
        this(type, 0);
    }

    public MessageSendInfoToServer(ServerMessageType type, int data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = ServerMessageType.values()[buf.readInt()];
        this.data = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.type.ordinal());
        buf.writeInt(this.data);
    }

    public static class Handler extends AbstractServerMessageHandler<MessageSendInfoToServer> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageSendInfoToServer message, MessageContext ctx) {

            HeroesExpansion.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                switch (message.type) {
                    case THROW_GRABBED_ENTITY:
                        for(AbilityGrabEntity abilityGrabEntity : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityGrabEntity.class))
                            abilityGrabEntity.throwEntity();
                        break;
                }

            });

            return null;
        }
    }

    public enum ServerMessageType {

        NONE, THROW_GRABBED_ENTITY

    }

}
