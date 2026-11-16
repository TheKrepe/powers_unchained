package com.star_1mp.powersunleashed.tech;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * A Tech Table recipe (3x3 grid): shaped (a pattern of up to 3x3 that may sit anywhere in the grid, mirrored too, like
 * the crafting table) or shapeless.
 *
 * @param id       unique id (lang {@code tech_recipe.powers_unleashed.<id>} is its name in the list)
 * @param group    list group (lang {@code tech_group.powers_unleashed.<group>})
 * @param cells    shaped: 9 cells row by row (null = empty); shapeless: the ingredients
 */
public record TechRecipe(String id, String group, boolean shaped, int width, int height, List<@Nullable TechIngredient> cells,
        Supplier<? extends Item> result, int count) {

    public ItemStack resultStack() {
        return new ItemStack(this.result.get(), this.count);
    }

    /**
     * A shaped recipe from a pattern like {@code {"IDI", "EPE", "ILI"}} and a key (char -> ingredient; ' ' = empty).
     */
    public static TechRecipe shaped(String id, String group, String[] pattern, Map<Character, TechIngredient> key,
            Supplier<? extends Item> result, int count) {
        List<TechIngredient> cells = new ArrayList<>();
        int w = 0;
        for (String row : pattern) {
            w = Math.max(w, row.length());
        }
        for (String row : pattern) {
            for (int x = 0; x < w; x++) {
                char c = x < row.length() ? row.charAt(x) : ' ';
                cells.add(c == ' ' ? null : key.get(c));
            }
        }
        return new TechRecipe(id, group, true, w, pattern.length, java.util.Collections.unmodifiableList(cells), result, count);
    }

    public static TechRecipe shapeless(String id, String group, List<TechIngredient> ingredients, Supplier<? extends Item> result, int count) {
        return new TechRecipe(id, group, false, 0, 0, List.copyOf(ingredients), result, count);
    }

    /** Where every ingredient goes in the 3x3 grid when the recipe is placed (index 0-8 -> ingredient or null). */
    public List<@Nullable TechIngredient> layout() {
        List<TechIngredient> grid = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            grid.add(null);
        }
        if (this.shaped) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    // centred horizontally like the recipe book does for narrow patterns
                    int ox = (3 - this.width) / 2;
                    grid.set(y * 3 + x + ox, this.cells.get(y * this.width + x));
                }
            }
        } else {
            for (int i = 0; i < this.cells.size() && i < 9; i++) {
                grid.set(i, this.cells.get(i));
            }
        }
        return grid;
    }

    /** Whether the 9 grid stacks make this recipe. */
    public boolean matches(List<ItemStack> grid) {
        return this.shaped ? this.matchesShaped(grid) : this.matchesShapeless(grid);
    }

    private boolean matchesShapeless(List<ItemStack> grid) {
        List<TechIngredient> left = new ArrayList<>(this.cells);
        for (ItemStack stack : grid) {
            if (stack.isEmpty()) {
                continue;
            }
            TechIngredient found = null;
            for (TechIngredient ing : left) {
                if (ing.matches(stack)) {
                    found = ing;
                    break;
                }
            }
            if (found == null) {
                return false;
            }
            left.remove(found);
        }
        return left.isEmpty();
    }

    private boolean matchesShaped(List<ItemStack> grid) {
        for (int ox = 0; ox <= 3 - this.width; ox++) {
            for (int oy = 0; oy <= 3 - this.height; oy++) {
                if (this.matchesAt(grid, ox, oy, false) || this.matchesAt(grid, ox, oy, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesAt(List<ItemStack> grid, int ox, int oy, boolean mirror) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int px = x - ox;
                int py = y - oy;
                TechIngredient ing = null;
                if (px >= 0 && py >= 0 && px < this.width && py < this.height) {
                    ing = this.cells.get(py * this.width + (mirror ? this.width - 1 - px : px));
                }
                ItemStack stack = grid.get(y * 3 + x);
                if (ing == null ? !stack.isEmpty() : !ing.matches(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** What stays in the grid cell after crafting (bottles of potions / honey). */
    public static ItemStack remainder(ItemStack used) {
        if (used.is(Items.POTION) || used.is(Items.HONEY_BOTTLE)) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        return ItemStack.EMPTY;
    }
}
