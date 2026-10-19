package lucraft.mods.lucraftcore.util.fluids;

import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class LCFluidUtil {

    public static ItemStack transferFluidFromItemToTank(ItemStack fluidContainer, FluidTank fluidTank, ItemStackHandler itemHandler) {
        if (!fluidContainer.isEmpty() && isDrainableFilledContainer(fluidContainer) && (fluidTank.getFluid() == null || fluidTank.getFluid().isFluidEqual(FluidUtil.getFluidContained(fluidContainer)))) {
            FluidActionResult result = FluidUtil.tryEmptyContainerAndStow(fluidContainer, fluidTank, itemHandler, fluidTank.getCapacity(), null, true);
            if (result.isSuccess()) {
                return result.getResult();
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack transferFluidFromTankToItem(ItemStack fluidContainer, FluidTank fluidTank, ItemStackHandler itemHandler) {
        if (!fluidContainer.isEmpty() && isFillableEmptyContainer(fluidContainer) && fluidTank.getFluid() != null && fluidTank.getFluid().amount > 0) {
            FluidActionResult result = FluidUtil.tryFillContainerAndStow(fluidContainer, fluidTank, itemHandler, fluidTank.getCapacity(), null, true);
            if (result.isSuccess()) {
                return result.getResult();
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isDrainableFilledContainer(ItemStack container) {
        IFluidHandler fluidHandler = FluidUtil.getFluidHandler(container);
        if (fluidHandler == null) {
            return false;
        }
        IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
        for (IFluidTankProperties properties : tankProperties) {
            if (!properties.canDrain()) {
                return false;
            }
            FluidStack contents = properties.getContents();
            if (contents == null || contents.amount == 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFillableEmptyContainer(ItemStack empty) {
        IFluidHandler fluidHandler = FluidUtil.getFluidHandler(empty);
        if (fluidHandler == null) {
            return false;
        }
        IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
        for (IFluidTankProperties properties : tankProperties) {
            if (!properties.canFill()) {
                return false;
            }
            FluidStack contents = properties.getContents();
            if (contents != null && contents.amount >= properties.getCapacity()) {
                return false;
            }
        }
        return true;
    }

    public static FluidStack parseFromJson(JsonObject jsonObject) {
        if (!FluidRegistry.isFluidRegistered(JsonUtils.getString(jsonObject, "fluid"))) {
            throw new RuntimeException("Fluid '" + JsonUtils.getString(jsonObject, "fluid") + "' is not registered!");
        }
        Fluid fluid = FluidRegistry.getFluid(JsonUtils.getString(jsonObject, "fluid"));
        int amount = JsonUtils.getInt(jsonObject, "amount");
        return new FluidStack(fluid, amount);
    }

    @SideOnly(Side.CLIENT)
    public static String getFormattedFluidInfo(int amount, int capacity) {
        return StringHelper.translateToLocal("lucraftcore.info.fluidtank_display", amount, capacity, "mB");
    }

    @SideOnly(Side.CLIENT)
    public static List<String> getFormattedFluidInfo(FluidTank fluidTank) {
        List<String> list = new ArrayList<>();
        if (fluidTank.getFluid() != null)
            list.add(fluidTank.getFluid().getLocalizedName());
        list.add(TextFormatting.GRAY + StringHelper.translateToLocal("lucraftcore.info.fluidtank_display", fluidTank.getFluidAmount(), fluidTank.getCapacity(), "mB"));
        return list;
    }

    @SideOnly(Side.CLIENT)
    public static void drawTooltip(FluidTank fluidTank, GuiContainer gui, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            gui.drawHoveringText(getFormattedFluidInfo(fluidTank), mouseX + 10, mouseY);
        }
    }

}
