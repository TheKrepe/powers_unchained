package net.threetag.palladium.item.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.threetag.palladium.component.PalladiumDataComponents;

import java.util.stream.Stream;

public record MultiversalCategoryIngredient(Identifier category) implements ICustomIngredient {

    public static final MapCodec<MultiversalCategoryIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("category").forGetter(MultiversalCategoryIngredient::category)
    ).apply(instance, MultiversalCategoryIngredient::new));

    @Override
    public boolean test(ItemStack stack) {
        return this.category.equals(stack.get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get()));
    }

    @Override
    public Stream<Holder<Item>> items() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(item -> this.category.equals(item.value().components().get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get())))
                .map(item -> item);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return PalladiumIngredientTypes.MULTIVERSAL_CATEGORY.get();
    }
}
