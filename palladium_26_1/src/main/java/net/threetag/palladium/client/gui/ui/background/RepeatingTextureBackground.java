package net.threetag.palladium.client.gui.ui.background;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class RepeatingTextureBackground extends UiBackground {

    public static final RepeatingTextureBackground RED_WOOL = new RepeatingTextureBackground(TextureReference.normal(Identifier.withDefaultNamespace("textures/block/red_wool.png")));

    public static final MapCodec<RepeatingTextureBackground> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextureReference.CODEC.fieldOf("texture").forGetter(s -> s.texture),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("width", 16).forGetter(s -> s.width),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("height", 16).forGetter(s -> s.height)
    ).apply(instance, RepeatingTextureBackground::new));

    private final TextureReference texture;
    private final int width;
    private final int height;

    public RepeatingTextureBackground(TextureReference texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public RepeatingTextureBackground(TextureReference texture) {
        this(texture, 16, 16);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, float alpha) {
        var texture = this.texture.getTexture(DataContext.forEntity(Minecraft.getInstance().player));
        if (texture == null) return;

        guiGraphics.enableScissor(x, y, x + width, y + height);
        for (int i = 0; i < Math.ceil((double) width / this.width); i++) {
            for (int j = 0; j < Math.ceil((double) height / this.height); j++) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                        x + (i * this.width), y + (j * this.height),
                        0, 0,
                        this.width, this.height,
                        this.width, this.height, ARGB.white(alpha));
            }
        }
        guiGraphics.disableScissor();
    }

    public TextureReference getTexture() {
        return this.texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public UiBackgroundSerializer<?> getSerializer() {
        return UiBackgroundSerializers.REPEATING_TEXTURE;
    }

    public static class Serializer extends UiBackgroundSerializer<RepeatingTextureBackground> {

        @Override
        public MapCodec<RepeatingTextureBackground> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiBackground, RepeatingTextureBackground> builder, HolderLookup.Provider provider) {
            builder.setName("Repeating Texture")
                    .setDescription("Repeating a texture so often that it fills the background.")
                    .add("texture", TYPE_TEXTURE_REFERENCE, "Texture path.")
                    .addOptional("width", TYPE_NON_NEGATIVE_INT, "Width of the texture file.", 256)
                    .addOptional("height", TYPE_NON_NEGATIVE_INT, "Height of the texture file.", 256);
        }
    }

}
