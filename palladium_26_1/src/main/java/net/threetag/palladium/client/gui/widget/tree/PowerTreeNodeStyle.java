package net.threetag.palladium.client.gui.widget.tree;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.icon.IconRenderer;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.logic.context.DataContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record PowerTreeNodeStyle(Optional<Integer> size,
                                 Optional<PerState<TextureReference>> frame,
                                 Optional<PerState<Optional<TextureReference>>> overlay,
                                 Optional<PerState<Boolean>> showIcon,
                                 Optional<Integer> iconSize) {

    public static final Codec<Optional<TextureReference>> OPTIONAL_TEXTURE_CODEC = Codec.either(Codec.BOOL, TextureReference.CODEC).xmap(
            either -> either.map(b -> Optional.empty(), Optional::of),
            optional -> optional.map(Either::<Boolean, TextureReference>right).orElse(Either.left(false))
    );

    public static final Codec<PowerTreeNodeStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("size").forGetter(PowerTreeNodeStyle::size),
            PerState.codec(TextureReference.CODEC).optionalFieldOf("frame").forGetter(PowerTreeNodeStyle::frame),
            PerState.codec(OPTIONAL_TEXTURE_CODEC).optionalFieldOf("overlay").forGetter(PowerTreeNodeStyle::overlay),
            PerState.codec(Codec.BOOL).optionalFieldOf("show_icon").forGetter(PowerTreeNodeStyle::showIcon),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("icon_size").forGetter(PowerTreeNodeStyle::iconSize)
    ).apply(instance, PowerTreeNodeStyle::new));

    public static final PowerTreeNodeStyle DEFAULT = new PowerTreeNodeStyle(
            Optional.of(26),
            Optional.of(PerState.of(
                    TextureReference.normal(Palladium.id("powers/ability_frame_locked")),
                    TextureReference.normal(Palladium.id("powers/ability_frame_locked")),
                    TextureReference.normal(Palladium.id("powers/ability_frame_unlocked")))),
            Optional.of(PerState.of(
                    Optional.of(TextureReference.normal(Palladium.id("powers/lock"))),
                    Optional.of(TextureReference.normal(Palladium.id("powers/lock"))),
                    Optional.empty())),
            Optional.of(PerState.of(false, false, true)),
            Optional.of(16)
    );

    public static final PowerTreeNodeStyle EMPTY = new PowerTreeNodeStyle(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static class Resolved {

        public static final Resolved DEFAULT = new Resolved(List.of(PowerTreeNodeStyle.DEFAULT));

        private final List<PowerTreeNodeStyle> chain;
        private final int size;
        private final int iconSize;

        public Resolved(List<PowerTreeNodeStyle> chain) {
            this.chain = chain;
            this.size = PerState.resolve(chain, PowerTreeNodeStyle::size);
            this.iconSize = PerState.resolve(chain, PowerTreeNodeStyle::iconSize);
        }

        public int getSize() {
            return this.size;
        }

        public void render(GuiGraphicsExtractor gui, int x, int y, PowerTreeState state, @Nullable Icon icon, DataContext context) {
            int half = this.size / 2;
            blit(gui, PerState.resolve(state, this.chain, PowerTreeNodeStyle::frame), context, x - half, y - half, this.size, this.size);

            if (icon != null && this.iconSize > 0 && PerState.resolve(state, this.chain, PowerTreeNodeStyle::showIcon)) {
                IconRenderer.drawIcon(icon, Minecraft.getInstance(), gui, context,
                        x - this.iconSize / 2, y - this.iconSize / 2, this.iconSize, this.iconSize, 1F);
            }

            PerState.resolve(state, this.chain, PowerTreeNodeStyle::overlay)
                    .ifPresent(overlay -> blit(gui, overlay, context, x - half, y - half, this.size, this.size));
        }
    }

    public static void blit(GuiGraphicsExtractor gui, TextureReference reference, DataContext context, int x, int y, int width, int height) {
        if (reference.isDynamic()) {
            var texture = reference.getTexture(context);

            if (texture != null) {
                gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, width, height, width, height);
            }
        } else {
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, reference.getPath(), x, y, width, height);
        }
    }
}
