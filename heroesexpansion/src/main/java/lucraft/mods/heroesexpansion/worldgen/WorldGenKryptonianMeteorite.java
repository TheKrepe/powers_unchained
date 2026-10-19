package lucraft.mods.heroesexpansion.worldgen;

import lucraft.mods.heroesexpansion.blocks.BlockKryptonite;
import lucraft.mods.heroesexpansion.blocks.HEBlocks;
import lucraft.mods.lucraftcore.materials.worldgen.WorldGeneratorMeteorite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WorldGenKryptonianMeteorite extends WorldGeneratorMeteorite {

    public WorldGenKryptonianMeteorite(int size) {
        super(size);
    }

    @Override
    public List<IBlockState> getBlocksForMeteor(World world, Random random, BlockPos position) {
        return Arrays.asList(HEBlocks.KRYPTONITE_BLOCK.getDefaultState().withProperty(BlockKryptonite.DROP_FOSSIL, true), Blocks.COBBLESTONE.getDefaultState(), Blocks.OBSIDIAN.getDefaultState(), Blocks.MAGMA.getDefaultState(), Blocks.OBSIDIAN.getDefaultState(), Blocks.COAL_BLOCK.getDefaultState());
    }
}
