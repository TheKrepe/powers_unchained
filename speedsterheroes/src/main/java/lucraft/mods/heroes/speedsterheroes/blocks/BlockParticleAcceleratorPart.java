package lucraft.mods.heroes.speedsterheroes.blocks;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityParticleAcceleratorPart;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockParticleAcceleratorPart extends BlockContainer {

	public BlockParticleAcceleratorPart() {
		super(Material.IRON);
		this.setHardness(5F);
		this.setResistance(10F);
		this.setHarvestLevel("pickaxe", 1);
		this.setSoundType(SoundType.METAL);
		this.setRegistryName("particleAcceleratorPart");
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this), new ResourceLocation(SpeedsterHeroes.MODID, "particleAcceleratorPart"));
		this.setUnlocalizedName("particleAcceleratorPart");
		this.setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
		LucraftCore.proxy.registerModel(this, new LCModelEntry(0, "particleAcceleratorPart"));
		GameRegistry.registerTileEntity(TileEntityParticleAcceleratorPart.class, "particleAcceleratorPart");
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

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if (worldIn.getTileEntity(pos) != null && worldIn.getTileEntity(pos) instanceof TileEntityParticleAcceleratorPart) {
			TileEntityParticleAcceleratorPart te = (TileEntityParticleAcceleratorPart) worldIn.getTileEntity(pos);
			if (te.isConnected) {
				return te.isSolid ? super.getCollisionBoundingBox(blockState, worldIn, pos) : null;
			}
		}
		return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		if (worldIn.getTileEntity(pos) != null && worldIn.getTileEntity(pos) instanceof TileEntityParticleAcceleratorPart) {
			TileEntityParticleAcceleratorPart te = (TileEntityParticleAcceleratorPart) worldIn.getTileEntity(pos);
			if (te.isConnected && !worldIn.isAirBlock(te.getMasterBlock(worldIn)) && worldIn.getBlockState(te.getMasterBlock(worldIn)).getBlock() == SHBlocks.particleAccelerator) {
				BlockParticleAccelerator b = (BlockParticleAccelerator) worldIn.getBlockState(te.getMasterBlock(worldIn)).getBlock();
				return new AxisAlignedBB(b.getMinBoundingBox(worldIn, te.getMasterBlock(worldIn)), b.getMaxBoundingBox(worldIn, te.getMasterBlock(worldIn)).add(1, 1, 1));
			}
		}
		return super.getSelectedBoundingBox(state, worldIn, pos);
	}

	public boolean isCollidable() {
		return true;
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		TileEntity te = world.getTileEntity(pos);

		if (te != null && te instanceof TileEntityParticleAcceleratorPart) {
			TileEntityParticleAcceleratorPart tileEntity = (TileEntityParticleAcceleratorPart) te;

			if (tileEntity.isConnected && world.isAirBlock(tileEntity.getMasterBlock(world))) {
				tileEntity.disconnectFromMaster();
				te.getWorld().setBlockToAir(pos);
				te.getWorld().setBlockState(pos, SHBlocks.particleAcceleratorPart.getDefaultState());
			}

			if (tileEntity.isConnected && world.getBlockState(tileEntity.getMasterBlock(world)) != null && world.getBlockState(tileEntity.getMasterBlock(world)).getBlock() == SHBlocks.particleAccelerator) {
				((BlockParticleAccelerator) world.getBlockState(tileEntity.getMasterBlock(world)).getBlock()).updateConnections(world, tileEntity.getMasterBlock(world));
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityParticleAcceleratorPart();
	}

}
