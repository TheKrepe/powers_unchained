package net.threetag.palladium.item.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class PalladiumRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Palladium.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<TailoringRecipe>> TAILORING = create("tailoring");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MultiversalIteratorRecipe>> MULTIVERSAL_ITERATOR = create("multiversal_iterator");

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> create(String path) {
        return RECIPE_TYPES.register(path, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return Palladium.id(path).toString();
            }
        });
    }

    @SubscribeEvent
    static void datapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(TAILORING.get());
        event.sendRecipes(MULTIVERSAL_ITERATOR.get());

        // The JEI plugin builds crafting entries for the extrapolator recipes from the actual loaded
        // recipes, so their type needs to be available on the client.
        if (ModList.get().isLoaded("jei")) {
            event.sendRecipes(RecipeType.CRAFTING);
        }
    }
}
