package net.threetag.palladium.client.trail;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.renderer.LaserRenderer;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class LightningTrailRenderer extends TrailRenderer<LightningTrailRenderer.Data> {

    public static final MapCodec<LightningTrailRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("amount", 10).forGetter(r -> r.amount),
            Vec3.CODEC.optionalFieldOf("area").forGetter(r -> r.area),
            LaserRenderer.codec(1).fieldOf("render_settings").forGetter(r -> r.laserRenderer)
    ).apply(instance, LightningTrailRenderer::new));

    private final int amount;
    private final Optional<Vec3> area;
    private final LaserRenderer laserRenderer;

    public LightningTrailRenderer(int amount, Optional<Vec3> area, LaserRenderer laserRenderer) {
        this.amount = amount;
        this.area = area;
        this.laserRenderer = laserRenderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LivingEntityRenderState ownerRenderState, TrailSegment<LightningTrailRenderer.Data> trailSegment, int packedLight, float partialTick) {
        var nextSegment = trailSegment.getNextSegment();
        var liveEntityPos = new Vec3(ownerRenderState.x, ownerRenderState.y, ownerRenderState.z);
        Vec3 liveAnchor = liveAnchor(ownerRenderState, trailSegment.getTrail());
        Vec3 nextPos;
        EntityDimensions nextDimensions;
        if (nextSegment != null) {
            nextPos = nextSegment.getPosition().subtract(trailSegment.getPosition());
            nextDimensions = nextSegment.getDimensions();
        } else if (liveAnchor != null) {
            nextDimensions = trailSegment.getDimensions();
            nextPos = liveAnchor.subtract(trailSegment.getPosition()).subtract(0, nextDimensions.height() / 2F, 0);
        } else if (trailSegment.getTrail().anchor().isPresent()) {
            nextPos = liveEntityPos.subtract(trailSegment.getEntityPosition());
            nextDimensions = trailSegment.getDimensions();
        } else {
            nextPos = liveEntityPos.subtract(trailSegment.getPosition());
            nextDimensions = EntityDimensions.fixed(ownerRenderState.boundingBoxWidth, ownerRenderState.boundingBoxHeight);
        }

        float endOpacity = trailSegment.getOpacity();
        float startOpacity = nextSegment != null ? nextSegment.getOpacity() : endOpacity;
        float[] opacities = {startOpacity, endOpacity};

        for (int i = 0; i < this.amount; i++) {
            var offsets = trailSegment.getTrailData().offsets[i];
            var nextOffsets = nextSegment != null ? nextSegment.getTrailData().offsets[i] : offsets;

            var start = getOffsetPos(nextPos, nextOffsets, nextDimensions, spreadBox(nextDimensions));
            var end = getOffsetPos(Vec3.ZERO, offsets, trailSegment.getDimensions(), spreadBox(trailSegment.getDimensions()));

            this.laserRenderer.renderPolyline(poseStack, submitNodeCollector, List.of(start, end),
                    (int) ownerRenderState.ageInTicks, ownerRenderState.partialTick, opacities, 1F);
        }
    }

    private Vec3 spreadBox(EntityDimensions dimensions) {
        return this.area.orElseGet(() -> new Vec3(dimensions.width(), dimensions.height(), dimensions.width()));
    }

    @Nullable
    private static Vec3 liveAnchor(LivingEntityRenderState state, Trail trail) {
        var instance = state.getRenderDataOrDefault(PalladiumRenderStateKeys.TRAILS, Map.of()).get(trail);
        return instance != null ? instance.getLiveAnchor() : null;
    }

    private static Vec3 getOffsetPos(Vec3 origin, Vec3 offset, EntityDimensions dimensions, Vec3 box) {
        return origin.add(0, dimensions.height() / 2F, 0).add(offset.multiply(box.x, box.y, box.z));
    }

    @Override
    public Data createData(Entity parent) {
        var trailData = new Data();
        var random = RandomSource.create();
        trailData.offsets = new Vec3[this.amount];

        for (int i = 0; i < this.amount; i++) {
            var spacingY = (1D / this.amount);
            trailData.offsets[i] = new Vec3((random.nextDouble() - 0.5D), ((spacingY * this.amount) / -2D) + (spacingY * i) + (spacingY / 2D) + ((random.nextDouble() - 0.5F) * spacingY / 1.5D), (random.nextDouble() - 0.5D));
        }

        return trailData;
    }

    @Override
    public TrailRendererSerializer<?> getSerializer() {
        return TrailRendererSerializers.LIGHTNING;
    }

    public static class Data {

        private Vec3[] offsets;

    }

    public static class Serializer extends TrailRendererSerializer<LightningTrailRenderer> {

        @Override
        public MapCodec<LightningTrailRenderer> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<TrailRenderer<?>, LightningTrailRenderer> builder, HolderLookup.Provider provider) {
            builder.setName("Lightning Trail").setDescription("Renders lightning behind the entity.")
                    .addOptional("amount", TYPE_INT, "Defines how many lightnings will follow the player", 10)
                    .addOptional("area", TYPE_VECTOR3, "Box (in blocks) the lightning spreads within. Defaults to the entity hitbox.", "[0.6, 1.8, 0.6]")
                    .add("render_settings", TYPE_LASER_RENDERER, "The render settings for the lightning.")
                    .addExampleObject(new LightningTrailRenderer(7, Optional.empty(), new LaserRenderer(
                            new LaserRenderer.LaserPart(Color.BLUE, 1F, 0F, null, LaserRenderer.BlendingMode.ADDITIVE),
                            new LaserRenderer.LaserPart(Color.WHITE, 1F, 0F, null, LaserRenderer.BlendingMode.ADDITIVE),
                            1, new Vector2f(2 / 16F, 2 / 16F), 0, 0
                    )));
        }
    }
}
