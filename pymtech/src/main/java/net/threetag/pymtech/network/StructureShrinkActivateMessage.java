package net.threetag.pymtech.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.threetag.pymtech.tileentity.StructureShrinkerTileEntity;

import java.util.function.Supplier;

public class StructureShrinkActivateMessage {

    public BlockPos pos;

    public StructureShrinkActivateMessage(BlockPos pos) {
        this.pos = pos;
    }

    public StructureShrinkActivateMessage(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(this.pos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        (ctx.get()).enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            TileEntity tileEntity = player.world.getTileEntity(this.pos);
            if (tileEntity instanceof StructureShrinkerTileEntity) {
                ((StructureShrinkerTileEntity) tileEntity).shrink(player);
            }
        });
        (ctx.get()).setPacketHandled(true);
    }

}
