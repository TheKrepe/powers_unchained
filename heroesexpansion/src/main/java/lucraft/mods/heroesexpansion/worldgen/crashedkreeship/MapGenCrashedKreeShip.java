package lucraft.mods.heroesexpansion.worldgen.crashedkreeship;

import com.google.common.collect.Lists;
import lucraft.mods.heroesexpansion.entities.EntityKree;
import net.minecraft.init.Biomes;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class MapGenCrashedKreeShip extends MapGenStructure {

    private static final List<Biome> BIOMELIST = Arrays.<Biome>asList(Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.SWAMPLAND, Biomes.ICE_PLAINS, Biomes.COLD_TAIGA);
    /**
     * contains possible spawns for scattered features
     */
    private final List<Biome.SpawnListEntry> monsters;
    /**
     * the maximum distance between scattered features
     */
    private int maxDistanceBetweenScatteredFeatures;
    /**
     * the minimum distance between scattered features
     */
    private final int minDistanceBetweenScatteredFeatures;

    public MapGenCrashedKreeShip() {
        this.monsters = Lists.<Biome.SpawnListEntry>newArrayList();
        this.maxDistanceBetweenScatteredFeatures = 80;
        this.minDistanceBetweenScatteredFeatures = 12;
        this.monsters.add(new Biome.SpawnListEntry(EntityKree.class, 1, 1, 5));
    }

    public MapGenCrashedKreeShip(Map<String, String> p_i2061_1_) {
        this();

        for (Entry<String, String> entry : p_i2061_1_.entrySet()) {
            if (((String) entry.getKey()).equals("distance")) {
                this.maxDistanceBetweenScatteredFeatures = MathHelper.getInt(entry.getValue(), this.maxDistanceBetweenScatteredFeatures, 9);
            }
        }
    }

    @Override
    public String getStructureName() {
        return "CrashedKreeShip";
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0) {
            chunkX -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        if (chunkZ < 0) {
            chunkZ -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        int k = chunkX / this.maxDistanceBetweenScatteredFeatures;
        int l = chunkZ / this.maxDistanceBetweenScatteredFeatures;
        Random random = this.world.setRandomSeed(k, l, 14357617);
        k = k * this.maxDistanceBetweenScatteredFeatures;
        l = l * this.maxDistanceBetweenScatteredFeatures;
        k = k + random.nextInt(this.maxDistanceBetweenScatteredFeatures - 8);
        l = l + random.nextInt(this.maxDistanceBetweenScatteredFeatures - 8);

        if (i == k && j == l) {
            Biome biome = this.world.getBiomeProvider().getBiome(new BlockPos(i * 16 + 8, 0, j * 16 + 8));

            if (biome == null) {
                return false;
            }

            for (Biome biome1 : BIOMELIST) {
                if (biome == biome1) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, this.maxDistanceBetweenScatteredFeatures, 8, 14357617, false, 100, findUnexplored);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new MapGenCrashedKreeShip.Start(this.world, this.rand, chunkX, chunkZ);
    }

    public List<Biome.SpawnListEntry> getMonsters() {
        return this.monsters;
    }

    public static class Start extends StructureStart {

        public Start() {
        }

        public Start(World worldIn, Random random, int chunkX, int chunkZ) {
            this(worldIn, random, chunkX, chunkZ, worldIn.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)));
        }

        public Start(World worldIn, Random random, int chunkX, int chunkZ, Biome biomeIn) {
            super(chunkX, chunkZ);

            this.components.add(new ComponentCrashedKreeShip.Main(worldIn.getSaveHandler().getStructureTemplateManager(), new BlockPos(chunkX * 16, 64, chunkZ * 16), Rotation.values()[random.nextInt(Rotation.values().length)]));
            this.updateBoundingBox();
        }
    }
}