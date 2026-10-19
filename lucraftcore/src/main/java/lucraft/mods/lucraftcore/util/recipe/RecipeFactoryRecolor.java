package lucraft.mods.lucraftcore.util.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Iterator;

public class RecipeFactoryRecolor implements IRecipeFactory {

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");

        Item original = ForgeRegistries.ITEMS.containsKey(new ResourceLocation(JsonUtils.getString(json, "original"))) ? ForgeRegistries.ITEMS.getValue(new ResourceLocation(JsonUtils.getString(json, "original"))) : null;
        if (original == null)
            throw new JsonParseException("Item '" + JsonUtils.getString(json, "original") + "' does not exist!");

        NonNullList<Ingredient> ings = NonNullList.create();
        for (JsonElement ele : JsonUtils.getJsonArray(json, "ingredients"))
            ings.add(CraftingHelper.getIngredient(ele, context));

        if (ings.isEmpty())
            throw new JsonParseException("No ingredients for 'alternate' recipe");
        if (ings.size() > 8)
            throw new JsonParseException("Too many ingredients for 'alternate' recipe");

        ItemStack itemstack = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
        return new RecipeRecolor(group, itemstack, original, ings);
    }

    public static class RecipeRecolor extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

        protected String group;
        public final ItemStack result;
        public final Item original;
        public final NonNullList<Ingredient> ingredientList;
        private final boolean isSimple;

        public RecipeRecolor(String group, ItemStack result, Item original, NonNullList<Ingredient> ingredientList) {
            this.group = group;
            this.result = result;
            this.original = original;
            this.ingredientList = ingredientList;
            boolean simple = true;
            for (Ingredient i : ingredientList)
                simple &= i.isSimple();
            this.isSimple = simple;
        }

        @Override
        public boolean matches(InventoryCrafting inv, World worldIn) {
            ArrayList<Ingredient> required = new ArrayList(this.ingredientList);
            boolean foundOriginal = false;

            for (int i = 0; i < inv.getSizeInventory(); ++i) {
                ItemStack slot = inv.getStackInSlot(i);
                if (!slot.isEmpty()) {
                    boolean inRecipe = false;
                    Iterator iterator = required.iterator();

                    if (!foundOriginal && slot.getItem() == this.original)
                        foundOriginal = true;
                    else {
                        while (iterator.hasNext()) {
                            Ingredient next = (Ingredient) iterator.next();
                            if (next.apply(slot)) {
                                inRecipe = true;
                                iterator.remove();
                                break;
                            }
                        }

                        if (!inRecipe) {
                            return false;
                        }
                    }
                }
            }
            return required.isEmpty() && foundOriginal;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            for (int i = 0; i < inv.getHeight(); ++i) {
                for (int j = 0; j < inv.getWidth(); ++j) {
                    ItemStack itemstack = inv.getStackInRowAndColumn(j, i);

                    if (this.original == itemstack.getItem()) {
                        ItemStack stack = new ItemStack(this.result.getItem());
                        stack.setTagCompound(itemstack.getTagCompound());
                        stack.setItemDamage(itemstack.getItemDamage());
                        return stack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canFit(int width, int height) {
            return width * height >= (this.ingredientList.size() + 1);
        }

        @Override
        public String getGroup() {
            return this.group;
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
}
