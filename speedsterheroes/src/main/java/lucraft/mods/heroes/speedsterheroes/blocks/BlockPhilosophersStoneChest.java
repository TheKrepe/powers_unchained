package lucraft.mods.heroes.speedsterheroes.blocks;

import java.util.ArrayList;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityPhilosophersStoneChest;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPhilosophersStoneChest extends BlockContainer {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	protected static final AxisAlignedBB AXIS_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 1.0D - 0.125D, 0.4375D, 1.0D - 0.125D);

	public BlockPhilosophersStoneChest() {
		super(Material.ROCK);

		String name = "philosophersStoneChest";
		this.setHardness(5F);
		this.setResistance(10F);
		this.setHarvestLevel("pickaxe", 1);
		this.setSoundType(SoundType.STONE);
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		this.setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

		GameRegistry.register(this);
		GameRegistry.register(new ItemBlockPhilosophersStoneChest(this), new ResourceLocation(SpeedsterHeroes.MODID, name));
		GameRegistry.registerTileEntity(TileEntityPhilosophersStoneChest.class, name);

		LucraftCore.proxy.registerModel(this, new LCModelEntry(0, name));
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AXIS_AABB;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityPhilosophersStoneChest();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if(world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityPhilosophersStoneChest) {
			TileEntityPhilosophersStoneChest chest = (TileEntityPhilosophersStoneChest) world.getTileEntity(pos);
			ItemStack stack = new ItemStack(SHBlocks.philosophersStoneChest);
			NBTTagCompound nbt = new NBTTagCompound();
			
			if(!chest.getStackInSlot(0).isEmpty()) {
				NBTTagCompound stackTag = new NBTTagCompound();
				chest.getStackInSlot(0).writeToNBT(stackTag);
				nbt.setTag("Item", stackTag);
			}
			
			stack.setTagCompound(nbt);
			world.spawnEntity(new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack));
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return new ArrayList<ItemStack>();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		return true;
	}

	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	public boolean isVisuallyOpaque() {
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		this.setDefaultFacing(worldIn, pos, state);
	}

	@SuppressWarnings("deprecation")
	private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			Block block = worldIn.getBlockState(pos.north()).getBlock();
			Block block1 = worldIn.getBlockState(pos.south()).getBlock();
			Block block2 = worldIn.getBlockState(pos.west()).getBlock();
			Block block3 = worldIn.getBlockState(pos.east()).getBlock();
			EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);

			if (enumfacing == EnumFacing.NORTH && block.isFullBlock(state) && !block1.isFullBlock(state)) {
				enumfacing = EnumFacing.SOUTH;
			} else if (enumfacing == EnumFacing.SOUTH && block1.isFullBlock(state) && !block.isFullBlock(state)) {
				enumfacing = EnumFacing.NORTH;
			} else if (enumfacing == EnumFacing.WEST && block2.isFullBlock(state) && !block3.isFullBlock(state)) {
				enumfacing = EnumFacing.EAST;
			} else if (enumfacing == EnumFacing.EAST && block3.isFullBlock(state) && !block2.isFullBlock(state)) {
				enumfacing = EnumFacing.WEST;
			}

			worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
		}
	}

	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		
		if(worldIn.getTileEntity(pos) != null && worldIn.getTileEntity(pos) instanceof TileEntityPhilosophersStoneChest) {
			TileEntityPhilosophersStoneChest tileEntity = (TileEntityPhilosophersStoneChest) worldIn.getTileEntity(pos);
			if(!stack.hasTagCompound())
				return;
			ItemStack item = new ItemStack(stack.getTagCompound().getCompoundTag("Item"));
			tileEntity.setInventorySlotContents(0, item);
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("Item"))
			tooltip.add(LucraftCoreUtil.translateToLocal("speedsterheroes.info.containsstone"));
	}
	
	@SideOnly(Side.CLIENT)
	public IBlockState getStateForEntityRender(IBlockState state) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
	}

	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(FACING)).getIndex();
	}

	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

}
