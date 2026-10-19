package lucraft.mods.lucraftcore.materials.worldgen;

import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WorldGeneratorMeteorite extends WorldGenerator {

    public int size;

    public WorldGeneratorMeteorite(int size) {
        this.size = size;
    }

    @Override
    public boolean generate(World world, Random random, BlockPos position) {
        generateCrater(world, random, position);

        int i = position.getX();
        int k = position.getY();
        int j = position.getZ();

        while (!world.getBlockState(new BlockPos(i, k, j)).isNormalCube() && k > 0) {
            k--;
        }

        k += size / 2;

        generateMeteorBlocks(world, random, new BlockPos(i, k, j));

        return true;
    }

    public List<IBlockState> getBlocksForCrater(World world, Random random, BlockPos position) {
        return Arrays.asList(Blocks.NETHERRACK.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.MAGMA.getDefaultState());
    }

    public void generateCrater(World world, Random random, BlockPos position) {
        Explosion explosion = new Explosion(world, null, position.getX(), position.getY(), position.getZ(), size * 4, true, true);
        explosion.doExplosionA();

        List<IBlockState> explosionBlocks = getBlocksForCrater(world, random, position);

        for (BlockPos pos : explosion.getAffectedBlockPositions()) {
            world.setBlockToAir(pos);

            if (world.getBlockState(pos.down()).isNormalCube() && random.nextInt(3) == 0) {
                IBlockState state = explosionBlocks.get(random.nextInt(explosionBlocks.size()));
                world.setBlockState(pos.down(), state);

                if (state == Blocks.NETHERRACK.getDefaultState() && random.nextInt(3) == 0)
                    world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }
    }

    public List<IBlockState> getBlocksForMeteor(World world, Random random, BlockPos position) {
        return Arrays.asList(Blocks.COBBLESTONE.getDefaultState(), Blocks.OBSIDIAN.getDefaultState(), Blocks.MAGMA.getDefaultState(), Material.DWARF_STAR_ALLOY.getBlock(MaterialComponent.ORE), Blocks.OBSIDIAN.getDefaultState(), Blocks.COAL_BLOCK.getDefaultState());
    }

    public void generateMeteorBlocks(World world, Random random, BlockPos position) {
        generateSphere(world, position.getX(), position.getY(), position.getZ(), size, getBlocksForMeteor(world, random, position), random);
    }

    public void generateSphere(World world, int posX, int posY, int posZ, int radius, List<IBlockState> blocks, Random rand) {
        BlockPos origin = new BlockPos(posX, posY, posZ);

        for (int x = posX - radius; x <= posX + radius; x++) {
            for (int y = posY - radius; y <= posY + radius; y++) {
                for (int z = posZ - radius; z <= posZ + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (pos.getDistance(origin.getX(), origin.getY(), origin.getZ()) < radius)
                        world.setBlockState(pos, blocks.get(rand.nextInt(blocks.size())));
                }
            }
        }
    }

}