package me.shedaniel.rei.plugin.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler.IntRange;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.plugin.autocrafting.InventoryCraftingTransferHandler;
import me.shedaniel.rei.plugin.autocrafting.recipebook.DefaultRecipeBookHandler;
import me.shedaniel.rei.plugin.client.categories.DefaultBrewingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultCampfireCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultCompostingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultFuelCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultInformationCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultOxidationScrapingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultOxidizingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultPathingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultSmithingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultStoneCuttingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultStrippingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultTillingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultWaxScrapingCategory;
import me.shedaniel.rei.plugin.client.categories.DefaultWaxingCategory;
import me.shedaniel.rei.plugin.client.categories.anvil.DefaultAnvilCategory;
import me.shedaniel.rei.plugin.client.categories.beacon.DefaultBeaconBaseCategory;
import me.shedaniel.rei.plugin.client.categories.beacon.DefaultBeaconPaymentCategory;
import me.shedaniel.rei.plugin.client.categories.cooking.DefaultCookingCategory;
import me.shedaniel.rei.plugin.client.categories.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.client.categories.tag.DefaultTagCategory;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCookingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedSmithingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedStoneCuttingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCookingDisplay.Blasting;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCookingDisplay.Smelting;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCookingDisplay.Smoking;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCraftingDisplay.Shaped;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCraftingDisplay.Shapeless;
import me.shedaniel.rei.plugin.client.exclusionzones.DefaultPotionEffectExclusionZones;
import me.shedaniel.rei.plugin.client.exclusionzones.DefaultRecipeBookExclusionZones;
import me.shedaniel.rei.plugin.client.favorites.GameModeFavoriteEntry;
import me.shedaniel.rei.plugin.client.favorites.TimeFavoriteEntry;
import me.shedaniel.rei.plugin.client.favorites.WeatherFavoriteEntry;
import me.shedaniel.rei.plugin.client.favorites.GameModeFavoriteEntry.Type;
import me.shedaniel.rei.plugin.client.favorites.TimeFavoriteEntry.Time;
import me.shedaniel.rei.plugin.client.favorites.WeatherFavoriteEntry.Weather;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultCompostingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultOxidationScrapingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultOxidizingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultPathingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultStrippingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultWaxScrapingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultWaxingDisplay;
import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconBaseDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconPaymentDisplay;
import me.shedaniel.rei.plugin.common.displays.brewing.BrewingRecipe;
import me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.common.displays.tag.DefaultTagDisplay;
import me.shedaniel.rei.plugin.common.displays.tag.TagNodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.BlockTransformer.BlockTransformData;
import net.minecraft.core.component.BlockTransformer.TransformParticle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.levelgen.blockpredicates.AllOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingBlockTagPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingBlocksPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider.Rule;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;

@OnlyIn(Dist.CLIENT)
@Internal
public class DefaultClientPlugin implements REIClientPlugin, BuiltinClientPlugin {
   private static Boolean fluidEntriesAvailable;

   public DefaultClientPlugin() {
      ClientInternals.attachInstance((Supplier<>)() -> this, "builtinClientPlugin");
   }

   public void registerBrewingRecipe(EntryIngredient input, EntryIngredient ingredient, EntryIngredient output) {
      DisplayRegistry.getInstance().add(new BrewingRecipe(input, ingredient, output));
   }

   public void registerInformation(EntryIngredient ingredient, Component name, UnaryOperator<List<Component>> textBuilder) {
      DisplayRegistry.getInstance().add(DefaultInformationDisplay.createFromEntries(ingredient, name).lines(textBuilder.apply(Lists.newArrayList())));
   }

   public void registerEntries(EntryRegistry registry) {
      Multimap<Item, EntryStack<ItemStack>> items = Multimaps.newListMultimap(new Reference2ObjectOpenHashMap(), ArrayList::new);

      for (Entry<CreativeModeTab, Collection<ItemStack>> entry : collectTabs().entrySet()) {
         try {
            for (ItemStack stack : entry.getValue()) {
               try {
                  items.put(stack.getItem(), EntryStacks.of(stack));
               } catch (Exception var9) {
               }
            }
         } catch (Exception var10) {
            var10.printStackTrace();
         }
      }

      for (Item item : BuiltInRegistries.ITEM) {
         Collection<EntryStack<ItemStack>> stacks = items.get(item);
         if (stacks.isEmpty()) {
            try {
               registry.addEntry(EntryStacks.of(item.getDefaultInstance()));
            } catch (Exception var8) {
               registry.addEntry(EntryStacks.of(item));
            }
         } else {
            registry.addEntries(stacks);
         }
      }

      if (canUseFluidEntries()) {
         for (Fluid fluid : BuiltInRegistries.FLUID) {
            FluidState state = fluid.defaultFluidState();
            if (!state.isEmpty() && state.isSource()) {
               registry.addEntry(EntryStacks.of(fluid));
            }
         }
      }
   }

