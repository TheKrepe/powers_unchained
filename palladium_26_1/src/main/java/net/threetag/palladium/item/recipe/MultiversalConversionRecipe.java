package net.threetag.palladium.item.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MultiversalConversionRecipe(Ingredient input, ItemStackTemplate result,
                                          ResourceKey<Universe> universe) implements MultiversalIteratorRecipe {

    public static final MapCodec<MultiversalConversionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(MultiversalConversionRecipe::input),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(MultiversalConversionRecipe::result),
            ResourceKey.codec(PalladiumRegistryKeys.MULTIVERSE).fieldOf("universe").forGetter(MultiversalConversionRecipe::universe)
    ).apply(instance, MultiversalConversionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiversalConversionRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, MultiversalConversionRecipe::input,
            ItemStackTemplate.STREAM_CODEC, MultiversalConversionRecipe::result,
            ResourceKey.streamCodec(PalladiumRegistryKeys.MULTIVERSE), MultiversalConversionRecipe::universe,
            MultiversalConversionRecipe::new
    );

    @Override
    public List<Conversion> getConversions(MultiversalIteratorRecipeInput input, HolderLookup.Provider registries) {
        if (!this.universe.equals(input.universe()) || !this.input.test(input.input())) {
            return List.of();
        }

        var result = this.result.create();
        return List.of(new Conversion(BuiltInRegistries.ITEM.getKey(result.getItem()), result));
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<MultiversalIteratorRecipeInput>> getSerializer() {
        return PalladiumRecipeSerializers.MULTIVERSAL_CONVERSION.get();
    }
}
