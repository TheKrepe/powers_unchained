package net.threetag.palladium.client.gui.widget.tree;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Optional;

public record PowerTreeConnectionStyle(Optional<PerState<Integer>> color,
                                       Optional<PerState<Integer>> outlineColor,
                                       Optional<Integer> width,
                                       Optional<Integer> outlineWidth,
                                       Optional<PowerTreeConnectionPath> path,
                                       Optional<Optional<List<Integer>>> dash) {

    public static final Codec<Integer> COLOR_CODEC = Codec.withAlternative(ExtraCodecs.STRING_ARGB_COLOR, ExtraCodecs.STRING_RGB_COLOR);

    public static final Codec<Optional<List<Integer>>> DASH_CODEC = Codec.either(Codec.BOOL, ExtraCodecs.POSITIVE_INT.listOf(2, 2)).xmap(
            either -> either.map(b -> Optional.empty(), Optional::of),
            optional -> optional.map(Either::<Boolean, List<Integer>>right).orElse(Either.left(false))
    );

    public static final Codec<PowerTreeConnectionStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PerState.codec(COLOR_CODEC).optionalFieldOf("color").forGetter(PowerTreeConnectionStyle::color),
            PerState.codec(COLOR_CODEC).optionalFieldOf("outline_color").forGetter(PowerTreeConnectionStyle::outlineColor),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("width").forGetter(PowerTreeConnectionStyle::width),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("outline_width").forGetter(PowerTreeConnectionStyle::outlineWidth),
            PowerTreeConnectionPath.CODEC.optionalFieldOf("path").forGetter(PowerTreeConnectionStyle::path),
            DASH_CODEC.optionalFieldOf("dash").forGetter(PowerTreeConnectionStyle::dash)
    ).apply(instance, PowerTreeConnectionStyle::new));

    public static final PowerTreeConnectionStyle DEFAULT = new PowerTreeConnectionStyle(
            Optional.of(PerState.of(0xFFFFFFFF, 0xFF00FFFF, 0xFF00FFFF)),
            Optional.of(PerState.of(0xFF000000)),
            Optional.of(1),
            Optional.of(1),
            Optional.of(PowerTreeConnectionPath.ELBOW_HORIZONTAL),
            Optional.of(Optional.empty())
    );

    public static class Resolved {

        public static final Resolved DEFAULT = new Resolved(List.of(PowerTreeConnectionStyle.DEFAULT));

        private final List<PowerTreeConnectionStyle> chain;
        private final int width;
        private final int outlineWidth;
        private final PowerTreeConnectionPath path;
        private final Optional<List<Integer>> dash;

        public Resolved(List<PowerTreeConnectionStyle> chain) {
            this.chain = chain;
            this.width = PerState.resolve(chain, PowerTreeConnectionStyle::width);
            this.outlineWidth = PerState.resolve(chain, PowerTreeConnectionStyle::outlineWidth);
            this.path = PerState.resolve(chain, PowerTreeConnectionStyle::path);
            this.dash = PerState.resolve(chain, PowerTreeConnectionStyle::dash);
        }

        public int getColor(PowerTreeState state) {
            return PerState.resolve(state, this.chain, PowerTreeConnectionStyle::color);
        }

        public int getOutlineColor(PowerTreeState state) {
            return PerState.resolve(state, this.chain, PowerTreeConnectionStyle::outlineColor);
        }

        public int getWidth() {
            return this.width;
        }

        public int getOutlineWidth() {
            return this.outlineWidth;
        }

        public PowerTreeConnectionPath getPath() {
            return this.path;
        }

        public Optional<List<Integer>> getDash() {
            return this.dash;
        }
    }
}
