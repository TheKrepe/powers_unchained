package lucraft.mods.heroesexpansion.worldgen.crashedkreeship;

import lucraft.mods.heroesexpansion.HELootTableList;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityKree;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Random;

public class ComponentCrashedKreeShip {

    public static void registerScatteredFeaturePieces() {
        MapGenStructureIO.registerStructureComponent(ComponentCrashedKreeShip.Main.class, "CrKrSh");
    }

    abstract static class Feature extends StructureComponent {
        /**
         * The size of the bounding box for this feature in the X axis
         */
        protected int width;
        /**
         * The size of the bounding box for this feature in the Y axis
         */
        protected int height;
        /**
         * The size of the bounding box for this feature in the Z axis
         */
        protected int depth;
        protected int horizontalPos = -1;

        public Feature() {
        }

        protected Feature(Random rand, int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
            super(0);
            this.width = sizeX;
            this.height = sizeY;
            this.depth = sizeZ;
            this.setCoordBaseMode(EnumFacing.Plane.HORIZONTAL.random(rand));

            if (this.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
                this.boundingBox = new StructureBoundingBox(x, y, z, x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1);
            } else {
                this.boundingBox = new StructureBoundingBox(x, y, z, x + sizeZ - 1, y + sizeY - 1, z + sizeX - 1);
            }
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            tagCompound.setInteger("Width", this.width);
            tagCompound.setInteger("Height", this.height);
            tagCompound.setInteger("Depth", this.depth);
            tagCompound.setInteger("HPos", this.horizontalPos);
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
            this.width = tagCompound.getInteger("Width");
            this.height = tagCompound.getInteger("Height");
            this.depth = tagCompound.getInteger("Depth");
            this.horizontalPos = tagCompound.getInteger("HPos");
        }

        /**
         * Calculates and offsets this structure boundingbox to average ground level
         */
        protected boolean offsetToAverageGroundLevel(World worldIn, StructureBoundingBox structurebb, int yOffset) {
            if (this.horizontalPos >= 0) {
                return true;
            } else {
                int i = 0;
                int j = 0;
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                for (int k = this.boundingBox.minZ; k <= this.boundingBox.maxZ; ++k) {
                    for (int l = this.boundingBox.minX; l <= this.boundingBox.maxX; ++l) {
                        blockpos$mutableblockpos.setPos(l, 64, k);

                        if (structurebb.isVecInside(blockpos$mutableblockpos)) {
                            i += Math.max(worldIn.getTopSolidOrLiquidBlock(blockpos$mutableblockpos).getY(), worldIn.provider.getAverageGroundLevel());
                            ++j;
                        }
                    }
                }

                if (j == 0) {
                    return false;
                } else {
                    this.horizontalPos = i / j;
                    this.boundingBox.offset(0, this.horizontalPos - this.boundingBox.minY + yOffset, 0);
                    return true;
                }
            }
        }
    }

    public static class Main extends StructureComponentTemplate {

        private static final PlacementSettings INSERT = (new PlacementSettings()).setIgnoreEntities(true).setReplacedBlock(Blocks.AIR);
        private Rotation rotation;
        private int kreeCount;

        public Main() {
        }

        public Main(TemplateManager templateManager, BlockPos pos, Rotation rotation) {
            super(0);
            this.templatePosition = pos;
            this.rotation = rotation;
            this.loadTemplate(templateManager);
        }

        private void loadTemplate(TemplateManager templateManager) {
            Template template = templateManager.getTemplate((MinecraftServer) null, new ResourceLocation(HeroesExpansion.MODID, "crashed_kree_ship"));
            PlacementSettings placementsettings = INSERT.copy().setRotation(this.rotation);
            this.setup(template, this.templatePosition, placementsettings);
        }

        @Override
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            this.spawnKree(worldIn, structureBoundingBoxIn, 0, 1, 13, 5);
            return super.addComponentParts(worldIn, randomIn, structureBoundingBoxIn);
        }

        protected void spawnKree(World worldIn, StructureBoundingBox structurebb, int x, int y, int z, int count) {
            if (this.kreeCount < count) {
                for (int i = this.kreeCount; i < count; ++i) {
                    int j = this.getXWithOffset(x + i, z);
                    int k = this.getYWithOffset(y);
                    int l = this.getZWithOffset(x + i, z);

                    if (!structurebb.isVecInside(new BlockPos(j, k, l))) {
                        break;
                    }

                    ++this.kreeCount;

                    EntityKree entityKree = new EntityKree(worldIn);
                    entityKree.setLocationAndAngles((double) j + 0.5D, (double) k, (double) l + 0.5D, 0.0F, 0.0F);
                    entityKree.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityKree)), (IEntityLivingData) null);
                    entityKree.enablePersistence();
                    worldIn.spawnEntity(entityKree);

                }
            }
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, World worldIn, Random rand, StructureBoundingBox sbb) {
            if (function.startsWith("Chest")) {
                BlockPos blockpos = pos.down();

                if (sbb.isVecInside(blockpos)) {
                    TileEntity tileentity = worldIn.getTileEntity(blockpos);

                    if (tileentity instanceof TileEntityChest) {

                        ((TileEntityChest) tileentity).setLootTable(HELootTableList.CRASHED_KREE_SHIP, rand.nextLong());
                    }
                }
            }
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setString("Rot", this.rotation.name());
            tagCompound.setInteger("KreeCount", this.kreeCount);
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager templateManager) {
            super.readStructureFromNBT(tagCompound, templateManager);
            this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
            this.kreeCount = tagCompound.getInteger("KreeCount");
            this.loadTemplate(templateManager);
        }
    }
}