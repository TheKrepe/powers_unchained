package net.threetag.palladium.addonpack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.component.DelayedDataComponentMap;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.ArmorMaterialRegistry;
import net.threetag.palladium.item.ItemPropertiesCodec;
import net.threetag.palladium.item.TabPlacement;
import net.threetag.palladium.item.multiverse.ItemMultiversalVariant;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.item.suitset.SuitSet;
import net.threetag.palladium.item.suitset.SuitSets;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SuitSetLoader extends SimpleJsonResourceReloadListener<SuitSetLoader.SuitSetConfig> {

    public final AddonPackManager.RegisterCallback<Item> registerCallback;

    public SuitSetLoader(AddonPackManager.RegisterCallback<Item> registerCallback) {
        super(SuitSetConfig.CODEC, FileToIdConverter.json("suit_set"));
        this.registerCallback = registerCallback;
    }

    @Override
    protected void apply(Map<Identifier, SuitSetConfig> objects, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        AtomicInteger i = new AtomicInteger();

        objects.forEach((id, object) -> {
            object.registerItems(id, this.registerCallback);
            i.getAndIncrement();
        });

        AddonPackLog.info("Registered " + i.get() + " addonpack suit sets");
    }

    public record SuitSetConfig(Optional<ArmorMaterial> armorMaterial,
                                DelayedDataComponentMap components,
                                Optional<MultiversalVariantSettings> multiversalVariant,
                                List<TabPlacement> tabs,
                                Map<ArmorType, SuitSetPart> parts) {

        public static final Codec<SuitSetConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ArmorMaterialRegistry.CODEC.optionalFieldOf("armor_material").forGetter(SuitSetConfig::armorMaterial),
                DelayedDataComponentMap.CODEC.optionalFieldOf("components", DelayedDataComponentMap.EMPTY).forGetter(SuitSetConfig::components),
                MultiversalVariantSettings.CODEC.optionalFieldOf("multiversal_variant").forGetter(SuitSetConfig::multiversalVariant),
                ExtraCodecs.compactListCodec(TabPlacement.CODEC).optionalFieldOf("tab", Collections.emptyList()).forGetter(SuitSetConfig::tabs),
                Codec.unboundedMap(ArmorType.CODEC, SuitSetPart.CODEC).fieldOf("slots").forGetter(SuitSetConfig::parts)
        ).apply(instance, SuitSetConfig::new));

        public void registerItems(Identifier suitSetId, AddonPackManager.RegisterCallback<Item> callback) {
            Map<ArmorType, Item> items = new HashMap<>();

            for (ArmorType armorType : this.parts.keySet()) {
                var part = this.parts.get(armorType);
                var properties = part.properties();
                this.components.addToProperties(properties);

                if (this.armorMaterial.isPresent()) {
                    properties = properties.humanoidArmor(this.armorMaterial.get(), armorType);
                } else {
                    properties = properties.equippable(armorType.getSlot());
                }

                if (properties instanceof ItemPropertiesCodec.ExtendedProperties ext) {
                    ext.tabs(this.tabs);
                    ext.loadComponentMap();
                }

                if (this.multiversalVariant.isPresent()) {
                    MultiversalVariantSettings settings = this.multiversalVariant.get();
                    properties.component(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get(), settings.category().withSuffix("_" + armorType.getName()));
                    if (settings.defaultVariant().isPresent()) {
                        properties.component(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get(), new ItemMultiversalVariant(settings.defaultVariant().get()));
                    }
                }

                var id = part.itemName().map(s -> Identifier.fromNamespaceAndPath(suitSetId.getNamespace(), s)).orElse(suitSetId.withSuffix("_" + armorType.getName()));
                properties.setId(ResourceKey.create(Registries.ITEM, id));
                var item = new Item(properties);
                callback.register(Registries.ITEM, id, item);
                items.put(armorType, item);
            }

            SuitSets.register(suitSetId, new SuitSet(
                    Optional.ofNullable(items.get(ArmorType.HELMET)),
                    Optional.ofNullable(items.get(ArmorType.CHESTPLATE)),
                    Optional.ofNullable(items.get(ArmorType.LEGGINGS)),
                    Optional.ofNullable(items.get(ArmorType.BOOTS))
            ));
        }

    }

    public record MultiversalVariantSettings(Identifier category, Optional<ResourceKey<MultiversalItemVariant>> defaultVariant) {

        public static final Codec<MultiversalVariantSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("category").forGetter(MultiversalVariantSettings::category),
                ResourceKey.codec(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT).optionalFieldOf("default").forGetter(MultiversalVariantSettings::defaultVariant)
        ).apply(instance, MultiversalVariantSettings::new));

    }

    private record SuitSetPart(Optional<String> itemName, Item.Properties properties) {

        public static final Codec<SuitSetPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("item_name").forGetter(SuitSetPart::itemName),
                ItemPropertiesCodec.CODEC_LAZY_LOAD_COMPONENTS.forGetter(SuitSetPart::properties)
        ).apply(instance, SuitSetPart::new));

    }

}
