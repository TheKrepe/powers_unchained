package net.threetag.palladium.client.trail;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public record TrailConfiguration(List<Trail> trails) {

    public static final Codec<TrailConfiguration> CODEC = ExtraCodecs.compactListCodec(Trail.CODEC).xmap(TrailConfiguration::new, TrailConfiguration::trails);

}
