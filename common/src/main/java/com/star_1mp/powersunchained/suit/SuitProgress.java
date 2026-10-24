package com.star_1mp.powersunchained.suit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * A player's Suit Creator progress (saved on the player, kept on death, synced to that player only): unlocked suits
 * and scanned fragments of still locked ones. Every suit starts locked.
 */
public record SuitProgress(Set<Identifier> unlocked, Map<Identifier, Integer> fragments) {

    public static final SuitProgress EMPTY = new SuitProgress(Set.of(), Map.of());

    public static final MapCodec<SuitProgress> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("unlocked", Set.of()).forGetter(SuitProgress::unlocked),
            Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("fragments", Map.of()).forGetter(SuitProgress::fragments)
    ).apply(i, SuitProgress::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SuitProgress> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), SuitProgress::unlocked,
            ByteBufCodecs.<RegistryFriendlyByteBuf, Identifier, Integer, Map<Identifier, Integer>>map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.VAR_INT),
            SuitProgress::fragments,
            SuitProgress::new);

    public boolean isUnlocked(Suit suit) {
        return this.unlocked.contains(suit.id());
    }

    public int fragments(Suit suit) {
        return this.fragments.getOrDefault(suit.id(), 0);
    }

    public SuitProgress unlock(Suit suit) {
        Set<Identifier> set = new HashSet<>(this.unlocked);
        set.add(suit.id());
        Map<Identifier, Integer> map = new HashMap<>(this.fragments);
        map.remove(suit.id());
        return new SuitProgress(Set.copyOf(set), Map.copyOf(map));
    }

    public SuitProgress lock(Suit suit) {
        Set<Identifier> set = new HashSet<>(this.unlocked);
        set.remove(suit.id());
        Map<Identifier, Integer> map = new HashMap<>(this.fragments);
        map.remove(suit.id());
        return new SuitProgress(Set.copyOf(set), Map.copyOf(map));
    }

    public SuitProgress withFragments(Suit suit, int count) {
        Map<Identifier, Integer> map = new HashMap<>(this.fragments);
        map.put(suit.id(), count);
        return new SuitProgress(this.unlocked, Map.copyOf(map));
    }

    public int unlockedCount() {
        return (int) Suits.ALL.stream().filter(this::isUnlocked).count();
    }
}
