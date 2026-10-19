package net.threetag.palladium.item.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record ItemUpgrade(HolderSet<Item> applicableItems, HolderSet<ItemUpgrade> incompatibilities, DataComponentPatch components) {

    public static final Codec<ItemUpgrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("applicable_items").forGetter(ItemUpgrade::applicableItems),
            RegistryCodecs.homogeneousList(PalladiumRegistryKeys.ITEM_UPGRADE).optionalFieldOf("incompatibilities", HolderSet.empty()).forGetter(ItemUpgrade::incompatibilities),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemUpgrade::components)
    ).apply(instance, ItemUpgrade::new));

    public boolean isApplicableTo(ItemStack stack) {
        return this.applicableItems.contains(stack.typeHolder());
    }

    public boolean isIncompatibleWith(Holder<ItemUpgrade> other) {
        return this.incompatibilities.contains(other);
    }

    @Nullable
    public static ItemUpgrade get(HolderLookup.Provider registries, ResourceKey<ItemUpgrade> key) {
        return getHolder(registries, key).map(Holder::value).orElse(null);
    }

    public static Optional<Holder<ItemUpgrade>> getHolder(HolderLookup.Provider registries, ResourceKey<ItemUpgrade> key) {
        return registries.lookup(PalladiumRegistryKeys.ITEM_UPGRADE).flatMap(lookup -> lookup.get(key)).map(holder -> holder);
    }

    public static Component getName(ResourceKey<ItemUpgrade> key) {
        return Component.translatable(Util.makeDescriptionId("palladium.item_upgrade", key.identifier()));
    }

}
