package net.threetag.palladium.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.threetag.palladium.addonpack.AddonObjectLoader;
import net.threetag.palladium.component.DelayedDataComponentMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ItemPropertiesCodec {

    public static final MapCodec<Item.Properties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DelayedDataComponentMap.CODEC.optionalFieldOf("components", DelayedDataComponentMap.EMPTY).forGetter(p -> DelayedDataComponentMap.EMPTY),
            ExtraCodecs.compactListCodec(TabPlacement.CODEC).optionalFieldOf("tab", Collections.emptyList()).forGetter(p -> p instanceof ExtendedProperties ext ? ext.getTabs() : Collections.emptyList())
    ).apply(instance, (components, tabs) -> {
        var properties = new ExtendedProperties();

        if (AddonObjectLoader.ID_TO_SET != null) {
            properties.setId(AddonObjectLoader.resourceId(Registries.ITEM, AddonObjectLoader.ID_TO_SET));
            AddonObjectLoader.ID_TO_SET = null;
        }

        components.addToProperties(properties);
        properties.tabs(tabs);

        return properties;
    }));

    public static final MapCodec<Item.Properties> CODEC_LAZY_LOAD_COMPONENTS = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DelayedDataComponentMap.CODEC.optionalFieldOf("components", DelayedDataComponentMap.EMPTY).forGetter(p -> DelayedDataComponentMap.EMPTY),
            ExtraCodecs.compactListCodec(TabPlacement.CODEC).optionalFieldOf("tab", Collections.emptyList()).forGetter(p -> p instanceof ExtendedProperties ext ? ext.getTabs() : Collections.emptyList())
    ).apply(instance, (components, tabs) -> {
        var properties = new ExtendedProperties();

        if (AddonObjectLoader.ID_TO_SET != null) {
            properties.setId(AddonObjectLoader.resourceId(Registries.ITEM, AddonObjectLoader.ID_TO_SET));
            AddonObjectLoader.ID_TO_SET = null;
        }

        properties.componentMap(components);
        properties.tabs(tabs);

        return properties;
    }));

    public static class ExtendedProperties extends Item.Properties {

        private DelayedDataComponentMap components = DelayedDataComponentMap.EMPTY;
        private final List<TabPlacement> tabs = new ArrayList<>();

        public void componentMap(DelayedDataComponentMap components) {
            this.components = components;
        }

        public void loadComponentMap() {
            this.components.addToProperties(this);
        }

        public ExtendedProperties tab(TabPlacement tab) {
            this.tabs.add(tab);
            return this;
        }

        public ExtendedProperties tabs(Collection<TabPlacement> tabs) {
            this.tabs.addAll(tabs);
            return this;
        }

        public List<TabPlacement> getTabs() {
            return tabs;
        }
    }

}
