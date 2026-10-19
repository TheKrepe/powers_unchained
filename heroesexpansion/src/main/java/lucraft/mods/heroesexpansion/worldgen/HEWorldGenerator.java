package lucraft.mods.heroesexpansion.worldgen;

import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.blocks.BlockHeartShapedHerb;
import lucraft.mods.heroesexpansion.blocks.HEBlocks;
import lucraft.mods.heroesexpansion.fluids.HEFluids;
import lucraft.mods.heroesexpansion.worldgen.crashedkreeship.ComponentCrashedKreeShip;
import lucraft.mods.heroesexpansion.worldgen.crashedkreeship.MapGenCrashedKreeShip;
import lucraft.mods.heroesexpansion.worldgen.norsevillage.MapGenNorseVillage;
import lucraft.mods.heroesexpansion.worldgen.norsevillage.StructureNorseVillagePieces;
import lucraft.mods.heroesexpansion.worldgen.spiderlab.VillageSpiderLabHouse;
import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.util.commands.CommandLocateExt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.Random;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HEWorldGenerator implements IWorldGenerator {

    static {
        MapGenStructureIO.registerStructure(MapGenNorseVillage.Start.class, "NorseVillage");
        MapGenStructureIO.registerStructure(MapGenCrashedKreeShip.Start.class, "CrashedKreeShip");
        StructureNorseVillagePieces.registerVillagePieces();
        ComponentCrashedKreeShip.registerScatteredFeaturePieces();

        CommandLocateExt.ENTRIES.put("NorseVillage", (w, p) -> new MapGenNorseVillage().getNearestStructurePos(w, p, false));
        CommandLocateExt.ENTRIES.put("CrashedKreeShip", (w, p) -> new MapGenCrashedKreeShip().getNearestStructurePos(w, p, false));

        VillagerRegistry villageRegistry = VillagerRegistry.instance();

        // Spider Lab
        villageRegistry.registerVillageCreationHandler(new VillageSpiderLabHouse.SpiderLabVillageManager());
        MapGenStructureIO.registerStructureComponent(VillageSpiderLabHouse.class, HeroesExpansion.MODID + ":spider_lab");
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimensionType() == DimensionType.OVERWORLD) {
            generateMjolnir(world, random, chunkX * 16, chunkZ * 16);
            generateKryptonianMeteorite(world, random, chunkX * 16, chunkZ * 16);
            generateHeartShapedHerb(world, random, chunkX * 16, chunkZ * 16);
            generateRadioactiveAcidLake(world, random, chunkX * 16, chunkZ * 16);
        }
    }

    private void generateMjolnir(World world, Random random, int x, int z) {
        if (HEConfig.worldGeneration.MJOLNIR_CRATRE_CHANCE > 0 && random.nextInt(HEConfig.worldGeneration.MJOLNIR_CRATRE_CHANCE * 1000) == 0) {
            int posX = x + random.nextInt(16);
            int posY = 255;
            int posZ = z + random.nextInt(16);

            while (!world.getBlockState(new BlockPos(posX, posY, posZ)).isNormalCube() && posY > 0) {
                posY--;
            }
            new WorldGenMjolnir(1 + random.nextInt(2)).generate(world, random, new BlockPos(posX, posY, posZ));
        }
    }

    private void generateKryptonianMeteorite(World world, Random random, int x, int z) {
        if (HEConfig.worldGeneration.MJOLNIR_CRATRE_CHANCE > 0 && random.nextInt(HEConfig.worldGeneration.KRYPTONIAN_METEORITE_CHANCE * 1000) == 0) {
            int posX = x + random.nextInt(16);
            int posY = 255;
            int posZ = z + random.nextInt(16);

            while (!world.getBlockState(new BlockPos(posX, posY, posZ)).isNormalCube() && posY > 0) {
                posY--;
            }
            new WorldGenKryptonianMeteorite(2 + random.nextInt(2)).generate(world, random, new BlockPos(posX, posY, posZ));
        }
    }

    private void generateHeartShapedHerb(World world, Random random, int x, int z) {
        if (!HEConfig.worldGeneration.GENERATE_HEART_SHAPED_HERBS)
            return;
        for (int x1 = 0; x1 < 16; x1++) {
            for (int z1 = 0; z1 < 16; z1++) {
                for (int y = 0; y < LCConfig.materials.ore_settings.get(Material.VIBRANIUM.getResourceName())[4]; y++) {
                    BlockPos pos = new BlockPos(x + x1, y, z + z1);
                    if (world.getBlockState(pos) == Material.VIBRANIUM.getBlock(Material.MaterialComponent.ORE) && random.nextInt(10) == 0) {
                        setHeartShapedHerbAtHighestPlace(world, pos.getX(), pos.getZ(), pos.getY());
                    }
                }
            }
        }
    }

    public void setHeartShapedHerbAtHighestPlace(World world, int x, int z, int oreY) {
        BlockPos pos = new BlockPos(x, oreY, z);
        while (!((BlockHeartShapedHerb) HEBlocks.HEART_SHAPED_HERB).canBlockStay(world, pos, HEBlocks.HEART_SHAPED_HERB.getDefaultState()) && pos.getY() < 255) {
            pos = pos.up();
        }
        if (pos.getY() < 255 && world.isAirBlock(pos)) {
            world.setBlockState(pos, HEBlocks.HEART_SHAPED_HERB.getDefaultState(), 2);
        }
    }

    private void generateRadioactiveAcidLake(World world, Random random, int x, int z) {
        if (HEConfig.worldGeneration.RADIOACTIVE_ACID_LAKE_CHANCE > 0 && random.nextInt(HEConfig.worldGeneration.RADIOACTIVE_ACID_LAKE_CHANCE * 1000) == 0) {
            BlockPos blockpos1 = new BlockPos(x, 0, z).add(random.nextInt(16) + 8, random.nextInt(random.nextInt(248) + 8), random.nextInt(16) + 8);

            if (blockpos1.getY() < world.getSeaLevel() || random.nextInt(10) == 0) {
                new WorldGenLakes(HEFluids.RADIOACTIVE_ACID.getBlock()).generate(world, random, blockpos1);
            }
        }
    }

    @SubscribeEvent
    public static void onReplaceBiomesBlocks(ChunkGeneratorEvent.ReplaceBiomeBlocks e) {
        if (e.getWorld().provider.getDimensionType() == DimensionType.OVERWORLD) {
            if (HEConfig.worldGeneration.GENERATE_NORSE_VILLAGES)
                new MapGenNorseVillage().generate(e.getWorld(), e.getX(), e.getZ(), e.getPrimer());
            if (HEConfig.worldGeneration.GENERATE_CRASHED_KREE_SHIPS)
                new MapGenCrashedKreeShip().generate(e.getWorld(), e.getX(), e.getZ(), e.getPrimer());
        }
    }

    @SubscribeEvent
    public static void onPopulateChunkPre(PopulateChunkEvent.Pre e) {
        if (e.getWorld().provider.getDimensionType() == DimensionType.OVERWORLD) {
            if (HEConfig.worldGeneration.GENERATE_NORSE_VILLAGES)
                new MapGenNorseVillage().generateStructure(e.getWorld(), e.getRand(), new ChunkPos(e.getChunkX(), e.getChunkZ()));
            if (HEConfig.worldGeneration.GENERATE_CRASHED_KREE_SHIPS)
                new MapGenCrashedKreeShip().generateStructure(e.getWorld(), e.getRand(), new ChunkPos(e.getChunkX(), e.getChunkZ()));
        }
    }

}
