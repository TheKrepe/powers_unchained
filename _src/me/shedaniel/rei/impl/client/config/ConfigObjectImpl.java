package me.shedaniel.rei.impl.client.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.config.AppearanceTheme;
import me.shedaniel.rei.api.client.gui.config.CheatingMode;
import me.shedaniel.rei.api.client.gui.config.ConfigButtonPosition;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.config.DisplayScreenType;
import me.shedaniel.rei.api.client.gui.config.EntryPanelOrdering;
import me.shedaniel.rei.api.client.gui.config.FavoriteAddWidgetMode;
import me.shedaniel.rei.api.client.gui.config.ForceLocalRecipesMode;
import me.shedaniel.rei.api.client.gui.config.ItemCheatingMode;
import me.shedaniel.rei.api.client.gui.config.ItemCheatingStyle;
import me.shedaniel.rei.api.client.gui.config.RecipeBorderType;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.client.gui.config.SyntaxHighlightingMode;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl.Advanced;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl.Appearance;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl.Basics;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl.Functionality;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesEntriesManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Config(
   name = "roughlyenoughitems/config"
)
@OnlyIn(Dist.CLIENT)
@Internal
public class ConfigObjectImpl implements ConfigObject, ConfigData {
   public Basics basics = new Basics();
   public Appearance appearance = new Appearance();
   public Functionality functionality = new Functionality();
   public Advanced advanced = new Advanced();

   public boolean isOverlayVisible() {
      return this.basics.overlayVisible;
   }

   public void setOverlayVisible(boolean overlayVisible) {
      this.basics.overlayVisible = overlayVisible;
   }

   public boolean isCheating() {
      return this.basics.cheating == CheatingMode.ON
         || this.basics.cheating == CheatingMode.WHEN_CREATIVE
            && Minecraft.getInstance().gameMode != null
            && Minecraft.getInstance().gameMode.getPlayerMode() == GameType.CREATIVE;
   }

   public void setCheating(boolean cheating) {
      this.basics.cheating = cheating ? CheatingMode.ON : CheatingMode.OFF;
   }

   public CheatingMode getCheatingMode() {
      return this.basics.cheating;
   }

   public EntryPanelOrdering getItemListOrdering() {
      return this.advanced.layout.entryPanelOrdering.getOrdering();
   }

   public boolean isItemListAscending() {
      return this.advanced.layout.entryPanelOrdering.isAscending();
   }

   public boolean isUsingDarkTheme() {
      return this.appearance.theme == AppearanceTheme.DARK;
   }

   public void setUsingDarkTheme(boolean dark) {
      this.appearance.theme = dark ? AppearanceTheme.DARK : AppearanceTheme.LIGHT;
   }

   public boolean isGrabbingItems() {
      return this.basics.cheatingStyle == ItemCheatingStyle.GRAB;
   }

   public boolean isReducedMotion() {
      return this.basics.reduceMotion;
   }

   public boolean setReducedMotion(boolean reducedMotion) {
      return this.basics.reduceMotion = reducedMotion;
   }

   public boolean isToastDisplayedOnCopyIdentifier() {
      return this.advanced.accessibility.toastDisplayedOnCopyIdentifier;
   }

   public boolean isEntryListWidgetScrolled() {
      return this.appearance.scrollingEntryListWidget;
   }

   public void setEntryListWidgetScrolled(boolean scrollingEntryListWidget) {
      this.appearance.scrollingEntryListWidget = scrollingEntryListWidget;
   }

   public boolean isHidingEntryPanelIfIdle() {
      return this.appearance.hideEntryPanelIfIdle;
   }

   public void setHidingEntryPanelIfIdle(boolean hideEntryPanelIfIdle) {
      this.appearance.hideEntryPanelIfIdle = hideEntryPanelIfIdle;
   }

   public boolean shouldAppendModNames() {
      return this.advanced.tooltips.appendModNames;
   }

   public DisplayScreenType getRecipeScreenType() {
      return this.appearance.recipeScreenType;
   }

