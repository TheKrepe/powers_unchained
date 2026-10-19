package net.threetag.palladium.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry-context-agnostic replacement for {@link RegistryCodecs#homogeneousList(ResourceKey)}.
 * <p>
 * The vanilla/NeoForge {@link HolderSet} codec requires a {@link RegistryOps} to decode: it resolves ids and tags
 * against a live registry. That works fine on the server (and anywhere decoding runs with registry context), but
 * client-side asset loading (e.g. render layers via
 * {@code net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayerManager}) decodes with plain
 * {@link com.mojang.serialization.JsonOps} - no registry - so the vanilla codec breaks.
 * <p>
 * This wrapper branches on the {@link DynamicOps} used at decode time:
 * <ul>
 *     <li>With a {@link RegistryOps} present (server / datagen / networking) it delegates to the real
 *     {@link RegistryCodecs#homogeneousList(ResourceKey)} and stores the resulting {@link HolderSet} as a
 *     {@link Direct} - keeping all NeoForge-added HolderSet behaviour intact.</li>
 *     <li>Without registry context (client asset load) it decodes the raw ids/tags into a {@link Raw}, which needs
 *     no registry. {@link #contains(Holder)} then works registry-free (a {@link Holder} knows its own id and tags),
 *     and {@link #resolve(HolderLookup.Provider)} expands the raw entries lazily at runtime, where registry access
 *     is available on both sides.</li>
 * </ul>
 * Supports plain ids, {@code #tags}, and lists of those - not NeoForge's extended HolderSet syntax on the client.
 */
public interface PalladiumHolderSet<T> {

    /**
     * Whether the given holder is part of this set. Registry-free: safe to call during client asset load.
     */
    boolean contains(Holder<T> holder);

    /**
     * Expands this set into its individual holders. Requires registry access, so only call at runtime (never during
     * client asset load).
     */
    List<Holder<T>> resolve(HolderLookup.Provider registries);

    /**
     * The raw id/tag entries backing this set, used for registry-free encoding.
     */
    List<Either<Identifier, TagKey<T>>> toRawEntries();

    static <T> PalladiumHolderSet<T> direct(HolderSet<T> holderSet) {
        return new Direct<>(holderSet);
    }

    static <T> Codec<PalladiumHolderSet<T>> codec(ResourceKey<? extends Registry<T>> registryKey) {
        Codec<HolderSet<T>> vanilla = RegistryCodecs.homogeneousList(registryKey);
        Codec<PalladiumHolderSet<T>> raw = rawCodec(registryKey);

        return new Codec<>() {
            @Override
            public <U> DataResult<Pair<PalladiumHolderSet<T>, U>> decode(DynamicOps<U> ops, U input) {
                if (registryAccessible(ops)) {
                    return vanilla.decode(ops, input).map(pair -> pair.mapFirst(Direct::new));
                }
                return raw.decode(ops, input);
            }

            @Override
            public <U> DataResult<U> encode(PalladiumHolderSet<T> input, DynamicOps<U> ops, U prefix) {
                if (registryAccessible(ops) && input instanceof Direct<T> direct) {
                    return vanilla.encode(direct.holderSet(), ops, prefix);
                }
                return raw.encode(input, ops, prefix);
            }

            private <U> boolean registryAccessible(DynamicOps<U> ops) {
                return ops instanceof RegistryOps<U> registryOps && registryOps.getter(registryKey).isPresent();
            }
        };
    }

    private static <T> Codec<PalladiumHolderSet<T>> rawCodec(ResourceKey<? extends Registry<T>> registryKey) {
        Codec<Either<Identifier, TagKey<T>>> entryCodec = Codec.STRING.comapFlatMap(str -> {
            if (str.startsWith("#")) {
                Identifier id = Identifier.tryParse(str.substring(1));
                return id == null
                        ? DataResult.error(() -> "Not a valid tag id: " + str)
                        : DataResult.success(Either.right(TagKey.create(registryKey, id)));
            } else {
                Identifier id = Identifier.tryParse(str);
                return id == null
                        ? DataResult.error(() -> "Not a valid id: " + str)
                        : DataResult.success(Either.left(id));
            }
        }, either -> either.map(Identifier::toString, tag -> "#" + tag.location()));

        return ExtraCodecs.compactListCodec(entryCodec)
                .xmap(entries -> new Raw<>(registryKey, entries), PalladiumHolderSet::toRawEntries);
    }

    /**
     * A resolved set backed by a real {@link HolderSet} - produced when decoding with registry context.
     */
    record Direct<T>(HolderSet<T> holderSet) implements PalladiumHolderSet<T> {

        @Override
        public boolean contains(Holder<T> holder) {
            return this.holderSet.contains(holder);
        }

        @Override
        public List<Holder<T>> resolve(HolderLookup.Provider registries) {
            return this.holderSet.stream().toList();
        }

        @Override
        public List<Either<Identifier, TagKey<T>>> toRawEntries() {
            return this.holderSet.unwrap().map(
                    tag -> List.of(Either.right(tag)),
                    holders -> holders.stream()
                            .map(holder -> Either.<Identifier, TagKey<T>>left(holder.unwrapKey().orElseThrow().identifier()))
                            .toList()
            );
        }
    }

    /**
     * A raw id/tag representation - produced when decoding without registry context (client asset load).
     */
    record Raw<T>(ResourceKey<? extends Registry<T>> registryKey,
                  List<Either<Identifier, TagKey<T>>> entries) implements PalladiumHolderSet<T> {

        @Override
        public boolean contains(Holder<T> holder) {
            for (Either<Identifier, TagKey<T>> entry : this.entries) {
                boolean matches = entry.map(holder::is, holder::is);
                if (matches) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<Holder<T>> resolve(HolderLookup.Provider registries) {
            HolderLookup.RegistryLookup<T> lookup = registries.lookupOrThrow(this.registryKey);
            List<Holder<T>> result = new ArrayList<>();
            for (Either<Identifier, TagKey<T>> entry : this.entries) {
                entry.ifLeft(id -> lookup.get(ResourceKey.create(this.registryKey, id)).ifPresent(result::add));
                entry.ifRight(tag -> lookup.get(tag).ifPresent(named -> named.forEach(result::add)));
            }
            return result;
        }

        @Override
        public List<Either<Identifier, TagKey<T>>> toRawEntries() {
            return this.entries;
        }
    }
}
