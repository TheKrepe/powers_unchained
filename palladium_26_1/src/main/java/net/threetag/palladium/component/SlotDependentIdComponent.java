package net.threetag.palladium.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.util.Utils;

import java.util.*;

public record SlotDependentIdComponent(Map<PlayerSlot, List<Identifier>> entries) {

    public static final Codec<SlotDependentIdComponent> CODEC = Codec.unboundedMap(
            PlayerSlot.CODEC,
            ExtraCodecs.compactListCodec(Identifier.CODEC)
    ).xmap(SlotDependentIdComponent::new, SlotDependentIdComponent::entries);
    public static final StreamCodec<FriendlyByteBuf, SlotDependentIdComponent> STREAM_CODEC = ByteBufCodecs.map(
            Utils::newMap,
            PlayerSlot.STREAM_CODEC,
            ByteBufCodecs.collection(Utils::newList, Identifier.STREAM_CODEC)
    ).map(SlotDependentIdComponent::new, SlotDependentIdComponent::entries);

    public List<Identifier> forSlot(PlayerSlot slot) {
        List<Identifier> list = new ArrayList<>();
        list.addAll(this.entries.getOrDefault(PlayerSlot.AnySlot.INSTANCE, Collections.emptyList()));
        list.addAll(this.entries.getOrDefault(slot, Collections.emptyList()));
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SlotDependentIdComponent that = (SlotDependentIdComponent) o;
        return Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entries);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<PlayerSlot, List<Identifier>> entries = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder add(PlayerSlot slot, Identifier... ids) {
            this.entries.computeIfAbsent(slot, s -> new ArrayList<>()).addAll(Arrays.asList(ids));
            return this;
        }

        public Builder add(EquipmentSlot slot, Identifier... ids) {
            return this.add(PlayerSlot.get(slot), ids);
        }

        public SlotDependentIdComponent build() {
            return new SlotDependentIdComponent(Collections.unmodifiableMap(new LinkedHashMap<>(this.entries)));
        }
    }

}
