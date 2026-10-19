package net.threetag.palladium.item.tab;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;

import java.util.List;

public sealed interface TabContentEntry {

    Codec<TabContentEntry> CODEC = Codec.either(Identifier.CODEC, VariantGroup.CODEC).xmap(
            either -> either.map(ItemEntry::new, GroupEntry::new),
            entry -> entry instanceof ItemEntry(Identifier item)
                    ? Either.left(item)
                    : Either.right(((GroupEntry) entry).group()));

    List<ItemStack> resolve(HolderLookup.Provider registries);

    record ItemEntry(Identifier item) implements TabContentEntry {

        @Override
        public List<ItemStack> resolve(HolderLookup.Provider registries) {
            return BuiltInRegistries.ITEM.containsKey(this.item)
                    ? List.of(new ItemStack(BuiltInRegistries.ITEM.getValue(this.item)))
                    : List.of();
        }
    }

    record GroupEntry(VariantGroup group) implements TabContentEntry {

        @Override
        public List<ItemStack> resolve(HolderLookup.Provider registries) {
            return MultiversalItemVariant.collectGroupedVariantStacks(registries, this.group);
        }
    }
}
