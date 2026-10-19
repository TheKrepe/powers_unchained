package net.threetag.palladium.item.tab;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mcexpanded.fancytabsections.FancyTabSections;
import net.mcexpanded.fancytabsections.Section.Section;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.threetag.palladium.addonpack.AddonObjectLoader;

import java.util.Collections;
import java.util.Optional;

public class CreativeModeTabCodec {

    public static final Codec<CreativeModeTab> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(CreativeModeTab::getDisplayName),
            Identifier.CODEC.fieldOf("icon").forGetter(tab -> BuiltInRegistries.ITEM.getKey(tab.getIconItem().getItem())),
            Identifier.CODEC.optionalFieldOf("background_texture").forGetter(tab -> Optional.of(tab.getBackgroundTexture())),
            Codec.BOOL.optionalFieldOf("search_bar", false).forGetter(CreativeModeTab::hasSearchBar),
            TabContentEntry.CODEC.listOf().optionalFieldOf("items", Collections.emptyList()).forGetter(tab -> Collections.<TabContentEntry>emptyList()),
            SectionSerializer.CODEC.listOf().optionalFieldOf("sections", Collections.emptyList()).forGetter(t -> Collections.emptyList())
    ).apply(instance, (name, icon, bg, searchBar, items, sections) -> {
        var builder = CreativeModeTab.builder()
                .title(name)
                .icon(() -> BuiltInRegistries.ITEM.getValue(icon).getDefaultInstance());

        if (searchBar) {
            builder.withSearchBar();
        }

        bg.ifPresent(builder::backgroundTexture);
        builder.displayItems((parameters, output) -> items.forEach(entry -> entry.resolve(parameters.holders()).forEach(output::accept)));

        for (Section<?> section : sections) {
            FancyTabSections.addSection(AddonObjectLoader.ID_TO_SET, section);
        }
        AddonObjectLoader.ID_TO_SET = null;

        return builder.build();
    }));

}
