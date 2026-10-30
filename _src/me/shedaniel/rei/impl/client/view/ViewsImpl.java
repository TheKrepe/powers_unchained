package me.shedaniel.rei.impl.client.view;

import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry.CategoryConfiguration;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplayMerger;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.craftable.CraftableFilterCalculator;
import me.shedaniel.rei.impl.client.gui.widget.AutoCraftingEvaluator;
import me.shedaniel.rei.impl.client.gui.widget.AutoCraftingEvaluator.AutoCraftingResult;
import me.shedaniel.rei.impl.client.registry.display.DisplayCache;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class ViewsImpl implements Views {
   private static final ThreadLocal<ViewSearchBuilder> BUILDER = new ThreadLocal<>();

   @Nullable
   public ViewSearchBuilder getContext() {
      return BUILDER.get();
   }

   public static Map<DisplayCategory<?>, List<DisplaySpec>> buildMapFor(ViewSearchBuilder builder) {
      BUILDER.set(builder);

      Map var1;
      try {
         var1 = _buildMapFor(builder);
      } finally {
         BUILDER.remove();
      }

      return var1;
   }

   private static Map<DisplayCategory<?>, List<DisplaySpec>> _buildMapFor(ViewSearchBuilder builder) {
      if (PluginManager.areAnyReloading()) {
         InternalLogger.getInstance().info("Cancelled Views buildMap since plugins have not finished reloading.");
         return Maps.newLinkedHashMap();
      } else {
         Stopwatch stopwatch = Stopwatch.createStarted();
         boolean processingVisibilityHandlers = builder.isProcessingVisibilityHandlers();
         Set<CategoryIdentifier<?>> categories = new HashSet<>(builder.getCategories());
         Set<CategoryIdentifier<?>> filteringCategories = builder.getFilteringCategories();
         List<EntryStack<?>> recipesForStacks = builder.getRecipesFor();
         List<EntryStack<?>> usagesForStacks = builder.getUsagesFor();
         Function<EntryStack<?>, Collection<EntryStack<?>>> wildcardFunction = stack -> {
            EntryStack<?> wildcard = stack.wildcard();
            return EntryStacks.equalsFuzzy(wildcard, stack) ? Collections.emptyList() : Collections.singletonList(wildcard);
         };
         List<EntryStack<?>> recipesForStacksWildcard = CollectionUtils.flatMap(recipesForStacks, wildcardFunction);
         List<EntryStack<?>> usagesForStacksWildcard = CollectionUtils.flatMap(usagesForStacks, wildcardFunction);
         DisplayRegistry displayRegistry = DisplayRegistry.getInstance();
         DisplayCache displayCache = ((DisplayRegistryImpl)displayRegistry).cache();
         Map<DisplayCategory<?>, Set<Display>> result = Maps.newHashMap();
         forCategories(processingVisibilityHandlers, filteringCategories, displayRegistry, result, (configuration, categoryId, displays, set) -> {
            if (categories.contains(categoryId)) {
               for (Display display : displays) {
                  if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(configuration.getCategory(), display)) {
                     set.add(display);
                  }
               }

               if (!set.isEmpty()) {
                  getOrPutEmptyLinkedSet(result, configuration.getCategory()).addAll(set);
               }
            } else {
               for (Display displayx : displays) {
                  if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(configuration.getCategory(), displayx)) {
                     if (!recipesForStacks.isEmpty() && isRecipesFor(displayCache, recipesForStacks, displayx)) {
                        set.add(displayx);
                     } else if (!usagesForStacks.isEmpty() && isUsagesFor(displayCache, usagesForStacks, displayx)) {
                        set.add(displayx);
                     }
                  }
               }
            }
         });
         int generatorsCount = 0;

         for (Entry<CategoryIdentifier<?>, List<DynamicDisplayGenerator<?>>> entry : displayRegistry.getCategoryDisplayGenerators().entrySet()) {
            CategoryIdentifier<?> categoryId = entry.getKey();
            DisplayCategory<?> category = CategoryRegistry.getInstance().get(categoryId).getCategory();
            if ((!processingVisibilityHandlers || !CategoryRegistry.getInstance().isCategoryInvisible(category))
               && (filteringCategories.isEmpty() || filteringCategories.contains(categoryId))) {
               Set<Display> set = new LinkedHashSet<>();
               generatorsCount += entry.getValue().size();

               for (DynamicDisplayGenerator<Display> generator : entry.getValue()) {
                  generateLiveDisplays(displayRegistry, wrapForError(generator), builder, set::add);
               }

               if (!set.isEmpty()) {
                  getOrPutEmptyLinkedSet(result, category).addAll(set);
               }
            }
         }

         Consumer<Display> displayConsumer = display -> {
            CategoryIdentifier<?> categoryIdentifier = display.getCategoryIdentifier();
            if (filteringCategories.isEmpty() || filteringCategories.contains(categoryIdentifier)) {
               getOrPutEmptyLinkedSet(result, CategoryRegistry.getInstance().get(categoryIdentifier).getCategory()).add(display);
            }
         };

         for (DynamicDisplayGenerator<Display> generator : displayRegistry.getGlobalDisplayGenerators()) {
            generatorsCount++;
            generateLiveDisplays(displayRegistry, wrapForError(generator), builder, displayConsumer);
         }

         if (CollectionUtils.allMatch(result.values(), Set::isEmpty) && (!recipesForStacksWildcard.isEmpty() || !usagesForStacksWildcard.isEmpty())) {
            forCategories(processingVisibilityHandlers, filteringCategories, displayRegistry, result, (configuration, categoryId, displays, set) -> {
               if (!categories.contains(categoryId)) {
                  for (Display display : displays) {
                     if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(configuration.getCategory(), display)) {
                        if (!recipesForStacksWildcard.isEmpty() && isRecipesFor(displayCache, recipesForStacksWildcard, display)) {
                           set.add(display);
                        } else if (!usagesForStacksWildcard.isEmpty() && isUsagesFor(displayCache, usagesForStacksWildcard, display)) {
                           set.add(display);
                        }
                     }
                  }
               }
            });
         }

         forCategories(processingVisibilityHandlers, filteringCategories, displayRegistry, result, (configuration, categoryId, displays, set) -> {
            if (!categories.contains(categoryId)) {
               for (EntryStack<?> usagesFor : Iterables.concat(usagesForStacks, usagesForStacksWildcard)) {
                  if (isStackWorkStationOfCategory(configuration, usagesFor)) {
                     categories.add(categoryId);
                     if (processingVisibilityHandlers) {
                        set.addAll(CollectionUtils.filterToSet(displays, display -> displayRegistry.isDisplayVisible(configuration.getCategory(), display)));
                     } else {
                        set.addAll(displays);
                     }
                     break;
                  }
               }
            }
         });
         Stopwatch mergingStopwatch = Stopwatch.createStarted();
         Stopwatch sortingStopwatch = Stopwatch.createUnstarted();
         Map<DisplayCategory<?>, List<DisplaySpec>> merged = new LinkedHashMap<>();

         for (Entry<DisplayCategory<?>, Set<Display>> entryx : result.entrySet()) {
            merged.put(entryx.getKey(), new ArrayList<>(entryx.getValue()));
         }

         if (builder.isMergingDisplays() && ConfigObject.getInstance().doMergeDisplayUnderOne()) {
            mergeAndOptimize(result, merged);
         }

         mergingStopwatch.stop();
         sortingStopwatch.start();
         Map<DisplayCategory<?>, List<DisplaySpec>> sorted = sortDisplays(merged);
         sortingStopwatch.stop();
         String message = String.format(
            "Built Recipe View in %s for %d categories, %d recipes for, %d usages for and %d live recipe generators.",
            stopwatch.stop(),
            categories.size(),
            recipesForStacks.size(),
            usagesForStacks.size(),
            generatorsCount
         );
         if (ConfigObject.getInstance().doDebugSearchTimeRequired()) {
            InternalLogger.getInstance().info(message);
         } else {
            InternalLogger.getInstance().trace(message);
         }

         return sorted;
      }
   }

   private static Map<DisplayCategory<?>, List<DisplaySpec>> sortDisplays(Map<DisplayCategory<?>, List<DisplaySpec>> unsorted) {
      Object2IntMap<CategoryIdentifier<?>> categoryOrder = new Object2IntOpenHashMap();
      categoryOrder.defaultReturnValue(Integer.MAX_VALUE);
      int i = 100000;

      for (CategoryConfiguration<?> configuration : CategoryRegistry.getInstance()) {
         categoryOrder.put(configuration.getCategoryIdentifier(), i++);
      }

      i = 0;

      for (CategoryIdentifier<?> identifier : ConfigObject.getInstance().getCategoryOrdering()) {
         categoryOrder.put(identifier, i++);
      }

      Map<DisplayCategory<?>, List<DisplaySpec>> result = new TreeMap<>(
         Comparator.comparingInt(category -> categoryOrder.getInt(category.getCategoryIdentifier()))
      );
      result.putAll(unsorted);
      return result;
   }

   private static void forCategories(
      boolean processingVisibilityHandlers,
      Set<CategoryIdentifier<?>> filteringCategories,
      DisplayRegistry displayRegistry,
      Map<DisplayCategory<?>, Set<Display>> result,
      ViewsImpl.QuadConsumer<CategoryConfiguration<?>, CategoryIdentifier<?>, List<Display>, Set<Display>> displayConsumer
   ) {
      for (CategoryConfiguration<?> configuration : CategoryRegistry.getInstance()) {
         if (!processingVisibilityHandlers || !CategoryRegistry.getInstance().isCategoryInvisible(configuration.getCategory())) {
            CategoryIdentifier<?> categoryId = configuration.getCategoryIdentifier();
            if (filteringCategories.isEmpty() || filteringCategories.contains(categoryId)) {
               List<Display> allRecipesFromCategory = displayRegistry.get(categoryId);
               Set<Display> set = new LinkedHashSet<>();
               displayConsumer.accept(configuration, categoryId, allRecipesFromCategory, set);
               if (!set.isEmpty()) {
                  getOrPutEmptyLinkedSet(result, configuration.getCategory()).addAll(set);
               }
            }
         }
      }
   }

   public static <A, B> Set<B> getOrPutEmptyLinkedSet(Map<A, Set<B>> map, A key) {
      Set<B> b = map.get(key);
      if (b != null) {
         return b;
      } else {
         map.put(key, new ReferenceLinkedOpenHashSet());
         return map.get(key);
      }
   }

   public static boolean isRecipesFor(@Nullable DisplayCache displayCache, List<EntryStack<?>> stacks, Display display) {
      if (displayCache != null && displayCache.isCached(display)) {
         Iterator var3 = stacks.iterator();
         if (var3.hasNext()) {
            EntryStack<?> recipesFor = (EntryStack<?>)var3.next();
            return displayCache.getDisplaysByOutput(recipesFor).contains(display);
         }
      }

      return checkUsages(stacks, display, display.getOutputEntries());
   }

   public static boolean isUsagesFor(@Nullable DisplayCache displayCache, List<EntryStack<?>> stacks, Display display) {
      if (displayCache != null && displayCache.isCached(display)) {
         Iterator var3 = stacks.iterator();
         if (var3.hasNext()) {
            EntryStack<?> recipesFor = (EntryStack<?>)var3.next();
            return displayCache.getDisplaysByInput(recipesFor).contains(display);
         }
      }

      return checkUsages(stacks, display, display.getInputEntries());
   }

   private static boolean checkUsages(List<EntryStack<?>> stacks, Display display, List<EntryIngredient> entries) {
      for (EntryIngredient results : entries) {
         for (EntryStack<?> otherEntry : results) {
            for (EntryStack<?> recipesFor : stacks) {
               if (EntryStacks.equalsFuzzy(otherEntry, recipesFor)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static Iterable<Display> sortAutoCrafting(Iterable<Display> displays) {
      Set<Display> successfulDisplays = new LinkedHashSet<>();
      Set<Display> applicableDisplays = new LinkedHashSet<>();

      for (Display display : displays) {
         AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, display, null);
         if (result.successful) {
            successfulDisplays.add(display);
         } else if (result.hasApplicable) {
            applicableDisplays.add(display);
         }
      }

      return Iterables.concat(
         successfulDisplays,
         applicableDisplays,
         Iterables.filter(displays, displayx -> !successfulDisplays.contains(displayx) && !applicableDisplays.contains(displayx))
      );
   }

   private static <T extends Display> void generateLiveDisplays(
      DisplayRegistry displayRegistry, DynamicDisplayGenerator<T> generator, ViewSearchBuilder builder, Consumer<T> displayConsumer
   ) {
      boolean processingVisibilityHandlers = builder.isProcessingVisibilityHandlers();

      for (EntryStack<?> stack : builder.getRecipesFor()) {
         Optional<List<T>> recipeForDisplays = generator.getRecipeFor(stack);
         if (recipeForDisplays.isPresent()) {
            for (T display : recipeForDisplays.get()) {
               if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(display)) {
                  displayConsumer.accept(display);
               }
            }
         }
      }

      for (EntryStack<?> stackx : builder.getUsagesFor()) {
         Optional<List<T>> usageForDisplays = generator.getUsageFor(stackx);
         if (usageForDisplays.isPresent()) {
            for (T displayx : usageForDisplays.get()) {
               if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(displayx)) {
                  displayConsumer.accept(displayx);
               }
            }
         }
      }

      Optional<List<T>> displaysGenerated = generator.generate(builder);
      if (displaysGenerated.isPresent()) {
         for (T displayxx : displaysGenerated.get()) {
            if (!processingVisibilityHandlers || displayRegistry.isDisplayVisible(displayxx)) {
               displayConsumer.accept(displayxx);
            }
         }
      }
   }

   private static <T extends Display> DynamicDisplayGenerator<T> wrapForError(final DynamicDisplayGenerator<T> generator) {
      return new DynamicDisplayGenerator<T>() {
         public Optional<List<T>> getRecipeFor(EntryStack<?> entry) {
            try {
               return generator.getRecipeFor(entry);
            } catch (Throwable var4) {
               CrashReport report = CrashReportUtils.essential(var4, "Error while generating recipes for an entry stack");
               CrashReportUtils.renderer(report, entry);
               InternalLogger.getInstance().throwException(new ReportedException(report));
               return Optional.empty();
            }
         }

         public Optional<List<T>> getUsageFor(EntryStack<?> entry) {
            try {
               return generator.getUsageFor(entry);
            } catch (Throwable var4) {
               CrashReport report = CrashReportUtils.essential(var4, "Error while generating usages for an entry stack");
               CrashReportUtils.renderer(report, entry);
               InternalLogger.getInstance().throwException(new ReportedException(report));
               return Optional.empty();
            }
         }

         public Optional<List<T>> generate(ViewSearchBuilder builder) {
            try {
               return generator.generate(builder);
            } catch (Throwable var4) {
               CrashReport report = CrashReportUtils.essential(var4, "Error while generating recipes for a search");
               InternalLogger.getInstance().throwException(new ReportedException(report));
               return Optional.empty();
            }
         }
      };
   }

   public Predicate<HashedEntryStackWrapper> getCraftableEntriesPredicate() {
      return (Predicate<HashedEntryStackWrapper>)(PluginManager.areAnyReloading() ? Predicates.alwaysTrue() : new CraftableFilterCalculator());
   }

   private static <T> boolean isStackWorkStationOfCategory(CategoryConfiguration<?> category, EntryStack<T> stack) {
      for (EntryIngredient ingredient : category.getWorkstations()) {
         if (EntryIngredients.testFuzzy(ingredient, stack)) {
            return true;
         }
      }

      return false;
   }

   public void startReload() {
   }

   private static void mergeAndOptimize(Map<DisplayCategory<?>, Set<Display>> displays, Map<DisplayCategory<?>, List<DisplaySpec>> resultSpec) {
      for (Entry<DisplayCategory<?>, Set<Display>> entry : displays.entrySet()) {
         DisplayMerger<Display> merger = entry.getKey().getDisplayMerger();
         if (merger != null) {
            Map<ViewsImpl.WrappedDisplaySpec, ViewsImpl.WrappedDisplaySpec> wrappedSet = new LinkedHashMap<>();
            List<ViewsImpl.WrappedDisplaySpec> specs = new ArrayList<>();

            for (Display display : sortAutoCrafting(entry.getValue())) {
               ViewsImpl.WrappedDisplaySpec wrapped = new ViewsImpl.WrappedDisplaySpec(merger, display);
               if (wrappedSet.containsKey(wrapped)) {
                  wrappedSet.get(wrapped).add(display);
               } else {
                  wrappedSet.put(wrapped, wrapped);
                  specs.add(wrapped);
               }
            }

            resultSpec.put(entry.getKey(), specs);
         }
      }
   }

   @FunctionalInterface
   private interface QuadConsumer<P1, P2, P3, P4> {
      void accept(P1 var1, P2 var2, P3 var3, P4 var4);
   }

   private static class WrappedDisplaySpec implements DisplaySpec {
      private final DisplayMerger<Display> merger;
      private final Display display;
      private List<Identifier> ids = null;
      private final int hash;

      public WrappedDisplaySpec(DisplayMerger<Display> merger, Display display) {
         this.merger = merger;
         this.display = display;
         this.hash = merger.hashOf(display);
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof ViewsImpl.WrappedDisplaySpec wrapped)
               ? false
               : this.hash == wrapped.hash && this.merger.canMerge(this.display, wrapped.display);
         }
      }

      @Override
      public int hashCode() {
         return this.hash;
      }

      public Display provideInternalDisplay() {
         return this.display;
      }

      public Collection<Identifier> provideInternalDisplayIds() {
         if (this.ids == null) {
            this.ids = new ArrayList<>();
            Optional<Identifier> location = this.display.getDisplayLocation();
            if (location.isPresent()) {
               this.ids.add(location.get());
            }
         }

         return this.ids;
      }

      public void add(Display display) {
         Optional<Identifier> location = display.getDisplayLocation();
         if (location.isPresent()) {
            this.provideInternalDisplayIds().add(location.get());
         }
      }
   }
}
