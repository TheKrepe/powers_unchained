package net.threetag.palladium.client.trail;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.renderer.LaserRenderer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import org.joml.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class LightningAuraTrailRenderer extends TrailRenderer<LightningAuraTrailRenderer.Data> {

    public static final MapCodec<LightningAuraTrailRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("amount", 1).forGetter(r -> r.amount),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("branches", 2).forGetter(r -> r.branches),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("segments", 5).forGetter(r -> r.segments),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("growth", 3).forGetter(r -> r.growth),
            LaserRenderer.codec(2).fieldOf("render_settings").forGetter(r -> r.laserRenderer),
            ArcSettings.CODEC.codec().optionalFieldOf("arc_settings", ArcSettings.DEFAULT).forGetter(r -> r.settings)
    ).apply(instance, LightningAuraTrailRenderer::new));

    private final int amount;
    private final int branches;
    private final int segments;
    private final int growth;
    private final ArcSettings settings;
    private final LaserRenderer laserRenderer;

    public LightningAuraTrailRenderer(int amount, int branches, int segments, int growth,
                                      LaserRenderer laserRenderer, ArcSettings settings) {
        this.amount = amount;
        this.branches = branches;
        this.segments = segments;
        this.growth = growth;
        this.laserRenderer = laserRenderer;
        this.settings = settings;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LivingEntityRenderState ownerRenderState,
                       TrailSegment<Data> trailSegment, int packedLight, float partialTick) {
        float progress = this.growth == 0 ? 1F : Mth.clamp((trailSegment.getTickCount() + partialTick) / this.growth, 0F, 1F);
        float opacity = trailSegment.getOpacity() * progress;
        double time = ownerRenderState.ageInTicks + partialTick;

        for (Arc arc : trailSegment.getTrailData().arcs) {
            Vec3 segmentOrigin = trailSegment.getPosition();
            Vec3 currentPosition = new Vec3(ownerRenderState.x, ownerRenderState.y, ownerRenderState.z);
            Vec3 currentStart = attachmentPoint(trailSegment.getDimensions(), arc.seed, ownerRenderState.bodyRot)
                    .add(currentPosition.subtract(segmentOrigin));
            Vec3 mainEnd = arc.targetWorld.subtract(segmentOrigin);
            List<Vec3> mainPoints = createBezierPoints(currentStart, mainEnd, arc.seed, false, time);
            renderPoints(poseStack, submitNodeCollector, mainPoints, progress, opacity, ownerRenderState);
            for (Branch branch : arc.branches) {
                Vec3 branchStart = pointAlongPath(mainPoints, branch.startProgress);
                List<Vec3> branchPoints = createBezierPoints(branchStart, branch.targetWorld.subtract(segmentOrigin),
                        branch.seed, true, time);
                double branchProgress = Mth.clamp(
                        (progress - branch.startProgress - branch.activationDelay)
                                / Math.max(0.08D, 1D - branch.startProgress - branch.activationDelay),
                        0D, 1D);
                if (branchProgress > 0D) {
                    renderPoints(poseStack, submitNodeCollector, branchPoints, (float) branchProgress, opacity, ownerRenderState);
                }
            }
        }
    }

    private void renderPoints(PoseStack poseStack, SubmitNodeCollector collector, List<Vec3> points,
                              float progress, float opacity, LivingEntityRenderState state) {
        List<Vec3> visible = LaserRenderer.partialPoints(points, progress);
        if (visible.size() < 2) {
            return;
        }
        this.laserRenderer.renderPolyline(poseStack, collector, visible, (int) state.ageInTicks, state.partialTick, opacity);
    }

    @Override
    public Data createData(Entity parent) {
        long seed = parent.getId() * 341873128712L + parent.tickCount * 1327217885L;
        RandomSource random = RandomSource.create(seed);
        List<Arc> arcs = new ArrayList<>();
        EntityDimensions dimensions = parent.getDimensions(parent.getPose());
        Vec3 parentPosition = parent.position();
        Vec3 velocity = parent.getDeltaMovement();
        Vec3 forward = velocity.horizontalDistanceSqr() > 1.0E-4D
                ? new Vec3(velocity.x, 0, velocity.z).normalize() : Vec3.ZERO;
        double speed = velocity.horizontalDistance();
        double range = speed >= 0.85D ? this.settings.sprintingRange
                : speed >= 0.42D ? this.settings.runningRange : this.settings.idleRange;

        for (int i = 0; i < this.amount; i++) {
            Vec3 start = attachmentPoint(dimensions, seed + i * 7919L, parent.getYRot());
            Anchor anchor = findAnchor(parent, parentPosition.add(start), forward, range, seed + i * 7919L);
            if (anchor == null) continue;
            List<Vec3> initialPoints = createBezierPoints(start, anchor.position.subtract(parentPosition),
                    seed + i * 7919L, false, 0D);
            List<Branch> branches = new ArrayList<>();

            for (int branch = 0; branch < this.branches; branch++) {
                double branchProgress = this.settings.branchStartMin
                        + random.nextDouble() * (this.settings.branchStartMax - this.settings.branchStartMin);
                Vec3 branchStart = pointAlongPath(initialPoints, branchProgress);
                Anchor branchAnchor = findAnchor(parent, parentPosition.add(branchStart), randomDirection(random),
                        Math.min(5.5D, range * 0.72D), seed + i * 7919L + branch * 997L);
                if (branchAnchor != null) {
                    long branchSeed = seed + i * 7919L + branch * 997L;
                    branches.add(new Branch(branchAnchor.position, branchSeed, branchProgress,
                            this.settings.branchDelayMin
                                    + random.nextDouble() * (this.settings.branchDelayMax - this.settings.branchDelayMin)));
                }
            }

            arcs.add(new Arc(anchor.position, seed + i * 7919L, branches));
        }

        return new Data(arcs);
    }

    private static Vec3 attachmentPoint(EntityDimensions dimensions, long seed, float bodyYaw) {
        double width = dimensions.width();
        double height = dimensions.height();
        int slot = Math.floorMod(seed, 12);
        Vec3 point = switch (slot) {
            case 0 -> new Vec3(-width * .34, height * .84, -width * .05);
            case 1 -> new Vec3(width * .34, height * .84, -width * .05);
            case 2 -> new Vec3(-width * .28, height * .66, width * .04);
            case 3 -> new Vec3(width * .28, height * .66, width * .04);
            case 4 -> new Vec3(-width * .22, height * .48, width * .06);
            case 5 -> new Vec3(width * .22, height * .48, width * .06);
            case 6 -> new Vec3(-width * .18, height * .24, width * .02);
            case 7 -> new Vec3(width * .18, height * .24, width * .02);
            case 8 -> new Vec3(-width * .12, height * .58, -width * .10);
            case 9 -> new Vec3(width * .12, height * .58, -width * .10);
            case 10 -> new Vec3(-width * .06, height * .36, -width * .12);
            default -> new Vec3(width * .06, height * .36, -width * .12);
        };
        double yaw = Math.toRadians(-bodyYaw);
        return new Vec3(point.x * Math.cos(yaw) - point.z * Math.sin(yaw), point.y,
                point.x * Math.sin(yaw) + point.z * Math.cos(yaw));
    }

    private static Vec3 randomDirection(RandomSource random) {
        Vec3 direction = new Vec3(random.nextDouble() - 0.5D, random.nextDouble() * 1.5D - 0.75D, random.nextDouble() - 0.5D);
        return direction.normalize();
    }

    private Anchor findAnchor(Entity entity, Vec3 origin, Vec3 forward, double range, long seed) {
        Anchor best = null;
        RandomSource random = RandomSource.create(seed ^ 0x5DEECE66DL);
        for (int i = 0; i < this.settings.searchDirections; i++) {
            Vec3 direction = randomDirection(random);
            double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
            double reach = Math.max(horizontal * range, Math.abs(direction.y) * (range + .5D));
            if (forward.lengthSqr() > 1.0E-4D && horizontal > .1D) {
                Vec3 planar = new Vec3(direction.x, 0, direction.z).normalize();
                double dot = planar.dot(forward);
                if (dot < -.2D) reach *= this.settings.rearRangeMultiplier;
                else if (Math.abs(dot) < .35D) reach *= this.settings.sideRangeMultiplier;
            }
            Vec3 end = origin.add(direction.scale(reach));
            BlockHitResult hit = entity.level().clip(new ClipContext(origin, end,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            if (hit.getType() != HitResult.Type.BLOCK) continue;
            double distance = origin.distanceTo(hit.getLocation());
            if (distance < this.settings.minimumTargetDistance
                    || entity.level().getBlockState(hit.getBlockPos()).isAir()) continue;
            double normalized = distance / reach;
            double desired = .60D + random.nextDouble() * .30D;
            double score = Math.max(0D, normalized - .35D) * .70D
                    + Math.max(0D, 1D - Math.abs(normalized - desired) / .22D) * .18D
                    + random.nextDouble() * .18D;
            if (best == null || score > best.score) best = new Anchor(hit.getLocation(), score);
        }
        return best;
    }

    private List<Vec3> createBezierPoints(Vec3 start, Vec3 end, long seed, boolean branch, double time) {
        List<Vec3> points = new ArrayList<>();
        Vec3 direction = end.subtract(start);
        double length = direction.length();
        if (length < .1D) return points;
        Vec3 forward = direction.normalize();
        Vec3 side = Math.abs(forward.y) > .9D ? new Vec3(1, 0, 0) : forward.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = side.cross(forward).normalize();
        int count = Math.max(this.segments, branch ? 6 : 10);
        double curve = length * (branch ? .085D : .11D);
        Vec3 controlA = start.add(forward.scale(length * (branch ? .18D : .22D))).add(up.scale(curve));
        Vec3 controlB = end.subtract(forward.scale(length * (branch ? .34D : .42D))).add(up.scale(curve));
        for (int i = 0; i <= count; i++) {
            double t = i / (double) count;
            Vec3 p = cubic(start, controlA, controlB, end, t);
            if (i > 0 && i < count) {
                double wave = Math.sin(Math.PI * t);
                double sideNoise = signedNoise(seed, 131L + i);
                double upNoise = signedNoise(seed, 173L + i);
                double crawl = time * this.settings.jitterSpeed * (branch ? 1.26D : 1D);
                double sideWave = Math.sin(crawl + signedNoise(seed, 211L + i) * 5D);
                double upWave = Math.cos(crawl * 1.23D + signedNoise(seed, 257L + i) * 5D);
                p = p.add(side.scale((sideNoise * this.settings.jitterAmount * .82D
                        + sideWave * this.settings.jitterAmount * .33D
                        + ((i & 1) == 0 ? .032D : -.032D)) * wave));
                p = p.add(up.scale((upNoise * this.settings.jitterAmount * .73D
                        + upWave * this.settings.jitterAmount * .25D) * wave));
            }
            points.add(p);
        }
        return points;
    }

    private static Vec3 cubic(Vec3 a, Vec3 b, Vec3 c, Vec3 d, double t) {
        double n = 1D - t;
        return a.scale(n*n*n).add(b.scale(3D*n*n*t)).add(c.scale(3D*n*t*t)).add(d.scale(t*t*t));
    }

    private static Vec3 pointAlongPath(List<Vec3> points, double progress) {
        if (points.isEmpty()) return Vec3.ZERO;
        if (points.size() == 1) return points.getFirst();
        double total = 0D;
        for (int i = 1; i < points.size(); i++) total += points.get(i - 1).distanceTo(points.get(i));
        double target = total * Mth.clamp(progress, 0D, 1D);
        double traversed = 0D;
        for (int i = 1; i < points.size(); i++) {
            Vec3 from = points.get(i - 1);
            Vec3 to = points.get(i);
            double length = from.distanceTo(to);
            if (traversed + length >= target) {
                return from.lerp(to, length <= 1.0E-6D ? 0D : (target - traversed) / length);
            }
            traversed += length;
        }
        return points.getLast();
    }

    private static double signedNoise(long seed, long salt) {
        long value = seed ^ (salt * 0x9E3779B97F4A7C15L);
        value ^= value >>> 30; value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27; value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return ((value >>> 11) * 0x1.0p-53) * 2D - 1D;
    }

    private record Anchor(Vec3 position, double score) {
    }

    @Override
    public int defaultTickDelay() {
        return 2;
    }

    @Override
    public TrailRendererSerializer<?> getSerializer() {
        return TrailRendererSerializers.LIGHTNING_AURA;
    }

    public record Data(List<Arc> arcs) {
    }

    public record ArcSettings(
            float idleRange,
            float runningRange,
            float sprintingRange,
            float sideRangeMultiplier,
            float rearRangeMultiplier,
            float branchStartMin,
            float branchStartMax,
            float branchDelayMin,
            float branchDelayMax,
            float jitterAmount,
            float jitterSpeed,
            int searchDirections,
            float minimumTargetDistance) {

        private static final ArcSettings DEFAULT = new ArcSettings(
                4.5F, 8F, 11F, 0.70F, 1.20F,
                0.22F, 0.60F, 0.08F, 0.30F,
                0.055F, 0.27F, 64, 0.75F);

        private static final com.mojang.serialization.MapCodec<ArcSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("idle_range", DEFAULT.idleRange).forGetter(ArcSettings::idleRange),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("running_range", DEFAULT.runningRange).forGetter(ArcSettings::runningRange),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("sprinting_range", DEFAULT.sprintingRange).forGetter(ArcSettings::sprintingRange),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("side_range_multiplier", DEFAULT.sideRangeMultiplier).forGetter(ArcSettings::sideRangeMultiplier),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("rear_range_multiplier", DEFAULT.rearRangeMultiplier).forGetter(ArcSettings::rearRangeMultiplier),
                ExtraCodecs.floatRange(0F, 1F).optionalFieldOf("branch_start_min", DEFAULT.branchStartMin).forGetter(ArcSettings::branchStartMin),
                ExtraCodecs.floatRange(0F, 1F).optionalFieldOf("branch_start_max", DEFAULT.branchStartMax).forGetter(ArcSettings::branchStartMax),
                ExtraCodecs.floatRange(0F, 1F).optionalFieldOf("branch_delay_min", DEFAULT.branchDelayMin).forGetter(ArcSettings::branchDelayMin),
                ExtraCodecs.floatRange(0F, 1F).optionalFieldOf("branch_delay_max", DEFAULT.branchDelayMax).forGetter(ArcSettings::branchDelayMax),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("jitter_amount", DEFAULT.jitterAmount).forGetter(ArcSettings::jitterAmount),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("jitter_speed", DEFAULT.jitterSpeed).forGetter(ArcSettings::jitterSpeed),
                ExtraCodecs.intRange(1, 256).optionalFieldOf("search_directions", DEFAULT.searchDirections).forGetter(ArcSettings::searchDirections),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("minimum_target_distance", DEFAULT.minimumTargetDistance).forGetter(ArcSettings::minimumTargetDistance)
        ).apply(instance, ArcSettings::new));
    }

    private record Arc(Vec3 targetWorld, long seed, List<Branch> branches) {
    }

    private record Branch(Vec3 targetWorld, long seed, double startProgress, double activationDelay) {
    }

    public static class Serializer extends TrailRendererSerializer<LightningAuraTrailRenderer> {

        @Override
        public MapCodec<LightningAuraTrailRenderer> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<TrailRenderer<?>, LightningAuraTrailRenderer> builder,
                                     HolderLookup.Provider provider) {
            builder.setName("Lightning Aura Trail")
                    .setDescription("Renders independently growing lightning arcs with branches around an entity (inspired by DCEU Flash).")
                    .addOptional("amount", TYPE_INT, "Arcs created per timer event", 1)
                    .addOptional("branches", TYPE_INT, "Branches created for each arc", 2)
                    .addOptional("segments", TYPE_INT, "Segments in each arc", 5)
                    .addOptional("growth", TYPE_INT, "Ticks required for an arc to fully grow", 3)
                    .add("render_settings", TYPE_LASER_RENDERER, "The render settings for the arcs.")
                    .addOptional("arc_settings", TYPE_ANY, "Arc range, branch timing, jitter, and block-target search settings", ArcSettings.DEFAULT)
                    .addExampleObject(new LightningAuraTrailRenderer(1, 1, 5, 3, new LaserRenderer(
                            new LaserRenderer.LaserPart(new Color(0xFF7A1A), 0.75F, 0F, null, LaserRenderer.BlendingMode.ADDITIVE),
                            new LaserRenderer.LaserPart(Color.WHITE, 1F, 0F, null, LaserRenderer.BlendingMode.ADDITIVE),
                            2, new Vector2f(1 / 16F, 1 / 16F), 0F, 0F
                    ), ArcSettings.DEFAULT))
                    .addExampleObject(new LightningAuraTrailRenderer(2, 3, 8, 4, new LaserRenderer(
                            new LaserRenderer.LaserPart(new Color(0x00AAFF), 0.8F, 0.5F, new LaserRenderer.Pulse(0.25F, 2F), LaserRenderer.BlendingMode.ADDITIVE),
                            new LaserRenderer.LaserPart(Color.WHITE, 1F, 0F, new LaserRenderer.Pulse(0.1F, 1F), LaserRenderer.BlendingMode.ADDITIVE),
                            3, new Vector2f(2 / 16F, 2 / 16F), 0F, 4F
                    ), new ArcSettings(4.5F, 8F, 11F, 0.7F, 1.2F, 0.22F, 0.6F, 0.08F, 0.3F, 0.055F, 0.27F, 48, 0.75F)));
        }
    }
}
