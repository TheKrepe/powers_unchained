package lucraft.mods.heroes.speedsterheroes.network;

import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.gui.GuiIds;
import lucraft.mods.lucraftcore.access.LucraftTickrateHooks;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSendInfoToClient implements IMessage {

	public InfoType type;
	public int i;

	public MessageSendInfoToClient() {}
	
	public MessageSendInfoToClient(InfoType type) {
		this.type = type;
		this.i = 0;
	}
	
	public MessageSendInfoToClient(InfoType type, int i) {
		this.type = type;
		this.i = i;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		type = MessageSendInfoToClient.InfoType.getInfoTypeFromId(buf.readInt());
		i = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(type.ordinal());
		buf.writeInt(i);
	}
	
	public static class Handler extends AbstractClientMessageHandler<MessageSendInfoToClient> {
		
		@Override
		public IMessage handleClientMessage(EntityPlayer player, MessageSendInfoToClient message, MessageContext ctx) {
			InfoType type = message.type;
			switch (type) {
			
			case DIMENSION_BREACH:
				LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
				player.openGui(SpeedsterHeroes.instance, GuiIds.dimensionBreach, player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
				break;
			case SLOWMO:
				if(message.i == 0) {
					LucraftTickrateHooks.updateClientTickrate(LucraftTickrateHooks.DEFAULT_TICKS_PER_SECOND);
				} else {
					LucraftTickrateHooks.updateClientTickrate(5);
				}
				break;
			default:
				break;
			}
			
			
			return null;
		}
	}

	public static HashMap<Integer, InfoType> ids = new HashMap<Integer, MessageSendInfoToClient.InfoType>();
	
	public enum InfoType {
		
		LEVELING, DIMENSION_BREACH, SLOWMO;
		
		private InfoType() {
			MessageSendInfoToClient.ids.put(this.ordinal(), this);
		}
		
		public static InfoType getInfoTypeFromId(int id) {
			return MessageSendInfoToClient.ids.get(id);
		}
		
	}
	
}