   public void setRecipeScreenType(DisplayScreenType displayScreenType) {
      this.appearance.recipeScreenType = displayScreenType;
   }

   public SearchFieldLocation getSearchFieldLocation() {
      return this.appearance.layout.searchFieldLocation;
   }

   public void setSearchFieldLocation(SearchFieldLocation location) {
      this.appearance.layout.searchFieldLocation = location;
   }

   public DisplayPanelLocation getDisplayPanelLocation() {
      return this.advanced.accessibility.displayPanelLocation;
   }

   public void setDisplayPanelLocation(DisplayPanelLocation location) {
      this.advanced.accessibility.displayPanelLocation = location;
   }

   public boolean isCraftableFilterEnabled() {
      return this.appearance.layout.showCraftableOnlyButton;
   }

   public void setCraftableFilterEnabled(boolean enabled) {
      this.appearance.layout.showCraftableOnlyButton = enabled;
   }

   public String getGamemodeCommand() {
      return this.advanced.commands.gamemodeCommand;
   }

   public String getGiveCommand() {
      return this.advanced.commands.giveCommand;
   }

   public String getWeatherCommand() {
      return this.advanced.commands.weatherCommand;
   }

   public String getTimeCommand() {
      return this.advanced.commands.timeCommand;
   }

   public int getMaxRecipePerPage() {
      return this.advanced.layout.maxRecipesPerPage;
   }

   public int getMaxRecipesPageHeight() {
      return this.advanced.layout.maxRecipesPageHeight;
   }

   @Nullable
   public Identifier getInputMethodId() {
      return this.functionality.inputMethod;
   }

   public void setInputMethodId(@Nullable Identifier id) {
      this.functionality.inputMethod = id;
   }

   public boolean doesDisableRecipeBook() {
      return this.functionality.disableRecipeBook;
   }

   public void setDisableRecipeBook(boolean disableRecipeBook) {
      this.functionality.disableRecipeBook = disableRecipeBook;
   }

   public boolean doesFixTabCloseContainer() {
      return this.functionality.disableRecipeBook;
   }

   public boolean isLeftSideMobEffects() {
      return this.functionality.leftSideMobEffects;
   }

   public void setLeftSideMobEffects(boolean leftSideMobEffects) {
      this.functionality.leftSideMobEffects = leftSideMobEffects;
   }

   public boolean areClickableRecipeArrowsEnabled() {
      return this.advanced.miscellaneous.clickableRecipeArrows;
   }

   public RecipeBorderType getRecipeBorderType() {
      return this.appearance.recipeBorder;
   }

   public boolean isCompositeScrollBarPermanent() {
      return this.advanced.accessibility.compositeScrollBarPermanent;
   }

   public boolean doesRegisterRecipesInAnotherThread() {
      return this.advanced.miscellaneous.registerRecipesInAnotherThread;
   }

   public boolean doesSnapToRows() {
      return false;
   }

   public boolean isFavoritesEnabled() {
      return this.basics.favoritesEnabled;
   }

   public boolean doDisplayFavoritesTooltip() {
      return this.isFavoritesEnabled() && this.advanced.tooltips.displayFavoritesTooltip;
   }

   public boolean doDisplayIMEHints() {
      return this.advanced.tooltips.displayIMEHints;
   }

   public void setDoDisplayIMEHints(boolean displayIMEHints) {
      this.advanced.tooltips.displayIMEHints = displayIMEHints;
   }

   public boolean doesCacheEntryRendering() {
      return this.advanced.miscellaneous.cachingFastEntryRendering;
   }

   public void setDoesCacheEntryRendering(boolean doesCacheEntryRendering) {
      this.advanced.miscellaneous.cachingFastEntryRendering = doesCacheEntryRendering;
   }

   public boolean doesCacheDisplayLookup() {
      return this.advanced.miscellaneous.cachingDisplayLookup;
   }

   public ForceLocalRecipesMode getForceLocalRecipes() {
      return this.advanced.miscellaneous.forceLocalRecipes;
   }

