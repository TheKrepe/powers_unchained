package net.threetag.palladium.item.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiversalVariantsRecipe implements MultiversalIteratorRecipe {

    public static final MultiversalVariantsRecipe INSTANCE = new MultiversalVariantsRecipe();
    public static final MapCodec<MultiversalVariantsRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, MultiversalVariantsRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private MultiversalVariantsRecipe() {
    }

    @Override
    public List<Conversion> getConversions(MultiversalIteratorRecipeInput input, HolderLookup.Provider registries) {
        var universe = input.universe();
        var category = input.input().get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get());

        if (universe == null || category == null) {
            return List.of();
        }

        var lookup = registries.lookup(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT);

        if (lookup.isEmpty()) {
            return List.of();
        }

        var currentVariant = input.input().get(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get());

        return MultiversalItemVariant.variantsForCategory(registries, category).stream()
                .filter(key -> lookup.get().get(key)
                        .map(Holder.Reference::value)
                        .map(definition -> definition.universe().equals(universe))
                        .orElse(false))
                .filter(key -> currentVariant == null || !currentVariant.variant().equals(key))
                .map(key -> {
                    var result = input.input().copyWithCount(1);
                    MultiversalItemVariant.apply(registries, result, key);
                    return new Conversion(key.identifier(), result);
                })
                .toList();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<MultiversalIteratorRecipeInput>> getSerializer() {
        return PalladiumRecipeSerializers.MULTIVERSAL_VARIANTS.get();
    }
}
