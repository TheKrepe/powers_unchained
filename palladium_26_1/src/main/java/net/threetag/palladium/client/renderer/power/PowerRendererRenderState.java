package net.threetag.palladium.client.renderer.power;

import net.minecraft.resources.Identifier;
import net.threetag.palladium.client.model.PowerRendererModel;
import org.jetbrains.annotations.Nullable;

public record PowerRendererRenderState(
        PowerRenderer renderer,
        float progress,
        boolean isSlim,
        @Nullable PowerRendererModel model,
        Identifier texture,
        boolean showModel
) {}
