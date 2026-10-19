package lucraft.mods.lucraftcore.utilities.blocks;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.utilities.gui.GuiHandlerEntrySuitStand;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockSuitStand extends BlockContainer {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<SuitStandPart> PART = PropertyEnum.create("part", SuitStandPart.class);

    public BlockSuitStand() {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(PART, SuitStandPart.BOTTOM));
        this.setHardness(5F);
        this.setResistance(10F);
        this.setSoundType(SoundType.METAL);
        this.setRegistryName(LucraftCore.MODID, "suit_stand");
        this.setTranslationKey("suit_stand");
        this.setCreativeTab(LucraftCore.CREATIVE_TAB);
        GameRegistry.registerTileEntity(TileEntitySuitStand.class, "suit_stand");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return getStateFromMeta(meta).getValue(PART) == SuitStandPart.UPPER ? null : new TileEntitySuitStand();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        float pixel = 1F / 16F;
        SuitStandPart part = state.getValue(PART);
        return new AxisAlignedBB(1.2F * pixel, 0, 1.2F * pixel, 14.8 * pixel, part == SuitStandPart.BOTTOM ? 1 : 13 * pixel, 14.8 * pixel);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);

    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            if (state.getValue(PART) == SuitStandPart.UPPER)
                pos = pos.down();
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntitySuitStand) {
                playerIn.openGui(LucraftCore.INSTANCE, GuiHandlerEntrySuitStand.ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }

            return true;
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (state.getValue(PART) == SuitStandPart.UPPER) {
            BlockPos downPos = pos.down();
            IBlockState other = worldIn.getBlockState(downPos);
            if (other.getBlock() != this)
                worldIn.setBlockToAir(pos);
            else if (blockIn != this)
                other.neighborChanged(worldIn, downPos, blockIn, fromPos);
        } else {
            boolean shouldBreak = true;
            BlockPos upPos = pos.up();
            IBlockState other = worldIn.getBlockState(upPos);
            if (other.getBlock() != this) {
                worldIn.setBlockToAir(pos);
                if (!worldIn.isRemote)
                    this.dropBlockAsItem(worldIn, pos, state, 0);
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntitySuitStand) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntitySuitStand) tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(PART) == SuitStandPart.UPPER ? null : Item.getItemFromBlock(UtilitiesBlocks.SUIT_STAND);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return pos.getY() >= worldIn.getHeight() - 1 ? false : worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) && super.canPlaceBlockAt(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos.up());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

        if (worldIn.isAirBlock(pos.up())) {
            worldIn.setBlockState(pos.up(), state.withProperty(PART, SuitStandPart.UPPER).withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
        }

        if (stack.hasDisplayName()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntitySuitStand) {
                ((TileEntitySuitStand) tileentity).setCustomInventoryName(stack.getDisplayName());
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(PART, (meta & 1) > 0 ? SuitStandPart.UPPER : SuitStandPart.BOTTOM).withProperty(FACING, EnumFacing.byHorizontalIndex(meta >> 2));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(FACING).getHorizontalIndex() << 2) + (state.getValue(PART) == SuitStandPart.UPPER ? 1 : 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{PART, FACING});
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        BlockPos blockpos = pos.down();
        BlockPos blockpos1 = pos.up();

        if (player.capabilities.isCreativeMode && state.getValue(PART) == SuitStandPart.UPPER && worldIn.getBlockState(blockpos).getBlock() == this) {
            worldIn.setBlockToAir(blockpos);
        }

        if (state.getValue(PART) == SuitStandPart.BOTTOM && worldIn.getBlockState(blockpos1).getBlock() == this) {
            if (player.capabilities.isCreativeMode) {
                worldIn.setBlockToAir(pos);
            }

            worldIn.setBlockToAir(blockpos1);
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    public static enum SuitStandPart implements IStringSerializable {

        UPPER, BOTTOM;

        @Override
        public String toString() {
            return this.getName();
        }

        @Override
        public String getName() {
            return this == UPPER ? "upper" : "bottom";
        }
    }

}
