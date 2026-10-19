package net.threetag.pymtech.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.threetag.pymtech.sizechangetype.PTSizeChangeTypes;
import net.threetag.threecore.capability.CapabilitySizeChanging;
import net.threetag.threecore.capability.ISizeChanging;

import java.util.function.Supplier;

public class PymParticleFluidBlock extends FlowingFluidBlock {

    public PymParticleFluidBlock(Supplier<? extends FlowingFluid> supplier, Properties properties) {
        super(supplier, properties);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.onEntityCollision(state, worldIn, pos, entityIn);

        if (!entityIn.getCapability(CapabilitySizeChanging.SIZE_CHANGING).isPresent())
            return;

        if (state.get(FlowingFluidBlock.LEVEL) == 0 && changeSize(entityIn, this.getFluid().isEquivalentTo(PTFluids.SHRINK_PYM_PARTICLES) ? 0.1F : 3F)) {
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
        } else {
            for (Direction direction : Direction.values()) {
                BlockPos blockPos = pos.add(direction.getDirectionVec());
                BlockState state1 = worldIn.getBlockState(blockPos);

                if (state1.getBlock() == this && state1.get(FlowingFluidBlock.LEVEL) == 0 && changeSize(entityIn, this.getFluid().isEquivalentTo(PTFluids.SHRINK_PYM_PARTICLES) ? 0.1F : 3F)) {
                    worldIn.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    public static boolean changeSize(Entity entity, float size) {
        if (!entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).isPresent())
            return false;

        ISizeChanging sizeChanging = entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).orElseGet(() -> null);

        if (sizeChanging.getScale() == size)
            return false;

        float newSize = (sizeChanging.getScale() < 1F && size > 1F) || (sizeChanging.getScale() > 1F && size < 1F) ? 1F : size;
        sizeChanging.startSizeChange(PTSizeChangeTypes.PYM_PARTICLES, newSize);

        return true;
    }

}
