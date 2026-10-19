package net.threetag.pymtech.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.threetag.pymtech.client.renderer.item.ShrunkenStructureItemRenderer;
import net.threetag.pymtech.storage.ShrunkenStructureData;

import java.util.function.Supplier;

public class SyncShrunkenStructureMessage {

    public ShrunkenStructureData shrunkenStructure;

    public SyncShrunkenStructureMessage(ShrunkenStructureData shrunkenStructure) {
        this.shrunkenStructure = shrunkenStructure;
    }

    public SyncShrunkenStructureMessage(PacketBuffer buf) {
        this.shrunkenStructure = new ShrunkenStructureData(buf.readString());
        this.shrunkenStructure.readFromBuffer(buf);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeString(this.shrunkenStructure.getName());
        this.shrunkenStructure.writeToBuffer(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        (ctx.get()).enqueueWork(() -> {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
                syncStructure(this.shrunkenStructure);
            });
        });
        (ctx.get()).setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void syncStructure(ShrunkenStructureData data) {
        ShrunkenStructureData.register(Minecraft.getInstance().world, data);
        if (ShrunkenStructureItemRenderer.requested.contains(data.getId()))
            ShrunkenStructureItemRenderer.requested.remove(Integer.valueOf(data.getId()));
    }

}
