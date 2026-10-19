package net.threetag.palladium.client.beam;

import net.minecraft.resources.Identifier;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.RenderTypeRegistry;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.client.util.PerspectiveValue;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.function.BiConsumer;

public class PalladiumBeams {

    public static final Identifier SWINGING_WEB_RIGHT = Palladium.id("swinging/web_right");
    public static final Identifier SWINGING_WEB_LEFT = Palladium.id("swinging/web_left");

    public static void bootstrap(BiConsumer<Identifier, BeamConfiguration> provider) {
        provider.accept(SWINGING_WEB_RIGHT, new BeamConfiguration(Collections.singletonList(
                new Beam(
                        "right_arm",
                        new PerspectiveValue<>(new Vector3f(0, 10F / 16F, 0)),
                        Beam.DEFAULT_VISIBILITY,
                        new FlatTexturedBeamRenderer(
                                TextureReference.normal(Palladium.id("textures/beam/web.png")),
                                new Vector2f(1 / 16F, 64 / 16F),
                                RenderTypeRegistry.ENTITY_TRANSLUCENT,
                                0
                        ),
                        Collections.emptyList()
                )
        )));
        provider.accept(SWINGING_WEB_LEFT, new BeamConfiguration(Collections.singletonList(
                new Beam(
                        "left_arm",
                        new PerspectiveValue<>(new Vector3f(0, 10F / 16F, 0)),
                        Beam.DEFAULT_VISIBILITY,
                        new FlatTexturedBeamRenderer(
                                TextureReference.normal(Palladium.id("textures/beam/web.png")),
                                new Vector2f(1 / 16F, 64 / 16F),
                                RenderTypeRegistry.ENTITY_TRANSLUCENT,
                                0
                        ),
                        Collections.emptyList()
                )
        )));
    }

}
