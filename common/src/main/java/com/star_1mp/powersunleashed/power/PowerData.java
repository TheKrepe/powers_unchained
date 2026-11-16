package com.star_1mp.powersunleashed.power;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A player's superpowers (saved, kept on death, synced to that player): the owned powers ({@link Superpower} ids) and
 * which switchable abilities are on ({@link PowerAbility} ids).
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

    /** Some owned superpower has the ability. */
    public boolean has(PowerAbility ability) {
        for (String id : this.owned) {
            Superpower p = Superpower.byId(id);
            if (p != null && p.abilities().contains(ability)) {
                return true;
            }
        }
        return false;
    }

    /** Owned and switched on (abilities that can't be switched off are always on). */
    public boolean isActive(PowerAbility ability) {
        return this.has(ability) && (!ability.toggleable() || this.enabled.contains(ability.id()));
    }

    public boolean hasAny() {
        for (String id : this.owned) {
            if (Superpower.byId(id) != null) {
                return true;
            }
        }
        return false;
    }

    public PowerData add(Superpower power) {
        if (this.has(power)) {
            return this;
        }
        Set<String> o = new HashSet<>(this.owned);
        o.add(power.id());
        Set<String> e = new HashSet<>(this.enabled);
        for (PowerAbility a : power.abilities()) {
            if (a.onByDefault()) {
                e.add(a.id());
            }
        }
        return new PowerData(Set.copyOf(o), Set.copyOf(e));
    }

    public PowerData remove(Superpower power) {
        Set<String> o = new HashSet<>(this.owned);
        o.remove(power.id());
        PowerData left = new PowerData(Set.copyOf(o), this.enabled);
        Set<String> e = new HashSet<>(this.enabled);
        for (PowerAbility a : power.abilities()) {
            if (!left.has(a)) {
                e.remove(a.id());
            }
        }
        return new PowerData(Set.copyOf(o), Set.copyOf(e));
    }

    public PowerData toggle(PowerAbility ability) {
        Set<String> e = new HashSet<>(this.enabled);
        if (!e.remove(ability.id())) {
            e.add(ability.id());
        }
        return new PowerData(this.owned, Set.copyOf(e));
    }
}
