package net.threetag.palladium.item.tab;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mcexpanded.fancytabsections.Section.Section;
import net.mcexpanded.fancytabsections.Section.SectionTextured;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.Collections;
import java.util.Optional;

public class TexturedSectionSerializer extends SectionSerializer<SectionTextured> {

    public static final MapCodec<SectionTextured> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(SectionTextured::id),
            PalladiumCodecs.COLOR_INT_CODEC.optionalFieldOf("text_color", 0xFFFFFFFF).forGetter(s -> s.textColor),
            PalladiumCodecs.COLOR_INT_CODEC.optionalFieldOf("text_outline", 0x00000000).forGetter(s -> s.textOutline),
            Codec.BOOL.optionalFieldOf("text_shadow", true).forGetter(s -> s.textShadow),
            Identifier.CODEC.optionalFieldOf("texture").forGetter(s -> Optional.empty()),
            Codec.INT.optionalFieldOf("horizontal_size").forGetter(s -> Optional.empty()),
            Codec.INT.optionalFieldOf("vertical_size").forGetter(s -> Optional.empty()),
            Codec.INT.optionalFieldOf("texture_offset_x").forGetter(s -> Optional.empty()),
            Codec.INT.optionalFieldOf("texture_offset_y").forGetter(s -> Optional.empty()),
            TabContentEntry.CODEC.listOf().optionalFieldOf("items", Collections.emptyList()).forGetter(s -> Collections.<TabContentEntry>emptyList())
    ).apply(instance, (id, textColor, textOutline, textShadow, texture, horizontalSize, verticalSize, textureOffsetX, textureOffsetY, items) -> {
        var section = new SectionTextured(id)
                .setTextColor(textColor)
                .setTextOutline(textOutline)
                .setTextShadow(textShadow);
        texture.ifPresent(section::setTextureRaw);
        horizontalSize.ifPresent(section::setHorizontalSize);
        verticalSize.ifPresent(section::setVerticalSize);
        textureOffsetX.ifPresent(section::setTextureOffsetX);
        textureOffsetY.ifPresent(section::setTextureOffsetY);
        section.add(registries -> SectionItemLayout.resolve(items, registries));
        return section;
    }));

    @Override
    public MapCodec<SectionTextured> codec() {
        return CODEC;
    }

    @Override
    public void addDocumentation(CodecDocumentationBuilder<Section<?>, SectionTextured> builder, HolderLookup.Provider provider) {
        builder.setName("Textured")
                .setDescription("A creative tab section with a custom texture banner.")
                .add("id", TYPE_IDENTIFIER, "Unique identifier of the section.")
                .addOptional("text_color", TYPE_COLOR, "Color of the title text.")
                .addOptional("text_outline", TYPE_COLOR, "Outline color of the title text.")
                .addOptional("text_shadow", TYPE_BOOLEAN, "Whether the title text is rendered with a shadow.")
                .addOptional("texture", TYPE_IDENTIFIER, "Texture used for the banner. Defaults to a texture derived from the id.")
                .addOptional("horizontal_size", TYPE_INT, "Width of the banner texture.")
                .addOptional("vertical_size", TYPE_INT, "Height of the banner texture.")
                .addOptional("texture_offset_x", TYPE_INT, "Horizontal offset at which the texture is rendered.")
                .addOptional("texture_offset_y", TYPE_INT, "Vertical offset at which the texture is rendered.")
                .addOptional("items", SettingType.simple("Item ID or variant group array"), "Items and/or multiversal variant groups displayed in this section.")
                .addExampleObject(new SectionTextured(Identifier.fromNamespaceAndPath("palladium", "example"))
                        .setTextColor(0xFFFFFFFF));
    }
}
