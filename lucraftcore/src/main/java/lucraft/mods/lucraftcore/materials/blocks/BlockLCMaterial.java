package lucraft.mods.lucraftcore.materials.blocks;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import lucraft.mods.lucraftcore.materials.ModuleMaterials;
import lucraft.mods.lucraftcore.materials.events.LCMaterialBlockTickEvent;
import lucraft.mods.lucraftcore.materials.potions.MaterialsPotions;
import lucraft.mods.lucraftcore.util.blocks.BlockBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockLCMaterial extends BlockBase {

    public Material material;
    public MaterialComponent component;

    public BlockLCMaterial(Material material, MaterialComponent component) {
        super(component.getName() + "_" + material.getResourceName(), component == MaterialComponent.ORE ? net.minecraft.block.material.Material.ROCK : net.minecraft.block.material.Material.IRON);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(component == MaterialComponent.ORE ? SoundType.STONE : SoundType.METAL);
        this.setCreativeTab(ModuleMaterials.INSTANCE.TAB_MATERIALS);
        this.setTickRandomly(true);
        this.setHarvestLevel("pickaxe", material.getHarvestLevel(component));
        this.material = material;
        this.component = component;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return this.material == Material.VIBRANIUM ? BlockRenderLayer.CUTOUT_MIPPED : super.getRenderLayer();
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
        return material.getBlockHardness(blockState, world, pos);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return material.getExplosionResistance(world, pos, exploder, explosion);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (entityIn instanceof EntityLivingBase && material.isRadioactive() && LCConfig.materials.radiation_)
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MaterialsPotions.RADIATION, 5 * 20, 1));
        super.onEntityCollision(worldIn, pos, state, entityIn);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof EntityLivingBase && material.isRadioactive() && LCConfig.materials.radiation_)
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MaterialsPotions.RADIATION, 5 * 20, 1));
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (material.isRadioactive() && LCConfig.materials.radiation_) {
            float radius = component == MaterialComponent.ORE ? 3 : 1.5F;
            int amplifier = component == MaterialComponent.ORE ? 1 : 0;

            for (EntityLivingBase entity : worldIn.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius)))) {
                entity.addPotionEffect(new PotionEffect(MaterialsPotions.RADIATION, 5 * 20, amplifier));
            }
        }

        MinecraftForge.EVENT_BUS.post(new LCMaterialBlockTickEvent(this.material, this.component, worldIn, pos, state, rand));

        super.updateTick(worldIn, pos, state, rand);
    }

}
