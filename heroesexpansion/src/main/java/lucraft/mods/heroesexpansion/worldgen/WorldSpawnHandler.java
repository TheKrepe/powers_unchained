package lucraft.mods.heroesexpansion.worldgen;

import com.google.common.collect.Lists;
import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.HELootTableList;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityKree;
import lucraft.mods.heroesexpansion.entities.EntityThorWeapon;
import lucraft.mods.heroesexpansion.items.HEItems;
import net.minecraft.block.Block;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
public class WorldSpawnHandler {

    private static List<WorldSpawn> WORLD_SPAWNS = Lists.newLinkedList();

    public static void register(ResourceLocation type, BiConsumer<World, BlockPos> spawner, Supplier<Integer> delay) {
        WORLD_SPAWNS.add(new WorldSpawn(type, spawner, delay));
    }

    static {

        // Mjolnir
        register(new ResourceLocation(HeroesExpansion.MODID, "mjolnir"), (world, pos) -> {
            EntityThorWeapon entity = new EntityThorWeapon(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(HEItems.MJOLNIR));
            entity.mode = EntityThorWeapon.Mode.FALLING;
            world.spawnEntity(entity);
        }, () -> HEConfig.worldGeneration.MJOLNIR_SPAWN_CHANCE);


        // Kryptonian Meteorite
        register(new ResourceLocation(HeroesExpansion.MODID, "kryptonian_meteorite"), (world, pos) -> {
            while (!world.getBlockState(pos).isNormalCube() && pos.getY() > 0) {
                pos = pos.down();
            }
            Random random = new Random();
            int size = 2 + random.nextInt(2);
            world.newExplosion(null, pos.getX(), pos.getY(), pos.getZ(), size, false, false);
            new WorldGenKryptonianMeteorite(size).generate(world, random, pos);
        }, () -> HEConfig.worldGeneration.KRYPTONIAN_METEORITE_SPAWN_CHANCE);


        // Crashed Kree Ships
        register(new ResourceLocation(HeroesExpansion.MODID, "crashed_kree_ship"), (world, pos) -> {
            while (!world.getBlockState(pos).isNormalCube() && pos.getY() > 0) {
                pos = pos.down();
            }
            Random random = new Random();
            MinecraftServer minecraftserver = world.getMinecraftServer();
            TemplateManager templatemanager = world.getSaveHandler().getStructureTemplateManager();
            Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(HeroesExpansion.MODID, "crashed_kree_ship"));
            PlacementSettings placementsettings = (new PlacementSettings()).setChunk(null).setReplacedBlock((Block) null);

            if (template == null)
                return;

            world.newExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 5, false, false);
            template.addBlocksToWorld(world, pos.add(-8, 0, -8), placementsettings, 18);
            Map<BlockPos, String> map = template.getDataBlocks(pos.add(-8, 0, -8), placementsettings);

            for (Map.Entry<BlockPos, String> entry : map.entrySet()) {
                String s = entry.getValue();
                if (s.startsWith("Chest")) {
                    BlockPos blockpos = entry.getKey().down();
                    TileEntity tileentity = world.getTileEntity(blockpos);
                    if (tileentity instanceof TileEntityChest) {
                        ((TileEntityChest) tileentity).setLootTable(HELootTableList.CRASHED_KREE_SHIP, new Random().nextLong());
                    }
                }
            }

            for (int i = 0; i < 4; i++) {
                EntityKree entityKree = new EntityKree(world);
                entityKree.setLocationAndAngles((double) pos.getX() + 0.5D + (random.nextInt(20) - 10), (double) pos.getY() + 2, (double) pos.getZ() + 0.5D + (random.nextInt(20) - 10), 0.0F, 0.0F);
                entityKree.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityKree)), (IEntityLivingData) null);
                entityKree.enablePersistence();
                world.spawnEntity(entityKree);
            }
        }, () -> HEConfig.worldGeneration.CRASHED_KREE_SHIP_SPAWN_CHANCE);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            WORLD_SPAWNS.forEach(ws -> ws.tick());
        }
    }

    public static List<Chunk> getLoadedChunks() {
        List<Chunk> chunks = new ArrayList<>();
        for (WorldServer world : DimensionManager.getWorlds()) {
            chunks.addAll(world.getChunkProvider().getLoadedChunks());
        }
        return chunks;
    }

    public static Chunk getRandomChunk(List<Chunk> chunks, Random random, ResourceLocation type) {
        Chunk chunk = chunks.get(random.nextInt(chunks.size()));
        if (MinecraftForge.EVENT_BUS.post(new WorldSpawnEvent(chunk.getWorld(), chunk, type)))
            return null;
        else
            return chunk;
    }

    private static class WorldSpawn {

        public ResourceLocation type;
        public BiConsumer<World, BlockPos> spawner;
        public Supplier<Integer> delay;
        public int timer;

        public WorldSpawn(ResourceLocation type, BiConsumer<World, BlockPos> spawner, Supplier<Integer> delay) {
            this.type = type;
            this.spawner = spawner;
            this.delay = delay;
        }

        public void tick() {
            if (this.delay.get() < 0)
                return;

            this.timer++;

            if (this.timer >= this.delay.get()) {
                this.timer = 0;

                Chunk chunk = getRandomChunk(getLoadedChunks(), new Random(), this.type);
                if (chunk != null) {
                    BlockPos pos = new BlockPos(chunk.x * 16 + 8, 256, chunk.z * 16 + 8);
                    this.spawner.accept(chunk.getWorld(), pos);
                }
            }
        }
    }

    @Cancelable
    public static class WorldSpawnEvent extends WorldEvent {

        protected final Chunk chunk;
        protected final ResourceLocation type;

        public WorldSpawnEvent(World world, Chunk chunk, ResourceLocation type) {
            super(world);
            this.chunk = chunk;
            this.type = type;
        }

        public Chunk getChunk() {
            return chunk;
        }

        public ResourceLocation getType() {
            return type;
        }
    }
}
