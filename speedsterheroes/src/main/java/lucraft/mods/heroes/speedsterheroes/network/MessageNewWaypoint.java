package lucraft.mods.heroes.speedsterheroes.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.gui.GuiIds;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.TeleportDestination;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageNewWaypoint implements IMessage {

	public NBTTagCompound td;
	
	public MessageNewWaypoint(TeleportDestination td) {
		this.td = td.serializeNBT();
	}
	
	public MessageNewWaypoint() {
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		td = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, td);
	}

	public static class Handler extends AbstractServerMessageHandler<MessageNewWaypoint> {

		@Override
		public IMessage handleServerMessage(final EntityPlayer player, final MessageNewWaypoint message, MessageContext ctx) {
			LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

				@Override
				public void run() {
					SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
					data.waypoints.add(TeleportDestination.fromNBT(message.td));
					LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
					player.openGui(SpeedsterHeroes.instance, GuiIds.dimensionBreach, player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
				}
				
			});
			return null;
		}
	}
	
}
