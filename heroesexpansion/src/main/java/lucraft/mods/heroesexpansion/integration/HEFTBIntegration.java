package lucraft.mods.heroesexpansion.integration;

import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import lucraft.mods.heroesexpansion.worldgen.WorldSpawnHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HEFTBIntegration {

    @SubscribeEvent
    public void onWorldEvent(WorldSpawnHandler.WorldSpawnEvent e) {
        ChunkDimPos pos = new ChunkDimPos(e.getChunk().getPos(), e.getWorld().provider.getDimension());
        if (ClaimedChunks.instance.getChunk(pos) != null) {
            e.setCanceled(true);
        }
    }

}
