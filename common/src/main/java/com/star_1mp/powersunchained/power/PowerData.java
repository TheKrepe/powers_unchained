package com.star_1mp.powersunchained.power;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A player's superpowers (saved, kept on death, synced to that player): the owned powers and which of them are
 * switched on (ids of {@link Superpower}).
 */
public record PowerData(Set<String> owned, Set<String> enabled) {

    public static final PowerData EMPTY = new PowerData(Set.of(), Set.of());

    public static final MapCodec<PowerData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            com.mojang.serialization.Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("owned", Set.of())
                    .forGetter(PowerData::owned),
            com.mojang.serialization.Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("enabled", Set.of())
                    .forGetter(PowerData::enabled)
    ).apply(i, PowerData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PowerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), PowerData::owned,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), PowerData::enabled,
            PowerData::new);

    public boolean has(Superpower power) {
        return this.owned.contains(power.id());
    }

    /** Owned and switched on (powers that can't be switched off are always on). */
    public boolean isActive(Superpower power) {
        return this.has(power) && (!power.toggleable() || this.enabled.contains(power.id()));
    }

    public boolean isEnabled(Superpower power) {
        return this.enabled.contains(power.id());
    }

    public PowerData add(Superpower power) {
        if (this.has(power)) {
            return this;
        }
        Set<String> o = new HashSet<>(this.owned);
        o.add(power.id());
        Set<String> e = new HashSet<>(this.enabled);
        if (power.onByDefault()) {
            e.add(power.id());
        }
        return new PowerData(Set.copyOf(o), Set.copyOf(e));
    }

    public PowerData remove(Superpower power) {
        Set<String> o = new HashSet<>(this.owned);
        o.remove(power.id());
        Set<String> e = new HashSet<>(this.enabled);
        e.remove(power.id());
        return new PowerData(Set.copyOf(o), Set.copyOf(e));
    }

    public PowerData toggle(Superpower power) {
        Set<String> e = new HashSet<>(this.enabled);
        if (!e.remove(power.id())) {
            e.add(power.id());
        }
        return new PowerData(this.owned, Set.copyOf(e));
    }
}