   public boolean doDebugRenderTimeRequired() {
      return this.advanced.layout.debugRenderTimeRequired;
   }

   public boolean doMergeDisplayUnderOne() {
      return this.advanced.layout.mergeDisplayUnderOne;
   }

   public FavoriteAddWidgetMode getFavoriteAddWidgetMode() {
      return this.advanced.layout.favoriteAddWidgetMode;
   }

   public ModifierKeyCode getFavoriteKeyCode() {
      return this.basics.keyBindings.favoriteKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.favoriteKeybind;
   }

   public ModifierKeyCode getRecipeKeybind() {
      return this.basics.keyBindings.recipeKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.recipeKeybind;
   }

   public ModifierKeyCode getUsageKeybind() {
      return this.basics.keyBindings.usageKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.usageKeybind;
   }

   public ModifierKeyCode getHideKeybind() {
      return this.basics.keyBindings.hideKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.hideKeybind;
   }

   public ModifierKeyCode getPreviousPageKeybind() {
      return this.basics.keyBindings.previousPageKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.previousPageKeybind;
   }

   public ModifierKeyCode getNextPageKeybind() {
      return this.basics.keyBindings.nextPageKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.nextPageKeybind;
   }

   public ModifierKeyCode getFocusSearchFieldKeybind() {
      return this.basics.keyBindings.focusSearchFieldKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.focusSearchFieldKeybind;
   }

   public ModifierKeyCode getCopyRecipeIdentifierKeybind() {
      return this.basics.keyBindings.copyRecipeIdentifierKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.copyRecipeIdentifierKeybind;
   }

   public ModifierKeyCode getExportImageKeybind() {
      return ModifierKeyCode.unknown();
   }

   public ModifierKeyCode getPreviousScreenKeybind() {
      return this.basics.keyBindings.previousScreenKeybind == null ? ModifierKeyCode.unknown() : this.basics.keyBindings.previousScreenKeybind;
   }

   public double getEntrySize() {
      return this.advanced.accessibility.entrySize;
   }

   public boolean setEntrySize(double entrySize) {
      double original = this.advanced.accessibility.entrySize;
      return (this.advanced.accessibility.entrySize = Mth.clamp(entrySize, 0.25, 4.0)) != original;
   }

   public boolean isUsingCompactTabs() {
      return this.advanced.accessibility.useCompactTabs;
   }

   public boolean isUsingCompactTabButtons() {
      return this.advanced.accessibility.useCompactTabButtons;
   }

   public boolean isLowerConfigButton() {
      return this.appearance.layout.configButtonLocation == ConfigButtonPosition.LOWER;
   }

   public List<FavoriteEntry> getFavoriteEntries() {
      return FavoritesEntriesManager.INSTANCE.asListView();
   }

   public List<FavoriteEntry> getConfigFavoriteEntries() {
      return FavoritesConfigManager.getInstance().getConfig().favorites;
   }

   public List<FavoriteEntry> getHiddenFavoriteEntries() {
      return FavoritesConfigManager.getInstance().getConfig().hiddenFavorites;
   }

   public List<CompoundTag> getDisplayHistory() {
      return FavoritesConfigManager.getInstance().getConfig().displays;
   }

   public List<EntryStackProvider<?>> getFilteredStackProviders() {
      return this.advanced.filtering.filteredStacks;
   }

   @Experimental
   public boolean shouldFilterDisplays() {
      return this.advanced.filtering.shouldFilterDisplays;
   }

   @Internal
   public List<FilteringRule<?>> getFilteringRules() {
      return this.advanced.filtering.filteringRules;
   }

   @Experimental
   public Map<CategoryIdentifier<?>, Boolean> getFilteringQuickCraftCategories() {
      return this.advanced.miscellaneous.categorySettings.filteringQuickCraftCategories;
   }

   @Internal
   public void setFilteringQuickCraftCategories(Map<CategoryIdentifier<?>, Boolean> filteringQuickCraftCategories) {
      this.advanced.miscellaneous.categorySettings.filteringQuickCraftCategories = filteringQuickCraftCategories;
   }

