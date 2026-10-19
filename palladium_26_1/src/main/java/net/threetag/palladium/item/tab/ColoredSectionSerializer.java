package net.threetag.palladium.item.tab;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mcexpanded.fancytabsections.Section.Section;
import net.mcexpanded.fancytabsections.Section.SectionColored;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.Collections;

public class ColoredSectionSerializer extends SectionSerializer<SectionColored> {

    public static final MapCodec<SectionColored> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(SectionColored::id),
            PalladiumCodecs.COLOR_INT_CODEC.optionalFieldOf("banner_color", 0xFF45BB77).forGetter(s -> s.bannerColor),
            PalladiumCodecs.COLOR_INT_CODEC.optionalFieldOf("text_color", 0xFFFFFFFF).forGetter(s -> s.textColor),
            PalladiumCodecs.COLOR_INT_CODEC.optionalFieldOf("text_outline", 0x00000000).forGetter(s -> s.textOutline),
            Codec.BOOL.optionalFieldOf("text_shadow", false).forGetter(s -> s.textShadow),
            TabContentEntry.CODEC.listOf().optionalFieldOf("items", Collections.emptyList()).forGetter(s -> Collections.<TabContentEntry>emptyList())
    ).apply(instance,(id, bannerColor, textColor, textOutline, textShadow, items) -> {
        var section = new SectionColored(id)
                .setBannerColor(bannerColor)
                .setTextColor(textColor)
                .setTextOutline(textOutline)
                .setTextShadow(textShadow);
        section.add(registries -> SectionItemLayout.resolve(items, registries));
        return section;
    }));

    @Override
    public MapCodec<SectionColored> codec() {
        return CODEC;
    }

    @Override
    public void addDocumentation(CodecDocumentationBuilder<Section<?>, SectionColored> builder, HolderLookup.Provider provider) {
        builder.setName("Colored")
                .setDescription("A creative tab section with a flat colored banner.")
                .add("id", TYPE_IDENTIFIER, "Unique identifier of the section.")
                .addOptional("banner_color", TYPE_COLOR, "Color of the banner background.")
                .addOptional("text_color", TYPE_COLOR, "Color of the title text.")
                .addOptional("text_outline", TYPE_COLOR, "Outline color of the title text.")
                .addOptional("text_shadow", TYPE_BOOLEAN, "Whether the title text is rendered with a shadow.")
                .addOptional("items", SettingType.simple("Item ID or variant group array"), "Items and/or multiversal variant groups displayed in this section.")
                .addExampleObject(new SectionColored(Identifier.fromNamespaceAndPath("palladium", "example"))
                        .setBannerColor(0xFF45BB77)
                        .setTextColor(0xFFFFFFFF));
    }
}
