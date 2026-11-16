package com.star_1mp.powersunleashed.suit;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * A player's Suit Creator progress (saved on the player, kept on death, synced to that player only): the unlocked
 * suits. Every suit starts locked.
 */
public record SuitProgress(Set<Identifier> unlocked) {

    public static final SuitProgress EMPTY = new SuitProgress(Set.of());

    public static final MapCodec<SuitProgress> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("unlocked", Set.of()).forGetter(SuitProgress::unlocked)
    ).apply(i, SuitProgress::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SuitProgress> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), SuitProgress::unlocked,
            SuitProgress::new);

    public boolean isUnlocked(Suit suit) {
        return this.unlocked.contains(suit.id());
    }

    public SuitProgress unlock(Suit suit) {
        Set<Identifier> set = new HashSet<>(this.unlocked);
        set.add(suit.id());
        return new SuitProgress(Set.copyOf(set));
    }

    public SuitProgress lock(Suit suit) {
        Set<Identifier> set = new HashSet<>(this.unlocked);
        set.remove(suit.id());
        return new SuitProgress(Set.copyOf(set));
    }

    public int unlockedCount() {
        return (int) Suits.ALL.stream().filter(this::isUnlocked).count();
    }
}
