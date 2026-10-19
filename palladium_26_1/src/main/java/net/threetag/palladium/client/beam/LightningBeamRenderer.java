package net.threetag.palladium.client.beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.renderer.LaserRenderer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PalladiumCodecs;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LightningBeamRenderer extends BeamRenderer {

    public static final MapCodec<LightningBeamRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LaserRenderer.codec(1).fieldOf("render_settings").forGetter(beam -> beam.laserRenderer),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("segments", 5).forGetter(beam -> beam.segments),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("frequency", 2).forGetter(beam -> beam.frequency),
            PalladiumCodecs.NON_NEGATIVE_VOXEL_FLOAT.optionalFieldOf("spread", 5F).forGetter(beam -> beam.spread)
    ).apply(instance, LightningBeamRenderer::new));

    private final LaserRenderer laserRenderer;
    private final int segments;
    private final int frequency;
    private final float spread;

    public LightningBeamRenderer(LaserRenderer laserRenderer, int segments, int frequency, float spread) {
        this.laserRenderer = laserRenderer;
        this.segments = segments;
        this.frequency = frequency;
        this.spread = spread;
    }

    @Override
    public void submit(DataContext context, Vec3 origin, Vec3 target, Vec2 sizeMultiplier, float lengthMultiplier, float opacityMultiplier, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLightIn, int ageInTicks, float partialTick) {
        var relativeTarget = target.subtract(origin);
        var segmentPartVec = relativeTarget.scale(1F / this.segments);
        var random = RandomSource.create(ageInTicks / this.frequency);

        List<Vec3> points = new ArrayList<>();
        points.add(Vec3.ZERO);
        for (int i = 0; i < this.segments - 1; i++) {
            points.add(segmentPartVec.scale(i + 1).add(randomizeVector(random, this.spread)));
        }
        points.add(relativeTarget);

        List<Vec3> visible = LaserRenderer.partialPoints(points, lengthMultiplier);
        if (visible.size() >= 2) {
            this.laserRenderer.renderPolyline(poseStack, submitNodeCollector, visible, ageInTicks, partialTick, opacityMultiplier, sizeMultiplier.x);
        }
    }

    private static Vec3 randomizeVector(RandomSource random, float spread) {
        return new Vec3((random.nextFloat() - 0.5F) * 2F * spread, (random.nextFloat() - 0.5F) * 2F * spread, (random.nextFloat() - 0.5F) * 2F * spread);
    }

    @Override
    public BeamRendererSerializer<?> getSerializer() {
        return BeamRendererSerializers.LIGHTNING;
    }

    public static class Serializer extends BeamRendererSerializer<LightningBeamRenderer> {

        @Override
        public MapCodec<LightningBeamRenderer> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BeamRenderer, LightningBeamRenderer> builder, HolderLookup.Provider provider) {
            builder.setName("Lightning Beam")
                    .setDescription("Renders a fluctuating lightning between two points.")
                    .add("render_settings", TYPE_LASER_RENDERER, "The render settings for the lightning.")
                    .addOptional("segments", TYPE_INT, "The amount of segments the lightning should have.", 5)
                    .addOptional("frequency", TYPE_INT, "The frequency of the lightning fluctuation.", 2)
                    .addOptional("spread", TYPE_FLOAT, "The spread of the lightning fluctuation. It defines how \"far\" the lightning can spread out from the core", 5F)
                    .addExampleObject(new LightningBeamRenderer(
                            new LaserRenderer(
                                    new LaserRenderer.LaserPart(Color.BLUE, 1F, 0F, null, LaserRenderer.BlendingMode.ADDITIVE),
                                    new LaserRenderer.LaserPart(Color.WHITE, 1F, 0F, null, LaserRenderer.BlendingMode.ADDITIVE),
                                    2, new Vector2f(2 / 16F, 2 / 16F), 0, 0
                            ),
                            10,
                            4,
                            7F
                    ));
        }
    }
}