   private static Map<CreativeModeTab, Collection<ItemStack>> collectTabs() {
      try {
         return (Map<CreativeModeTab, Collection<ItemStack>>)Class.forName(
               Platform.isNeoForge()
                  ? "me.shedaniel.rei.impl.client.forge.CreativeModeTabCollectorImpl"
                  : "me.shedaniel.rei.impl.client.fabric.CreativeModeTabCollectorImpl"
            )
            .getDeclaredMethod("collectTabs")
            .invoke(null);
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException var1) {
         throw new RuntimeException(var1);
      }
   }

   public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
      registry.group(
         Identifier.fromNamespaceAndPath("roughlyenoughitems", "enchanted_book"),
         Component.translatable("item.minecraft.enchanted_book"),
         stack -> stack.getType() == VanillaEntryTypes.ITEM && ((ItemStack)stack.castValue()).is(Items.ENCHANTED_BOOK)
      );
      registry.group(
         Identifier.fromNamespaceAndPath("roughlyenoughitems", "potion"),
         Component.translatable("item.minecraft.potion"),
         stack -> stack.getType() == VanillaEntryTypes.ITEM && ((ItemStack)stack.castValue()).is(Items.POTION)
      );
      registry.group(
         Identifier.fromNamespaceAndPath("roughlyenoughitems", "splash_potion"),
         Component.translatable("item.minecraft.splash_potion"),
         stack -> stack.getType() == VanillaEntryTypes.ITEM && ((ItemStack)stack.castValue()).is(Items.SPLASH_POTION)
      );
      registry.group(
         Identifier.fromNamespaceAndPath("roughlyenoughitems", "lingering_potion"),
         Component.translatable("item.minecraft.lingering_potion"),
         stack -> stack.getType() == VanillaEntryTypes.ITEM && ((ItemStack)stack.castValue()).is(Items.LINGERING_POTION)
      );
      registry.group(
         Identifier.fromNamespaceAndPath("roughlyenoughitems", "spawn_egg"),
         Component.translatable("text.rei.spawn_egg"),
         stack -> stack.getType() == VanillaEntryTypes.ITEM && ((ItemStack)stack.castValue()).getItem() instanceof SpawnEggItem
      );
      registry.group(
         Identifier.fromNamespaceAndPath("roughlyenoughitems", "tipped_arrow"),
         Component.translatable("item.minecraft.tipped_arrow"),
         stack -> stack.getType() == VanillaEntryTypes.ITEM && ((ItemStack)stack.castValue()).is(Items.TIPPED_ARROW)
      );
      registry.group(
         Identifier.fromNamespaceAndPath("roughlyenoughitems", "music_disc"),
         Component.translatable("text.rei.music_disc"),
         stack -> stack.getType() == VanillaEntryTypes.ITEM && ((ItemStack)stack.castValue()).has(DataComponents.JUKEBOX_PLAYABLE)
      );
   }

   public void registerCategories(CategoryRegistry registry) {
      registry.add(
         new DisplayCategory[]{
            new DefaultCraftingCategory(),
            new DefaultCookingCategory(SMELTING, EntryStacks.of(Items.FURNACE), "category.rei.smelting", 200.0),
            new DefaultCookingCategory(SMOKING, EntryStacks.of(Items.SMOKER), "category.rei.smoking", 100.0),
            new DefaultCookingCategory(BLASTING, EntryStacks.of(Items.BLAST_FURNACE), "category.rei.blasting", 100.0),
            new DefaultCampfireCategory(),
            new DefaultStoneCuttingCategory(),
            new DefaultFuelCategory(),
            new DefaultBrewingCategory(),
            new DefaultCompostingCategory(),
            new DefaultStrippingCategory(),
            new DefaultSmithingCategory(),
            new DefaultAnvilCategory(),
            new DefaultBeaconBaseCategory(),
            new DefaultBeaconPaymentCategory(),
            new DefaultTillingCategory(),
            new DefaultPathingCategory(),
            new DefaultWaxingCategory(),
            new DefaultWaxScrapingCategory(),
            new DefaultOxidizingCategory(),
            new DefaultOxidationScrapingCategory()
         }
      );
      registry.addWorkstations(CRAFTING, new EntryStack[]{EntryStacks.of(Items.CRAFTING_TABLE)});
      registry.addWorkstations(SMELTING, new EntryStack[]{EntryStacks.of(Items.FURNACE)});
      registry.addWorkstations(SMOKING, new EntryStack[]{EntryStacks.of(Items.SMOKER)});
      registry.addWorkstations(BLASTING, new EntryStack[]{EntryStacks.of(Items.BLAST_FURNACE)});
      registry.addWorkstations(CAMPFIRE, new EntryStack[]{EntryStacks.of(Items.CAMPFIRE), EntryStacks.of(Items.SOUL_CAMPFIRE)});
      registry.addWorkstations(FUEL, new EntryStack[]{EntryStacks.of(Items.FURNACE), EntryStacks.of(Items.SMOKER), EntryStacks.of(Items.BLAST_FURNACE)});
      registry.addWorkstations(BREWING, new EntryStack[]{EntryStacks.of(Items.BREWING_STAND)});
      registry.addWorkstations(ANVIL, new EntryStack[]{EntryStacks.of(Items.ANVIL)});
      registry.addWorkstations(STONE_CUTTING, new EntryStack[]{EntryStacks.of(Items.STONECUTTER)});
      registry.addWorkstations(COMPOSTING, new EntryStack[]{EntryStacks.of(Items.COMPOSTER)});
      registry.addWorkstations(SMITHING, new EntryStack[]{EntryStacks.of(Items.SMITHING_TABLE)});
      registry.addWorkstations(BEACON_BASE, new EntryStack[]{EntryStacks.of(Items.BEACON)});
      registry.addWorkstations(BEACON_PAYMENT, new EntryStack[]{EntryStacks.of(Items.BEACON)});
      registry.addWorkstations(WAXING, new EntryStack[]{EntryStacks.of(Items.HONEYCOMB)});
      registry.configure(INFO, config -> config.setQuickCraftingEnabledByDefault(false));
      registry.configure(TAG, config -> config.setQuickCraftingEnabledByDefault(false));
      registry.registerVisibilityPredicate(
         category -> category instanceof DefaultTagCategory
               && Minecraft.getInstance().getSingleplayerServer() == null
               && !NetworkManager.canServerReceive(TagNodes.REQUEST_TAGS_C2S_PACKET_ID)
            ? EventResult.interruptFalse()
            : EventResult.pass()
      );
      Set<Item> axes = Sets.newHashSet();
      Set<Item> hoes = Sets.newHashSet();
      Set<Item> shovels = Sets.newHashSet();
      EntryRegistry.getInstance()
         .getEntryStacks()
         .filter(stackx -> stackx.getValueType() == ItemStack.class)
         .map(stackx -> ((ItemStack)stackx.getValue()).getItem())
         .forEach(item -> {
            Holder<BlockTransformer> transformer = (Holder<BlockTransformer>)item.components().get(DataComponents.BLOCK_TRANSFORMER);
            if (transformer != null && transformer.is(BlockTransformers.AXE) && axes.add(item)) {
               registry.addWorkstations(STRIPPING, new EntryStack[]{EntryStacks.of(item)});
               registry.addWorkstations(WAX_SCRAPING, new EntryStack[]{EntryStacks.of(item)});
               registry.addWorkstations(OXIDATION_SCRAPING, new EntryStack[]{EntryStacks.of(item)});
            }

            if (transformer != null && transformer.is(BlockTransformers.HOE) && hoes.add(item)) {
               registry.addWorkstations(TILLING, new EntryStack[]{EntryStacks.of(item)});
            }

            if (transformer != null && transformer.is(BlockTransformers.SHOVEL) && shovels.add(item)) {
               registry.addWorkstations(PATHING, new EntryStack[]{EntryStacks.of(item)});
            }
         });

      for (EntryStack<?> stack : getTag(Identifier.fromNamespaceAndPath("c", "axes"))) {
         if (axes.add(((ItemStack)stack.castValue()).getItem())) {
            registry.addWorkstations(STRIPPING, new EntryStack[]{stack});
            registry.addWorkstations(WAX_SCRAPING, new EntryStack[]{stack});
            registry.addWorkstations(OXIDATION_SCRAPING, new EntryStack[]{stack});
         }
      }

      for (EntryStack<?> stackx : getTag(Identifier.fromNamespaceAndPath("c", "hoes"))) {
         if (hoes.add(((ItemStack)stackx.castValue()).getItem())) {
            registry.addWorkstations(TILLING, new EntryStack[]{stackx});
         }
      }

      for (EntryStack<?> stackxx : getTag(Identifier.fromNamespaceAndPath("c", "shovels"))) {
         if (shovels.add(((ItemStack)stackxx.castValue()).getItem())) {
            registry.addWorkstations(PATHING, new EntryStack[]{stackxx});
         }
      }
   }

   private static EntryIngredient getTag(Identifier tagId) {
      return EntryIngredients.ofItemTag(TagKey.create(Registries.ITEM, tagId));
   }

   private static boolean hasCraftingStation(FurnaceRecipeDisplay display, Item station) {
      return EntryIngredients.testFuzzy(EntryIngredients.ofSlotDisplay(display.craftingStation()), EntryStacks.of(station));
   }

   private static ClientsidedCookingDisplay createCookingFallbackDisplay(FurnaceRecipeDisplay display, Optional<RecipeDisplayId> id) {
      if (hasCraftingStation(display, Items.SMOKER)) {
         return new Smoking(display, id);
      } else {
         return (ClientsidedCookingDisplay)(hasCraftingStation(display, Items.BLAST_FURNACE) ? new Blasting(display, id) : new Smelting(display, id));
      }
   }

   private static boolean canUseFluidEntries() {
      if (fluidEntriesAvailable != null) {
         return fluidEntriesAvailable;
      } else {
         try {
            EntryStacks.of((Fluid)BuiltInRegistries.FLUID.getValue(Identifier.withDefaultNamespace("water")));
            fluidEntriesAvailable = true;
         } catch (Throwable var1) {
            fluidEntriesAvailable = false;
            InternalLogger.getInstance().warn("Disabling REI fluid entries because Architectury FluidStack failed to initialize.", var1);
         }

         return fluidEntriesAvailable;
      }
   }

   public void registerDisplays(DisplayRegistry registry) {
      CategoryRegistry.getInstance().add(new DisplayCategory[]{new DefaultInformationCategory(), new DefaultTagCategory()});
      registry.beginRecipeFiller(ShapedCraftingRecipeDisplay.class).fill(Shaped::new);
      registry.beginRecipeFiller(ShapelessCraftingRecipeDisplay.class).fill(Shapeless::new);
      registry.beginRecipeFiller(FurnaceRecipeDisplay.class).filterType(FurnaceRecipeDisplay.TYPE).fill(DefaultClientPlugin::createCookingFallbackDisplay);
      registry.beginRecipeFiller(StonecutterRecipeDisplay.class).filterType(StonecutterRecipeDisplay.TYPE).fill(ClientsidedStoneCuttingDisplay::new);
      registry.beginRecipeFiller(SmithingRecipeDisplay.class).filterType(SmithingRecipeDisplay.TYPE).fill(ClientsidedSmithingDisplay::new);
      registry.beginFiller(AnvilRecipe.class).fill(DefaultAnvilDisplay::new);
      registry.beginFiller(BrewingRecipe.class).fill(DefaultBrewingDisplay::new);
      registry.beginFiller(TagKey.class).fill(tagKey -> {
         if (tagKey.isFor(Registries.ITEM)) {
            return DefaultTagDisplay.ofItems(tagKey);
         } else if (tagKey.isFor(Registries.BLOCK)) {
            return DefaultTagDisplay.ofItems(tagKey);
         } else if (tagKey.isFor(Registries.FLUID)) {
            return !canUseFluidEntries() ? null : DefaultTagDisplay.ofFluids(tagKey);
         } else {
            return null;
         }
      });
      Iterator<List<EntryIngredient>> iterator = Iterators.partition(
         BuiltInRegistries.ITEM
            .stream()
            .filter(itemx -> DefaultCompostingCategory.getCompostChance(itemx) > 0.0F)
            .sorted(Comparator.comparing(DefaultCompostingCategory::getCompostChance))
            .map(EntryIngredients::of)
            .iterator(),
         35
      );

      while (iterator.hasNext()) {
         List<EntryIngredient> entries = iterator.next();
         registry.add(new DefaultCompostingDisplay(entries, Collections.singletonList(EntryIngredients.of(new ItemStack(Items.BONE_MEAL)))));
      }

      getTransformedBlocks(BlockTransformers.AXE)
         .entrySet()
         .stream()
         .sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey((Block)b.getKey())))
         .forEach(set -> registry.add(new DefaultStrippingDisplay(EntryStacks.of((ItemLike)set.getKey()), EntryStacks.of((ItemLike)set.getValue()))));
      getTransformedBlocks(BlockTransformers.SHOVEL)
         .entrySet()
         .stream()
         .sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey((Block)b.getKey())))
         .forEach(set -> registry.add(new DefaultPathingDisplay(EntryStacks.of((ItemLike)set.getKey()), EntryStacks.of((ItemLike)set.getValue()))));
      registry.add(new DefaultBeaconBaseDisplay(Collections.singletonList(EntryIngredients.ofItemTag(BlockTags.BEACON_BASE_BLOCKS)), Collections.emptyList()));
      registry.add(
         new DefaultBeaconPaymentDisplay(Collections.singletonList(EntryIngredients.ofItemTag(ItemTags.BEACON_PAYMENT_ITEMS)), Collections.emptyList())
      );
      ((BiMap)HoneycombItem.WAXABLES.get())
         .entrySet()
         .stream()
         .sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey((Block)b.getKey())))
         .forEach(set -> registry.add(new DefaultWaxingDisplay(EntryStacks.of((ItemLike)set.getKey()), EntryStacks.of((ItemLike)set.getValue()))));
      ((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get())
         .entrySet()
         .stream()
         .sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey((Block)b.getKey())))
         .forEach(set -> registry.add(new DefaultWaxScrapingDisplay(EntryStacks.of((ItemLike)set.getKey()), EntryStacks.of((ItemLike)set.getValue()))));
      ((BiMap)WeatheringCopper.NEXT_BY_BLOCK.get())
         .entrySet()
         .stream()
         .sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey((Block)b.getKey())))
         .forEach(set -> registry.add(new DefaultOxidizingDisplay(EntryStacks.of((ItemLike)set.getKey()), EntryStacks.of((ItemLike)set.getValue()))));
      ((BiMap)WeatheringCopper.PREVIOUS_BY_BLOCK.get())
         .entrySet()
         .stream()
         .sorted(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey((Block)b.getKey())))
         .forEach(set -> registry.add(new DefaultOxidationScrapingDisplay(EntryStacks.of((ItemLike)set.getKey()), EntryStacks.of((ItemLike)set.getValue()))));

      for (Item item : BuiltInRegistries.ITEM) {
         ItemStack stack = item.getDefaultInstance();
         if (stack.isDamageableItem() && item.components().has(DataComponents.REPAIRABLE)) {
            Repairable repairable = (Repairable)item.components().get(DataComponents.REPAIRABLE);
            EntryIngredient repairMaterialBase = EntryIngredients.ofItemsHolderSet(repairable.items());
            if (!repairMaterialBase.isEmpty()) {
               for (int[] i = new int[]{1}; i[0] <= 4; i[0]++) {
                  ItemStack baseStack = item.getDefaultInstance();
                  int toRepair = i[0] == 4 ? baseStack.getMaxDamage() : baseStack.getMaxDamage() / 4 * i[0];
                  baseStack.setDamageValue(toRepair);
                  EntryIngredient repairMaterial = repairMaterialBase.map(s -> {
                     EntryStack<?> newStack = s.copy();
                     ((ItemStack)newStack.castValue()).setCount(i[0]);
                     return newStack;
                  });
                  Optional<Pair<ItemStack, Integer>> output = DefaultAnvilDisplay.calculateOutput(
                     baseStack, (ItemStack)((EntryStack)repairMaterial.get(0)).castValue()
                  );
                  if (!output.isEmpty()) {
                     registry.add(
                        new DefaultAnvilDisplay(
                           List.of(EntryIngredients.of(baseStack), repairMaterial),
                           Collections.singletonList(EntryIngredients.of((ItemStack)output.get().getLeft())),
                           Optional.empty(),
                           OptionalInt.of((Integer)output.get().getRight())
                        )
                     );
                  }
               }
            }
         }
      }

      List<Pair<EnchantmentInstance, ItemStack>> enchantmentBooks = BasicDisplay.registryAccess()
         .lookup(Registries.ENCHANTMENT)
         .stream()
         .<Reference>flatMap(HolderLookup::listElements)
         .flatMap(
            holder -> {
               if (!holder.isBound()) {
                  return Stream.empty();
               } else {
                  Enchantment enchantment = (Enchantment)holder.value();
                  return enchantment.getMaxLevel() - enchantment.getMinLevel() >= 10
                     ? IntStream.of(enchantment.getMinLevel(), enchantment.getMaxLevel()).mapToObj(lvl -> new EnchantmentInstance(holder, lvl))
                     : IntStream.rangeClosed(enchantment.getMinLevel(), enchantment.getMaxLevel()).mapToObj(lvl -> new EnchantmentInstance(holder, lvl));
               }
            }
         )
         .map(instance -> Pair.of(instance, EnchantmentHelper.createBook(instance)))
         .toList();
      EntryRegistry.getInstance()
         .getEntryStacks()
         .forEach(
            stack -> {
               if (stack.getType() == VanillaEntryTypes.ITEM) {
                  ItemStack itemStack = (ItemStack)stack.castValue();
                  if (itemStack.isEnchantable()) {
                     for (Pair<EnchantmentInstance, ItemStack> pair : enchantmentBooks) {
                        if (((Enchantment)((EnchantmentInstance)pair.getKey()).enchantment().value()).canEnchant(itemStack)) {
                           Optional<Pair<ItemStack, Integer>> outputx = DefaultAnvilDisplay.calculateOutput(itemStack, (ItemStack)pair.getValue());
                           if (!outputx.isEmpty()) {
                              registry.add(
                                 new DefaultAnvilDisplay(
                                    List.of(EntryIngredients.of(itemStack), EntryIngredients.of((ItemStack)pair.getValue())),
                                    Collections.singletonList(EntryIngredients.of((ItemStack)outputx.get().getLeft())),
                                    Optional.empty(),
                                    OptionalInt.of((Integer)outputx.get().getRight())
                                 )
                              );
                           }
                        }
                     }
                  }
               }
            }
         );

      for (Registry<?> reg : BuiltInRegistries.REGISTRY) {
         reg.getTags().forEach(tagPair -> tagPair.unwrap().ifLeft(registry::add));
      }
   }

   public void registerExclusionZones(ExclusionZones zones) {
      zones.register(InventoryScreen.class, new DefaultPotionEffectExclusionZones());
      zones.register(CreativeModeInventoryScreen.class, new DefaultPotionEffectExclusionZones());
      zones.register(RecipeUpdateListener.class, new DefaultRecipeBookExclusionZones());
   }

   public void registerScreens(ScreenRegistry registry) {
      registry.registerContainerClickArea(new Rectangle(88, 32, 28, 23), CraftingScreen.class, new CategoryIdentifier[]{CRAFTING});
      registry.registerContainerClickArea(new Rectangle(137, 29, 10, 13), InventoryScreen.class, new CategoryIdentifier[]{CRAFTING});
      registry.registerContainerClickArea(new Rectangle(97, 16, 14, 30), BrewingStandScreen.class, new CategoryIdentifier[]{BREWING});
      registry.registerContainerClickArea(new Rectangle(78, 32, 28, 23), FurnaceScreen.class, new CategoryIdentifier[]{SMELTING});
      registry.registerContainerClickArea(new Rectangle(78, 32, 28, 23), SmokerScreen.class, new CategoryIdentifier[]{SMOKING});
      registry.registerContainerClickArea(new Rectangle(78, 32, 28, 23), BlastFurnaceScreen.class, new CategoryIdentifier[]{BLASTING});
   }

   public void registerTransferHandlers(TransferHandlerRegistry registry) {
      registry.register(SimpleTransferHandler.create(CraftingMenu.class, BuiltinPlugin.CRAFTING, new IntRange(1, 10)));
      registry.register(new InventoryCraftingTransferHandler(SimpleTransferHandler.create(InventoryMenu.class, BuiltinPlugin.CRAFTING, new IntRange(1, 5))));
      registry.register(SimpleTransferHandler.create(FurnaceMenu.class, BuiltinPlugin.SMELTING, new IntRange(0, 1)));
      registry.register(SimpleTransferHandler.create(SmokerMenu.class, BuiltinPlugin.SMOKING, new IntRange(0, 1)));
      registry.register(SimpleTransferHandler.create(BlastFurnaceMenu.class, BuiltinPlugin.BLASTING, new IntRange(0, 1)));
      registry.register(new DefaultRecipeBookHandler());
   }

   public void registerFavorites(me.shedaniel.rei.api.client.favorites.FavoriteEntryType.Registry registry) {
      registry.register(GameModeFavoriteEntry.ID, Type.INSTANCE);
      registry.getOrCrateSection(Component.translatable("favorite.section.gamemode"))
         .add(
            Stream.concat(Arrays.stream(GameType.values()).filter(type -> type.getId() >= 0), Stream.of((GameType)null))
               .map(GameModeFavoriteEntry::new)
               .toArray(FavoriteEntry[]::new)
         );
      registry.register(WeatherFavoriteEntry.ID, me.shedaniel.rei.plugin.client.favorites.WeatherFavoriteEntry.Type.INSTANCE);
      registry.getOrCrateSection(Component.translatable("favorite.section.weather"))
         .add(Stream.concat(Arrays.stream(Weather.values()), Stream.of((Weather)null)).map(WeatherFavoriteEntry::new).toArray(FavoriteEntry[]::new));
      registry.register(TimeFavoriteEntry.ID, me.shedaniel.rei.plugin.client.favorites.TimeFavoriteEntry.Type.INSTANCE);
      registry.getOrCrateSection(Component.translatable("favorite.section.time"))
         .add(Stream.concat(Arrays.stream(Time.values()), Stream.of((Time)null)).map(TimeFavoriteEntry::new).toArray(FavoriteEntry[]::new));
   }

   public double getPriority() {
      return -100.0;
   }

   private static Map<Block, Block> getTransformedBlocks(ResourceKey<BlockTransformer> key) {
      Map<Block, Block> blocks = new LinkedHashMap<>();
      BasicDisplay.registryAccess().lookupOrThrow(Registries.BLOCK_TRANSFORMER).get(key).ifPresent(holder -> {
         for (BlockTransformData data : ((BlockTransformer)holder.value()).transforms()) {
            if (data.particle() == TransformParticle.NONE && data.blockStateProvider().value() instanceof RuleBasedStateProvider provider) {
               for (Rule rule : provider.rules()) {
                  Block result = getResultBlock((BlockStateProvider)rule.then().value());
                  if (result != null) {
                     for (Block block : getMatchedBlocks(rule.ifTrue())) {
                        blocks.put(block, result);
                     }
                  }
               }
            }
         }
      });
      return blocks;
   }

   private static Block getResultBlock(BlockStateProvider provider) {
      return switch (provider) {
         case SimpleStateProvider simple -> simple.state().getBlock();
         case CopyPropertiesProvider copy -> getResultBlock((BlockStateProvider)copy.source().value());
         default -> null;
      };
   }

   private static List<Block> getMatchedBlocks(BlockPredicate predicate) {
      List<Block> blocks = new ArrayList<>();
      Objects.requireNonNull(predicate);
      switch (predicate) {
         case MatchingBlocksPredicate matching when matching.offset.equals(Vec3i.ZERO):
            for (Holder<Block> block : matching.blocks) {
               blocks.add((Block)block.value());
            }
            break;
         case MatchingBlockTagPredicate matchingx when matchingx.offset.equals(Vec3i.ZERO):
            BasicDisplay.registryAccess().lookupOrThrow(Registries.BLOCK).get(matchingx.tag).ifPresent(set -> {
               for (Holder<Block> block : set) {
                  blocks.add((Block)block.value());
               }
            });
            break;
         case AllOfPredicate allOf:
            for (BlockPredicate child : allOf.predicates) {
               blocks.addAll(getMatchedBlocks(child));
               if (!blocks.isEmpty()) {
                  break;
               }
            }
            break;
         default:
      }

      return blocks;
   }
}
