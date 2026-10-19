package lucraft.mods.lucraftcore.superpowers.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.superpowers.suitsets.AddonPackSuitSetReader;
import lucraft.mods.lucraftcore.superpowers.suitsets.ItemSuitSetArmor;
import lucraft.mods.lucraftcore.superpowers.suitsets.JsonSuitSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSyncJsonSuitSet implements IMessage {

    public JsonSuitSet suitset;
    public JsonObject json;

    public MessageSyncJsonSuitSet() {
    }

    public MessageSyncJsonSuitSet(JsonSuitSet suitset, JsonObject json) {
        this.suitset = suitset;
        this.json = json;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        String s = ByteBufUtils.readUTF8String(buf);
        for (JsonSuitSet jss : AddonPackSuitSetReader.SUIT_SETS) {
            if (jss.loc.equals(new ResourceLocation(s))) {
                this.suitset = jss;
                break;
            }
        }
        this.json = (new JsonParser()).parse(ByteBufUtils.readUTF8String(buf)).getAsJsonObject();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, suitset.loc.toString());
        ByteBufUtils.writeUTF8String(buf, json.toString());
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncJsonSuitSet> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncJsonSuitSet message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    try {
                        message.suitset.deserialize(message.json, message.suitset.loc);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    updateItemAttributes(message.suitset.getHelmet(), message.suitset);
                    updateItemAttributes(message.suitset.getChestplate(), message.suitset);
                    updateItemAttributes(message.suitset.getLegs(), message.suitset);
                    updateItemAttributes(message.suitset.getBoots(), message.suitset);
                }

                public void updateItemAttributes(Item item, JsonSuitSet suitset) {
                    if (item != null && item instanceof ItemSuitSetArmor) {
                        ((ItemSuitSetArmor) item).suitSet = suitset;
                    }
                }

            });

            return null;
        }

    }

}
