package lucraft.mods.lucraftcore.util.fluids;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;

public class FluidTankExt extends FluidTank {

    public boolean update = false;

    public FluidTankExt(int capacity) {
        super(capacity);
    }

    public FluidTankExt(@Nullable FluidStack fluidStack, int capacity) {
        super(fluidStack, capacity);
    }

    public FluidTankExt(Fluid fluid, int amount, int capacity) {
        super(fluid, amount, capacity);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int i = super.fill(resource, doFill);
        update = true;
        return i;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        FluidStack stack = super.drain(resource, doDrain);
        update = true;
        return stack;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack stack = super.drain(maxDrain, doDrain);
        update = true;
        return stack;
    }

}
