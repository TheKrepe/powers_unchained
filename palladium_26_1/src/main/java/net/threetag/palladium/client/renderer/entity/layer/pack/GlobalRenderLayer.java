package net.threetag.palladium.client.renderer.entity.layer.pack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.List;

public record GlobalRenderLayer(
        List<EntityType<?>> entities,
        List<Identifier> renderLayers
) {

    public static final Codec<GlobalRenderLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.compactListCodec(BuiltInRegistries.ENTITY_TYPE.byNameCodec()).fieldOf("entities").forGetter(GlobalRenderLayer::entities),
            ExtraCodecs.compactListCodec(Identifier.CODEC).fieldOf("render_layers").forGetter(GlobalRenderLayer::renderLayers)
    ).apply(instance, GlobalRenderLayer::new));

}
