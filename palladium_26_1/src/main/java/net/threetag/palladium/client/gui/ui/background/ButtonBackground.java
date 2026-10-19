package net.threetag.palladium.client.gui.ui.background;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ButtonBackground {

    public static final Codec<ButtonBackground> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UiBackground.Codecs.CODEC.fieldOf("enabled").forGetter(b -> b.enabled),
            UiBackground.Codecs.CODEC.optionalFieldOf("disabled").forGetter(b -> Optional.ofNullable(b.disabled).filter(d -> d != b.enabled)),
            UiBackground.Codecs.CODEC.optionalFieldOf("highlighted").forGetter(b -> Optional.ofNullable(b.highlighted).filter(h -> h != b.enabled))
    ).apply(instance, ButtonBackground::new));
    public static final Codec<ButtonBackground> CODEC = Codec.either(UiBackground.Codecs.CODEC, DIRECT_CODEC).xmap(either -> either.map(
            ButtonBackground::new,
            Function.identity()
    ), bg -> bg.disabled == bg.enabled && bg.highlighted == bg.enabled ? Either.left(bg.enabled) : Either.right(bg));

    private final UiBackground enabled;
    private final UiBackground disabled;
    private final UiBackground highlighted;

    public ButtonBackground(UiBackground enabled) {
        this(enabled, Optional.empty(), Optional.empty());
    }

    public ButtonBackground(WidgetSprites sprites) {
        this(new SpriteBackground(sprites.enabled()), new SpriteBackground(sprites.disabled()), new SpriteBackground(sprites.enabledFocused()));
    }

    public ButtonBackground(UiBackground enabled, UiBackground disabled, UiBackground highlighted) {
        this.enabled = enabled;
        this.disabled = disabled;
        this.highlighted = highlighted;
    }

    public ButtonBackground(UiBackground enabled, Optional<UiBackground> disabled, Optional<UiBackground> highlighted) {
        this.enabled = enabled;
        this.disabled = disabled.orElse(enabled);
        this.highlighted = highlighted.orElse(enabled);
    }

    public UiBackground get(boolean active, boolean hovered) {
        if (!active) {
            return this.disabled;
        }
        return hovered ? this.highlighted : this.enabled;
    }

    public void render(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, boolean active, boolean hovered, float alpha) {
        this.get(active, hovered).render(guiGraphics, x, y, width, height, alpha);
    }
}
