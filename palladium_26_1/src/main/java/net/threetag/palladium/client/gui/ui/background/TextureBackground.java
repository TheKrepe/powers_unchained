package net.threetag.palladium.client.gui.ui.background;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class TextureBackground extends UiBackground {

    public static final MapCodec<TextureBackground> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextureReference.CODEC.fieldOf("texture").forGetter(s -> s.texture),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("width", 256).forGetter(s -> s.width),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("height", 256).forGetter(s -> s.height)
    ).apply(instance, TextureBackground::new));

    private final TextureReference texture;
    private final int width;
    private final int height;

    public TextureBackground(TextureReference texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, float alpha) {
        var texture = this.texture.getTexture(DataContext.forEntity(Minecraft.getInstance().player));

        if (texture != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                    x, y, 0, 0, Math.min(width, this.width), Math.min(height, this.height), this.width, this.height, ARGB.white(alpha));
        }
    }

    @Override
    public UiBackgroundSerializer<?> getSerializer() {
        return UiBackgroundSerializers.TEXTURE;
    }

    public static class Serializer extends UiBackgroundSerializer<TextureBackground> {

        @Override
        public MapCodec<TextureBackground> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiBackground, TextureBackground> builder, HolderLookup.Provider provider) {
            builder.setName("Texture")
                    .setDescription("Renders a static texture as a background.")
                    .add("texture", TYPE_TEXTURE_REFERENCE, "Texture path.")
                    .addOptional("width", TYPE_NON_NEGATIVE_INT, "Width of the texture file.", 256)
                    .addOptional("height", TYPE_NON_NEGATIVE_INT, "Height of the texture file.", 256);
        }
    }

}
