package lucraft.mods.heroes.speedsterheroes.worldgen;

import java.util.Random;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import net.minecraft.block.Block;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGenHinduTemple extends WorldGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.getDimensionType() == DimensionType.OVERWORLD) {
			if(random.nextInt(4000) == 0) {
				int x = chunkX * 16 + random.nextInt(16);
				int y = 70;
				int z = chunkZ * 16 + random.nextInt(16);
				
				if(world.getBiome(new BlockPos(x, y, z)) != Biomes.DESERT)
					return;
				
				while(!world.getBlockState(new BlockPos(x, y, z)).isFullBlock() && y > 0) {
					y--;
				}
				
				generate(world, random, new BlockPos(x-13, y-6, z-18));
//				world.setBlockState(new BlockPos(x, y+5, z), Blocks.LAPIS_BLOCK.getDefaultState());
			}
		}
	}
	
	@Override
	public boolean generate(World worldIn, Random rand, BlockPos position) {
		MinecraftServer minecraftserver = worldIn.getMinecraftServer();
		TemplateManager templatemanager = worldIn.getSaveHandler().getStructureTemplateManager();
		Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(SpeedsterHeroes.MODID, "hinduTemple"));
		PlacementSettings placementsettings = (new PlacementSettings()).setChunk((ChunkPos)null).setReplacedBlock((Block)null);
		
		if(template == null)
			return false;
		
		template.addBlocksToWorld(worldIn, position, placementsettings);
		
		return true;
	}

}
