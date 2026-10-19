package net.threetag.palladium.item.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;

public class PalladiumRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Palladium.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ItemTailoringRecipe>> ITEM_TAILORING = RECIPE_SERIALIZERS.register("item_tailoring", () -> new RecipeSerializer<>(ItemTailoringRecipe.CODEC, ItemTailoringRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ItemUpgradeRecipe>> ITEM_UPGRADE = RECIPE_SERIALIZERS.register("item_upgrade", () -> new RecipeSerializer<>(ItemUpgradeRecipe.CODEC, ItemUpgradeRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MultiversalVariantsRecipe>> MULTIVERSAL_VARIANTS = RECIPE_SERIALIZERS.register("multiversal_variants", () -> new RecipeSerializer<>(MultiversalVariantsRecipe.CODEC, MultiversalVariantsRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MultiversalConversionRecipe>> MULTIVERSAL_CONVERSION = RECIPE_SERIALIZERS.register("multiversal_conversion", () -> new RecipeSerializer<>(MultiversalConversionRecipe.CODEC, MultiversalConversionRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ExtrapolatorTransferRecipe>> MULTIVERSAL_EXTRAPOLATOR_TRANSFER = RECIPE_SERIALIZERS.register("multiversal_extrapolator_transfer", () -> new RecipeSerializer<>(ExtrapolatorTransferRecipe.CODEC, ExtrapolatorTransferRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ExtrapolatorCloningRecipe>> MULTIVERSAL_EXTRAPOLATOR_CLONING = RECIPE_SERIALIZERS.register("multiversal_extrapolator_cloning", () -> new RecipeSerializer<>(ExtrapolatorCloningRecipe.CODEC, ExtrapolatorCloningRecipe.STREAM_CODEC));

}
