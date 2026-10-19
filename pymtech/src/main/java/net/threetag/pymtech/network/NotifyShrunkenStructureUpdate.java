package net.threetag.pymtech.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.storage.ShrunkenStructureData;

import java.util.function.Supplier;

public class NotifyShrunkenStructureUpdate {

    public int id;

    public NotifyShrunkenStructureUpdate(int id) {
        this.id = id;
    }

    public NotifyShrunkenStructureUpdate(PacketBuffer buf) {
        this.id = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(this.id);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        (ctx.get()).enqueueWork(() -> {
            if (ShrunkenStructureData.shrunkenStructures.containsKey(this.id)) {
                PymTech.NETWORK_CHANNEL.sendToServer(new RequestShrunkenStructureMessage(this.id));
            }
        });
        (ctx.get()).setPacketHandled(true);
    }
}
