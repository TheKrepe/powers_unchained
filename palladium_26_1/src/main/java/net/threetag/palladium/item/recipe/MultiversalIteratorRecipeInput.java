package net.threetag.palladium.item.recipe;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.threetag.palladium.item.MultiversalExtrapolatorItem;
import net.threetag.palladium.item.multiverse.Universe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MultiversalIteratorRecipeInput(ItemStack extrapolator, ItemStack input) implements RecipeInput {

    @Override
    public @NotNull ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> this.extrapolator;
            case 1 -> this.input;
            default -> throw new IllegalArgumentException("Recipe does not contain slot " + index);
        };
    }

    @Override
    public int size() {
        return 2;
    }

    @Nullable
    public ResourceKey<Universe> universe() {
        return MultiversalExtrapolatorItem.getUniverse(this.extrapolator);
    }
}
