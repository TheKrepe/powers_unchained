package lucraft.mods.heroes.speedsterheroes.blocks;

import java.util.ArrayList;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityParticleAccelerator;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityParticleAcceleratorPart;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.items.LCItems;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockParticleAccelerator extends BlockContainer {

	public static final Vec3d minMultiblockSizeVec = new Vec3d(-1, 0, 0);
	public static final Vec3d maxMultiblockSizeVec = new Vec3d(1, 2, 4);

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockParticleAccelerator() {
		super(Material.IRON);
		this.setHardness(5F);
		this.setResistance(10F);
		this.setHarvestLevel("pickaxe", 1);
		this.setSoundType(SoundType.METAL);
		this.setUnlocalizedName("particleAccelerator");
		this.setRegistryName("particleAccelerator");
		this.setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this), new ResourceLocation(SpeedsterHeroes.MODID, "particleAccelerator"));
		GameRegistry.registerTileEntity(TileEntityParticleAccelerator.class, "particleAccelerator");

		LucraftCore.proxy.registerModel(this, new LCModelEntry(0, "particleAccelerator"));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	public boolean isVisuallyOpaque() {
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		if(worldIn.isRemote)
			return false;
		
		if (te != null && te instanceof TileEntityParticleAccelerator) {
			TileEntityParticleAccelerator tileEntity = (TileEntityParticleAccelerator) te;

			if(isMultiBlockStructed(worldIn, pos)) {
				if (!tileEntity.isEnabled && !playerIn.getHeldItemMainhand().isEmpty() && playerIn.getHeldItemMainhand().getItem() == LCItems.hammer) {
					tileEntity.setIsEnabledAndUpdate(true);
					
					List<BlockPos> list = getMultiblockPosList(worldIn, state.getValue(FACING), pos);
					for (int i = 0; i < list.size(); i++) {
						BlockPos p = list.get(i);
						if (!worldIn.isAirBlock(p) && worldIn.getTileEntity(p) != null && worldIn.getTileEntity(p) instanceof TileEntityParticleAcceleratorPart) {
							((TileEntityParticleAcceleratorPart) worldIn.getTileEntity(p)).setMasterBlock(pos, pos.getY() == p.getY());
							worldIn.notifyBlockUpdate(pos, worldIn.getBlockState(pos), worldIn.getBlockState(pos), 3);
						}
					}
					worldIn.notifyBlockUpdate(pos, worldIn.getBlockState(pos), worldIn.getBlockState(pos), 3);
				} else if(tileEntity.isEnabled) {
					if(!playerIn.getHeldItemMainhand().isEmpty() && playerIn.getHeldItemMainhand().getItem() == Items.WATER_BUCKET) {
						if(tileEntity.addWater())
							playerIn.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BUCKET));
					} else {
						EnumFacing facing = state.getValue(FACING);
						tileEntity.letPlayerSit(playerIn, facing);
					}
				}
			}
		}

		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
	}

	public void updateConnections(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		if (!isMultiBlockStructed(world, pos)) {
			for (BlockPos p : getMultiblockPosList(world, facing, pos)) {
				if (p.equals(pos) && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityParticleAccelerator) {
					((TileEntityParticleAccelerator) world.getTileEntity(pos)).setIsEnabledAndUpdate(false);
					((TileEntityParticleAccelerator) world.getTileEntity(pos)).deleteSitableEntity();
				} else if (world.getTileEntity(p) != null && world.getTileEntity(p) instanceof TileEntityParticleAcceleratorPart) {
					((TileEntityParticleAcceleratorPart) world.getTileEntity(p)).disconnectFromMaster();
				}
			}
		}
	}
	
	public void updateConnections(IBlockAccess world, BlockPos pos) {
		updateConnections(world, pos, world.getBlockState(pos).getValue(FACING));
	}
	
	public BlockPos getMinBoundingBox(World world, BlockPos masterBlockPos) {
		if(world.getBlockState(masterBlockPos) != null && world.getBlockState(masterBlockPos).getBlock().hasTileEntity(world.getBlockState(masterBlockPos))) {
			
			double x = 0D;
			double y = 0D;
			double z = 0D;
			
			for(int i = 0; i < getMultiblockPosList(world, world.getBlockState(masterBlockPos).getValue(FACING), masterBlockPos).size(); i++) {
				BlockPos pos = getMultiblockPosList(world, world.getBlockState(masterBlockPos).getValue(FACING), masterBlockPos).get(i);
				
				if(i == 0) {
					x = pos.getX();
					y = pos.getY();
					z = pos.getZ();
				}
				
				if(pos.getX() < x)
					x = pos.getX();
				if(pos.getY() < y)
					y = pos.getY();
				if(pos.getZ() < z)
					z = pos.getZ();
			}
			
			return new BlockPos(x, y, z);
		}
		
		return null;
	}
	
	public BlockPos getMaxBoundingBox(World world, BlockPos masterBlockPos) {
		if(world.getBlockState(masterBlockPos) != null && world.getBlockState(masterBlockPos).getBlock().hasTileEntity(world.getBlockState(masterBlockPos))) {
			
			double x = 0D;
			double y = 0D;
			double z = 0D;
			
			for(int i = 0; i < getMultiblockPosList(world, world.getBlockState(masterBlockPos).getValue(FACING), masterBlockPos).size(); i++) {
				BlockPos pos = getMultiblockPosList(world, world.getBlockState(masterBlockPos).getValue(FACING), masterBlockPos).get(i);
				
				if(i == 0) {
					x = pos.getX();
					y = pos.getY();
					z = pos.getZ();
				}
				
				if(pos.getX() > x)
					x = pos.getX();
				if(pos.getY() > y)
					y = pos.getY();
				if(pos.getZ() > z)
					z = pos.getZ();
			}
			
			return new BlockPos(x, y, z);
		}
		
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityParticleAccelerator();
	}

	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		this.setDefaultFacing(worldIn, pos, state);
	}

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
	}

	public ArrayList<BlockPos> getMultiblockPosList(IBlockAccess world, EnumFacing facing, BlockPos pos) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();

		float degrees = facing == EnumFacing.NORTH ? 0F : (facing == EnumFacing.EAST ? -90F : (facing == EnumFacing.WEST ? 90 : 180F));
		Vec3d v1 = minMultiblockSizeVec.rotateYaw(degrees * (float) Math.PI / 180F);
		Vec3d v2 = maxMultiblockSizeVec.rotateYaw(degrees * (float) Math.PI / 180F);
		v1 = new Vec3d(Math.round(v1.xCoord), Math.round(v1.yCoord), Math.round(v1.zCoord));
		v2 = new Vec3d(Math.round(v2.xCoord), Math.round(v2.yCoord), Math.round(v2.zCoord));

		for (int x = (int) Math.min(v1.xCoord, v2.xCoord); x <= Math.max(v1.xCoord, v2.xCoord); x++) {
			for (int y = (int) Math.min(v1.yCoord, v2.yCoord); y <= Math.max(v1.yCoord, v2.yCoord); y++) {
				for (int z = (int) Math.min(v1.zCoord, v2.zCoord); z <= Math.max(v1.zCoord, v2.zCoord); z++) {

					BlockPos bp = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
					list.add(bp);
				}
			}
		}

		return list;
	}

	public boolean isMultiBlockStructed(IBlockAccess world, BlockPos pos) {
		for (BlockPos p : getMultiblockPosList(world, world.getBlockState(pos).getValue(FACING), pos)) {
			if (!p.equals(pos)) {
				if (world.getBlockState(p).getBlock() != SHBlocks.particleAcceleratorPart) {
					return false;
				}
			}
		}

		return true;
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
