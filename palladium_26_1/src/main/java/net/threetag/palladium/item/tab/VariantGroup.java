package net.threetag.palladium.item.tab;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.List;
import java.util.Optional;

public record VariantGroup(List<Identifier> items, Optional<ResourceKey<Universe>> universe) {

    public static final Codec<VariantGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.listOf().fieldOf("variant_group").forGetter(VariantGroup::items),
            ResourceKey.codec(PalladiumRegistryKeys.MULTIVERSE).optionalFieldOf("universe").forGetter(VariantGroup::universe)
    ).apply(instance, VariantGroup::new));
}
