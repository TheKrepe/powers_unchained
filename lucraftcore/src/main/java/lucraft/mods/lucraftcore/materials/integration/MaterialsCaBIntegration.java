package lucraft.mods.lucraftcore.materials.integration;

import lucraft.mods.lucraftcore.materials.ModuleMaterials;
import net.minecraft.block.state.IBlockState;

public class MaterialsCaBIntegration {

    public static void postInit() {
        for (IBlockState state : ModuleMaterials.chiselsAndBitsBlocks) {
            mod.chiselsandbits.chiseledblock.BlockBitInfo.forceStateCompatibility(state, true);
        }

        ModuleMaterials.chiselsAndBitsBlocks.clear();
    }
}
