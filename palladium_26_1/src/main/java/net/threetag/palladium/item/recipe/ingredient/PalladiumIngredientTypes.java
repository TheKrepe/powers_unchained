package net.threetag.palladium.item.recipe.ingredient;

import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.Palladium;

public class PalladiumIngredientTypes {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Palladium.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<UniverseIngredient>> UNIVERSE = INGREDIENT_TYPES.register("universe", () -> new IngredientType<>(UniverseIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<MultiversalCategoryIngredient>> MULTIVERSAL_CATEGORY = INGREDIENT_TYPES.register("multiversal_category", () -> new IngredientType<>(MultiversalCategoryIngredient.CODEC));

}
