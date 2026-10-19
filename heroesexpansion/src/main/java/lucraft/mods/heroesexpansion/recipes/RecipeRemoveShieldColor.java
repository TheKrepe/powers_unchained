package lucraft.mods.heroesexpansion.recipes;

import lucraft.mods.heroesexpansion.items.HEItems;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeRemoveShieldColor extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean shield = false;
        boolean bucket = false;

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty()) {
                if (stack.getItem() == HEItems.CAPTAIN_AMERICA_SHIELD) {
                    if (!shield)
                        shield = true;
                    else
                        return false;
                }

                if (stack.getItem() == Items.WATER_BUCKET) {
                    if (!bucket)
                        bucket = true;
                    else
                        return false;
                }
            }
        }

        return shield && bucket;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty()) {
                if (stack.getItem() == HEItems.CAPTAIN_AMERICA_SHIELD) {
                    ItemStack shield = new ItemStack(HEItems.VIBRANIUM_SHIELD);
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
