package net.threetag.palladium.customization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.logic.cost.Cost;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultCustomization extends Customization {

    public static final MapCodec<DefaultCustomization> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(a -> Optional.ofNullable(a.customTitle)),
            ResourceKey.codec(PalladiumRegistryKeys.CUSTOMIZATION_CATEGORY).fieldOf("category").forGetter(a -> a.category),
            PreviewableIdentifier.CODEC.optionalFieldOf("render_layer").forGetter(a -> Optional.ofNullable(a.renderLayer)),
            ExtraCodecs.compactListCodec(Identifier.CODEC).optionalFieldOf("trail", Collections.emptyList()).forGetter(a -> a.trails),
            Codec.BOOL.optionalFieldOf("unlockable", false).forGetter(a -> a.unlockable),
            Cost.CODEC.optionalFieldOf("unlock_cost").forGetter(a -> Optional.ofNullable(a.unlockCost))
    ).apply(instance, (t, s, r, tr, u, c) ->
            new DefaultCustomization(t.orElse(null), s, r.orElse(null), tr, u, c.orElse(null))));

    private final Component customTitle;
    private final ResourceKey<CustomizationCategory> category;
    private final PreviewableIdentifier renderLayer;
    private final List<Identifier> trails;
    private final boolean unlockable;
    private final Cost unlockCost;
    private Identifier cachedRenderLayerId;

    public DefaultCustomization(Component customTitle, ResourceKey<CustomizationCategory> category, PreviewableIdentifier renderLayer, List<Identifier> trails, boolean unlockable, Cost unlockCost) {
        this.customTitle = customTitle;
        this.category = category;
        this.renderLayer = renderLayer;
        this.trails = trails;
        this.unlockable = unlockable;
        this.unlockCost = unlockCost;
    }

    @Override
    public CustomizationSerializer<?> getSerializer() {
        return CustomizationSerializers.DEFAULT.get();
    }

    @Override
    public Component getTitle(RegistryAccess registryAccess) {
        if (this.customTitle != null) {
            return this.customTitle;
        } else {
            return super.getTitle(registryAccess);
        }
    }

    @Override
    public ResourceKey<CustomizationCategory> getCategoryKey() {
        return this.category;
    }

    public Identifier getRenderLayerId(RegistryAccess registryAccess, boolean preview) {
        if (this.renderLayer != null) {
            return this.renderLayer.get(preview);
        } else {
            if (this.cachedRenderLayerId == null) {
                var id = registryAccess.lookupOrThrow(PalladiumRegistryKeys.CUSTOMIZATION).getKey(this);
                this.cachedRenderLayerId = Objects.requireNonNull(id).withPrefix("customization/");
            }

            return this.cachedRenderLayerId;
        }
    }

    @Override
    public List<Identifier> getTrails() {
        return this.trails;
    }

    @Override
    public UnlockedBy unlockedBy() {
        return this.unlockable ? UnlockedBy.COMMAND : UnlockedBy.NONE;
    }

    @Override
    public Cost getUnlockCost() {
        return this.unlockCost;
    }

    public static class Serializer extends CustomizationSerializer<DefaultCustomization> {

        @Override
        public MapCodec<DefaultCustomization> codec() {
            return CODEC;
        }
    }
}
