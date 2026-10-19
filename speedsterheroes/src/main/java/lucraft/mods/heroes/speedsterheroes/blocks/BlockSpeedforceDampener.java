package lucraft.mods.heroes.speedsterheroes.blocks;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntitySpeedforceDampener;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockSpeedforceDampener extends BlockContainer {

	public BlockSpeedforceDampener() {
		super(Material.IRON);

		this.setHardness(5F);
		this.setResistance(10F);
		this.setHarvestLevel("pickaxe", 1);
		this.setSoundType(SoundType.METAL);

		this.setRegistryName("speedforceDampener");
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this), new ResourceLocation(SpeedsterHeroes.MODID, "speedforceDampener"));
		this.setUnlocalizedName("speedforceDampener");
		this.setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
		LucraftCore.proxy.registerModel(this, new LCModelEntry(0, "speedforceDampener"));
		GameRegistry.registerTileEntity(TileEntitySpeedforceDampener.class, "speedforceDampener");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySpeedforceDampener();
	}

	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

}
