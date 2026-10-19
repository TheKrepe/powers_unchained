package net.threetag.palladium.logic.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.function.Predicate;

public record IdentifierPredicate(Optional<String> namespace, Optional<String> path) implements Predicate<Identifier> {

    public static final Codec<IdentifierPredicate> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("namespace").forGetter(IdentifierPredicate::namespace),
            Codec.STRING.optionalFieldOf("path").forGetter(IdentifierPredicate::path)
    ).apply(instance, IdentifierPredicate::new));

    // A string with a colon is a full identifier ("mypack:my_thing"), one without is a namespace-only match ("mypack")
    public static final Codec<IdentifierPredicate> CODEC = Codec.withAlternative(DIRECT_CODEC,
            Codec.STRING.comapFlatMap(
                    s -> {
                        if (!s.contains(":")) {
                            return DataResult.success(new IdentifierPredicate(Optional.of(s), Optional.empty()));
                        }

                        Identifier id = Identifier.tryParse(s);
                        return id != null
                                ? DataResult.success(new IdentifierPredicate(id))
                                : DataResult.error(() -> "Invalid identifier in predicate: " + s);
                    },
                    predicate -> predicate.path().isEmpty()
                            ? predicate.namespace().orElse("")
                            : Identifier.fromNamespaceAndPath(predicate.namespace().orElse(Identifier.DEFAULT_NAMESPACE), predicate.path().get()).toString()
            )
    );

    public IdentifierPredicate(Identifier identifier) {
        this(Optional.of(identifier.getNamespace()), Optional.of(identifier.getPath()));
    }

    @Override
    public boolean test(Identifier identifier) {
        if (this.namespace.isPresent() && !identifier.getNamespace().equals(this.namespace.get())) {
            return false;
        }

        return this.path.isEmpty() || identifier.getPath().equals(this.path.get());
    }
}
