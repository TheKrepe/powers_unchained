package net.threetag.palladium.item.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExtrapolatorCloningRecipe extends ExtrapolatorCraftingRecipe {

    public static final MapCodec<ExtrapolatorCloningRecipe> CODEC = ExtraCodecs.compactListCodec(SizedIngredient.NESTED_CODEC)
            .optionalFieldOf("ingredients", List.of())
            .xmap(ExtrapolatorCloningRecipe::new, ExtrapolatorCloningRecipe::getIngredients);

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtrapolatorCloningRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), ExtrapolatorCloningRecipe::getIngredients,
            ExtrapolatorCloningRecipe::new
    );

    public ExtrapolatorCloningRecipe(List<SizedIngredient> ingredients) {
        super(ingredients);
    }

    @Override
    public int blankExtrapolatorCount() {
        return 0;
    }

    @Override
    public @NotNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return PalladiumRecipeSerializers.MULTIVERSAL_EXTRAPOLATOR_CLONING.get();
    }
}
