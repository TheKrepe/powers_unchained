package net.threetag.palladium.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.customization.BuiltinCustomization;
import net.threetag.palladium.customization.Customization;
import net.threetag.palladium.customization.CustomizationCategory;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.item.suitset.SuitSets;
import net.threetag.palladium.item.upgrade.ItemUpgrade;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.registry.PalladiumRegistries;

import java.util.Objects;

public abstract class ExtendedLangProvider extends LanguageProvider {

    protected final String modid;

    public ExtendedLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.modid = modid;
    }

    public void addCreativeModeTab(Holder<? extends CreativeModeTab> key, String name) {
        this.add(key.value(), name);
    }

    public void add(CreativeModeTab key, String name) {
        Identifier id = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(key);
        this.add(Util.makeDescriptionId("itemGroup", id), name);
    }

    public void addPower(ResourceKey<Power> power, String name) {
        this.add(Util.makeDescriptionId(Palladium.MOD_ID + ".power", power.identifier()), name);
    }

    public void addAbility(Holder<? extends AbilitySerializer<?>> key, String name) {
        this.add(key.value(), name);
    }

    public void add(AbilitySerializer<?> key, String name) {
        Identifier id = PalladiumRegistries.ABILITY_SERIALIZER.getKey(key);
        this.add(Palladium.MOD_ID + ".ability." + Objects.requireNonNull(id).getNamespace() + "." + id.getPath(), name);
    }

    public void addConfigEntry(ModConfigSpec.ConfigValue<?> configSpec, String name) {
        this.add(this.modid + ".configuration." + String.join(".", configSpec.getPath()), name);
    }

    public void addKeyMapping(String key, String name) {
        this.add("key.palladium." + key, name);
    }

    public void addCustomizationCategory(ResourceKey<CustomizationCategory> key, String name) {
        this.add(CustomizationCategory.makeDescriptionId(key), name);
    }

    public void addCustomization(ResourceKey<Customization> key, String name) {
        this.add(Customization.makeDescriptionId(key), name);
    }

    public void addBuiltinCustomization(BuiltinCustomization.Type type, String name) {
        this.add(Customization.makeDescriptionId(Palladium.id(type.name())), name);
    }

    public void addBannerPattern(ResourceKey<BannerPattern> patternKey, DyeColor color, String name) {
        this.add(Util.makeDescriptionId("banner_pattern", patternKey.identifier()) + "." + color.getName(), name);
    }

    public void addJukeboxSong(ResourceKey<JukeboxSong> key, String name) {
        this.add(Util.makeDescriptionId("jukebox_song", key.identifier()), name);
    }

    public void addSubtitle(DeferredHolder<SoundEvent, SoundEvent> soundEvent, String name) {
        this.add("subtitles." + soundEvent.getId().getNamespace() + "." + soundEvent.getId().getPath(), name);
    }

    public void addUniverse(ResourceKey<Universe> key, String name) {
        this.add(Universe.makeTranslationKey(key.identifier()), name);
    }

    public void addCommonUniverse(String key, String name) {
        this.add(Universe.makeTranslationKey(Identifier.fromNamespaceAndPath("c", key)), name);
    }

    public void addItemVariant(ResourceKey<MultiversalItemVariant> key, String name) {
        this.add(MultiversalItemVariant.makeDescriptionId(key.identifier()), name);
    }

    public void addSuitSet(Identifier id, String name) {
        this.add(SuitSets.makeDescriptionId(id), name);
    }

    public void addItemUpgrade(ResourceKey<ItemUpgrade> key, String name) {
        this.add(Util.makeDescriptionId("palladium.item_upgrade", key.identifier()), name);
    }
}
