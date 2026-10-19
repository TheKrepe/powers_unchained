package lucraft.mods.lucraftcore.util.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageSyncPotionEffects implements IMessage {

    private int entityId;
    private NBTTagCompound compound;

    public MessageSyncPotionEffects() {

    }

    public MessageSyncPotionEffects(EntityLivingBase entity) {
        this.entityId = entity.getEntityId();
        this.compound = new NBTTagCompound();
        Collection<PotionEffect> effects = entity.getActivePotionEffects();
        compound.setInteger("Amount", effects.size());

        List<PotionEffect> list = new ArrayList<PotionEffect>();
        for (PotionEffect e : effects) {
            list.add(e);
        }

        for (int i = 0; i < list.size(); i++) {
            NBTTagCompound nbt = new NBTTagCompound();
            list.get(i).writeCustomPotionEffectToNBT(nbt);
            compound.setTag("" + i, nbt);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        ByteBufUtils.writeTag(buf, compound);
    }

    public static class Handler extends AbstractClientMessageHandler<MessageSyncPotionEffects> {

        @Override
        public IMessage handleClientMessage(EntityPlayer player, MessageSyncPotionEffects message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    Entity entity = player.world.getEntityByID(message.entityId);

                    if (entity != null && entity instanceof EntityLivingBase) {
                        int amount = message.compound.getInteger("Amount");
                        ((EntityLivingBase) entity).clearActivePotions();

                        for (int i = 0; i < amount; i++) {
                            PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT((NBTTagCompound) message.compound.getTag("" + i));
                            ((EntityLivingBase) entity).addPotionEffect(effect);
                        }
                    }
                }
            });

            return null;
        }

    }

}
