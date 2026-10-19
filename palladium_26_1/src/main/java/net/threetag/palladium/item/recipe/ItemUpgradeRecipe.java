package net.threetag.palladium.item.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.level.Level;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.upgrade.ItemUpgrade;
import net.threetag.palladium.item.upgrade.ItemUpgrades;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.List;
import java.util.Optional;

public class ItemUpgradeRecipe extends SimpleSmithingRecipe {

    public static final MapCodec<ItemUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            Ingredient.CODEC.fieldOf("template").forGetter(r -> r.template),
            Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
            Ingredient.CODEC.optionalFieldOf("addition").forGetter(r -> r.addition),
            RegistryFixedCodec.create(PalladiumRegistryKeys.ITEM_UPGRADE).fieldOf("upgrade").forGetter(r -> r.upgrade)
    ).apply(instance, ItemUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.template,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, r -> r.addition,
            ByteBufCodecs.holderRegistry(PalladiumRegistryKeys.ITEM_UPGRADE), r -> r.upgrade,
            ItemUpgradeRecipe::new
    );

    private final Ingredient template;
    private final Ingredient base;
    private final Optional<Ingredient> addition;
    private final Holder<ItemUpgrade> upgrade;

    public ItemUpgradeRecipe(Recipe.CommonInfo commonInfo, Ingredient template, Ingredient base, Optional<Ingredient> addition, Holder<ItemUpgrade> upgrade) {
        super(commonInfo);
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.upgrade = upgrade;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.of(this.template);
    }

    @Override
    public Ingredient baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return this.addition;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!Ingredient.testOptionalIngredient(this.templateIngredient(), input.template())
                || !this.base.test(input.base())
                || !Ingredient.testOptionalIngredient(this.additionIngredient(), input.addition())) {
            return false;
        }

        ItemUpgrade upgrade = this.upgrade.value();
        if (!upgrade.isApplicableTo(input.base())) {
            return false;
        }

        ItemUpgrades installed = ItemUpgrades.get(input.base());
        if (installed.has(this.upgrade)) {
            return false;
        }

        for (Holder<ItemUpgrade> installedUpgrade : installed.upgrades()) {
            if (upgrade.isIncompatibleWith(installedUpgrade) || installedUpgrade.value().isIncompatibleWith(this.upgrade)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack result = input.base().copyWithCount(1);
        ItemUpgrade upgrade = this.upgrade.value();
        result.set(PalladiumDataComponents.Items.ITEM_UPGRADES.get(), ItemUpgrades.get(result).with(this.upgrade));
        result.applyComponents(upgrade.components());
        return result;
    }

    @Override
    public RecipeSerializer<ItemUpgradeRecipe> getSerializer() {
        return PalladiumRecipeSerializers.ITEM_UPGRADE.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(List.of(Optional.of(this.template), Optional.of(this.base), this.addition));
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SmithingRecipeDisplay(
                this.template.display(),
                this.base.display(),
                Ingredient.optionalIngredientToDisplay(this.addition),
                this.base.display(),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
        ));
    }

}