   @Experimental
   public Set<CategoryIdentifier<?>> getHiddenCategories() {
      return this.advanced.miscellaneous.categorySettings.hiddenCategories;
   }

   @Internal
   public void setHiddenCategories(Set<CategoryIdentifier<?>> hiddenCategories) {
      this.advanced.miscellaneous.categorySettings.hiddenCategories = hiddenCategories;
   }

   @Experimental
   public List<CategoryIdentifier<?>> getCategoryOrdering() {
      return this.advanced.miscellaneous.categorySettings.categoryOrdering;
   }

   @Internal
   public void setCategoryOrdering(List<CategoryIdentifier<?>> categoryOrdering) {
      this.advanced.miscellaneous.categorySettings.categoryOrdering = categoryOrdering;
   }

   @Experimental
   public boolean shouldAsyncSearch() {
      return this.advanced.search.asyncSearch;
   }

   @Experimental
   public int getAsyncSearchPartitionSize() {
      return this.advanced.search.asyncSearchPartitionSize;
   }

   @Experimental
   public boolean isPatchingAsyncThreadCrash() {
      return this.advanced.search.patchAsyncThreadCrash;
   }

   @Experimental
   public boolean doDebugSearchTimeRequired() {
      return this.advanced.search.debugSearchTimeRequired;
   }

   public boolean isSubsetsEnabled() {
      return this.functionality.isSubsetsEnabled;
   }

   public boolean isInventoryHighlightingAllowed() {
      return this.functionality.allowInventoryHighlighting;
   }

   public ItemCheatingMode getItemCheatingMode() {
      return this.functionality.itemCheatingMode;
   }

   @Experimental
   public double getHorizontalEntriesBoundariesPercentage() {
      return Mth.clamp(this.appearance.horizontalEntriesBoundaries, 0.1, 1.0);
   }

   @Experimental
   public double getVerticalEntriesBoundariesPercentage() {
      return Mth.clamp(this.appearance.verticalEntriesBoundaries, 0.1, 1.0);
   }

   @Experimental
   public double getHorizontalEntriesBoundariesColumns() {
      return Mth.clamp(this.appearance.horizontalEntriesBoundariesColumns, 1, 1000);
   }

   @Experimental
   public double getVerticalEntriesBoundariesRows() {
      return Mth.clamp(this.appearance.verticalEntriesBoundariesRows, 1, 1000);
   }

   public double getHorizontalEntriesBoundariesAlignments() {
      return this.appearance.horizontalEntriesBoundariesAlignment;
   }

   public double getVerticalEntriesBoundariesAlignments() {
      return this.appearance.verticalEntriesBoundariesAlignment;
   }

   @Experimental
   public double getFavoritesHorizontalEntriesBoundariesPercentage() {
      return Mth.clamp(this.appearance.favoritesHorizontalEntriesBoundaries, 0.1, 1.0);
   }

   public double getFavoritesHorizontalEntriesBoundariesColumns() {
      return Mth.clamp(this.appearance.favoritesHorizontalEntriesBoundariesColumns, 1, 1000);
   }

   public SyntaxHighlightingMode getSyntaxHighlightingMode() {
      return this.appearance.syntaxHighlightingMode;
   }

   public void setSyntaxHighlightingMode(SyntaxHighlightingMode mode) {
      this.appearance.syntaxHighlightingMode = mode;
   }

   public boolean isFocusModeZoomed() {
      return this.appearance.isFocusModeZoomed;
   }

   public SearchMode getTooltipSearchMode() {
      return this.advanced.search.tooltipSearch;
   }

   public SearchMode getTagSearchMode() {
      return this.advanced.search.tagSearch;
   }

   public SearchMode getIdentifierSearchMode() {
      return this.advanced.search.identifierSearch;
   }

   public SearchMode getModSearchMode() {
      return this.advanced.search.modSearch;
   }
}
