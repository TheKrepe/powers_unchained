package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.client.util.InterpolatedValue;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.logic.value.holder.IntValueHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ProgressBarUiWidget extends RenderableUiWidget {

    public static final MapCodec<ProgressBarUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntValueHolder.CODEC.fieldOf("value").forGetter(w -> w.value),
            IntValueHolder.CODEC.fieldOf("max_value").forGetter(w -> w.maxValue),
            TextureReference.CODEC.optionalFieldOf("background_texture").forGetter(w -> Optional.ofNullable(w.background)),
            TextureReference.CODEC.fieldOf("filled_texture").forGetter(w -> w.filled),
            Direction.CODEC.optionalFieldOf("direction", Direction.LEFT_TO_RIGHT).forGetter(w -> w.direction),
            IntValueHolder.CODEC.optionalFieldOf("u", IntValueHolder.ZERO).forGetter(w -> w.u),
            IntValueHolder.CODEC.optionalFieldOf("v", IntValueHolder.ZERO).forGetter(w -> w.v),
            IntValueHolder.CODEC.optionalFieldOf("texture_width", IntValueHolder._256).forGetter(w -> w.texWidth),
            IntValueHolder.CODEC.optionalFieldOf("texture_height", IntValueHolder._256).forGetter(w -> w.texHeight),
            FloatValueHolder.CODEC.optionalFieldOf("opacity", FloatValueHolder.ONE).forGetter(w -> w.opacity),
            propertiesCodec()
    ).apply(instance, (value, maxValue, background, filled, direction, u, v, texWidth, texHeight, opacity, properties) ->
            new ProgressBarUiWidget(value, maxValue, background.orElse(null), filled, direction, u, v, texWidth, texHeight, opacity, properties)));

    private final IntValueHolder value;
    private final IntValueHolder maxValue;
    @Nullable
    private final TextureReference background;
    private final TextureReference filled;
    private final Direction direction;
    private final IntValueHolder u;
    private final IntValueHolder v;
    private final IntValueHolder texWidth;
    private final IntValueHolder texHeight;
    private final FloatValueHolder opacity;

    private final InterpolatedValue interpolatedProgress = new InterpolatedValue();
    private final InterpolatedValue interpolatedOpacity = new InterpolatedValue();

    public ProgressBarUiWidget(IntValueHolder value, IntValueHolder maxValue, @Nullable TextureReference background, TextureReference filled,
                               Direction direction, IntValueHolder u, IntValueHolder v, IntValueHolder texWidth, IntValueHolder texHeight,
                               FloatValueHolder opacity, UiWidgetProperties properties) {
        super(properties);
        this.value = value;
        this.maxValue = maxValue;
        this.background = background;
        this.filled = filled;
        this.direction = direction;
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.opacity = opacity;
    }

    public ProgressBarUiWidget(IntValueHolder value, IntValueHolder maxValue, @Nullable Identifier background, Identifier filled,
                               Direction direction, IntValueHolder u, IntValueHolder v, IntValueHolder texWidth, IntValueHolder texHeight,
                               FloatValueHolder opacity, UiWidgetProperties properties) {
        this(value, maxValue, background != null ? TextureReference.normal(background) : null, TextureReference.normal(filled),
                direction, u, v, texWidth, texHeight, opacity, properties);
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.PROGRESS_BAR;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor gui, DataContext context, int x, int y, int width, int height, int mouseX, int mouseY, UiAlignment alignment) {
        var opacity = Mth.clamp(this.interpolatedOpacity.get(this.opacity, context, 1F), 0F, 1F);

        if (opacity <= 0F) {
            return;
        }

        int u = this.u.get(context, 0);
        int v = this.v.get(context, 0);
        int texWidth = this.texWidth.get(context, 256);
        int texHeight = this.texHeight.get(context, 256);
        int color = ARGB.white(opacity);

        if (this.background != null) {
            var texture = this.background.getTexture(context);

            if (texture != null) {
                gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, texWidth, texHeight, color);
            }
        }

        int max = this.maxValue.get(context, 0);
        float progress = max <= 0 ? 0F : Mth.clamp((float) this.value.get(context, 0) / max, 0F, 1F);
        progress = Mth.clamp(this.interpolatedProgress.get(progress), 0F, 1F);

        if (progress <= 0F) {
            return;
        }

        var texture = this.filled.getTexture(context);

        if (texture == null) {
            return;
        }

        int filledWidth = this.direction.vertical() ? width : Math.round(width * progress);
        int filledHeight = this.direction.vertical() ? Math.round(height * progress) : height;

        if (filledWidth <= 0 || filledHeight <= 0) {
            return;
        }

        int offsetX = this.direction == Direction.RIGHT_TO_LEFT ? width - filledWidth : 0;
        int offsetY = this.direction == Direction.BOTTOM_TO_TOP ? height - filledHeight : 0;

        gui.blit(RenderPipelines.GUI_TEXTURED, texture, x + offsetX, y + offsetY, u + offsetX, v + offsetY,
                filledWidth, filledHeight, texWidth, texHeight, color);
    }

    public enum Direction implements StringRepresentable {

        LEFT_TO_RIGHT("left_to_right"),
        RIGHT_TO_LEFT("right_to_left"),
        BOTTOM_TO_TOP("bottom_to_top"),
        TOP_TO_BOTTOM("top_to_bottom");

        public static final Codec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values);

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        public boolean vertical() {
            return this == BOTTOM_TO_TOP || this == TOP_TO_BOTTOM;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

    public static class Serializer extends UiWidgetSerializer<ProgressBarUiWidget> {

        @Override
        public MapCodec<ProgressBarUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, ProgressBarUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Progress Bar")
                    .setDescription("Renders a progress bar, where the filled texture is cut off according to the current value.")
                    .add("value", TYPE_INT_VALUE, "The current value of the bar.")
                    .add("max_value", TYPE_INT_VALUE, "The value at which the bar is full. Values of 0 or less leave the bar empty.")
                    .add("filled", TYPE_TEXTURE_REFERENCE, "Texture path/dynamic texture for the filled part of the bar.")
                    .addOptional("background_texture", TYPE_TEXTURE_REFERENCE, "Texture path/dynamic texture drawn behind the filled part. If not set, only the filled part is drawn.")
                    .addOptional("direction_texture", SettingType.enumList(Direction.values()), "The direction in which the bar fills up.", Direction.LEFT_TO_RIGHT.getSerializedName())
                    .addOptional("u", TYPE_INT_VALUE, "U-position on the textures where to start drawing from", 0)
                    .addOptional("v", TYPE_INT_VALUE, "V-position on the textures where to start drawing from", 0)
                    .addOptional("texture_width", TYPE_INT_VALUE, "Total width of the texture files", 256)
                    .addOptional("texture_height", TYPE_INT_VALUE, "Total height of the texture files", 256)
                    .addOptional("opacity", TYPE_FLOAT_VALUE, "Opacity between 0 (invisible) and 1 (fully opaque)", 1F)
                    .addExampleObject(new ProgressBarUiWidget(
                            IntValueHolder.ZERO,
                            new IntValueHolder(100),
                            Identifier.fromNamespaceAndPath("mypack", "textures/gui/progress_bar_background.png"),
                            Identifier.fromNamespaceAndPath("mypack", "textures/gui/progress_bar_filled.png"),
                            Direction.LEFT_TO_RIGHT,
                            IntValueHolder.ZERO,
                            IntValueHolder.ZERO,
                            IntValueHolder._256,
                            IntValueHolder._256,
                            FloatValueHolder.ONE,
                            UiWidgetProperties.DEFAULT));
        }
    }
}
