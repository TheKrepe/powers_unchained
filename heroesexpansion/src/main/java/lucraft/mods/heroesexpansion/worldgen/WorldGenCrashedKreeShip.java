package lucraft.mods.heroesexpansion.worldgen;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityKree;
import lucraft.mods.lucraftcore.materials.worldgen.WorldGeneratorMeteorite;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Random;

public class WorldGenCrashedKreeShip extends WorldGeneratorMeteorite {

    public WorldGenCrashedKreeShip(int size) {
        super(size);
    }

    @Override
    public void generateCrater(World world, Random random, BlockPos position) {

    }

    @Override
    public void generateMeteorBlocks(World world, Random random, BlockPos position) {
        MinecraftServer minecraftserver = world.getMinecraftServer();
        TemplateManager templatemanager = world.getSaveHandler().getStructureTemplateManager();
        Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(HeroesExpansion.MODID, "crashed_kree_ship"));
        PlacementSettings placementsettings = (new PlacementSettings()).setChunk((ChunkPos) null).setReplacedBlock((Block) null);

        if (template == null)
            return;

        template.addBlocksToWorld(world, position.add(-3, -4, -13), placementsettings);

        int amount = random.nextInt(15);
        for (int i = 0; i < amount; i++) {
            EntityKree kree = new EntityKree(world);
            BlockPos pos = position.add(-3, -4, -13).add(template.getSize().getX() / 2, 80, template.getSize().getZ() / 2);
            pos = pos.add(-20 + random.nextInt(40), 0, -20 + random.nextInt(40));
            while (world.isAirBlock(pos) && pos.getY() > 0) {
                pos = pos.down();
            }
            pos = pos.up();
            kree.onInitialSpawn(world.getDifficultyForLocation(pos), null);
            kree.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
            world.spawnEntity(kree);
        }
    }
}
