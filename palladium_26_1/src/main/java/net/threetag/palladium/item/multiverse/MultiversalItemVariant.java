package net.threetag.palladium.item.multiverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.component.SlotDependentIdComponent;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.item.tab.VariantGroup;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public record MultiversalItemVariant(ResourceKey<Universe> universe, Map<Identifier, Appearance> categories) {

    public static final Codec<MultiversalItemVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(PalladiumRegistryKeys.MULTIVERSE).fieldOf("universe").forGetter(MultiversalItemVariant::universe),
            Codec.unboundedMap(Identifier.CODEC, Appearance.CODEC).fieldOf("categories").forGetter(MultiversalItemVariant::categories)
    ).apply(instance, MultiversalItemVariant::new));

    @Nullable
    public Appearance forCategory(Identifier category) {
        return this.categories.get(category);
    }

    @Nullable
    public static MultiversalItemVariant get(HolderLookup.Provider registries, ResourceKey<MultiversalItemVariant> id) {
        return registries.lookup(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT)
                .flatMap(lookup -> lookup.get(id))
                .map(Holder::value)
                .orElse(null);
    }

    @Nullable
    public static Appearance resolve(HolderLookup.Provider registries, ItemStack stack) {
        var variant = stack.get(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get());
        var constant = stack.get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get());
        if (variant == null || constant == null) {
            return null;
        }
        var definition = get(registries, variant.variant());
        return definition == null ? null : definition.forCategory(constant);
    }

    public static void apply(HolderLookup.Provider registries, ItemStack stack, ResourceKey<MultiversalItemVariant> variant) {
        Appearance previous = resolve(registries, stack);
        stack.set(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get(), new ItemMultiversalVariant(variant));
        Appearance current = resolve(registries, stack);
        DataComponentPatch components = current == null ? DataComponentPatch.EMPTY : current.components();

        if (previous != null) {
            revert(stack, previous.components(), components);
        }

        stack.applyComponents(components);
    }

    private static void revert(ItemStack stack, DataComponentPatch applied, DataComponentPatch replacement) {
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : applied.entrySet()) {
            DataComponentType<?> type = entry.getKey();

            if (replacement.getPatch(type) != null || !Objects.equals(stack.get(type), entry.getValue().orElse(null))) {
                continue;
            }

            resetToDefault(stack, type);
        }
    }

    private static <T> void resetToDefault(ItemStack stack, DataComponentType<T> type) {
        T prototype = stack.getItem().components().get(type);

        if (prototype != null) {
            stack.set(type, prototype);
        } else {
            stack.remove(type);
        }
    }

    public static boolean hasRenderLayers(HolderLookup.Provider registries, ItemStack stack, PlayerSlot slot) {
        Appearance appearance = resolve(registries, stack);
        return appearance != null && !appearance.renderLayers().forSlot(slot).isEmpty();
    }

    public static List<ResourceKey<MultiversalItemVariant>> variantsForCategory(HolderLookup.Provider registries, Identifier category) {
        return registries.lookup(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT)
                .map(lookup -> lookup.listElements()
                        .filter(ref -> ref.value().categories().containsKey(category))
                        .map(Holder.Reference::key)
                        .sorted(Comparator.comparing(key -> key.identifier().toString()))
                        .toList())
                .orElseGet(List::of);
    }

    public static String makeDescriptionId(Identifier id) {
        return Util.makeDescriptionId(Palladium.MOD_ID + ".multiversal_item_variant", id);
    }

    public static Component getTitle(Identifier id) {
        return Component.translatable(makeDescriptionId(id));
    }

    public static List<ItemStack> collectGroupedVariantStacks(HolderLookup.Provider registries, VariantGroup group) {
        List<ItemStack> flat = new ArrayList<>();
        for (List<ItemStack> variantStacks : collectVariantGroups(registries, group)) {
            flat.addAll(variantStacks);
        }
        return flat;
    }

    public static List<List<ItemStack>> collectVariantGroups(HolderLookup.Provider registries, VariantGroup group) {
        List<Item> groupItems = group.items().stream()
                .filter(BuiltInRegistries.ITEM::containsKey)
                .map(BuiltInRegistries.ITEM::getValue)
                .toList();

        Map<Identifier, List<Item>> itemsByCategory = new LinkedHashMap<>();
        Map<Identifier, ResourceKey<MultiversalItemVariant>> defaultByCategory = new HashMap<>();
        for (Item item : groupItems) {
            Identifier category = item.components().get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get());
            if (category == null) {
                continue;
            }
            itemsByCategory.computeIfAbsent(category, c -> new ArrayList<>()).add(item);
            ItemMultiversalVariant defaultVariant = item.components().get(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get());
            if (defaultVariant != null) {
                defaultByCategory.putIfAbsent(category, defaultVariant.variant());
            }
        }
        if (itemsByCategory.isEmpty()) {
            return List.of();
        }

        var lookup = registries.lookup(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT);
        if (lookup.isEmpty()) {
            return List.of();
        }

        Set<Identifier> categories = itemsByCategory.keySet();
        List<Holder.Reference<MultiversalItemVariant>> variants = lookup.get().listElements()
                .filter(ref -> group.universe().isEmpty() || ref.value().universe().equals(group.universe().get()))
                .filter(ref -> !Collections.disjoint(ref.value().categories().keySet(), categories))
                .toList();
        if (variants.isEmpty()) {
            return List.of();
        }

        Map<ResourceKey<MultiversalItemVariant>, ResourceKey<MultiversalItemVariant>> groupKeys = new HashMap<>();
        for (Holder.Reference<MultiversalItemVariant> ref : variants) {
            groupKeys.put(ref.key(), groupKey(ref.value(), defaultByCategory, ref.key()));
        }

        List<Holder.Reference<MultiversalItemVariant>> ordered = variants.stream()
                .sorted(Comparator.<Holder.Reference<MultiversalItemVariant>, String>comparing(ref -> groupKeys.get(ref.key()).identifier().toString())
                        .thenComparingInt(ref -> ref.key().equals(groupKeys.get(ref.key())) ? 0 : 1)
                        .thenComparing(ref -> ref.key().identifier().toString()))
                .toList();

        List<List<ItemStack>> result = new ArrayList<>();
        for (Holder.Reference<MultiversalItemVariant> variant : ordered) {
            List<ItemStack> variantStacks = new ArrayList<>();
            for (Identifier category : variant.value().categories().keySet()) {
                for (Item item : itemsByCategory.getOrDefault(category, List.of())) {
                    ItemStack stack = new ItemStack(item);
                    apply(registries, stack, variant.key());
                    variantStacks.add(stack);
                }
            }
            variantStacks.sort(Comparator.comparingInt(stack -> armorSlotOrder(stack.getItem())));
            if (!variantStacks.isEmpty()) {
                result.add(variantStacks);
            }
        }
        return result;
    }

    private static int armorSlotOrder(Item item) {
        Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
        if (equippable == null) return Integer.MAX_VALUE;
        return switch (equippable.slot()) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> Integer.MAX_VALUE;
        };
    }

    private static ResourceKey<MultiversalItemVariant> groupKey(MultiversalItemVariant variant, Map<Identifier, ResourceKey<MultiversalItemVariant>> defaultByCategory, ResourceKey<MultiversalItemVariant> self) {
        for (Identifier category : variant.categories().keySet()) {
            ResourceKey<MultiversalItemVariant> defaultVariant = defaultByCategory.get(category);
            if (defaultVariant != null) {
                return defaultVariant;
            }
        }
        return self;
    }

    public static Builder builder(ResourceKey<Universe> universe) {
        return new Builder(universe);
    }

    public static class Builder {

        private final ResourceKey<Universe> universe;
        private final Map<Identifier, Appearance> categories = new LinkedHashMap<>();

        private Builder(ResourceKey<Universe> universe) {
            this.universe = universe;
        }

        public Builder category(Identifier category, Appearance appearance) {
            this.categories.put(category, appearance);
            return this;
        }

        public Builder category(Identifier category, SlotDependentIdComponent renderLayers, @Nullable Identifier itemModel) {
            return this.category(category, new Appearance(renderLayers, Optional.ofNullable(itemModel)));
        }

        public Builder category(Identifier category, Identifier itemModel) {
            return this.category(category, new Appearance(Appearance.EMPTY.renderLayers(), Optional.of(itemModel)));
        }

        public Builder category(Identifier category, SlotDependentIdComponent renderLayers, @Nullable Identifier itemModel, DataComponentPatch components) {
            return this.category(category, new Appearance(renderLayers, Optional.ofNullable(itemModel), components));
        }

        public Builder category(Identifier category, Identifier itemModel, DataComponentPatch components) {
            return this.category(category, new Appearance(Appearance.EMPTY.renderLayers(), Optional.of(itemModel), components));
        }

        public Builder components(Identifier category, DataComponentPatch components) {
            return this.updateComponents(category, existing -> components);
        }

        public <T> Builder component(Identifier category, DataComponentType<T> type, T value) {
            return this.updateComponents(category, components -> rebuild(components, builder -> builder.set(type, value)));
        }

        public <T> Builder removeComponent(Identifier category, DataComponentType<T> type) {
            return this.updateComponents(category, components -> rebuild(components, builder -> builder.remove(type)));
        }

        private Builder updateComponents(Identifier category, UnaryOperator<DataComponentPatch> operator) {
            Appearance appearance = this.categories.getOrDefault(category, Appearance.EMPTY);
            this.categories.put(category, appearance.withComponents(operator.apply(appearance.components())));
            return this;
        }

        @SuppressWarnings("unchecked")
        private static DataComponentPatch rebuild(DataComponentPatch patch, Consumer<DataComponentPatch.Builder> addition) {
            DataComponentPatch.Builder builder = DataComponentPatch.builder();

            for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
                DataComponentType<Object> type = (DataComponentType<Object>) entry.getKey();
                entry.getValue().ifPresentOrElse(value -> builder.set(type, value), () -> builder.remove(type));
            }

            addition.accept(builder);
            return builder.build();
        }

        public MultiversalItemVariant build() {
            return new MultiversalItemVariant(this.universe, Collections.unmodifiableMap(new LinkedHashMap<>(this.categories)));
        }
    }

    public record Appearance(SlotDependentIdComponent renderLayers, Optional<Identifier> itemModel, DataComponentPatch components) {

        public static final Appearance EMPTY = new Appearance(new SlotDependentIdComponent(Map.of()), Optional.empty());

        public static final Codec<Appearance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SlotDependentIdComponent.CODEC.optionalFieldOf("render_layers", new SlotDependentIdComponent(Map.of())).forGetter(Appearance::renderLayers),
                Identifier.CODEC.optionalFieldOf("item_model").forGetter(Appearance::itemModel),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(Appearance::components)
        ).apply(instance, Appearance::new));

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public Appearance(SlotDependentIdComponent renderLayers, Optional<Identifier> itemModel) {
            this(renderLayers, itemModel, DataComponentPatch.EMPTY);
        }

        public Appearance withComponents(DataComponentPatch components) {
            return new Appearance(this.renderLayers, this.itemModel, components);
        }
    }
}
