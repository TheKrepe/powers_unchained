package lucraft.mods.heroesexpansion.worldgen.norsevillage;

import com.google.common.collect.Lists;
import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.HELootTableList;
import lucraft.mods.heroesexpansion.capabilities.CapabilityOnceInWorldStructures;
import lucraft.mods.heroesexpansion.capabilities.IOnceInWorldStructures;
import lucraft.mods.heroesexpansion.entities.EntityTesseract;
import lucraft.mods.heroesexpansion.items.HEItems;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class StructureNorseVillagePieces {

    public static void registerVillagePieces() {
        MapGenStructureIO.registerStructureComponent(StructureNorseVillagePieces.House1.class, "NoViBH");
        MapGenStructureIO.registerStructureComponent(StructureNorseVillagePieces.Field1.class, "NoViDF");
        MapGenStructureIO.registerStructureComponent(StructureNorseVillagePieces.Torch.class, "NoViL");
        MapGenStructureIO.registerStructureComponent(StructureNorseVillagePieces.Church.class, "NoViST");
        MapGenStructureIO.registerStructureComponent(StructureNorseVillagePieces.House2.class, "NoViS");
        MapGenStructureIO.registerStructureComponent(StructureNorseVillagePieces.Start.class, "NoViStart");
        MapGenStructureIO.registerStructureComponent(StructureNorseVillagePieces.Path.class, "NoViSR");
        MapGenStructureIO.registerStructureComponent(StructureNorseVillagePieces.Well.class, "NoViW");
    }

    public static List<StructureNorseVillagePieces.PieceWeight> getStructureVillageWeightedPieceList(Random random, int size) {
        List<StructureNorseVillagePieces.PieceWeight> list = Lists.<StructureNorseVillagePieces.PieceWeight>newArrayList();
        list.add(new StructureNorseVillagePieces.PieceWeight(StructureNorseVillagePieces.Church.class, 20, MathHelper.getInt(random, 0, 1)));
        list.add(new StructureNorseVillagePieces.PieceWeight(StructureNorseVillagePieces.House1.class, 20, MathHelper.getInt(random, 0 + size, 2 + size)));
        list.add(new StructureNorseVillagePieces.PieceWeight(StructureNorseVillagePieces.Field1.class, 3, MathHelper.getInt(random, 1 + size, 4 + size)));
        list.add(new StructureNorseVillagePieces.PieceWeight(StructureNorseVillagePieces.House2.class, 15, MathHelper.getInt(random, 1, 1 + size)));
        Iterator<StructureNorseVillagePieces.PieceWeight> iterator = list.iterator();

        while (iterator.hasNext()) {
            if ((iterator.next()).villagePiecesLimit == 0) {
                iterator.remove();
            }
        }

        return list;
    }

    private static int updatePieceWeight(List<StructureNorseVillagePieces.PieceWeight> p_75079_0_) {
        boolean flag = false;
        int i = 0;

        for (StructureNorseVillagePieces.PieceWeight StructureNorseVillagePieces$pieceweight : p_75079_0_) {
            if (StructureNorseVillagePieces$pieceweight.villagePiecesLimit > 0 && StructureNorseVillagePieces$pieceweight.villagePiecesSpawned < StructureNorseVillagePieces$pieceweight.villagePiecesLimit) {
                flag = true;
            }

            i += StructureNorseVillagePieces$pieceweight.villagePieceWeight;
        }

        return flag ? i : -1;
    }

    private static StructureNorseVillagePieces.Village findAndCreateComponentFactory(StructureNorseVillagePieces.Start start, StructureNorseVillagePieces.PieceWeight weight, List<StructureComponent> structureComponents, Random rand, int structureMinX, int structureMinY, int structureMinZ, EnumFacing facing, int componentType) {
        Class<? extends StructureNorseVillagePieces.Village> oclass = weight.villagePieceClass;
        StructureNorseVillagePieces.Village StructureNorseVillagePieces$village = null;

        if (oclass == StructureNorseVillagePieces.Church.class) {
            StructureNorseVillagePieces$village = StructureNorseVillagePieces.Church.createPiece(start, structureComponents, rand, structureMinX, structureMinY, structureMinZ, facing, componentType);
        } else if (oclass == StructureNorseVillagePieces.House1.class) {
            StructureNorseVillagePieces$village = StructureNorseVillagePieces.House1.createPiece(start, structureComponents, rand, structureMinX, structureMinY, structureMinZ, facing, componentType);
        } else if (oclass == StructureNorseVillagePieces.Field1.class) {
            StructureNorseVillagePieces$village = StructureNorseVillagePieces.Field1.createPiece(start, structureComponents, rand, structureMinX, structureMinY, structureMinZ, facing, componentType);
        } else if (oclass == StructureNorseVillagePieces.House2.class) {
            StructureNorseVillagePieces$village = StructureNorseVillagePieces.House2.createPiece(start, structureComponents, rand, structureMinX, structureMinY, structureMinZ, facing, componentType);
        }

        return StructureNorseVillagePieces$village;
    }

    private static StructureNorseVillagePieces.Village generateComponent(StructureNorseVillagePieces.Start start, List<StructureComponent> structureComponents, Random rand, int structureMinX, int structureMinY, int structureMinZ, EnumFacing facing, int componentType) {
        int i = updatePieceWeight(start.structureVillageWeightedPieceList);

        if (i <= 0) {
            return null;
        } else {
            int j = 0;

            while (j < 5) {
                ++j;
                int k = rand.nextInt(i);

                for (StructureNorseVillagePieces.PieceWeight StructureNorseVillagePieces$pieceweight : start.structureVillageWeightedPieceList) {
                    k -= StructureNorseVillagePieces$pieceweight.villagePieceWeight;

                    if (k < 0) {
                        if (!StructureNorseVillagePieces$pieceweight.canSpawnMoreVillagePiecesOfType(componentType) || StructureNorseVillagePieces$pieceweight == start.lastPlaced && start.structureVillageWeightedPieceList.size() > 1) {
                            break;
                        }

                        StructureNorseVillagePieces.Village StructureNorseVillagePieces$village = findAndCreateComponentFactory(start, StructureNorseVillagePieces$pieceweight, structureComponents, rand, structureMinX, structureMinY, structureMinZ, facing, componentType);

                        if (StructureNorseVillagePieces$village != null) {
                            ++StructureNorseVillagePieces$pieceweight.villagePiecesSpawned;
                            start.lastPlaced = StructureNorseVillagePieces$pieceweight;

                            if (!StructureNorseVillagePieces$pieceweight.canSpawnMoreVillagePieces()) {
                                start.structureVillageWeightedPieceList.remove(StructureNorseVillagePieces$pieceweight);
                            }

                            return StructureNorseVillagePieces$village;
                        }
                    }
                }
            }

            StructureBoundingBox structureboundingbox = StructureNorseVillagePieces.Torch.findPieceBox(start, structureComponents, rand, structureMinX, structureMinY, structureMinZ, facing);

            if (structureboundingbox != null) {
                return new StructureNorseVillagePieces.Torch(start, componentType, rand, structureboundingbox, facing);
            } else {
                return null;
            }
        }
    }

    private static StructureComponent generateAndAddComponent(StructureNorseVillagePieces.Start start, List<StructureComponent> structureComponents, Random rand, int structureMinX, int structureMinY, int structureMinZ, EnumFacing facing, int componentType) {
        if (componentType > 50) {
            return null;
        } else if (Math.abs(structureMinX - start.getBoundingBox().minX) <= 112 && Math.abs(structureMinZ - start.getBoundingBox().minZ) <= 112) {
            StructureComponent structurecomponent = generateComponent(start, structureComponents, rand, structureMinX, structureMinY, structureMinZ, facing, componentType + 1);

            if (structurecomponent != null) {
                structureComponents.add(structurecomponent);
                start.pendingHouses.add(structurecomponent);
                return structurecomponent;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static StructureComponent generateAndAddRoadPiece(StructureNorseVillagePieces.Start start, List<StructureComponent> p_176069_1_, Random rand, int p_176069_3_, int p_176069_4_, int p_176069_5_, EnumFacing facing, int p_176069_7_) {
        if (p_176069_7_ > 3 + start.terrainType) {
            return null;
        } else if (Math.abs(p_176069_3_ - start.getBoundingBox().minX) <= 112 && Math.abs(p_176069_5_ - start.getBoundingBox().minZ) <= 112) {
            StructureBoundingBox structureboundingbox = StructureNorseVillagePieces.Path.findPieceBox(start, p_176069_1_, rand, p_176069_3_, p_176069_4_, p_176069_5_, facing);

            if (structureboundingbox != null && structureboundingbox.minY > 10) {
                StructureComponent structurecomponent = new StructureNorseVillagePieces.Path(start, p_176069_7_, rand, structureboundingbox, facing);
                p_176069_1_.add(structurecomponent);
                start.pendingRoads.add(structurecomponent);
                return structurecomponent;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static class Church extends StructureNorseVillagePieces.Village {

        public Church() {
        }

        public Church(StructureNorseVillagePieces.Start start, int type, Random rand, StructureBoundingBox p_i45564_4_, EnumFacing facing) {
            super(start, type);
            this.setCoordBaseMode(facing);
            this.boundingBox = p_i45564_4_;
        }

        public static StructureNorseVillagePieces.Church createPiece(StructureNorseVillagePieces.Start start, List<StructureComponent> p_175854_1_, Random rand, int p_175854_3_, int p_175854_4_, int p_175854_5_, EnumFacing facing, int p_175854_7_) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175854_3_, p_175854_4_, p_175854_5_, 0, 0, 0, 22, 22, 14, facing);
            return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175854_1_, structureboundingbox) == null ? new StructureNorseVillagePieces.Church(start, p_175854_7_, rand, structureboundingbox, facing) : null;
        }

        /**
         * second Part of Structure generating, this for example places Spiderwebs, Mob
         * Spawners, it closes Mineshafts at the end, it adds Fences...
         */
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            if (this.averageGroundLvl < 0) {
                this.averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.averageGroundLvl < 0) {
                    return true;
                }

                this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 22 - 1, 0);
            }

            IBlockState brickState = Blocks.STONEBRICK.getDefaultState();

            for (int i = 1; i <= 21; i++) {
                for (int j = 5; j <= 13; j++) {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.COBBLESTONE.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            for (int i = 13; i <= 17; i++) {
                for (int j = 1; j <= 5; j++) {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.COBBLESTONE.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            // Walls & Roof(s)
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 5, 21, 7, 13, brickState, brickState, false);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 14, 1, 2, 16, 6, 5);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 2, 1, 6, 20, 7, 12);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 8, 6, 1, 8, 12, brickState, brickState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 9, 7, 1, 9, 11, brickState, brickState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 10, 8, 1, 10, 10, brickState, brickState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 21, 8, 6, 21, 8, 12, brickState, brickState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 21, 9, 7, 21, 9, 11, brickState, brickState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 21, 10, 8, 21, 10, 10, brickState, brickState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 7, 4, 22, 7, 4, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 8, 5, 22, 8, 5, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 9, 6, 22, 9, 6, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 10, 7, 22, 10, 7, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 7, 14, 22, 7, 14, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 8, 13, 22, 8, 13, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 9, 12, 22, 9, 12, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 10, 11, 22, 10, 11, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 11, 8, 22, 11, 10, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE), Blocks.STONE_SLAB.getDefaultState(), false);

            for (int i = 0; i < 4; i++) {
                this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 0, 7 + i, 5 + i, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 22, 7 + i, 5 + i, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 0, 7 + i, 13 - i, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 22, 7 + i, 13 - i, structureBoundingBoxIn);
            }

            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST), 0, 11, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST), 22, 11, 9, structureBoundingBoxIn);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 0, 1, 17, 16, 5, brickState, brickState, false);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 14, 1, 2, 16, 16, 4);

            for (int i = 0; i < 3; i++) {
                int y = (i + 1) * 5;
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, y, 1, 16, y, 1, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), Blocks.STONE_STAIRS.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_RIGHT), 13, y, 1, structureBoundingBoxIn);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 17, y, 2, 17, y, 4, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), Blocks.STONE_STAIRS.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_LEFT), 17, y, 1, structureBoundingBoxIn);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, y, 2, 13, y, 4, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), Blocks.STONE_STAIRS.getDefaultState(), false);

                if (i > 0) {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, y, 5, 16, y, 5, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), Blocks.STONE_STAIRS.getDefaultState(), false);
                    this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_RIGHT), 17, y, 5, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_LEFT), 13, y, 5, structureBoundingBoxIn);
                }
            }

            this.fillWithAir(worldIn, structureBoundingBoxIn, 15, 1, 1, 15, 2, 1);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 15, 3, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 15, 8, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK), 15, 6, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), 15, 7, 1, structureBoundingBoxIn);

            for (int i = 0; i < 3; i++) {
                this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK), 15, 11 + i, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK), 15, 11 + i, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK), 13, 11 + i, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK), 17, 11 + i, 3, structureBoundingBoxIn);
            }

            this.fillWithAir(worldIn, structureBoundingBoxIn, 14, 1, 5, 16, 4, 5);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 16, 4, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 14, 4, 5, structureBoundingBoxIn);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 16, 0, 17, 16, 0, Blocks.STONE_STAIRS.getDefaultState(), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 18, 16, 1, 18, 16, 5, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 16, 1, 12, 16, 5, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 16, 6, 17, 16, 6, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 16, 1, 17, 17, 5, Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE), 13, 17, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE), 17, 17, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE), 13, 17, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE), 17, 17, 5, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, 18, 1, 16, 18, 1, Blocks.STONE_STAIRS.getDefaultState(), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, 18, 5, 16, 18, 5, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 18, 2, 13, 18, 4, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 17, 18, 2, 17, 18, 4, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, 18, 2, 16, 19, 4, Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState(), 15, 19, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_LEFT), 16, 19, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST), 16, 19, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_LEFT), 16, 19, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 15, 19, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_LEFT), 14, 19, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST), 14, 19, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_LEFT), 14, 19, 2, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 20, 3, 15, 22, 3, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, 21, 3, 16, 21, 3, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);

            // Windows
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 3, 1, 5, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 5, 3, 4, 5, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 3, 5, 5, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 7, 1, 5, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 5, 7, 4, 5, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 7, 5, 5, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 11, 1, 5, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 2, 5, 11, 4, 5, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 11, 5, 5, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 19, 1, 5, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 19, 2, 5, 19, 4, 5, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 19, 5, 5, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 3, 1, 13, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 13, 3, 4, 13, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 3, 5, 13, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 7, 1, 13, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 13, 7, 4, 13, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 7, 5, 13, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 11, 1, 13, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 2, 13, 11, 4, 13, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 11, 5, 13, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 15, 1, 13, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 2, 13, 15, 4, 13, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 15, 5, 13, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 19, 1, 13, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 19, 2, 13, 19, 4, 13, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 19, 5, 13, structureBoundingBoxIn);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 8, 1, 5, 10, Blocks.GLASS_PANE.getDefaultState(), Blocks.GLASS_PANE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.GLASS_PANE.getDefaultState(), 1, 6, 9, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 8, 1, 1, 10, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST), Blocks.STONE_BRICK_STAIRS.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 1, 7, 9, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST), 1, 8, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.GLASS_PANE.getDefaultState(), 1, 9, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 1, 10, 9, structureBoundingBoxIn);

            // Interior
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 11, 3, 15, 13, 3, Blocks.GOLD_BLOCK.getDefaultState(), Blocks.GOLD_BLOCK.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 14, 3, 15, 15, 3, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);

            for (int i = 0; i < 4; i++) {
                int x = 5 + i * 4;
                this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), x, 5, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM), x, 6, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), x, 6, 7, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK).withProperty(BlockStoneSlab.HALF, BlockSlab.EnumBlockHalf.TOP), x, 6, 8, structureBoundingBoxIn);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 7, 8, x, 7, 10, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK), Blocks.STONE_SLAB.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK).withProperty(BlockStoneSlab.HALF, BlockSlab.EnumBlockHalf.TOP), x, 6, 10, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), x, 6, 11, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM), x, 6, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), x, 5, 12, structureBoundingBoxIn);
                this.placeTorch(worldIn, EnumFacing.NORTH, x, 3, 6, structureBoundingBoxIn);
                this.placeTorch(worldIn, EnumFacing.SOUTH, x, 3, 12, structureBoundingBoxIn);
            }

            for (int i = 0; i < 5; i++) {
                int x = 6 + i * 2;
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 1, i < 4 ? 6 : 11, x, 1, 12, Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST), Blocks.DARK_OAK_STAIRS.getDefaultState(), false);
            }

            this.fillWithAir(worldIn, structureBoundingBoxIn, 6, 1, 8, 12, 1, 10);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 8, 3, 1, 10, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK), Blocks.STONE_SLAB.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.STONEBRICK.getDefaultState(), 3, 1, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.DARK_OAK_FENCE.getDefaultState(), 3, 2, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState(), 3, 3, 9, structureBoundingBoxIn);

            BlockPos pos = new BlockPos(getXWithOffset(20, 9), getYWithOffset(2), getZWithOffset(20, 9));
            IOnceInWorldStructures cap = worldIn.getCapability(CapabilityOnceInWorldStructures.ONCE_IN_WORLD_STRUCTURES_CAP, null);
            BlockPos saved = cap.getPosition(CapabilityOnceInWorldStructures.EnumOnceInWorld.T);
            boolean flag = false;

            if (!HEConfig.worldGeneration.GENERATE_TESSERACT)
                flag = false;
            else if (saved != null && saved.equals(pos))
                flag = true;
            else if (saved == null && new Random(pos.getX() + pos.getY() + pos.getZ()).nextInt(10) == 0) {
                cap.addToWorld(CapabilityOnceInWorldStructures.EnumOnceInWorld.T, pos);
                flag = true;
            }

            int x = flag ? 19 : 20;
            IBlockState leaves = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK).withProperty(BlockLeaves.DECAYABLE, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 1, 7, x, 1, 11, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 2, 8, x, 2, 10, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 4, 8, x, 4, 10, leaves, leaves, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 5, 6, x, 5, 12, leaves, leaves, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 5, 8, x, 5, 10, Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), Blocks.LOG2.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 6, 6, x, 7, 12, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), x, 6, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), x, 6, 11, structureBoundingBoxIn);
            this.setBlockState(worldIn, leaves, x, 7, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, leaves, x, 7, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, leaves, x, 7, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, leaves, x, 7, 12, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 8, 7, x, 8, 11, leaves, leaves, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 9, 8, x, 9, 10, leaves, leaves, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 8, 8, x, 8, 10, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, x, 1, 9, x, 7, 9, Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK), Blocks.LOG2.getDefaultState(), false);

            if (flag) {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 20, 1, 6, 20, 8, 12, Blocks.STONEBRICK.getDefaultState(), Blocks.STONEBRICK.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 20, 9, 7, 20, 9, 11, Blocks.STONEBRICK.getDefaultState(), Blocks.STONEBRICK.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 20, 10, 8, 20, 10, 10, Blocks.STONEBRICK.getDefaultState(), Blocks.STONEBRICK.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), 20, 2, 9, structureBoundingBoxIn);
                spawnT(worldIn, structureBoundingBoxIn, 20, 2, 9);
            }

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 15, 1, 8, 3);

            return true;
        }

        protected int chooseProfession(int villagersSpawnedIn, int currentVillagerProfession) {
            return 2;
        }
    }

    public static class Field1 extends StructureNorseVillagePieces.Village {
        /**
         * First crop type for this field.
         */
        private Block cropTypeA;
        /**
         * Second crop type for this field.
         */
        private Block cropTypeB;
        /**
         * Third crop type for this field.
         */
        private Block cropTypeC;
        /**
         * Fourth crop type for this field.
         */
        private Block cropTypeD;

        public Field1() {
        }

        public Field1(StructureNorseVillagePieces.Start start, int type, Random rand, StructureBoundingBox p_i45570_4_, EnumFacing facing) {
            super(start, type);
            this.setCoordBaseMode(facing);
            this.boundingBox = p_i45570_4_;
            this.cropTypeA = this.getRandomCropType(rand);
            this.cropTypeB = this.getRandomCropType(rand);
            this.cropTypeC = this.getRandomCropType(rand);
            this.cropTypeD = this.getRandomCropType(rand);
        }

        /**
         * (abstract) Helper method to write subclass data to NBT
         */
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("CA", Block.REGISTRY.getIDForObject(this.cropTypeA));
            tagCompound.setInteger("CB", Block.REGISTRY.getIDForObject(this.cropTypeB));
            tagCompound.setInteger("CC", Block.REGISTRY.getIDForObject(this.cropTypeC));
            tagCompound.setInteger("CD", Block.REGISTRY.getIDForObject(this.cropTypeD));
        }

        /**
         * (abstract) Helper method to read subclass data from NBT
         */
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
            super.readStructureFromNBT(tagCompound, p_143011_2_);
            this.cropTypeA = Block.getBlockById(tagCompound.getInteger("CA"));
            this.cropTypeB = Block.getBlockById(tagCompound.getInteger("CB"));
            this.cropTypeC = Block.getBlockById(tagCompound.getInteger("CC"));
            this.cropTypeD = Block.getBlockById(tagCompound.getInteger("CD"));

            if (!(this.cropTypeA instanceof BlockCrops)) {
                this.cropTypeA = Blocks.WHEAT;
            }

            if (!(this.cropTypeB instanceof BlockCrops)) {
                this.cropTypeB = Blocks.CARROTS;
            }

            if (!(this.cropTypeC instanceof BlockCrops)) {
                this.cropTypeC = Blocks.POTATOES;
            }

            if (!(this.cropTypeD instanceof BlockCrops)) {
                this.cropTypeD = Blocks.BEETROOTS;
            }
        }

        private Block getRandomCropType(Random rand) {
            switch (rand.nextInt(10)) {
                case 0:
                case 1:
                    return Blocks.CARROTS;
                case 2:
                case 3:
                    return Blocks.POTATOES;
                case 4:
                    return Blocks.BEETROOTS;
                default:
                    return Blocks.WHEAT;
            }
        }

        public static StructureNorseVillagePieces.Field1 createPiece(StructureNorseVillagePieces.Start start, List<StructureComponent> p_175851_1_, Random rand, int p_175851_3_, int p_175851_4_, int p_175851_5_, EnumFacing facing, int p_175851_7_) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175851_3_, p_175851_4_, p_175851_5_, 0, 0, 0, 13, 4, 9, facing);
            return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175851_1_, structureboundingbox) == null ? new StructureNorseVillagePieces.Field1(start, p_175851_7_, rand, structureboundingbox, facing) : null;
        }

        /**
         * second Part of Structure generating, this for example places Spiderwebs, Mob
         * Spawners, it closes Mineshafts at the end, it adds Fences...
         */
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            if (this.averageGroundLvl < 0) {
                this.averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.averageGroundLvl < 0) {
                    return true;
                }

                this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 4 - 1, 0);
            }

            IBlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 12, 4, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 0, 1, 8, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 0, 1, 11, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 0, 8, iblockstate, iblockstate, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 0, 6, 0, 8, iblockstate, iblockstate, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 0, 0, 12, 0, 8, iblockstate, iblockstate, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 11, 0, 0, iblockstate, iblockstate, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 8, 11, 0, 8, iblockstate, iblockstate, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 0, 1, 3, 0, 7, Blocks.WATER.getDefaultState(), Blocks.WATER.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 0, 1, 9, 0, 7, Blocks.WATER.getDefaultState(), Blocks.WATER.getDefaultState(), false);

            for (int i = 1; i <= 7; ++i) {
                int j = ((BlockCrops) this.cropTypeA).getMaxAge();
                int k = j / 3;
                this.setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getInt(randomIn, k, j)), 1, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getInt(randomIn, k, j)), 2, 1, i, structureBoundingBoxIn);
                int l = ((BlockCrops) this.cropTypeB).getMaxAge();
                int i1 = l / 3;
                this.setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getInt(randomIn, i1, l)), 4, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getInt(randomIn, i1, l)), 5, 1, i, structureBoundingBoxIn);
                int j1 = ((BlockCrops) this.cropTypeC).getMaxAge();
                int k1 = j1 / 3;
                this.setBlockState(worldIn, this.cropTypeC.getStateFromMeta(MathHelper.getInt(randomIn, k1, j1)), 7, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeC.getStateFromMeta(MathHelper.getInt(randomIn, k1, j1)), 8, 1, i, structureBoundingBoxIn);
                int l1 = ((BlockCrops) this.cropTypeD).getMaxAge();
                int i2 = l1 / 3;
                this.setBlockState(worldIn, this.cropTypeD.getStateFromMeta(MathHelper.getInt(randomIn, i2, l1)), 10, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeD.getStateFromMeta(MathHelper.getInt(randomIn, i2, l1)), 11, 1, i, structureBoundingBoxIn);
            }

            for (int j2 = 0; j2 < 9; ++j2) {
                for (int k2 = 0; k2 < 13; ++k2) {
                    this.clearCurrentPositionBlocksUpwards(worldIn, k2, 4, j2, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.DIRT.getDefaultState(), k2, -1, j2, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class House1 extends StructureNorseVillagePieces.Village {

        public House1() {
        }

        public House1(StructureNorseVillagePieces.Start start, int type, Random rand, StructureBoundingBox p_i45571_4_, EnumFacing facing) {
            super(start, type);
            this.setCoordBaseMode(facing);
            this.boundingBox = p_i45571_4_;
        }

        public static StructureNorseVillagePieces.House1 createPiece(StructureNorseVillagePieces.Start start, List<StructureComponent> p_175850_1_, Random rand, int p_175850_3_, int p_175850_4_, int p_175850_5_, EnumFacing facing, int p_175850_7_) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175850_3_, p_175850_4_, p_175850_5_, 0, 0, 0, 10, 11, 13, facing);
            return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175850_1_, structureboundingbox) == null ? new StructureNorseVillagePieces.House1(start, p_175850_7_, rand, structureboundingbox, facing) : null;
        }

        /**
         * second Part of Structure generating, this for example places Spiderwebs, Mob
         * Spawners, it closes Mineshafts at the end, it adds Fences...
         */
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            if (this.averageGroundLvl < 0) {
                this.averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.averageGroundLvl < 0) {
                    return true;
                }

                this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 9 - 1, 0);
            }

            IBlockState sprucePlanksState = Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE);
            IBlockState oakLogsState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
            IBlockState glassPaneState = Blocks.GLASS_PANE.getDefaultState();
            IBlockState spruceStairState = Blocks.SPRUCE_STAIRS.getDefaultState();
            IBlockState woolState = Blocks.WOOL.getDefaultState();
            IBlockState bricksState = Blocks.STONEBRICK.getDefaultState();

            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 11; j++) {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.COBBLESTONE.getDefaultState(), i, 0, j, structureBoundingBoxIn);
                }
            }

            // Walls & Roof
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 8, 1, 11, sprucePlanksState, sprucePlanksState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 8, 6, 1, bricksState, bricksState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 11, 8, 6, 11, bricksState, bricksState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 1, 6, 11, bricksState, bricksState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 1, 8, 6, 11, bricksState, bricksState, false);

            this.setBlockState(worldIn, oakLogsState, 1, 7, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, oakLogsState, 8, 7, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, oakLogsState, 1, 7, 11, structureBoundingBoxIn);
            this.setBlockState(worldIn, oakLogsState, 8, 7, 11, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 7, 1, 7, 7, 1, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.X), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 7, 11, 7, 7, 11, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.X), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 7, 2, 1, 7, 10, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 7, 2, 8, 7, 10, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 8, 1, 8, 8, 1, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 9, 1, 7, 9, 1, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 10, 1, 6, 10, 1, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 11, 1, 5, 11, 1, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 8, 11, 8, 8, 11, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 9, 11, 7, 9, 11, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 10, 11, 6, 10, 11, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 11, 11, 5, 11, 11, woolState, woolState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 8, 0, 0, 8, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 9, 0, 1, 9, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 10, 0, 2, 10, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 11, 0, 3, 11, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 12, 0, 4, 12, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 8, 0, 9, 8, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 9, 0, 8, 9, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 10, 0, 7, 10, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 11, 0, 6, 11, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 12, 0, 5, 12, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);

            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 1, 8, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 2, 9, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 3, 10, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 4, 11, 0, structureBoundingBoxIn);

            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 1, 8, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 2, 9, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 3, 10, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 4, 11, 12, structureBoundingBoxIn);

            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 8, 8, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 7, 9, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 6, 10, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 5, 11, 0, structureBoundingBoxIn);

            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 8, 8, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 7, 9, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 6, 10, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 5, 11, 12, structureBoundingBoxIn);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 9, 1, 6, 9, 1, oakLogsState, oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 9, 1, 5, 9, 1, glassPaneState, glassPaneState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 9, 11, 6, 9, 11, oakLogsState, oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 9, 11, 5, 9, 11, glassPaneState, glassPaneState, false);

            // Void
            this.fillWithAir(worldIn, structureBoundingBoxIn, 2, 2, 2, 7, 7, 10);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 1, 8, 2, 8, 8, 10);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 2, 9, 2, 7, 9, 10);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 3, 10, 2, 6, 10, 10);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 4, 11, 2, 5, 11, 10);

            // 2nd Floor
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 7, 2, 7, 7, 10, sprucePlanksState, sprucePlanksState, false);

            // Windows
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 1, 6, 5, 1, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.X), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 3, 1, 6, 4, 1, glassPaneState, glassPaneState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 8, 1, 5, 9, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 8, 1, 4, 9, glassPaneState, glassPaneState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 3, 1, 5, 4, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 3, 1, 4, 4, glassPaneState, glassPaneState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 8, 8, 5, 9, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 3, 8, 8, 4, 9, glassPaneState, glassPaneState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 3, 8, 5, 4, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 3, 3, 8, 4, 4, glassPaneState, glassPaneState, false);

            // Door
            this.generateDoor(worldIn, structureBoundingBoxIn, randomIn, 3, 2, 1, EnumFacing.NORTH, Blocks.SPRUCE_DOOR);

            // Interior
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 2, 7, 2, 2, Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), Blocks.DARK_OAK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 4, 7, 2, 4, Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), Blocks.DARK_OAK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 3, 7, 2, 3, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 3, 7, 3, 3, Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.BROWN), Blocks.CARPET.getDefaultState(), false);
            this.placeTorch(worldIn, EnumFacing.WEST, 7, 4, 6, structureBoundingBoxIn);
            this.placeTorch(worldIn, EnumFacing.EAST, 2, 4, 6, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, 4, 2, 2, 8, Blocks.FURNACE.getDefaultState().withProperty(BlockFurnace.FACING, EnumFacing.EAST), Blocks.FURNACE.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, 5, 2, 2, 7, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.HALF, BlockSlab.EnumBlockHalf.TOP), Blocks.STONE_SLAB.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.CAULDRON.getDefaultState().withProperty(BlockCauldron.LEVEL, 3), 2, 2, 6, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 6, 5, 2, 8, Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.BROWN), Blocks.CARPET.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 10, 7, 3, 10, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 10, 3, 7, 10, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), Blocks.LADDER.getDefaultState(), false);
            this.placeTorch(worldIn, EnumFacing.NORTH, 6, 9, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 1, 8, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 5, 11, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 8, 8, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 5, 11, 2, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 8, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 9, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 2, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 2, 9, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 6, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 9, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 8, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 9, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 2, 8, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 2, 9, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 4, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 8, 5, 5, 9, 5, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 3, 9, 5, structureBoundingBoxIn);

            this.generateChest(worldIn, structureBoundingBoxIn, new Random(), 7, 8, 9, HELootTableList.NORSE_VILLAGE_HOUSE);
            this.generateChest(worldIn, structureBoundingBoxIn, new Random(), 2, 8, 2, HELootTableList.NORSE_VILLAGE_HOUSE);

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 4, 2, 6, 3);
            return true;
        }

        @Override
        protected int chooseProfession(int villagersSpawnedIn, int currentVillagerProfession) {
            return 0;
        }
    }

    public static class House2 extends StructureNorseVillagePieces.Village {
        private boolean hasMadeChest;

        public House2() {
        }

        public House2(StructureNorseVillagePieces.Start start, int type, Random rand, StructureBoundingBox p_i45563_4_, EnumFacing facing) {
            super(start, type);
            this.setCoordBaseMode(facing);
            this.boundingBox = p_i45563_4_;
        }

        /**
         * (abstract) Helper method to write subclass data to NBT
         */
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Chest", this.hasMadeChest);
        }

        /**
         * (abstract) Helper method to read subclass data from NBT
         */
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
            super.readStructureFromNBT(tagCompound, p_143011_2_);
            this.hasMadeChest = tagCompound.getBoolean("Chest");
        }

        public static StructureNorseVillagePieces.House1 createPiece(StructureNorseVillagePieces.Start start, List<StructureComponent> p_175850_1_, Random rand, int p_175850_3_, int p_175850_4_, int p_175850_5_, EnumFacing facing, int p_175850_7_) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175850_3_, p_175850_4_, p_175850_5_, 0, 0, 0, 10, 11, 13, facing);
            return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(p_175850_1_, structureboundingbox) == null ? new StructureNorseVillagePieces.House1(start, p_175850_7_, rand, structureboundingbox, facing) : null;
        }

        /**
         * second Part of Structure generating, this for example places Spiderwebs, Mob
         * Spawners, it closes Mineshafts at the end, it adds Fences...
         */
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            if (this.averageGroundLvl < 0) {
                this.averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.averageGroundLvl < 0) {
                    return true;
                }

                this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 9 - 1, 0);
            }

            IBlockState sprucePlanksState = Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE);
            IBlockState oakLogsState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
            IBlockState glassPaneState = Blocks.GLASS_PANE.getDefaultState();
            IBlockState spruceStairState = Blocks.SPRUCE_STAIRS.getDefaultState();
            IBlockState woolState = Blocks.COBBLESTONE.getDefaultState();
            IBlockState bricksState = Blocks.STONEBRICK.getDefaultState();

            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 11; j++) {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.COBBLESTONE.getDefaultState(), i, 0, j, structureBoundingBoxIn);
                }
            }

            // Walls & Roof
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 8, 1, 11, sprucePlanksState, sprucePlanksState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 8, 6, 1, bricksState, bricksState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 11, 8, 6, 11, bricksState, bricksState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 1, 6, 11, bricksState, bricksState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 1, 8, 6, 11, bricksState, bricksState, false);

            this.setBlockState(worldIn, oakLogsState, 1, 7, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, oakLogsState, 8, 7, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, oakLogsState, 1, 7, 11, structureBoundingBoxIn);
            this.setBlockState(worldIn, oakLogsState, 8, 7, 11, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 7, 1, 7, 7, 1, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.X), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 7, 11, 7, 7, 11, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.X), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 7, 2, 1, 7, 10, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 7, 2, 8, 7, 10, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 8, 1, 8, 8, 1, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 9, 1, 7, 9, 1, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 10, 1, 6, 10, 1, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 11, 1, 5, 11, 1, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 8, 11, 8, 8, 11, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 9, 11, 7, 9, 11, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 10, 11, 6, 10, 11, woolState, woolState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 11, 11, 5, 11, 11, woolState, woolState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 8, 0, 0, 8, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 9, 0, 1, 9, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 10, 0, 2, 10, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 11, 0, 3, 11, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 12, 0, 4, 12, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST), spruceStairState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 8, 0, 9, 8, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 9, 0, 8, 9, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 10, 0, 7, 10, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 11, 0, 6, 11, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 12, 0, 5, 12, 12, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST), spruceStairState, false);

            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 1, 8, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 2, 9, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 3, 10, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 4, 11, 0, structureBoundingBoxIn);

            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 1, 8, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 2, 9, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 3, 10, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 4, 11, 12, structureBoundingBoxIn);

            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 8, 8, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 7, 9, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 6, 10, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 5, 11, 0, structureBoundingBoxIn);

            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 8, 8, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 7, 9, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 6, 10, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, spruceStairState.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP), 5, 11, 12, structureBoundingBoxIn);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 9, 1, 6, 9, 1, oakLogsState, oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 9, 1, 5, 9, 1, glassPaneState, glassPaneState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 9, 11, 6, 9, 11, oakLogsState, oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 9, 11, 5, 9, 11, glassPaneState, glassPaneState, false);

            // Void
            this.fillWithAir(worldIn, structureBoundingBoxIn, 2, 2, 2, 7, 7, 10);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 1, 8, 2, 8, 8, 10);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 2, 9, 2, 7, 9, 10);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 3, 10, 2, 6, 10, 10);
            this.fillWithAir(worldIn, structureBoundingBoxIn, 4, 11, 2, 5, 11, 10);

            // 2nd Floor
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 7, 2, 7, 7, 10, sprucePlanksState, sprucePlanksState, false);

            // Windows
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 1, 6, 5, 1, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.X), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 3, 1, 6, 4, 1, glassPaneState, glassPaneState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 8, 1, 5, 9, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 8, 1, 4, 9, glassPaneState, glassPaneState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 3, 1, 5, 4, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 3, 1, 4, 4, glassPaneState, glassPaneState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 8, 8, 5, 9, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 3, 8, 8, 4, 9, glassPaneState, glassPaneState, false);

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 3, 8, 5, 4, oakLogsState.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Z), oakLogsState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 3, 3, 8, 4, 4, glassPaneState, glassPaneState, false);

            // Door
            this.generateDoor(worldIn, structureBoundingBoxIn, randomIn, 3, 2, 1, EnumFacing.NORTH, Blocks.SPRUCE_DOOR);

            // Interior
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 2, 7, 2, 2, Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), Blocks.DARK_OAK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 4, 7, 2, 4, Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), Blocks.DARK_OAK_STAIRS.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 3, 7, 2, 3, Blocks.DARK_OAK_FENCE.getDefaultState(), Blocks.DARK_OAK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 3, 7, 3, 3, Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.BROWN), Blocks.CARPET.getDefaultState(), false);
            this.placeTorch(worldIn, EnumFacing.WEST, 7, 4, 6, structureBoundingBoxIn);
            this.placeTorch(worldIn, EnumFacing.EAST, 2, 4, 6, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, 4, 2, 2, 8, Blocks.FURNACE.getDefaultState().withProperty(BlockFurnace.FACING, EnumFacing.EAST), Blocks.FURNACE.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, 5, 2, 2, 7, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.HALF, BlockSlab.EnumBlockHalf.TOP), Blocks.STONE_SLAB.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.CAULDRON.getDefaultState().withProperty(BlockCauldron.LEVEL, 3), 2, 2, 6, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 6, 5, 2, 8, Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.BROWN), Blocks.CARPET.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 10, 7, 3, 10, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 10, 3, 7, 10, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), Blocks.LADDER.getDefaultState(), false);
            this.placeTorch(worldIn, EnumFacing.NORTH, 6, 9, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 1, 8, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 5, 11, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 8, 8, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 5, 11, 2, structureBoundingBoxIn);

            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 8, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 9, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 2, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 2, 9, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 6, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 9, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 8, 8, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 8, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 7, 9, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 2, 8, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 2, 9, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.BOOKSHELF.getDefaultState(), 1, 8, 4, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 8, 5, 5, 9, 5, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.WEB.getDefaultState(), 3, 9, 5, structureBoundingBoxIn);

            this.generateChest(worldIn, structureBoundingBoxIn, new Random(), 7, 8, 9, HELootTableList.NORSE_VILLAGE_HOUSE);
            this.generateChest(worldIn, structureBoundingBoxIn, new Random(), 2, 8, 2, HELootTableList.NORSE_VILLAGE_HOUSE);

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 4, 2, 6, 3);
            return true;
        }

        protected int chooseProfession(int villagersSpawnedIn, int currentVillagerProfession) {
            return 3;
        }
    }

    public static class Path extends StructureNorseVillagePieces.Road {
        private int length;

        public Path() {
        }

        public Path(StructureNorseVillagePieces.Start start, int p_i45562_2_, Random rand, StructureBoundingBox p_i45562_4_, EnumFacing facing) {
            super(start, p_i45562_2_);
            this.setCoordBaseMode(facing);
            this.boundingBox = p_i45562_4_;
            this.length = Math.max(p_i45562_4_.getXSize(), p_i45562_4_.getZSize());
        }

        /**
         * (abstract) Helper method to write subclass data to NBT
         */
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("Length", this.length);
        }

        /**
         * (abstract) Helper method to read subclass data from NBT
         */
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
            super.readStructureFromNBT(tagCompound, p_143011_2_);
            this.length = tagCompound.getInteger("Length");
        }

        /**
         * Initiates construction of the Structure Component picked, at the current
         * Location of StructGen
         */
        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand) {
            boolean flag = false;

            for (int i = rand.nextInt(5); i < this.length - 8; i += 2 + rand.nextInt(5)) {
                StructureComponent structurecomponent = this.getNextComponentNN((Start) componentIn, listIn, rand, 0, i);

                if (structurecomponent != null) {
                    i += Math.max(structurecomponent.getBoundingBox().getXSize(), structurecomponent.getBoundingBox().getZSize());
                    flag = true;
                }
            }

            for (int j = rand.nextInt(5); j < this.length - 8; j += 2 + rand.nextInt(5)) {
                StructureComponent structurecomponent1 = this.getNextComponentPP((Start) componentIn, listIn, rand, 0, j);

                if (structurecomponent1 != null) {
                    j += Math.max(structurecomponent1.getBoundingBox().getXSize(), structurecomponent1.getBoundingBox().getZSize());
                    flag = true;
                }
            }

            EnumFacing enumfacing = this.getCoordBaseMode();

            if (flag && rand.nextInt(3) > 0 && enumfacing != null) {
                switch (enumfacing) {
                    case NORTH:
                    default:
                        StructureNorseVillagePieces.generateAndAddRoadPiece((Start) componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.WEST, this.getComponentType());
                        break;
                    case SOUTH:
                        StructureNorseVillagePieces.generateAndAddRoadPiece((Start) componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, EnumFacing.WEST, this.getComponentType());
                        break;
                    case WEST:
                        StructureNorseVillagePieces.generateAndAddRoadPiece((Start) componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
                        break;
                    case EAST:
                        StructureNorseVillagePieces.generateAndAddRoadPiece((Start) componentIn, listIn, rand, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
                }
            }

            if (flag && rand.nextInt(3) > 0 && enumfacing != null) {
                switch (enumfacing) {
                    case NORTH:
                    default:
                        StructureNorseVillagePieces.generateAndAddRoadPiece((Start) componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.EAST, this.getComponentType());
                        break;
                    case SOUTH:
                        StructureNorseVillagePieces.generateAndAddRoadPiece((Start) componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, EnumFacing.EAST, this.getComponentType());
                        break;
                    case WEST:
                        StructureNorseVillagePieces.generateAndAddRoadPiece((Start) componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
                        break;
                    case EAST:
                        StructureNorseVillagePieces.generateAndAddRoadPiece((Start) componentIn, listIn, rand, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
                }
            }
        }

        public static StructureBoundingBox findPieceBox(StructureNorseVillagePieces.Start start, List<StructureComponent> p_175848_1_, Random rand, int p_175848_3_, int p_175848_4_, int p_175848_5_, EnumFacing facing) {
            for (int i = 7 * MathHelper.getInt(rand, 3, 5); i >= 7; i -= 7) {
                StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175848_3_, p_175848_4_, p_175848_5_, 0, 0, 0, 3, 3, i, facing);

                if (StructureComponent.findIntersecting(p_175848_1_, structureboundingbox) == null) {
                    return structureboundingbox;
                }
            }

            return null;
        }

        /**
         * second Part of Structure generating, this for example places Spiderwebs, Mob
         * Spawners, it closes Mineshafts at the end, it adds Fences...
         */
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            IBlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.GRAVEL.getDefaultState());
            IBlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());

            for (int i = this.boundingBox.minX; i <= this.boundingBox.maxX; ++i) {
                for (int j = this.boundingBox.minZ; j <= this.boundingBox.maxZ; ++j) {
                    BlockPos blockpos = new BlockPos(i, 64, j);

                    if (structureBoundingBoxIn.isVecInside(blockpos)) {
                        blockpos = worldIn.getTopSolidOrLiquidBlock(blockpos).down();

                        if (blockpos.getY() < worldIn.getSeaLevel()) {
                            blockpos = new BlockPos(blockpos.getX(), worldIn.getSeaLevel() - 1, blockpos.getZ());
                        }

                        while (blockpos.getY() >= worldIn.getSeaLevel() - 1) {

                            worldIn.setBlockState(blockpos, iblockstate2, 2);
                            worldIn.setBlockState(blockpos.down(), iblockstate3, 2);

                            blockpos = blockpos.down();
                        }
                    }
                }
            }

            return true;
        }
    }

    public static class PieceWeight {
        public Class<? extends StructureNorseVillagePieces.Village> villagePieceClass;
        public final int villagePieceWeight;
        public int villagePiecesSpawned;
        public int villagePiecesLimit;

        public PieceWeight(Class<? extends StructureNorseVillagePieces.Village> p_i2098_1_, int p_i2098_2_, int p_i2098_3_) {
            this.villagePieceClass = p_i2098_1_;
            this.villagePieceWeight = p_i2098_2_;
            this.villagePiecesLimit = p_i2098_3_;
        }

        public boolean canSpawnMoreVillagePiecesOfType(int componentType) {
            return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
        }

        public boolean canSpawnMoreVillagePieces() {
            return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
        }
    }

    public abstract static class Road extends StructureNorseVillagePieces.Village {
        public Road() {
        }

        protected Road(StructureNorseVillagePieces.Start start, int type) {
            super(start, type);
        }
    }

    public static class Start extends StructureNorseVillagePieces.Well {
        public BiomeProvider biomeProvider;
        /**
         * World terrain type, 0 for normal, 1 for flap map
         */
        public int terrainType;
        public StructureNorseVillagePieces.PieceWeight lastPlaced;
        /**
         * Contains List of all spawnable Structure Piece Weights. If no more Pieces of
         * a type can be spawned, they are removed from this list
         */
        public List<StructureNorseVillagePieces.PieceWeight> structureVillageWeightedPieceList;
        public List<StructureComponent> pendingHouses = Lists.<StructureComponent>newArrayList();
        public List<StructureComponent> pendingRoads = Lists.<StructureComponent>newArrayList();
        public Biome biome;

        public Start() {
        }

        public Start(BiomeProvider biomeProviderIn, int p_i2104_2_, Random rand, int p_i2104_4_, int p_i2104_5_, List<StructureNorseVillagePieces.PieceWeight> p_i2104_6_, int p_i2104_7_) {
            super((StructureNorseVillagePieces.Start) null, 0, rand, p_i2104_4_, p_i2104_5_);
            this.biomeProvider = biomeProviderIn;
            this.structureVillageWeightedPieceList = p_i2104_6_;
            this.terrainType = p_i2104_7_;
            Biome biome = biomeProviderIn.getBiome(new BlockPos(p_i2104_4_, 0, p_i2104_5_), Biomes.DEFAULT);
            this.biome = biome;
            this.startPiece = this;

            if (biome instanceof BiomeDesert) {
                this.structureType = 1;
            } else if (biome instanceof BiomeSavanna) {
                this.structureType = 2;
            } else if (biome instanceof BiomeTaiga) {
                this.structureType = 3;
            }

            this.setStructureType(this.structureType);
            this.isZombieInfested = rand.nextInt(50) == 0;
        }
    }

    public static class Torch extends StructureNorseVillagePieces.Village {

        public Torch() {
        }

        public Torch(StructureNorseVillagePieces.Start start, int p_i45568_2_, Random rand, StructureBoundingBox p_i45568_4_, EnumFacing facing) {
            super(start, p_i45568_2_);
            this.setCoordBaseMode(facing);
            this.boundingBox = p_i45568_4_;
        }

        public static StructureBoundingBox findPieceBox(StructureNorseVillagePieces.Start start, List<StructureComponent> p_175856_1_, Random rand, int p_175856_3_, int p_175856_4_, int p_175856_5_, EnumFacing facing) {
            StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_175856_3_, p_175856_4_, p_175856_5_, 0, 0, 0, 3, 4, 2, facing);
            return StructureComponent.findIntersecting(p_175856_1_, structureboundingbox) != null ? null : structureboundingbox;
        }

        /**
         * second Part of Structure generating, this for example places Spiderwebs, Mob
         * Spawners, it closes Mineshafts at the end, it adds Fences...
         */
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            if (this.averageGroundLvl < 0) {
                this.averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.averageGroundLvl < 0) {
                    return true;
                }

                this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 4 - 1, 0);
            }

            IBlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 2, 3, 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.setBlockState(worldIn, iblockstate, 1, 0, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate, 1, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate, 1, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.WOOL.getStateFromMeta(EnumDyeColor.WHITE.getDyeDamage()), 1, 3, 0, structureBoundingBoxIn);
            this.placeTorch(worldIn, EnumFacing.EAST, 2, 3, 0, structureBoundingBoxIn);
            this.placeTorch(worldIn, EnumFacing.NORTH, 1, 3, 1, structureBoundingBoxIn);
            this.placeTorch(worldIn, EnumFacing.WEST, 0, 3, 0, structureBoundingBoxIn);
            this.placeTorch(worldIn, EnumFacing.SOUTH, 1, 3, -1, structureBoundingBoxIn);
            return true;
        }
    }

    public abstract static class Village extends StructureComponent {
        protected int averageGroundLvl = -1;
        /**
         * The number of villagers that have been spawned in this component.
         */
        private int villagersSpawned;
        protected boolean spawnedT = false;
        protected int structureType;
        protected boolean isZombieInfested;
        protected StructureNorseVillagePieces.Start startPiece;

        public Village() {
        }

        protected Village(StructureNorseVillagePieces.Start start, int type) {
            super(type);

            if (start != null) {
                this.structureType = start.structureType;
                this.isZombieInfested = start.isZombieInfested;
                startPiece = start;
            }
        }

        /**
         * (abstract) Helper method to write subclass data to NBT
         */
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            tagCompound.setInteger("HPos", this.averageGroundLvl);
            tagCompound.setInteger("VCount", this.villagersSpawned);
            tagCompound.setByte("Type", (byte) this.structureType);
            tagCompound.setBoolean("Zombie", this.isZombieInfested);
        }

        /**
         * (abstract) Helper method to read subclass data from NBT
         */
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
            this.averageGroundLvl = tagCompound.getInteger("HPos");
            this.villagersSpawned = tagCompound.getInteger("VCount");
            this.structureType = tagCompound.getByte("Type");

            if (tagCompound.getBoolean("Desert")) {
                this.structureType = 1;
            }

            this.isZombieInfested = tagCompound.getBoolean("Zombie");
        }

        /**
         * Gets the next village component, with the bounding box shifted -1 in the X
         * and Z direction.
         */
        @Nullable
        protected StructureComponent getNextComponentNN(StructureNorseVillagePieces.Start start, List<StructureComponent> structureComponents, Random rand, int p_74891_4_, int p_74891_5_) {
            EnumFacing enumfacing = this.getCoordBaseMode();

            if (enumfacing != null) {
                switch (enumfacing) {
                    case NORTH:
                    default:
                        return StructureNorseVillagePieces.generateAndAddComponent(start, structureComponents, rand, this.boundingBox.minX - 1, this.boundingBox.minY + p_74891_4_, this.boundingBox.minZ + p_74891_5_, EnumFacing.WEST, this.getComponentType());
                    case SOUTH:
                        return StructureNorseVillagePieces.generateAndAddComponent(start, structureComponents, rand, this.boundingBox.minX - 1, this.boundingBox.minY + p_74891_4_, this.boundingBox.minZ + p_74891_5_, EnumFacing.WEST, this.getComponentType());
                    case WEST:
                        return StructureNorseVillagePieces.generateAndAddComponent(start, structureComponents, rand, this.boundingBox.minX + p_74891_5_, this.boundingBox.minY + p_74891_4_, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
                    case EAST:
                        return StructureNorseVillagePieces.generateAndAddComponent(start, structureComponents, rand, this.boundingBox.minX + p_74891_5_, this.boundingBox.minY + p_74891_4_, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
                }
            } else {
                return null;
            }
        }

        /**
         * Gets the next village component, with the bounding box shifted +1 in the X
         * and Z direction.
         */
        @Nullable
        protected StructureComponent getNextComponentPP(StructureNorseVillagePieces.Start start, List<StructureComponent> structureComponents, Random rand, int p_74894_4_, int p_74894_5_) {
            EnumFacing enumfacing = this.getCoordBaseMode();

            if (enumfacing != null) {
                switch (enumfacing) {
                    case NORTH:
                    default:
                        return StructureNorseVillagePieces.generateAndAddComponent(start, structureComponents, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + p_74894_4_, this.boundingBox.minZ + p_74894_5_, EnumFacing.EAST, this.getComponentType());
                    case SOUTH:
                        return StructureNorseVillagePieces.generateAndAddComponent(start, structureComponents, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + p_74894_4_, this.boundingBox.minZ + p_74894_5_, EnumFacing.EAST, this.getComponentType());
                    case WEST:
                        return StructureNorseVillagePieces.generateAndAddComponent(start, structureComponents, rand, this.boundingBox.minX + p_74894_5_, this.boundingBox.minY + p_74894_4_, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
                    case EAST:
                        return StructureNorseVillagePieces.generateAndAddComponent(start, structureComponents, rand, this.boundingBox.minX + p_74894_5_, this.boundingBox.minY + p_74894_4_, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
                }
            } else {
                return null;
            }
        }

        /**
         * Discover the y coordinate that will serve as the ground level of the supplied
         * BoundingBox. (A median of all the levels in the BB's horizontal rectangle).
         */
        protected int getAverageGroundLevel(World worldIn, StructureBoundingBox structurebb) {
            int i = 0;
            int j = 0;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k = this.boundingBox.minZ; k <= this.boundingBox.maxZ; ++k) {
                for (int l = this.boundingBox.minX; l <= this.boundingBox.maxX; ++l) {
                    blockpos$mutableblockpos.setPos(l, 64, k);

                    if (structurebb.isVecInside(blockpos$mutableblockpos)) {
                        i += Math.max(worldIn.getTopSolidOrLiquidBlock(blockpos$mutableblockpos).getY(), worldIn.provider.getAverageGroundLevel() - 1);
                        ++j;
                    }
                }
            }

            if (j == 0) {
                return -1;
            } else {
                return i / j;
            }
        }

        protected static boolean canVillageGoDeeper(StructureBoundingBox structurebb) {
            return structurebb != null && structurebb.minY > 10;
        }

        protected void spawnT(World worldIn, StructureBoundingBox structurebb, int x, int y, int z) {
            if (!this.spawnedT) {
                int j = this.getXWithOffset(x, z);
                int k = this.getYWithOffset(y);
                int l = this.getZWithOffset(x, z);

                for (EntityTesseract item : worldIn.getEntitiesWithinAABB(EntityTesseract.class, new AxisAlignedBB(j - 3, k - 3, l - 3, j + 3, k + 3, l + 3))) {
                    if (item.getItem().getItem() == HEItems.TESSERACT) {
                        return;
                    }
                }

                EntityTesseract entity = new EntityTesseract(worldIn, j + 0.5D, k + 0.1D, l + 0.5D, new ItemStack(HEItems.TESSERACT));
                worldIn.spawnEntity(entity);
                this.spawnedT = true;
            }
        }

        /**
         * Spawns a number of villagers in this component. Parameters: world, component
         * bounding box, x offset, y offset, z offset, number of villagers
         */
        protected void spawnVillagers(World worldIn, StructureBoundingBox structurebb, int x, int y, int z, int count) {
            if (this.villagersSpawned < count) {
                for (int i = this.villagersSpawned; i < count; ++i) {
                    int j = this.getXWithOffset(x + i, z);
                    int k = this.getYWithOffset(y);
                    int l = this.getZWithOffset(x + i, z);

                    if (!structurebb.isVecInside(new BlockPos(j, k, l))) {
                        break;
                    }

                    ++this.villagersSpawned;

                    if (this.isZombieInfested) {
                        EntityZombieVillager entityzombievillager = new EntityZombieVillager(worldIn);
                        entityzombievillager.setLocationAndAngles((double) j + 0.5D, (double) k, (double) l + 0.5D, 0.0F, 0.0F);
                        entityzombievillager.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityzombievillager)), (IEntityLivingData) null);
                        entityzombievillager.enablePersistence();
                        worldIn.spawnEntity(entityzombievillager);
                    } else {
                        EntityVillager entityvillager = new EntityVillager(worldIn);
                        entityvillager.setLocationAndAngles((double) j + 0.5D, (double) k, (double) l + 0.5D, 0.0F, 0.0F);
                        entityvillager.setProfession(this.chooseForgeProfession(i, entityvillager.getProfessionForge()));
                        entityvillager.finalizeMobSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityvillager)), (IEntityLivingData) null, false);
                        worldIn.spawnEntity(entityvillager);
                    }
                }
            }
        }

        @Deprecated // Use Forge version below.
        protected int chooseProfession(int villagersSpawnedIn, int currentVillagerProfession) {
            return currentVillagerProfession;
        }

        protected net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession chooseForgeProfession(int count, net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession prof) {
            return net.minecraftforge.fml.common.registry.VillagerRegistry.getById(chooseProfession(count, net.minecraftforge.fml.common.registry.VillagerRegistry.getId(prof)));
        }

        protected IBlockState getBiomeSpecificBlockState(IBlockState blockstateIn) {
            net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID event = new net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID(startPiece == null ? null : startPiece.biome, blockstateIn);
            net.minecraftforge.common.MinecraftForge.TERRAIN_GEN_BUS.post(event);
            if (event.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                return event.getReplacement();
            if (this.structureType == 1) {
                if (blockstateIn.getBlock() == Blocks.LOG || blockstateIn.getBlock() == Blocks.LOG2) {
                    return Blocks.SANDSTONE.getDefaultState();
                }

                if (blockstateIn.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.DEFAULT.getMetadata());
                }

                if (blockstateIn.getBlock() == Blocks.PLANKS) {
                    return Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata());
                }

                if (blockstateIn.getBlock() == Blocks.OAK_STAIRS) {
                    return Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, blockstateIn.getValue(BlockStairs.FACING));
                }

                if (blockstateIn.getBlock() == Blocks.STONE_STAIRS) {
                    return Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, blockstateIn.getValue(BlockStairs.FACING));
                }

                if (blockstateIn.getBlock() == Blocks.GRAVEL) {
                    return Blocks.SANDSTONE.getDefaultState();
                }
            } else if (this.structureType == 3) {
                if (blockstateIn.getBlock() == Blocks.LOG || blockstateIn.getBlock() == Blocks.LOG2) {
                    return Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLog.LOG_AXIS, blockstateIn.getValue(BlockLog.LOG_AXIS));
                }

                if (blockstateIn.getBlock() == Blocks.PLANKS) {
                    return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE);
                }

                if (blockstateIn.getBlock() == Blocks.OAK_STAIRS) {
                    return Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, blockstateIn.getValue(BlockStairs.FACING));
                }

                if (blockstateIn.getBlock() == Blocks.OAK_FENCE) {
                    return Blocks.SPRUCE_FENCE.getDefaultState();
                }
            } else if (this.structureType == 2) {
                if (blockstateIn.getBlock() == Blocks.LOG || blockstateIn.getBlock() == Blocks.LOG2) {
                    return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, blockstateIn.getValue(BlockLog.LOG_AXIS));
                }

                if (blockstateIn.getBlock() == Blocks.PLANKS) {
                    return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA);
                }

                if (blockstateIn.getBlock() == Blocks.OAK_STAIRS) {
                    return Blocks.ACACIA_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, blockstateIn.getValue(BlockStairs.FACING));
                }

                if (blockstateIn.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y);
                }

                if (blockstateIn.getBlock() == Blocks.OAK_FENCE) {
                    return Blocks.ACACIA_FENCE.getDefaultState();
                }
            }

            return blockstateIn;
        }

        protected BlockDoor biomeDoor() {
            switch (this.structureType) {
                case 2:
                    return Blocks.ACACIA_DOOR;
                case 3:
                    return Blocks.SPRUCE_DOOR;
                default:
                    return Blocks.OAK_DOOR;
            }
        }

        protected void createVillageDoor(World p_189927_1_, StructureBoundingBox p_189927_2_, Random p_189927_3_, int p_189927_4_, int p_189927_5_, int p_189927_6_, EnumFacing p_189927_7_) {
            if (!this.isZombieInfested) {
                this.generateDoor(p_189927_1_, p_189927_2_, p_189927_3_, p_189927_4_, p_189927_5_, p_189927_6_, EnumFacing.NORTH, this.biomeDoor());
            }
        }

        protected void placeTorch(World p_189926_1_, EnumFacing p_189926_2_, int p_189926_3_, int p_189926_4_, int p_189926_5_, StructureBoundingBox p_189926_6_) {
            if (!this.isZombieInfested) {
                this.setBlockState(p_189926_1_, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, p_189926_2_), p_189926_3_, p_189926_4_, p_189926_5_, p_189926_6_);
            }
        }

        /**
         * Replaces air and liquid from given position downwards. Stops when hitting
         * anything else than air or liquid
         */
        protected void replaceAirAndLiquidDownwards(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
            IBlockState iblockstate = this.getBiomeSpecificBlockState(blockstateIn);
            super.replaceAirAndLiquidDownwards(worldIn, iblockstate, x, y, z, boundingboxIn);
        }

        protected void setStructureType(int p_189924_1_) {
            this.structureType = p_189924_1_;
        }
    }

    public static class Well extends StructureNorseVillagePieces.Village {
        public Well() {
        }

        public Well(StructureNorseVillagePieces.Start start, int type, Random rand, int x, int z) {
            super(start, type);
            this.setCoordBaseMode(EnumFacing.Plane.HORIZONTAL.random(rand));

            if (this.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
                this.boundingBox = new StructureBoundingBox(x, 64, z, x + 6 - 1, 78, z + 6 - 1);
            } else {
                this.boundingBox = new StructureBoundingBox(x, 64, z, x + 6 - 1, 78, z + 6 - 1);
            }
        }

        /**
         * Initiates construction of the Structure Component picked, at the current
         * Location of StructGen
         */
        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand) {
            StructureNorseVillagePieces.generateAndAddRoadPiece((StructureNorseVillagePieces.Start) componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, EnumFacing.WEST, this.getComponentType());
            StructureNorseVillagePieces.generateAndAddRoadPiece((StructureNorseVillagePieces.Start) componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, EnumFacing.EAST, this.getComponentType());
            StructureNorseVillagePieces.generateAndAddRoadPiece((StructureNorseVillagePieces.Start) componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
            StructureNorseVillagePieces.generateAndAddRoadPiece((StructureNorseVillagePieces.Start) componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
        }

        /**
         * second Part of Structure generating, this for example places Spiderwebs, Mob
         * Spawners, it closes Mineshafts at the end, it adds Fences...
         */
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            if (this.averageGroundLvl < 0) {
                this.averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.averageGroundLvl < 0) {
                    return true;
                }

                this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 3, 0);
            }

            IBlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
            IBlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 4, 12, 4, iblockstate, Blocks.FLOWING_WATER.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), 2, 12, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), 3, 12, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), 2, 12, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), 3, 12, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate1, 1, 13, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate1, 1, 14, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate1, 4, 13, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate1, 4, 14, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate1, 1, 13, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate1, 1, 14, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate1, 4, 13, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, iblockstate1, 4, 14, 4, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 15, 1, 4, 15, 4, iblockstate, iblockstate, false);

            for (int i = 0; i <= 5; ++i) {
                for (int j = 0; j <= 5; ++j) {
                    if (j == 0 || j == 5 || i == 0 || i == 5) {
                        this.setBlockState(worldIn, iblockstate, j, 11, i, structureBoundingBoxIn);
                        this.clearCurrentPositionBlocksUpwards(worldIn, j, 12, i, structureBoundingBoxIn);
                    }
                }
            }

            return true;
        }
    }
}