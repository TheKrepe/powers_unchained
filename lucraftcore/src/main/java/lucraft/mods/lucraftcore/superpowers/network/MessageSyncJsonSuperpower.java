package lucraft.mods.lucraftcore.superpowers.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.superpowers.JsonSuperpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSyncJsonSuperpower implements IMessage {

    public JsonSuperpower superpower;
    public JsonObject json;

    public MessageSyncJsonSuperpower() {
    }

    public MessageSyncJsonSuperpower(JsonSuperpower superpower, JsonObject json) {
        this.superpower = superpower;
        this.json = json;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.superpower = (JsonSuperpower) ByteBufUtils.readRegistryEntry(buf, SuperpowerHandler.SUPERPOWER_REGISTRY);
        this.json = (new JsonParser()).parse(ByteBufUtils.readUTF8String(buf)).getAsJsonObject();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, superpower);
        ByteBufUtils.writeUTF8String(buf, json.toString());
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncJsonSuperpower> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncJsonSuperpower message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    try {
                        message.superpower.deserialize(message.json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });

            return null;
        }

    }

}
