package net.threetag.pymtech.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.storage.ShrunkenStructureData;

import java.util.function.Supplier;

public class RequestShrunkenStructureMessage {

    public int id;

    public RequestShrunkenStructureMessage(int id) {
        this.id = id;
    }

    public RequestShrunkenStructureMessage(PacketBuffer buf) {
        this.id = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(this.id);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        (ctx.get()).enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                ShrunkenStructureData data = ShrunkenStructureData.get(player.world, this.id);
                PymTech.NETWORK_CHANNEL.sendTo(new SyncShrunkenStructureMessage(data), player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        (ctx.get()).setPacketHandled(true);
    }
}
