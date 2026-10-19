package lucraft.mods.lucraftcore.superpowers.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MessageSyncAbilityContainer implements IMessage {

    public UUID playerUUID;
    public Ability.EnumAbilityContext context;
    public NBTTagCompound nbt;

    public MessageSyncAbilityContainer() {
    }

    public MessageSyncAbilityContainer(EntityLivingBase entity, AbilityContainer container) {
        this.playerUUID = entity.getPersistentID();
        this.context = container.context;
        this.nbt = container.serializeNBTSync();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        this.context = Ability.EnumAbilityContext.fromString(ByteBufUtils.readUTF8String(buf));
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
        ByteBufUtils.writeUTF8String(buf, this.context.toString());
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncAbilityContainer> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncAbilityContainer message, MessageContext ctx) {
            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                if (message != null && ctx != null) {
                    Entity en = LCEntityHelper.getEntityByUUID(LucraftCore.proxy.getPlayerEntity(ctx).world, message.playerUUID);
                    if (en != null && en instanceof EntityLivingBase && message.context != null) {
                        AbilityContainer container = Ability.getAbilityContainer(message.context, (EntityLivingBase) en);
                        if (container != null)
                            container.deserializeNBTSync(message.nbt);
                    }
                }
            });

            return null;
        }

    }

}