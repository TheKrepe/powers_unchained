package lucraft.mods.heroesexpansion.blocks;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.items.ItemKryptonite;
import lucraft.mods.lucraftcore.util.blocks.BlockBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockKryptonite extends BlockBase {

    public static final PropertyBool DROP_FOSSIL = PropertyBool.create("drop_fossil");

    public BlockKryptonite(String name) {
        super(name, Material.GLASS);
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.setHardness(0.5F);
        this.setLightOpacity(3);
        this.setSoundType(SoundType.GLASS);
        this.setTickRandomly(true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
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
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return HEItems.KRYPTONITE;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return fortune > 0 ? 4 : quantityDropped(random);
    }

    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(2);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        Random rand = new Random();
        int max = 20 - MathHelper.clamp(fortune * 2, 0, 19);
        int i = rand.nextInt(max);
        if (world instanceof World && state.getValue(DROP_FOSSIL) && i == 0) {
            spawnAsEntity((World) world, pos, new ItemStack(HEItems.KRYPTONIAN_FOSSIL));
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityKryptonite();
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(HEBlocks.KRYPTONITE_BLOCK);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(HEBlocks.KRYPTONITE_BLOCK), 1, this.damageDropped(state));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DROP_FOSSIL, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DROP_FOSSIL) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DROP_FOSSIL);
    }

    public static class TileEntityKryptonite extends TileEntity implements ITickable {

        public TileEntityKryptonite() {
        }

        @Override
        public void update() {
            int range = 4;
            for (EntityPlayer players : this.world.getEntitiesWithinAABB(EntityPlayer.class, FULL_BLOCK_AABB.offset(pos).grow(range))) {
                ItemKryptonite.giveKryptonitePoison(players, 5 * 20);
            }
        }
    }
}
