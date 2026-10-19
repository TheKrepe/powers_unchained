package lucraft.mods.heroes.speedsterheroes.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChangeVelocity implements IMessage {

	public boolean faster;
	
	public MessageChangeVelocity() {
		this.faster = true;
	}
	
	public MessageChangeVelocity(boolean faster) {
		this.faster = faster;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.faster = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(faster);
	}

	public static class Handler extends AbstractServerMessageHandler<MessageChangeVelocity> {

		@Override
		public IMessage handleServerMessage(EntityPlayer player, MessageChangeVelocity message, MessageContext ctx) {
			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
			if(data != null) {
				data.increaseDecreaseSpeedLevel(message.faster);
			}
			return null;
		}
	}
	
}
