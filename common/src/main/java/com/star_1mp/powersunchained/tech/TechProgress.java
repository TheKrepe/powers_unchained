package com.star_1mp.powersunchained.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A player's Tech Table progress (saved on the player, kept on death, synced to that player only): the gadget modules
 * and gadget looks (skins) they unlocked. Unlocking is once per player; installing a module into a gadget costs again.
 */
public record TechProgress(Set<String> modules, Set<String> skins, Set<String> feats) {

    public static final TechProgress EMPTY = new TechProgress(Set.of(), Set.of(), Set.of());

    private static final Codec<Set<String>> SET = Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf);

    public static final MapCodec<TechProgress> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SET.optionalFieldOf("modules", Set.of()).forGetter(TechProgress::modules),
            SET.optionalFieldOf("skins", Set.of()).forGetter(TechProgress::skins),
            SET.optionalFieldOf("feats", Set.of()).forGetter(TechProgress::feats)
    ).apply(i, TechProgress::new));

    private static final StreamCodec<io.netty.buffer.ByteBuf, Set<String>> SET_STREAM =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf);

    public static final StreamCodec<RegistryFriendlyByteBuf, TechProgress> STREAM_CODEC = StreamCodec.composite(
            SET_STREAM, TechProgress::modules, SET_STREAM, TechProgress::skins, SET_STREAM, TechProgress::feats, TechProgress::new);

    public boolean hasModule(WebModule module) {
        return this.modules.contains(module.id());
    }

    public TechProgress withModule(WebModule module) {
        Set<String> set = new HashSet<>(this.modules);
        set.add(module.id());
        return new TechProgress(Set.copyOf(set), this.skins, this.feats);
    }

    public TechProgress withoutModule(WebModule module) {
        Set<String> set = new HashSet<>(this.modules);
        set.remove(module.id());
        return new TechProgress(Set.copyOf(set), this.skins, this.feats);
    }

    public boolean hasSkin(String skin) {
        return GadgetSkins.DEFAULT.equals(skin) || this.skins.contains(skin);
    }

    public TechProgress withSkin(String skin) {
        Set<String> set = new HashSet<>(this.skins);
        set.add(skin);
        return new TechProgress(this.modules, Set.copyOf(set), this.feats);
    }

    /** Feats are conditions a module needs besides materials (e.g. struck by lightning in web-shooters). */
    public boolean hasFeat(String feat) {
        return this.feats.contains(feat);
    }

    public TechProgress withFeat(String feat) {
        Set<String> set = new HashSet<>(this.feats);
        set.add(feat);
        return new TechProgress(this.modules, this.skins, Set.copyOf(set));
    }
}
