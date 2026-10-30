package com.star_1mp.powersunchained.compat.rei;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.client.suitcreator.SuitRecipeArt;
import com.star_1mp.powersunchained.client.techtable.TechRecipeArt;
import com.star_1mp.powersunchained.tech.TechRecipe;
import com.star_1mp.powersunchained.tech.TechRecipes;
import com.star_1mp.powersunchained.registry.PUItems;
import com.star_1mp.powersunchained.suit.SuitRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** REI: the Suit Creator's blueprints as a display category (only loaded by REI, which is optional). */
@REIPluginClient
public class PUReiPlugin implements REIClientPlugin {

    public static final CategoryIdentifier<SuitCreatorDisplay> SUIT_CREATOR = CategoryIdentifier.of(Constants.MOD_ID, "suit_creator");
    public static final CategoryIdentifier<TechTableDisplay> TECH_TABLE = CategoryIdentifier.of(Constants.MOD_ID, "tech_table");

    /** Dev/CI only: opens the Tech Table category. */
    public static void debugShowTechTable() {
        me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().addCategory(TECH_TABLE).open();
    }

    /** Dev/CI only: opens the Suit Creator category. */
    public static void debugShow() {
        me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().addCategory(SUIT_CREATOR).open();
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new SuitCreatorCategory());
        registry.addWorkstations(SUIT_CREATOR, EntryStacks.of(PUItems.SUIT_CREATOR.get()));
        registry.add(new TechTableCategory());
        registry.addWorkstations(TECH_TABLE, EntryStacks.of(PUItems.TECH_TABLE.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (SuitRecipe recipe : SuitRecipe.all()) {
            registry.add(new SuitCreatorDisplay(recipe));
        }
        for (TechRecipe recipe : TechRecipes.all()) {
            registry.add(new TechTableDisplay(recipe));
        }
    }

    /** Inputs = the used grid cells in order (see {@link TechTableCategory#setupDisplay}). */
    private static List<EntryIngredient> cells(TechRecipe recipe) {
        List<EntryIngredient> list = new ArrayList<>();
        for (var cell : recipe.layout()) {
            if (cell != null) {
                list.add(EntryIngredients.ofItemStacks(cell.display().get()));
            }
        }
        return list;
    }

    public static final class TechTableDisplay extends BasicDisplay {

        final TechRecipe recipe;

        TechTableDisplay(TechRecipe recipe) {
            super(cells(recipe), List.of(EntryIngredients.of(recipe.resultStack())),
                    Optional.of(Constants.id("tech_table/" + recipe.id())));
            this.recipe = recipe;
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return TECH_TABLE;
        }

        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return null;  // client-only, built from TechRecipes
        }
    }

    static final class TechTableCategory implements DisplayCategory<TechTableDisplay> {

        @Override
        public CategoryIdentifier<? extends TechTableDisplay> getCategoryIdentifier() {
            return TECH_TABLE;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("block.powers_unchained.tech_table");
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(PUItems.TECH_TABLE.get());
        }

        @Override
        public int getDisplayWidth(TechTableDisplay display) {
            return TechRecipeArt.WIDTH + 8;
        }

        @Override
        public int getDisplayHeight() {
            return TechRecipeArt.HEIGHT + 8;
        }

        @Override
        public List<Widget> setupDisplay(TechTableDisplay display, Rectangle bounds) {
            TechRecipe recipe = display.recipe;
            int x = bounds.x + 4;
            int y = bounds.y + 4;
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> TechRecipeArt.draw(graphics, recipe, x, y)));
            var layout = recipe.layout();
            int n = 0;
            for (int i = 0; i < 9; i++) {
                if (layout.get(i) != null) {
                    int[] s = TechRecipeArt.cellSlot(i);
                    widgets.add(Widgets.createSlot(new Point(x + s[0], y + s[1])).entries(display.getInputEntries().get(n++))
                            .disableBackground().markInput());
                }
            }
            widgets.add(Widgets.createSlot(new Point(x + TechRecipeArt.RESULT[0], y + TechRecipeArt.RESULT[1]))
                    .entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
            return widgets;
        }
    }

    private static List<EntryIngredient> ingredients(List<ItemStack> stacks) {
        List<EntryIngredient> list = new ArrayList<>();
        for (ItemStack stack : stacks) {
            list.add(EntryIngredients.of(stack));
        }
        return list;
    }

    public static final class SuitCreatorDisplay extends BasicDisplay {

        final SuitRecipe recipe;

        SuitCreatorDisplay(SuitRecipe recipe) {
            super(ingredients(recipe.inputs()), ingredients(recipe.outputs()),
                    Optional.of(Constants.id("suit_creator/" + recipe.suit().id().getPath() + "/" + recipe.piece())));
            this.recipe = recipe;
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return SUIT_CREATOR;
        }

        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return null;  // client-only, built from the suit catalog
        }
    }

    static final class SuitCreatorCategory implements DisplayCategory<SuitCreatorDisplay> {

        @Override
        public CategoryIdentifier<? extends SuitCreatorDisplay> getCategoryIdentifier() {
            return SUIT_CREATOR;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("block.powers_unchained.suit_creator");
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(PUItems.SUIT_CREATOR.get());
        }

        @Override
        public int getDisplayWidth(SuitCreatorDisplay display) {
            return SuitRecipeArt.WIDTH + 8;
        }

        @Override
        public int getDisplayHeight() {
            return SuitRecipeArt.HEIGHT + 8;
        }

        @Override
        public List<Widget> setupDisplay(SuitCreatorDisplay display, Rectangle bounds) {
            SuitRecipe recipe = display.recipe;
            int x = bounds.x + 4;
            int y = bounds.y + 4;
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> SuitRecipeArt.draw(graphics, recipe, x, y)));
            for (int i = 0; i < Math.min(8, recipe.inputs().size()); i++) {
                int[] s = SuitRecipeArt.inputSlot(i);
                widgets.add(Widgets.createSlot(new Point(x + s[0], y + s[1])).entries(display.getInputEntries().get(i))
                        .disableBackground().markInput());
            }
            for (int i = 0; i < recipe.outputs().size(); i++) {
                int[] s = SuitRecipeArt.outputSlot(recipe, i);
                widgets.add(Widgets.createSlot(new Point(x + s[0], y + s[1])).entries(display.getOutputEntries().get(i))
                        .disableBackground().markOutput());
            }
            int[] box = SuitRecipeArt.UNLOCK_BOX;
            widgets.add(Widgets.createTooltip(new Rectangle(x + box[0], y + box[1], box[2], box[3]),
                    SuitRecipeArt.unlockTooltip(recipe)));
            return widgets;
        }
    }
}
