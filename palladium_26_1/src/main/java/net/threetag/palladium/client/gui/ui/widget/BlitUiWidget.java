package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.client.util.InterpolatedValue;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.logic.value.holder.IntValueHolder;

import java.util.Objects;

public final class BlitUiWidget extends RenderableUiWidget {

    public static final MapCodec<BlitUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextureReference.CODEC.fieldOf("texture").forGetter(BlitUiWidget::texture),
            IntValueHolder.CODEC.optionalFieldOf("u", IntValueHolder.ZERO).forGetter(BlitUiWidget::u),
            IntValueHolder.CODEC.optionalFieldOf("v", IntValueHolder.ZERO).forGetter(BlitUiWidget::v),
            IntValueHolder.CODEC.optionalFieldOf("texture_width", IntValueHolder._256).forGetter(BlitUiWidget::texWidth),
            IntValueHolder.CODEC.optionalFieldOf("texture_height", IntValueHolder._256).forGetter(BlitUiWidget::texHeight),
            FloatValueHolder.CODEC.optionalFieldOf("rotation", FloatValueHolder.ZERO).forGetter(BlitUiWidget::rotation),
            FloatValueHolder.CODEC.optionalFieldOf("scale", FloatValueHolder.ONE).forGetter(BlitUiWidget::scale),
            FloatValueHolder.CODEC.optionalFieldOf("opacity", FloatValueHolder.ONE).forGetter(BlitUiWidget::opacity),
            propertiesCodec()
    ).apply(instance, BlitUiWidget::new));

    private final TextureReference texture;
    private final IntValueHolder u;
    private final IntValueHolder v;
    private final IntValueHolder texWidth;
    private final IntValueHolder texHeight;
    private final FloatValueHolder rotation;
    private final FloatValueHolder scale;
    private final FloatValueHolder opacity;

    private final InterpolatedValue interpolatedRotation = new InterpolatedValue();
    private final InterpolatedValue interpolatedScale = new InterpolatedValue();
    private final InterpolatedValue interpolatedOpacity = new InterpolatedValue();

    public BlitUiWidget(TextureReference texture, IntValueHolder u, IntValueHolder v, IntValueHolder texWidth, IntValueHolder texHeight, FloatValueHolder rotation, FloatValueHolder scale, FloatValueHolder opacity, UiWidgetProperties properties) {
        super(properties);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.rotation = rotation;
        this.scale = scale;
        this.opacity = opacity;
    }

    public BlitUiWidget(Identifier texture, IntValueHolder u, IntValueHolder v, IntValueHolder texWidth, IntValueHolder texHeight, FloatValueHolder rotation, FloatValueHolder scale, FloatValueHolder opacity, UiWidgetProperties properties) {
        this(TextureReference.normal(texture), u, v, texWidth, texHeight, rotation, scale, opacity, properties);
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.BLIT;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor gui, DataContext context, int x, int y, int width, int height, int mouseX, int mouseY, UiAlignment alignment) {
        var rotation = this.interpolatedRotation.get(this.rotation, context, 0F);
        var scale = this.interpolatedScale.get(this.scale, context, 1F);
        var opacity = Mth.clamp(this.interpolatedOpacity.get(this.opacity, context, 1F), 0F, 1F);

        if (opacity <= 0F) {
            return;
        }

        var transformed = rotation != 0F || scale != 1F;

        if (transformed) {
            var centerX = x + width / 2F;
            var centerY = y + height / 2F;

            gui.pose().pushMatrix();
            gui.pose().translate(centerX, centerY);

            if (rotation != 0F) {
                gui.pose().rotate((float) Math.toRadians(rotation));
            }

            if (scale != 1F) {
                gui.pose().scale(scale, scale);
            }

            gui.pose().translate(-centerX, -centerY);
        }

        gui.blit(RenderPipelines.GUI_TEXTURED, Objects.requireNonNull(this.texture.getTexture(context)), x, y,
                this.u.get(context, 0),
                this.v.get(context, 0),
                width, height, this.texWidth.get(context, 256), this.texHeight.get(context, 256), ARGB.white(opacity));

        if (transformed) {
            gui.pose().popMatrix();
        }
    }

    public TextureReference texture() {
        return texture;
    }

    public IntValueHolder u() {
        return u;
    }

    public IntValueHolder v() {
        return v;
    }

    public IntValueHolder texWidth() {
        return texWidth;
    }

    public IntValueHolder texHeight() {
        return texHeight;
    }

    public FloatValueHolder rotation() {
        return rotation;
    }

    public FloatValueHolder scale() {
        return scale;
    }

    public FloatValueHolder opacity() {
        return opacity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlitUiWidget) obj;
        return Objects.equals(this.texture, that.texture) &&
                this.u == that.u &&
                this.v == that.v &&
                this.texWidth == that.texWidth &&
                this.texHeight == that.texHeight &&
                this.rotation == that.rotation &&
                this.scale == that.scale &&
                this.opacity == that.opacity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, u, v, texWidth, texHeight, rotation, scale, opacity);
    }

    @Override
    public String toString() {
        return "BlitUiWidget[" +
                "texture=" + texture + ", " +
                "u=" + u + ", " +
                "v=" + v + ", " +
                "texWidth=" + texWidth + ", " +
                "texHeight=" + texHeight + ", " +
                "rotation=" + rotation + ", " +
                "scale=" + scale + ", " +
                "opacity=" + opacity + ']';
    }


    public static class Serializer extends UiWidgetSerializer<BlitUiWidget> {

        @Override
        public MapCodec<BlitUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, BlitUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Blit")
                    .setDescription("Renders a simple (piece of a) texture")
                    .add("texture", TYPE_TEXTURE_REFERENCE, "Texture path/dynamic texture")
                    .addOptional("u", TYPE_INT_VALUE, "U-position on the texture where to start drawing from", 0)
                    .addOptional("v", TYPE_INT_VALUE, "V-position on the texture where to start drawing from", 0)
                    .addOptional("texture_width", TYPE_INT_VALUE, "Total width of the texture file", 256)
                    .addOptional("texture_height", TYPE_INT_VALUE, "Total height of the texture file", 256)
                    .addOptional("rotation", TYPE_FLOAT_VALUE, "Rotation in degrees, applied around the center of the widget", 0F)
                    .addOptional("scale", TYPE_FLOAT_VALUE, "Scale multiplier, applied around the center of the widget. Only affects rendering, the widget keeps its original bounds", 1F)
                    .addOptional("opacity", TYPE_FLOAT_VALUE, "Opacity between 0 (invisible) and 1 (fully opaque)", 1F)
                    .addExampleObject(new BlitUiWidget(
                            Identifier.fromNamespaceAndPath("mypack", "textures/gui/texture_file.png"),
                            IntValueHolder.ZERO,
                            IntValueHolder.ZERO,
                            IntValueHolder._256,
                            IntValueHolder._256,
                            FloatValueHolder.ZERO,
                            FloatValueHolder.ONE,
                            FloatValueHolder.ONE,
                            UiWidgetProperties.DEFAULT));
        }

    }
}
