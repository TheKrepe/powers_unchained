package net.threetag.palladium.item.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.threetag.palladium.item.MultiversalExtrapolatorItem;
import net.threetag.palladium.item.PalladiumItems;

import java.util.List;

public abstract class ExtrapolatorCraftingRecipe extends CustomRecipe {

    protected final List<SizedIngredient> ingredients;

    protected ExtrapolatorCraftingRecipe(List<SizedIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<SizedIngredient> getIngredients() {
        return this.ingredients;
    }

    public abstract int blankExtrapolatorCount();

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int designated = 0;
        int blank = 0;
        int[] remaining = new int[this.ingredients.size()];

        for (int i = 0; i < remaining.length; i++) {
            remaining[i] = this.ingredients.get(i).count();
        }

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof MultiversalExtrapolatorItem) {
                if (MultiversalExtrapolatorItem.getUniverse(stack) != null) {
                    designated++;
                } else {
                    blank++;
                }
                continue;
            }

            if (!this.consumeIngredient(stack, remaining)) {
                return false;
            }
        }

        if (designated != 1 || blank != this.blankExtrapolatorCount()) {
            return false;
        }

        for (int left : remaining) {
            if (left > 0) {
                return false;
            }
        }

        return true;
    }

    private boolean consumeIngredient(ItemStack stack, int[] remaining) {
        for (int i = 0; i < this.ingredients.size(); i++) {
            if (remaining[i] > 0 && this.ingredients.get(i).ingredient().test(stack)) {
                remaining[i]--;
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        var designated = findDesignatedExtrapolator(input);

        if (designated.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var universe = MultiversalExtrapolatorItem.getUniverse(designated);
        var result = PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get().getDefaultInstance();

        if (universe != null) {
            MultiversalExtrapolatorItem.setUniverse(result, universe);
        }

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = CraftingRecipe.defaultCraftingReminder(input);

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);

            if (stack.getItem() instanceof MultiversalExtrapolatorItem && MultiversalExtrapolatorItem.getUniverse(stack) != null) {
                remaining.set(slot, stack.copyWithCount(1));
                break;
            }
        }

        return remaining;
    }

    private static ItemStack findDesignatedExtrapolator(CraftingInput input) {
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);

            if (stack.getItem() instanceof MultiversalExtrapolatorItem && MultiversalExtrapolatorItem.getUniverse(stack) != null) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
