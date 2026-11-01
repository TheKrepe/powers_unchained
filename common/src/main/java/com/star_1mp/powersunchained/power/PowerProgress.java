package com.star_1mp.powersunchained.power;

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

/**
 * Levelling of the player's superpowers (saved, kept on death, synced to that player): per power its experience and the
 * skill-tree nodes bought. Every power has its own experience and skill points (owner: Spider-Man points are not for
 * other powers). Level / points: {@link PowerLevels}.
 */
public record PowerProgress(Map<String, Entry> powers) {

    public static final PowerProgress EMPTY = new PowerProgress(Map.of());

    /** One power: total experience, bought nodes. */
    public record Entry(int xp, Set<String> nodes) {
        public static final Entry EMPTY = new Entry(0, Set.of());

        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("xp", 0).forGetter(Entry::xp),
                Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("nodes", Set.of()).forGetter(Entry::nodes)
        ).apply(i, Entry::new));

        static final StreamCodec<io.netty.buffer.ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Entry::xp,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), Entry::nodes,
                Entry::new);
    }

    public static final MapCodec<PowerProgress> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.unboundedMap(Codec.STRING, Entry.CODEC).optionalFieldOf("powers", Map.of()).forGetter(PowerProgress::powers)
    ).apply(i, PowerProgress::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PowerProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, Entry.STREAM_CODEC), p -> new HashMap<>(p.powers()),
            m -> new PowerProgress(Map.copyOf(m)));

    public Entry get(Superpower power) {
        return this.powers.getOrDefault(power.id(), Entry.EMPTY);
    }

    public boolean has(Superpower power, String node) {
        return this.get(power).nodes().contains(node);
    }

    public PowerProgress with(Superpower power, Entry entry) {
        Map<String, Entry> map = new HashMap<>(this.powers);
        map.put(power.id(), entry);
        return new PowerProgress(Map.copyOf(map));
    }

    public PowerProgress withXp(Superpower power, int xp) {
        Entry e = this.get(power);
        return this.with(power, new Entry(Math.max(0, xp), e.nodes()));
    }

    public PowerProgress withNode(Superpower power, String node, boolean owned) {
        Entry e = this.get(power);
        Set<String> nodes = new HashSet<>(e.nodes());
        if (owned) {
            nodes.add(node);
        } else {
            nodes.remove(node);
        }
        return this.with(power, new Entry(e.xp(), Set.copyOf(nodes)));
    }
}
