package net.threetag.palladium.customization;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.function.Function;

public record PreviewableIdentifier(Identifier main, Optional<Identifier> preview) {

    private static final Codec<PreviewableIdentifier> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("main").forGetter(PreviewableIdentifier::main),
            Identifier.CODEC.optionalFieldOf("preview").forGetter(PreviewableIdentifier::preview)
    ).apply(instance, PreviewableIdentifier::new));

    public static final Codec<PreviewableIdentifier> CODEC = Codec.either(
            Identifier.CODEC,
            DIRECT_CODEC
    ).xmap(either -> either.map(p -> new PreviewableIdentifier(p, Optional.empty()), Function.identity()),
            previewable -> previewable.preview().isEmpty() ? Either.left(previewable.main()) : Either.right(previewable));

    public PreviewableIdentifier(Identifier main) {
        this(main, Optional.empty());
    }

    public Identifier get(boolean preview) {
        if (preview && this.preview().isPresent()) {
            return this.preview().get();
        } else {
            return this.main();
        }
    }

}
