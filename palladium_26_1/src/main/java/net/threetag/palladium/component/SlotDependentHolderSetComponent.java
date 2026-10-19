package net.threetag.palladium.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.entity.PlayerSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record SlotDependentHolderSetComponent<T>(Map<PlayerSlot, HolderSet<T>> entries) {

    public static <T> Codec<SlotDependentHolderSetComponent<T>> codec(ResourceKey<? extends Registry<T>> registryKey) {
        return Codec.unboundedMap(
                PlayerSlot.CODEC,
                RegistryCodecs.homogeneousList(registryKey)
        ).xmap(SlotDependentHolderSetComponent::new, SlotDependentHolderSetComponent::entries);
    }

    public boolean contains(PlayerSlot slot, Holder<T> holder) {
        if (this.entries.containsKey(PlayerSlot.AnySlot.INSTANCE) && this.entries.get(PlayerSlot.AnySlot.INSTANCE).contains(holder)) {
            return true;
        }

        return this.entries.containsKey(slot) && this.entries.get(slot).contains(holder);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SlotDependentHolderSetComponent<?> that = (SlotDependentHolderSetComponent<?>) o;
        return Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entries);
    }

    public static class Builder<T> {

        private final Map<PlayerSlot, HolderSet<T>> entries = new HashMap<>();

        public Builder<T> set(PlayerSlot slot, HolderSet<T> holderSet) {
            this.entries.put(slot, holderSet);
            return this;
        }

        public SlotDependentHolderSetComponent<T> build() {
            return new SlotDependentHolderSetComponent<>(this.entries);
        }
    }

}
