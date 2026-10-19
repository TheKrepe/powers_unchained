package lucraft.mods.heroesexpansion.recipes;

import lucraft.mods.heroesexpansion.items.HEItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.DyeUtils;

public class RecipeCaptainAmericaShield extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean shield = false;
        boolean red = false;
        boolean blue = false;
        boolean white = false;

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty()) {
                if (stack.getItem() == HEItems.VIBRANIUM_SHIELD) {
                    if (!shield)
                        shield = true;
                    else
                        return false;
                }

                if (DyeUtils.isDye(stack) && EnumDyeColor.byMetadata(DyeUtils.metaFromStack(stack).getAsInt()) == EnumDyeColor.RED) {
                    if (!red)
                        red = true;
                    else
                        return false;
                }

                if (DyeUtils.isDye(stack) && EnumDyeColor.byMetadata(DyeUtils.metaFromStack(stack).getAsInt()) == EnumDyeColor.BLUE) {
                    if (!blue)
                        blue = true;
                    else
                        return false;
                }

                if (DyeUtils.isDye(stack) && EnumDyeColor.byMetadata(DyeUtils.metaFromStack(stack).getAsInt()) == EnumDyeColor.WHITE) {
                    if (!white)
                        white = true;
                    else
                        return false;
                }
            }
        }

        return shield && red && blue && white;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty()) {
                if (stack.getItem() == HEItems.VIBRANIUM_SHIELD) {
                    ItemStack shield = new ItemStack(HEItems.CAPTAIN_AMERICA_SHIELD);
                    shield.setItemDamage(stack.getItemDamage());
                    if (stack.hasTagCompound())
                        shield.setTagCompound(stack.getTagCompound());
                    return shield;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 4;
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