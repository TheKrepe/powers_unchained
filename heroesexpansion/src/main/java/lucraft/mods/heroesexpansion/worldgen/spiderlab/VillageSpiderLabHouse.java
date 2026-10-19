package lucraft.mods.heroesexpansion.worldgen.spiderlab;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityRadioactiveSpider;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.utilities.blocks.BlockExtractor;
import lucraft.mods.lucraftcore.utilities.blocks.UtilitiesBlocks;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

import java.util.List;
import java.util.Random;

public class VillageSpiderLabHouse extends Village {

    private int groundLevel = -1;
    private int spidersSpawned;

    public VillageSpiderLabHouse() {
    }

    public VillageSpiderLabHouse(Start villagePiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, EnumFacing facing) {
        super(villagePiece, par2);
        this.setCoordBaseMode(facing);
        this.boundingBox = par4StructureBoundingBox;
    }

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {

        if (groundLevel < 0) {
            groundLevel = this.getAverageGroundLevel(world, box);
            if (groundLevel < 0)
                return true;
            boundingBox.offset(0, groundLevel - boundingBox.maxY + 6 - 1, 0);
        }

        // BlockStates
        IBlockState woodenStair = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState());
        IBlockState woodenSlab = this.getBiomeSpecificBlockState(Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockWoodSlab.VARIANT, BlockPlanks.EnumType.OAK));
        IBlockState woodenFence = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
        IBlockState leaves = this.getBiomeSpecificBlockState(Blocks.LEAVES.getDefaultState());
        IBlockState cobble = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
        IBlockState wall = this.biomeWall();

        // Clear Space
        this.fillWithBlocks(world, box, 0, 0, 0, 14, 5, 9, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);

        // Floor
        this.fillWithBlocks(world, box, 0, 0, 0, 14, 0, 9, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), Blocks.DOUBLE_STONE_SLAB.getDefaultState(), false);
        this.fillWithBlocks(world, box, 0, 0, 0, 5, 0, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);

        // Cobble Pillars
        this.fillWithBlocks(world, box, 6, 0, 0, 6, 4, 0, cobble, cobble, false);
        this.fillWithBlocks(world, box, 14, 0, 0, 14, 4, 0, cobble, cobble, false);
        this.fillWithBlocks(world, box, 14, 0, 9, 14, 4, 9, cobble, cobble, false);
        this.fillWithBlocks(world, box, 0, 0, 9, 0, 4, 9, cobble, cobble, false);
        this.fillWithBlocks(world, box, 0, 0, 4, 0, 4, 4, cobble, cobble, false);
        this.fillWithBlocks(world, box, 6, 0, 4, 6, 4, 4, cobble, cobble, false);

        // Walls
        this.fillWithBlocks(world, box, 7, 1, 0, 13, 3, 0, this.biomeWall(), this.biomeWall(), false);
        this.fillWithBlocks(world, box, 7, 4, 0, 13, 4, 0, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);

        this.fillWithBlocks(world, box, 6, 1, 1, 6, 3, 3, this.biomeWall(), this.biomeWall(), false);
        this.fillWithBlocks(world, box, 6, 4, 1, 6, 4, 3, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);

        this.fillWithBlocks(world, box, 1, 1, 4, 5, 3, 4, this.biomeWall(), this.biomeWall(), false);
        this.fillWithBlocks(world, box, 1, 4, 4, 5, 4, 4, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);

        this.fillWithBlocks(world, box, 0, 1, 5, 0, 3, 8, this.biomeWall(), this.biomeWall(), false);
        this.fillWithBlocks(world, box, 0, 4, 5, 0, 4, 8, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);

        this.fillWithBlocks(world, box, 1, 1, 9, 13, 3, 9, this.biomeWall(), this.biomeWall(), false);
        this.fillWithBlocks(world, box, 1, 4, 9, 13, 4, 9, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);

        this.fillWithBlocks(world, box, 14, 1, 1, 14, 3, 8, this.biomeWall(), this.biomeWall(), false);
        this.fillWithBlocks(world, box, 14, 4, 1, 14, 4, 8, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);

        // Windows
        this.fillWithBlocks(world, box, 8, 1, 0, 8, 3, 0, Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), false);
        this.fillWithBlocks(world, box, 12, 1, 0, 12, 3, 0, Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), false);
        this.fillWithBlocks(world, box, 6, 1, 2, 6, 3, 2, Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), false);
        this.fillWithBlocks(world, box, 8, 1, 0, 8, 3, 0, Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), false);
        this.fillWithBlocks(world, box, 14, 1, 2, 14, 3, 2, Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), false);
        this.fillWithBlocks(world, box, 14, 1, 4, 14, 3, 5, Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), false);
        this.fillWithBlocks(world, box, 14, 1, 7, 14, 3, 7, Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), false);
        this.fillWithBlocks(world, box, 8, 1, 9, 8, 3, 9, Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), Blocks.STAINED_GLASS_PANE.getStateFromMeta(0), false);

        this.fillWithBlocks(world, box, 2, 1, 4, 2, 3, 4, Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), false);
        this.fillWithBlocks(world, box, 2, 2, 4, 2, 2, 4, Blocks.STAINED_GLASS.getStateFromMeta(0), Blocks.STAINED_GLASS.getStateFromMeta(0), false);

        this.fillWithBlocks(world, box, 4, 1, 4, 4, 3, 4, Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), false);
        this.fillWithBlocks(world, box, 4, 2, 4, 4, 2, 4, Blocks.STAINED_GLASS.getStateFromMeta(0), Blocks.STAINED_GLASS.getStateFromMeta(0), false);

        this.fillWithBlocks(world, box, 0, 1, 6, 0, 3, 7, Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), false);
        this.fillWithBlocks(world, box, 0, 2, 6, 0, 2, 7, Blocks.STAINED_GLASS.getStateFromMeta(0), Blocks.STAINED_GLASS.getStateFromMeta(0), false);

        this.fillWithBlocks(world, box, 2, 1, 9, 2, 3, 9, Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), false);
        this.fillWithBlocks(world, box, 2, 2, 9, 2, 2, 9, Blocks.STAINED_GLASS.getStateFromMeta(0), Blocks.STAINED_GLASS.getStateFromMeta(0), false);

        this.fillWithBlocks(world, box, 4, 1, 9, 4, 3, 9, Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), false);
        this.fillWithBlocks(world, box, 4, 2, 9, 4, 2, 9, Blocks.STAINED_GLASS.getStateFromMeta(0), Blocks.STAINED_GLASS.getStateFromMeta(0), false);

        this.fillWithBlocks(world, box, 6, 1, 9, 6, 3, 9, Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), Material.STEEL.getBlock(Material.MaterialComponent.BLOCK), false);

        // Door
        this.generateDoor(world, box, rand, 10, 1, 0, EnumFacing.NORTH, this.biomeDoor());
        this.setBlockState(world, Blocks.STONE_STAIRS.getDefaultState(), 10, 0, -1, box);

        // Roof
        this.fillWithBlocks(world, box, 0, 5, 0, 14, 5, 9, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
        this.fillWithBlocks(world, box, 0, 5, 0, 5, 5, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithBlocks(world, box, 1, 4, 5, 13, 4, 8, Blocks.CONCRETE.getStateFromMeta(15), Blocks.CONCRETE.getStateFromMeta(15), false);
        this.fillWithBlocks(world, box, 7, 4, 1, 13, 4, 4, Blocks.CONCRETE.getStateFromMeta(15), Blocks.CONCRETE.getStateFromMeta(15), false);

        // Sitting corner
        this.setBlockState(world, Blocks.GRASS.getDefaultState(), 7, 1, 1, box);
        this.fillWithBlocks(world, box, 7, 2, 1, 7, 3, 1, leaves.withProperty(BlockLeaves.DECAYABLE, false), leaves.withProperty(BlockLeaves.DECAYABLE, false), false);

        this.setBlockState(world, Blocks.WALL_SIGN.getDefaultState().withProperty(BlockWallSign.FACING, EnumFacing.NORTH), 7, 1, 4, box);
        this.setBlockState(world, woodenStair.withProperty(BlockStairs.FACING, EnumFacing.WEST), 7, 1, 3, box);
        this.setBlockState(world, woodenStair.withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 7, 1, 2, box);

        this.setBlockState(world, Blocks.WALL_SIGN.getDefaultState().withProperty(BlockWallSign.FACING, EnumFacing.EAST), 10, 1, 1, box);
        this.setBlockState(world, woodenStair.withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 9, 1, 1, box);
        this.setBlockState(world, woodenStair.withProperty(BlockStairs.FACING, EnumFacing.WEST), 8, 1, 1, box);

        this.setBlockState(world, Blocks.OAK_FENCE.getDefaultState(), 9, 1, 3, box);
        this.setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 9, 2, 3, box);

        // Misc
        this.fillWithBlocks(world, box, 10, 1, 4, 12, 1, 6, Blocks.CARPET.getStateFromMeta(11), Blocks.CARPET.getStateFromMeta(11), false);
        this.fillWithBlocks(world, box, 13, 1, 1, 13, 3, 1, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
        this.setBlockState(world, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST), 13, 2, 3, box);
        this.setBlockState(world, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST), 13, 2, 6, box);

        // Big Shelf
        this.setBlockState(world, woodenStair.withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withProperty(BlockStairs.FACING, EnumFacing.EAST), 13, 1, 8, box);
        this.fillWithBlocks(world, box, 9, 1, 8, 12, 1, 8, woodenSlab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP), woodenSlab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP), false);
        this.setBlockState(world, Blocks.CAULDRON.getDefaultState(), 10, 1, 8, box);
        this.setBlockState(world, Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.SOUTH).withProperty(BlockLever.POWERED, true), 10, 2, 8, box);
        this.setBlockState(world, woodenFence, 9, 2, 8, box);
        this.setBlockState(world, UtilitiesBlocks.EXTRACTOR.getDefaultState().withProperty(BlockExtractor.FACING, EnumFacing.NORTH), 11, 2, 8, box);
        this.setBlockState(world, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState(), 12, 2, 8, box);
        this.placeItemframe(rand, world, 12, 2, 8, EnumFacing.NORTH, new ItemStack(Blocks.STAINED_GLASS_PANE, 1, 3));
        this.fillWithBlocks(world, box, 9, 3, 8, 10, 3, 8, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
        this.fillWithBlocks(world, box, 11, 3, 8, 13, 3, 8, woodenSlab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP), woodenSlab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP), false);

        // Spider Room
        this.fillWithBlocks(world, box, 6, 1, 5, 6, 3, 6, this.biomeWall(), this.biomeWall(), false);
        this.generateDoor(world, box, rand, 6, 1, 5, EnumFacing.WEST, Blocks.IRON_DOOR);
        this.fillWithBlocks(world, box, 6, 1, 7, 6, 3, 8, Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlassPane.COLOR, EnumDyeColor.YELLOW), this.biomeWall(), false);
        this.setBlockState(world, Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlassPane.COLOR, EnumDyeColor.BLACK), 6, 1, 7, box);
        this.setBlockState(world, Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlassPane.COLOR, EnumDyeColor.BLACK), 6, 2, 8, box);
        this.setBlockState(world, Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty(BlockStainedGlassPane.COLOR, EnumDyeColor.BLACK), 6, 3, 7, box);
        this.setBlockState(world, Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.EAST), 7, 2, 4, box);
        this.fillWithBlocks(world, box, 2, 1, 7, 2, 3, 7, woodenFence, woodenFence, false);
        this.setBlockState(world, woodenSlab, 2, 3, 6, box);
        this.setBlockState(world, woodenSlab, 3, 2, 7, box);
        this.setBlockState(world, leaves, 4, 1, 8, box);
        this.setBlockState(world, leaves, 2, 1, 5, box);
        this.setBlockState(world, leaves, 1, 1, 5, box);
        this.setBlockState(world, leaves, 1, 2, 5, box);

        this.spawnVillagers(world, box, 11, 1, 5, 2);
        this.spawnRadioactiveSpiders(world, box, 1, 1, 6, 5);

        return true;
    }

    protected void spawnRadioactiveSpiders(World worldIn, StructureBoundingBox structurebb, int x, int y, int z, int count) {
        for (int i = this.spidersSpawned; i < count; ++i) {
            int j = this.getXWithOffset(x + i, z);
            int k = this.getYWithOffset(y);
            int l = this.getZWithOffset(x + i, z);

            if (!structurebb.isVecInside(new BlockPos(j, k, l))) {
                break;
            }

            ++this.spidersSpawned;

            EntityRadioactiveSpider entityspider = new EntityRadioactiveSpider(worldIn);
            entityspider.setLocationAndAngles((double) j + 0.5D, (double) k, (double) l + 0.5D, 0.0F, 0.0F);
            worldIn.spawnEntity(entityspider);
            entityspider.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20 * 120, 10));
        }
    }

    public void placeItemframe(Random random, World world, int x, int y, int z, EnumFacing side, ItemStack stack) {
        int i1 = this.getXWithOffset(x, z);
        int j1 = this.getYWithOffset(y);
        int k1 = this.getZWithOffset(x, z);

        EntityItemFrame e = new EntityItemFrame(world, new BlockPos(i1, j1, k1), side);
        e.setDisplayedItem(stack);
        if (e.onValidSurface() && world.getEntitiesWithinAABB(EntityHanging.class, new AxisAlignedBB(i1 - .125, j1, k1 - .125, i1 + 1.125, j1 + 1, k1 + 1.125)).isEmpty())
            if (!world.isRemote)
                world.spawnEntity(e);
    }

    @Override
    protected IBlockState getBiomeSpecificBlockState(IBlockState blockstateIn) {
        if (structureType == 1 && blockstateIn.getBlock() == Blocks.COBBLESTONE)
            return Blocks.SANDSTONE.getStateFromMeta(1);

        blockstateIn = super.getBiomeSpecificBlockState(blockstateIn);

        if (blockstateIn.getBlock() == Blocks.WOODEN_SLAB)
            blockstateIn = this.structureType == 1 ? Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SAND) : (this.structureType == 3 ? Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockWoodSlab.VARIANT, BlockPlanks.EnumType.SPRUCE) : (this.structureType == 2 ? Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockWoodSlab.VARIANT, BlockPlanks.EnumType.ACACIA) : blockstateIn));

        if (blockstateIn.getBlock() == Blocks.OAK_FENCE)
            blockstateIn = this.structureType == 1 ? Blocks.COBBLESTONE_WALL.getDefaultState() : (this.structureType == 3 ? Blocks.SPRUCE_FENCE.getDefaultState() : (this.structureType == 2 ? Blocks.ACACIA_FENCE.getDefaultState() : blockstateIn));

        if (blockstateIn.getBlock() == Blocks.LEAVES)
            blockstateIn = this.structureType == 1 ? Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH) : (this.structureType == 3 ? Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE) : (this.structureType == 2 ? Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA) : blockstateIn));

        return blockstateIn;
    }

    private IBlockState biomeWall() {
        switch (this.structureType) {
            case 1:
                return Blocks.SANDSTONE.getStateFromMeta(2);
            case 2:
                return Blocks.HARDENED_CLAY.getDefaultState();
            case 3:
                return Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(12);
            default:
                return Blocks.CONCRETE.getStateFromMeta(8);
        }
    }

    @Override
    protected VillagerProfession chooseForgeProfession(int count, VillagerProfession prof) {
        return SpiderLabVillageManager.VILLAGER_PROFESSION_SCIENTIST;
    }

    @EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class SpiderLabVillageManager implements IVillageCreationHandler {

        public static VillagerRegistry.VillagerProfession VILLAGER_PROFESSION_SCIENTIST;
        public static VillagerRegistry.VillagerCareer VILLAGER_CAREER_SPIDER_SCIENTIST;

        @SubscribeEvent
        public static void onRegisterVillagerProfession(RegistryEvent.Register<VillagerProfession> e) {
            e.getRegistry().register(VILLAGER_PROFESSION_SCIENTIST = new VillagerProfession(HeroesExpansion.MODID + ":scientist", HeroesExpansion.MODID + ":textures/models/villager_scientist.png", HeroesExpansion.MODID + ":textures/models/villager_scientist_zombie.png"));

            VILLAGER_CAREER_SPIDER_SCIENTIST = new VillagerCareer(VILLAGER_PROFESSION_SCIENTIST, HeroesExpansion.MODID + ":spider_scientist");
            VILLAGER_CAREER_SPIDER_SCIENTIST.addTrade(1, new HEVillagerTrades.EmeraldForItemstack(new ItemStack(UtilitiesItems.INJECTION, 8), new EntityVillager.PriceInfo(1, 2)), new HEVillagerTrades.ItemstackForEmerald(new ItemStack(UtilitiesBlocks.EXTRACTOR), new EntityVillager.PriceInfo(4, 5)));
        }

        @Override
        public Village buildComponent(PieceWeight villagePiece, Start startPiece, List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int p5) {
            StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 15, 6, 10, facing);
            return (!canVillageGoDeeper(box)) || (StructureComponent.findIntersecting(pieces, box) != null) ? null : new VillageSpiderLabHouse(startPiece, p5, random, box, facing);
        }

        @Override
        public PieceWeight getVillagePieceWeight(Random random, int i) {
            return new PieceWeight(VillageSpiderLabHouse.class, 15, MathHelper.getInt(random, 0 + i, 1 + i));
        }

        @Override
        public Class<?> getComponentClass() {
            return VillageSpiderLabHouse.class;
        }
    }

}
