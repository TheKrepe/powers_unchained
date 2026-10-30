package me.shedaniel.rei.impl.client.registry.category;

import com.google.common.base.MoreObjects;
import dev.architectury.event.EventResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry.CategoryConfiguration;
import me.shedaniel.rei.api.client.registry.category.extension.CategoryExtensionProvider;
import me.shedaniel.rei.api.client.registry.category.visibility.CategoryVisibilityPredicate;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.impl.common.InternalLogger;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class CategoryRegistryImpl implements CategoryRegistry {
   private final Map<CategoryIdentifier<?>, CategoryRegistryImpl.Configuration<?>> categories = new LinkedHashMap<>();
   private final Map<CategoryIdentifier<?>, List<Consumer<CategoryConfiguration<?>>>> listeners = new HashMap<>();
   private final List<CategoryVisibilityPredicate> visibilityPredicates = new ArrayList<>();

   public void acceptPlugin(REIClientPlugin plugin) {
      plugin.registerCategories(this);
   }

   public void startReload() {
      this.categories.clear();
      this.listeners.clear();
      this.visibilityPredicates.clear();
      this.registerVisibilityPredicate(
         category -> ConfigObject.getInstance().getHiddenCategories().contains(category.getCategoryIdentifier())
            ? EventResult.interruptFalse()
            : EventResult.pass()
      );
   }

   public void endReload() {
      InternalLogger.getInstance()
         .debug(
            "Registered %d categories: %s",
            new Object[]{
               this.categories.size(),
               this.categories.values().stream().map(configuration -> configuration.getCategory().getTitle().getString()).collect(Collectors.joining(", "))
            }
         );
   }

   public <T extends Display> void add(DisplayCategory<T> category, Consumer<CategoryConfiguration<T>> configurator) {
      CategoryRegistryImpl.Configuration<T> configuration = new CategoryRegistryImpl.Configuration<>(category);
      this.categories.put(category.getCategoryIdentifier(), configuration);
      configurator.accept(configuration);
      List<Consumer<CategoryConfiguration<?>>> listeners = this.listeners.get(category.getCategoryIdentifier());
      if (listeners != null) {
         this.listeners.remove(category.getCategoryIdentifier());

         for (Consumer<CategoryConfiguration<?>> listener : listeners) {
            listener.accept(configuration);
         }
      }

      InternalLogger.getInstance().debug("Added category [%s]: %s", new Object[]{category.getCategoryIdentifier(), category.getTitle().getString()});
   }

   public <T extends Display> CategoryConfiguration<T> get(CategoryIdentifier<T> category) {
      return Objects.requireNonNull((CategoryRegistryImpl.Configuration)this.categories.get(category), category.toString());
   }

   public <T extends Display> Optional<CategoryConfiguration<T>> tryGet(CategoryIdentifier<T> category) {
      return Optional.ofNullable((CategoryConfiguration<T>)this.categories.get(category));
   }

   public <T extends Display> void configure(CategoryIdentifier<T> category, Consumer<CategoryConfiguration<T>> action) {
      if (this.categories.containsKey(category)) {
         action.accept(this.get(category));
      } else {
         this.listeners.computeIfAbsent(category, location -> new ArrayList<>()).add(action);
      }
   }

   public Iterator<CategoryConfiguration<?>> iterator() {
      return this.categories.values().iterator();
   }

   public int size() {
      return this.categories.size();
   }

   public void registerVisibilityPredicate(CategoryVisibilityPredicate predicate) {
      this.visibilityPredicates.add(predicate);
      this.visibilityPredicates.sort(Comparator.reverseOrder());
      InternalLogger.getInstance().debug("Added category visibility predicate: %s [%.2f priority]", new Object[]{predicate, predicate.getPriority()});
   }

   public boolean isCategoryVisible(DisplayCategory<?> category) {
      for (CategoryVisibilityPredicate predicate : this.visibilityPredicates) {
         try {
            EventResult result = predicate.handleCategory(category);
            if (result.interruptsFurtherEvaluation()) {
               return result.isEmpty() || result.isTrue();
            }
         } catch (Throwable var5) {
            InternalLogger.getInstance().error("Failed to check if the category is visible!", var5);
         }
      }

      return true;
   }

   public List<CategoryVisibilityPredicate> getVisibilityPredicates() {
      return Collections.unmodifiableList(this.visibilityPredicates);
   }

   private static class Configuration<T extends Display> implements CategoryConfiguration<T> {
      private final DisplayCategory<T> category;
      private final List<EntryIngredient> workstations = Collections.synchronizedList(new ArrayList<>());
      private final List<CategoryExtensionProvider<T>> extensionProviders = Collections.synchronizedList(new ArrayList<>());
      private boolean quickCraftingEnabledByDefault = true;
      private ButtonArea plusButtonArea = ButtonArea.defaultArea();

      public Configuration(DisplayCategory<T> category) {
         this.category = category;
      }

      public void addWorkstations(EntryIngredient... stations) {
         this.workstations.addAll(Arrays.asList(stations));
      }

      public void setPlusButtonArea(ButtonArea area) {
         this.plusButtonArea = (ButtonArea)MoreObjects.firstNonNull(area, ButtonArea.defaultArea());
      }

      public boolean isQuickCraftingEnabledByDefault() {
         return this.quickCraftingEnabledByDefault;
      }

      public void setQuickCraftingEnabledByDefault(boolean enabled) {
         this.quickCraftingEnabledByDefault = enabled;
      }

      public Optional<ButtonArea> getPlusButtonArea() {
         return Optional.of(bounds -> Objects.requireNonNullElseGet(this.plusButtonArea.get(bounds), () -> ButtonArea.defaultArea().get(bounds)));
      }

      public List<EntryIngredient> getWorkstations() {
         return Collections.unmodifiableList(this.workstations);
      }

      public DisplayCategory<T> getCategory() {
         return this.category;
      }

      public CategoryIdentifier<?> getCategoryIdentifier() {
         return this.category.getCategoryIdentifier();
      }

      public void registerExtension(CategoryExtensionProvider<T> provider) {
         this.extensionProviders.add(provider);
      }

      public DisplayCategoryView<T> getView(T display) {
         DisplayCategory<T> category = this.getCategory();
         DisplayCategoryView<T> view = this.getCategory();

         for (CategoryExtensionProvider<T> provider : this.extensionProviders) {
            view = Objects.requireNonNull(provider.provide(display, category, view));
         }

         return view;
      }
   }
}
