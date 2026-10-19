package net.threetag.palladium.item.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.stream.Stream;

public record UniverseIngredient(ResourceKey<Universe> universe) implements ICustomIngredient {

    public static final MapCodec<UniverseIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(PalladiumRegistryKeys.MULTIVERSE).fieldOf("universe").forGetter(UniverseIngredient::universe)
    ).apply(instance, UniverseIngredient::new));

    @Override
    public boolean test(ItemStack stack) {
        var variant = stack.get(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get());

        if (variant == null) {
            return false;
        }

        var registries = Palladium.PROXY.getRegistryAccess();

        if (registries == null) {
            return false;
        }

        return registries.lookup(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT)
                .flatMap(lookup -> lookup.get(variant.variant()))
                .map(definition -> definition.value().universe().equals(this.universe))
                .orElse(false);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(item -> item.value().components().has(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get()))
                .map(item -> item);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return PalladiumIngredientTypes.UNIVERSE.get();
    }
}
