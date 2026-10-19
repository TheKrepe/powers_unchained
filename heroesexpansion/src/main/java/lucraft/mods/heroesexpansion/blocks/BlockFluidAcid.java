package lucraft.mods.heroesexpansion.blocks;

import lucraft.mods.heroesexpansion.superpowers.HESuperpowers;
import lucraft.mods.lucraftcore.materials.potions.MaterialsPotions;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockFluidAcid extends BlockFluidClassic {

    public BlockFluidAcid(Fluid fluid, Material material) {
        super(fluid, material);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (entityIn instanceof EntityLivingBase) {
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MaterialsPotions.RADIATION, 60, 2));
        }
        if (this.isSourceBlock(worldIn, pos) && entityIn instanceof EntityLivingBase && SuperpowerHandler.getSuperpower((EntityLivingBase) entityIn) != HESuperpowers.BLINDNESS) {
            worldIn.setBlockToAir(pos);
            SuperpowerHandler.giveSuperpower((EntityLivingBase) entityIn, HESuperpowers.BLINDNESS);
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60, 2));
        }
    }
}
