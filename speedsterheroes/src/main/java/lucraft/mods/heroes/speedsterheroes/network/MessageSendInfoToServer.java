package lucraft.mods.heroes.speedsterheroes.network;

import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSendInfoToServer implements IMessage {

	public InfoType type;
	public int info;
	
	public MessageSendInfoToServer() {
	}

	public MessageSendInfoToServer(InfoType type) {
		this.type = type;
		this.info = 0;
	}

	public MessageSendInfoToServer(InfoType type, int i) {
		this.type = type;
		this.info = i;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		type = MessageSendInfoToServer.InfoType.getInfoTypeFromId(buf.readInt());
		info = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(type.ordinal());
		buf.writeInt(info);
	}

	public static class Handler extends AbstractServerMessageHandler<MessageSendInfoToServer> {

		@Override
		public IMessage handleServerMessage(final EntityPlayer player, final MessageSendInfoToServer message, final MessageContext ctx) {
			LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

				@Override
				public void run() {
					InfoType type = message.type;
					SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
//					SpeedsterType speedsterType = SpeedsterHeroesUtil.getSpeedsterType(player);
					
					switch (type) {
						
					case DIMENSION_BREACH_CANCEL:
						if(player.getRidingEntity() != null && player.getRidingEntity() instanceof EntityDimensionBreach) {
							((EntityDimensionBreach)player.getRidingEntity()).startDespawn();
						}
						break;
						
					case DIMENSION_BREACH_TELEPORT:
						player.dismountRidingEntity();
						if(!player.world.isRemote && player instanceof EntityPlayerMP) {
					        MinecraftServer server = ((EntityPlayerMP) player).world.getMinecraftServer();
					        WorldServer worldServer = server.worldServerForDimension(message.info);
							BlockPos spawn = worldServer.getSpawnPoint();
							if(worldServer.provider.getDimension() == 1)
								spawn = worldServer.getSpawnCoordinate();
							
							while (!worldServer.isAirBlock(spawn))
								spawn = spawn.up();
							
							LucraftCoreUtil.teleportToDimension(player, message.info, spawn.getX(), spawn.getY(), spawn.getZ());
//							player.changeDimension(message.info);
						}
						break;
					case CHOSEN_WAYPOINT:
						SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class).chosenWaypointIndex = message.info;
						LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
						break;
					case DELETE_WAYPOINT:
						if(message.info > -1 && message.info < data.waypoints.size()) {
							data.waypoints.remove(message.info);
							if(data.chosenWaypointIndex == message.info)
								data.chosenWaypointIndex = -1;
							else if(data.chosenWaypointIndex > message.info)
								data.chosenWaypointIndex--;
							LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
						}
						break;
					default:
						break;
					}					
				}
				
			});
			
			return null;
		}
	}

	public static HashMap<Integer, InfoType> ids = new HashMap<Integer, MessageSendInfoToServer.InfoType>();

	public enum InfoType {

		DIMENSION_BREACH_CANCEL, DIMENSION_BREACH_TELEPORT, CHOSEN_WAYPOINT, DELETE_WAYPOINT;

		private InfoType() {
			MessageSendInfoToServer.ids.put(this.ordinal(), this);
		}

		public static InfoType getInfoTypeFromId(int id) {
			return MessageSendInfoToServer.ids.get(id);
		}

	}

}
