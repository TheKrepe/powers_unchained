package lucraft.mods.lucraftcore.materials.worldgen;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class MaterialsWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimensionType() == DimensionType.OVERWORLD) {
            generateOverworld(world, random, chunkX * 16, chunkZ * 16);
        }
    }

    private void generateOverworld(World world, Random random, int x, int z) {
        for (Material type : Material.getMaterials()) {
            if (type.generateOre()) {
                int minVeinSize = LCConfig.materials.ore_settings.get(type.getResourceName())[0];
                int maxVeinSize = LCConfig.materials.ore_settings.get(type.getResourceName())[1];
                int chance = LCConfig.materials.ore_settings.get(type.getResourceName())[2];
                int minY = LCConfig.materials.ore_settings.get(type.getResourceName())[3];
                int maxY = LCConfig.materials.ore_settings.get(type.getResourceName())[4];
                addOreSpawn(type, world, random, x, z, 16, 16, minVeinSize + random.nextInt(maxVeinSize - minVeinSize), chance, minY, maxY);
            }
        }

        if (LCConfig.materials.meteorite_chance > 0 && random.nextInt(LCConfig.materials.meteorite_chance * 1000) == 0) {
            int posX = x + random.nextInt(16);
            int posY = 255;
            int posZ = z + random.nextInt(16);

            while (!world.getBlockState(new BlockPos(posX, posY, posZ)).isNormalCube() && posY > 0) {
                posY--;
            }

            new WorldGeneratorMeteorite(2 + random.nextInt(4)).generate(world, random, new BlockPos(posX, posY, posZ));
        }
    }

    public void addOreSpawn(Material material, World world, Random random, int blockXPos, int blockZPos, int maxX, int maxZ, int maxVeinSize, int chance, int minY, int maxY) {
        for (int i = 0; i < chance; i++) {
            int posX = blockXPos + random.nextInt(maxX);
            int posY = minY + random.nextInt(maxY - minY);
            int posZ = blockZPos + random.nextInt(maxZ);

            WorldGenMinable wgm = new WorldGenMinable(material.getBlock(MaterialComponent.ORE), maxVeinSize, BlockMatcher.forBlock(Blocks.STONE));
            wgm.generate(world, random, new BlockPos(posX, posY, posZ));
        }

    }

}
