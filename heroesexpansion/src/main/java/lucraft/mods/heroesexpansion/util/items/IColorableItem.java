package lucraft.mods.heroesexpansion.util.items;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IColorableItem {

    int getDefaultColor(ItemStack stack);

    default boolean hasColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        return nbttagcompound != null && nbttagcompound.hasKey("display", 10) ? nbttagcompound.getCompoundTag("display").hasKey("color", 3) : false;
    }

    default int getColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();

        if (nbttagcompound != null) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

            if (nbttagcompound1 != null && nbttagcompound1.hasKey("color", 3)) {
                return nbttagcompound1.getInteger("color");
            }
        }

        return getDefaultColor(stack);
    }

    default void setColor(ItemStack stack, int color) {
        if (getDefaultColor(stack) == color) {
            removeColor(stack);
            return;
        }
        NBTTagCompound nbttagcompound = stack.getTagCompound();

        if (nbttagcompound == null) {
            nbttagcompound = new NBTTagCompound();
            stack.setTagCompound(nbttagcompound);
        }

        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

        if (!nbttagcompound.hasKey("display", 10)) {
            nbttagcompound.setTag("display", nbttagcompound1);
        }

        nbttagcompound1.setInteger("color", color);
    }

    default void removeColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();

        if (nbttagcompound != null) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

            if (nbttagcompound1.hasKey("color")) {
                nbttagcompound1.removeTag("color");
            }
        }
    }

    @SideOnly(Side.CLIENT)
    default int colorMultiplier(ItemStack stack, int tintIndex) {
        return tintIndex == 1 ? ((IColorableItem) stack.getItem()).getColor(stack) : -1;
    }

    @SideOnly(Side.CLIENT)
    class IColorableItemColor implements IItemColor {

        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            return ((IColorableItem) stack.getItem()).colorMultiplier(stack, tintIndex);
        }

    }

}
