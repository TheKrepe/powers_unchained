package net.threetag.palladium.customization;

import com.mojang.serialization.Codec;
import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.logic.cost.Cost;
import net.threetag.palladium.registry.PalladiumRegistries;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.Collections;
import java.util.List;

public abstract class Customization {

    private Component title;

    public abstract CustomizationSerializer<?> getSerializer();

    public Component getTitle(RegistryAccess registryAccess) {
        if (this.title == null) {
            this.title = Component.translatable(makeDescriptionId(this, registryAccess));
        }
        return this.title;
    }

    public static String makeDescriptionId(Customization customization, RegistryAccess registryAccess) {
        return Util.makeDescriptionId(Palladium.MOD_ID + ".customization", registryAccess.lookupOrThrow(PalladiumRegistryKeys.CUSTOMIZATION).getKey(customization));
    }

    public static String makeDescriptionId(ResourceKey<Customization> key) {
        return Util.makeDescriptionId(Palladium.MOD_ID + ".customization", key.identifier());
    }

    public static String makeDescriptionId(Identifier id) {
        return Util.makeDescriptionId(Palladium.MOD_ID + ".customization", id);
    }

    public abstract ResourceKey<CustomizationCategory> getCategoryKey();

    public Holder<CustomizationCategory> getCategory(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(PalladiumRegistryKeys.CUSTOMIZATION_CATEGORY).get(this.getCategoryKey()).orElseThrow();
    }

    public Identifier getRenderLayerId(RegistryAccess registryAccess, boolean preview) {
        return null;
    }

    public List<Identifier> getTrails() {
        return Collections.emptyList();
    }

    public UnlockedBy unlockedBy() {
        return UnlockedBy.NONE;
    }

    public Cost getUnlockCost() {
        return null;
    }

    public static class Codecs {

        public static final Codec<Holder<Customization>> HOLDER_CODEC = RegistryFixedCodec.create(PalladiumRegistryKeys.CUSTOMIZATION);

        public static final Codec<Customization> CODEC = PalladiumRegistries.CUSTOMIZATION_SERIALIZERS.byNameCodec().dispatch(Customization::getSerializer, CustomizationSerializer::codec);

        public static final Codec<Customization> SIMPLE_CODEC = Codec.withAlternative(
                CODEC,
                DefaultCustomization.CODEC.codec()
        );

    }

    public enum UnlockedBy {

        NONE,
        COMMAND,
        REWARD

    }
}
