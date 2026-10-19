package net.threetag.palladium.client.renderer.entity.layer.pack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.threetag.palladium.client.util.PerspectiveAwareConditions;

import java.util.Collections;
import java.util.List;

public record PackRenderLayerProperties(List<String> hiddenModelParts, List<Identifier> renderers, List<PlayerModelPart> shrinkPlayerParts, PerspectiveAwareConditions conditions) {

    public static final PackRenderLayerProperties DEFAULT = new PackRenderLayerProperties(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), PerspectiveAwareConditions.EMPTY);

    public static final MapCodec<PackRenderLayerProperties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.compactListCodec(Codec.STRING).optionalFieldOf("hidden_model_parts", Collections.emptyList()).forGetter(PackRenderLayerProperties::hiddenModelParts),
            ExtraCodecs.compactListCodec(Identifier.CODEC).optionalFieldOf("renderer", Collections.emptyList()).forGetter(PackRenderLayerProperties::renderers),
            ExtraCodecs.compactListCodec(PlayerModelPart.CODEC).optionalFieldOf("shrink_player_parts", Collections.emptyList()).forGetter(PackRenderLayerProperties::shrinkPlayerParts),
            PerspectiveAwareConditions.CODEC.optionalFieldOf("conditions", PerspectiveAwareConditions.EMPTY).forGetter(PackRenderLayerProperties::conditions)
    ).apply(instance, PackRenderLayerProperties::new));

}
