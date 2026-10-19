package net.threetag.palladium.client.gui.widget.tree;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record PerState<T>(Optional<T> locked, Optional<T> available, Optional<T> unlocked) {

    public static <T> PerState<T> of(T value) {
        return new PerState<>(Optional.of(value), Optional.of(value), Optional.of(value));
    }

    public static <T> PerState<T> of(T locked, T available, T unlocked) {
        return new PerState<>(Optional.of(locked), Optional.of(available), Optional.of(unlocked));
    }

    public static <T> Codec<PerState<T>> codec(Codec<T> codec) {
        Codec<PerState<T>> mapCodec = RecordCodecBuilder.create(instance -> instance.group(
                codec.optionalFieldOf("locked").forGetter(PerState::locked),
                codec.optionalFieldOf("available").forGetter(PerState::available),
                codec.optionalFieldOf("unlocked").forGetter(PerState::unlocked)
        ).apply(instance, PerState::new));

        return Codec.either(codec, mapCodec).xmap(
                either -> either.map(PerState::of, s -> s),
                s -> s.isUniform() ? Either.left(s.locked.orElseThrow()) : Either.right(s)
        );
    }

    public Optional<T> get(PowerTreeState state) {
        return switch (state) {
            case LOCKED -> this.locked;
            case AVAILABLE -> this.available.or(() -> this.locked);
            case UNLOCKED -> this.unlocked;
        };
    }

    private boolean isUniform() {
        return this.locked.isPresent() && this.locked.equals(this.available) && this.locked.equals(this.unlocked);
    }

    public static <S, T> T resolve(PowerTreeState state, List<S> chain, Function<S, Optional<PerState<T>>> getter) {
        for (S style : chain) {
            var value = getter.apply(style);

            if (value.isPresent()) {
                var result = value.get().get(state);

                if (result.isPresent()) {
                    return result.get();
                }
            }
        }

        throw new IllegalStateException("No value found for state " + state.getSerializedName());
    }

    public static <S, T> T resolve(List<S> chain, Function<S, Optional<T>> getter) {
        for (S style : chain) {
            var value = getter.apply(style);

            if (value.isPresent()) {
                return value.get();
            }
        }

        throw new IllegalStateException("No value found");
    }
}
