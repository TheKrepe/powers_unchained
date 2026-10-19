package net.threetag.palladium.datagen.builder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.threetag.palladium.item.recipe.ItemTailoringRecipe;
import net.threetag.palladium.menu.slot.TailoringToolSlot;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemTailoringRecipeBuilder {

    private final Component title;
    private final Ingredient tool;
    private final Map<ArmorType, ItemStackTemplate> results = new EnumMap<>(ArmorType.class);
    private final List<SizedIngredient> ingredients = new ArrayList<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private Identifier toolIcon = TailoringToolSlot.EMPTY_TOOL_SLOT;
    @Nullable
    private Identifier category;
    private boolean requiresUnlocking = true;
    private boolean showNotification = true;

    private ItemTailoringRecipeBuilder(Component title, Ingredient tool) {
        this.title = title;
        this.tool = tool;
    }

    public static ItemTailoringRecipeBuilder tailoring(Component title, Ingredient tool) {
        return new ItemTailoringRecipeBuilder(title, tool);
    }

    public ItemTailoringRecipeBuilder toolIcon(@Nullable Identifier toolIcon) {
        this.toolIcon = toolIcon;
        return this;
    }

    public ItemTailoringRecipeBuilder result(ArmorType type, ItemLike item) {
        return this.result(type, new ItemStackTemplate(item.asItem()));
    }

    public ItemTailoringRecipeBuilder result(ArmorType type, ItemStackTemplate result) {
        this.results.put(type, result);
        return this;
    }

    public ItemTailoringRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public ItemTailoringRecipeBuilder requires(ItemLike item, int count) {
        return this.requires(SizedIngredient.of(item, count));
    }

    public ItemTailoringRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public ItemTailoringRecipeBuilder requires(Ingredient ingredient, int count) {
        return this.requires(new SizedIngredient(ingredient, count));
    }

    public ItemTailoringRecipeBuilder requires(SizedIngredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public ItemTailoringRecipeBuilder category(@Nullable Identifier category) {
        this.category = category;
        return this;
    }

    public ItemTailoringRecipeBuilder alwaysAvailable() {
        this.requiresUnlocking = false;
        return this;
    }

    public ItemTailoringRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    public ItemTailoringRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public void save(RecipeOutput output, Identifier id) {
        this.save(output, ResourceKey.create(Registries.RECIPE, id));
    }

    public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        this.ensureValid(key);

        var recipe = new ItemTailoringRecipe(
                new Recipe.CommonInfo(this.showNotification),
                this.title,
                Collections.unmodifiableMap(new EnumMap<>(this.results)),
                List.copyOf(this.ingredients),
                this.tool,
                Optional.ofNullable(this.toolIcon),
                Optional.ofNullable(this.category),
                this.requiresUnlocking
        );

        output.accept(key, recipe, this.criteria.isEmpty() ? null : this.buildAdvancement(output, key));
    }

    private AdvancementHolder buildAdvancement(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        Advancement.Builder builder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);
        return builder.build(key.identifier().withPrefix("recipes/tailoring/"));
    }

    private void ensureValid(ResourceKey<Recipe<?>> key) {
        if (this.results.isEmpty()) {
            throw new IllegalStateException("No results for tailoring recipe " + key.identifier());
        }

        if (this.ingredients.isEmpty()) {
            throw new IllegalStateException("No ingredients for tailoring recipe " + key.identifier());
        }

        if (this.requiresUnlocking && this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining tailoring recipe " + key.identifier());
        }
    }
}
