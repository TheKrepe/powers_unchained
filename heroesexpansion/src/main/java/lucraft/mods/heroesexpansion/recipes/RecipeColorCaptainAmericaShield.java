package lucraft.mods.heroesexpansion.recipes;

import lucraft.mods.heroesexpansion.items.HEItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.DyeUtils;

public class RecipeColorCaptainAmericaShield extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        ItemStack shield = ItemStack.EMPTY;
        int dye = 0;
        int amount = 0;

        for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);

            if (!stack.isEmpty())
                amount++;

            if (stack.getItem() == HEItems.CAPTAIN_AMERICA_SHIELD) {
                if (shield.isEmpty())
                    shield = stack;
                else
                    return false;

                if (net.minecraftforge.oredict.DyeUtils.isDye(inventoryCrafting.getStackInSlot(i + inventoryCrafting.getWidth())))
                    dye++;
                if (i - inventoryCrafting.getWidth() >= 0 && net.minecraftforge.oredict.DyeUtils.isDye(inventoryCrafting.getStackInSlot(i - inventoryCrafting.getWidth())))
                    dye++;
            }
        }

        return !shield.isEmpty() && dye > 0 && amount == dye + 1;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {

        for (int k = 0; k < inventoryCrafting.getSizeInventory(); ++k) {
            ItemStack stack = inventoryCrafting.getStackInSlot(k);

            if (stack.getItem() == HEItems.CAPTAIN_AMERICA_SHIELD) {
                stack = stack.copy();

                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                if (!stack.getTagCompound().hasKey("display"))
                    stack.getTagCompound().setTag("display", new NBTTagCompound());

                if (DyeUtils.isDye(inventoryCrafting.getStackInSlot(k + inventoryCrafting.getWidth()))) {
                    int[] aint = new int[3];
                    int i = 0;
                    float[] afloat = net.minecraftforge.oredict.DyeUtils.colorFromStack(inventoryCrafting.getStackInSlot(k + inventoryCrafting.getWidth())).get().getColorComponentValues();
                    int l1 = MathHelper.clamp((int) (afloat[0] * 255.0F) *2, 0, 255);
                    int i2 =  MathHelper.clamp((int) (afloat[1] * 255.0F) *2, 0, 255);
                    int j2 =  MathHelper.clamp((int) (afloat[2] * 255.0F) *2, 0, 255);
                    i += Math.max(l1, Math.max(i2, j2));
                    aint[0] += l1;
                    aint[1] += i2;
                    aint[2] += j2;
                    int i1 = aint[0];
                    int j1 = aint[1];
                    int k1 = aint[2];
                    float f3 = (float) i;
                    float f4 = (float) Math.max(i1, Math.max(j1, k1));
                    i1 = (int) ((float) i1 * f3 / f4);
                    j1 = (int) ((float) j1 * f3 / f4);
                    k1 = (int) ((float) k1 * f3 / f4);
                    int k2 = (i1 << 8) + j1;
                    k2 = (k2 << 8) + k1;
                    stack.getTagCompound().getCompoundTag("display").setInteger("PrimaryColor", k2);
                }

                if (k - inventoryCrafting.getWidth() >= 0 && net.minecraftforge.oredict.DyeUtils.isDye(inventoryCrafting.getStackInSlot(k - inventoryCrafting.getWidth()))) {
                    int[] aint = new int[3];
                    int i = 0;
                    float[] afloat = net.minecraftforge.oredict.DyeUtils.colorFromStack(inventoryCrafting.getStackInSlot(k - inventoryCrafting.getWidth())).get().getColorComponentValues();
                    int l1 = MathHelper.clamp((int) (afloat[0] * 255.0F) *2, 0, 255);
                    int i2 =  MathHelper.clamp((int) (afloat[1] * 255.0F) *2, 0, 255);
                    int j2 =  MathHelper.clamp((int) (afloat[2] * 255.0F) *2, 0, 255);
                    i += Math.max(l1, Math.max(i2, j2));
                    aint[0] += l1;
                    aint[1] += i2;
                    aint[2] += j2;
                    int i1 = aint[0];
                    int j1 = aint[1];
                    int k1 = aint[2];
                    float f3 = (float) i;
                    float f4 = (float) Math.max(i1, Math.max(j1, k1));
                    i1 = (int) ((float) i1 * f3 / f4);
                    j1 = (int) ((float) j1 * f3 / f4);
                    k1 = (int) ((float) k1 * f3 / f4);
                    int k2 = (i1 << 8) + j1;
                    k2 = (k2 << 8) + k1;
                    stack.getTagCompound().getCompoundTag("display").setInteger("SecondaryColor", k2);
                }

                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);
            nonnulllist.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
        }

        return nonnulllist;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
}
