package lucraft.mods.heroes.speedsterheroes.network;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class SHPacketDispatcher {

	private static byte packetId = 0;

	private static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(SpeedsterHeroes.MODID);

	public static final void registerPackets() {
		SHPacketDispatcher.registerMessage(MessageSendInfoToServer.Handler.class, MessageSendInfoToServer.class, Side.SERVER);
		SHPacketDispatcher.registerMessage(MessageSendInfoToClient.Handler.class, MessageSendInfoToClient.class, Side.CLIENT);
		SHPacketDispatcher.registerMessage(MessageChangeVelocity.Handler.class, MessageChangeVelocity.class, Side.SERVER);
		SHPacketDispatcher.registerMessage(MessageNewWaypoint.Handler.class, MessageNewWaypoint.class, Side.SERVER);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final void registerMessage(Class handlerClass, Class messageClass, Side side) {
		SHPacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, side);
	}

	public static final void sendTo(IMessage message, EntityPlayerMP player) {
		SHPacketDispatcher.dispatcher.sendTo(message, player);
	}
	
	public static final void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
		SHPacketDispatcher.dispatcher.sendToAllAround(message, point);
	}

	public static final void sendToAll(IMessage message) {
		SHPacketDispatcher.dispatcher.sendToAll(message);
	}

	public static final void sendToAllAround(IMessage message, int dimension, double x, double y, double z,

	double range) {
		SHPacketDispatcher.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, x, y, z,

		range));
	}

	public static final void sendToAllAround(IMessage message, EntityPlayer player, double range) {
		SHPacketDispatcher.sendToAllAround(message, player.world.provider.getDimension(), player.posX, player.posY, player.posZ, range);
	}

	public static final void sendToDimension(IMessage message, int dimensionId) {
		SHPacketDispatcher.dispatcher.sendToDimension(message, dimensionId);
	}

	public static final void sendToServer(IMessage message) {
		SHPacketDispatcher.dispatcher.sendToServer(message);
	}
}