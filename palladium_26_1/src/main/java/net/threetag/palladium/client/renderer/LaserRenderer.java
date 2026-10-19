package net.threetag.palladium.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record LaserRenderer(net.threetag.palladium.client.renderer.LaserRenderer.LaserPart glow,
                            net.threetag.palladium.client.renderer.LaserRenderer.LaserPart core,
                            int bloom, Vector2f size, float rotation,
                            float rotationSpeed) {

    public static final Codec<Vector2f> SIZE_CODEC = Codec.either(PalladiumCodecs.VOXEL_VECTOR_2F, PalladiumCodecs.NON_NEGATIVE_VOXEL_FLOAT).xmap(
            either -> either.map(vector2f -> vector2f, aFloat -> new Vector2f(aFloat, aFloat)),
            vector2f -> vector2f.x == vector2f.y ? Either.right(vector2f.x) : Either.left(vector2f));

    public static Codec<LaserRenderer> codec(int defaultBloom) {
        return RecordCodecBuilder.create(instance -> instance.group(
                LaserPart.CODEC.optionalFieldOf("glow", LaserPart.DEFAULT).forGetter(LaserRenderer::glow),
                LaserPart.CODEC.optionalFieldOf("core", LaserPart.DEFAULT).forGetter(LaserRenderer::core),
                ExtraCodecs.intRange(0, 10).optionalFieldOf("bloom", defaultBloom).forGetter(LaserRenderer::bloom),
                SIZE_CODEC.optionalFieldOf("size", new Vector2f(1 / 16F, 1 / 16F)).forGetter(LaserRenderer::size),
                ExtraCodecs.floatRange(0F, 360F).optionalFieldOf("rotation", 0F).forGetter(LaserRenderer::rotation),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("rotation_speed", 0F).forGetter(LaserRenderer::rotationSpeed)
        ).apply(instance, LaserRenderer::new));
    }

    public void face(PoseStack poseStack, Vec3 origin, Vec3 target) {
        RenderUtil.faceVec(poseStack, origin, target);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
    }

    public void faceAndRender(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 origin, Vec3 target, int ticks, float partialTick) {
        this.faceAndRender(poseStack, submitNodeCollector, origin, target, ticks, partialTick, 1F, 1F, Vec2.ONE);
    }

    public void faceAndRender(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 origin, Vec3 target, int ticks, float partialTick, float lengthMultiplier, float opacityMultiplier) {
        this.faceAndRender(poseStack, submitNodeCollector, origin, target, ticks, partialTick, lengthMultiplier, opacityMultiplier, Vec2.ONE);
    }

    public void faceAndRender(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 origin, Vec3 target, int ticks, float partialTick, float lengthMultiplier, float opacityMultiplier, float sizeMultiplier) {
        this.faceAndRender(poseStack, submitNodeCollector, origin, target, ticks, partialTick, lengthMultiplier, opacityMultiplier, new Vec2(sizeMultiplier, sizeMultiplier));
    }

    public void faceAndRender(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 origin, Vec3 target, int ticks, float partialTick, float lengthMultiplier, float opacityMultiplier, Vec2 sizeMultiplier) {
        poseStack.pushPose();
        this.face(poseStack, origin, target);
        this.render(poseStack, submitNodeCollector, ticks, origin.distanceTo(target) * lengthMultiplier, partialTick, opacityMultiplier, sizeMultiplier);
        poseStack.popPose();
    }

    public void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int ticks, double length, float partialTick, float opacityMultiplier, Vec2 sizeMultiplier) {
        var rot = this.rotation;

        if (this.rotationSpeed > 0F) {
            rot += (ticks + partialTick) * rotationSpeed;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rot % 360F));

        var size = new Vector2f(this.size).mul(sizeMultiplier.x, sizeMultiplier.y).mul(this.core.getPulseScale(ticks + partialTick));
        AABB box = new AABB(-size.x / 2F, 0, -size.y / 2F, size.x / 2F, length, size.y / 2F);

        var coreColor = computeColor(this.core, ticks, partialTick);
        var r = coreColor.getRed() / 255F;
        var g = coreColor.getGreen() / 255F;
        var b = coreColor.getBlue() / 255F;

        if (this.core.opacity > 0F) {
            RenderUtil.submitFilledBox(poseStack, this.core.renderType(), submitNodeCollector, box, r, g, b, this.core.opacity * opacityMultiplier, LightCoordsUtil.FULL_SKY);
        }

        var glowColor = computeColor(this.glow, ticks, partialTick);
        r = glowColor.getRed() / 255F;
        g = glowColor.getGreen() / 255F;
        b = glowColor.getBlue() / 255F;

        if (this.glow.opacity > 0F) {
            float pulse = this.glow.getPulseScale(ticks + partialTick);
            for (int i = 0; i < this.bloom + 1; i++) {
                RenderUtil.submitFilledBox(poseStack, this.glow.renderType(), submitNodeCollector, box.inflate(i * 0.5F * 0.0625F * pulse), r, g, b, (1F / i / 2) * this.glow.opacity * opacityMultiplier, LightCoordsUtil.FULL_SKY);
            }
        }

        poseStack.popPose();
    }

    public void renderPolyline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, List<Vec3> points, int ticks, float partialTick, float opacityMultiplier) {
        this.renderPolyline(poseStack, submitNodeCollector, points, ticks, partialTick, opacityMultiplier, 1F);
    }

    public void renderPolyline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, List<Vec3> points, int ticks, float partialTick, float opacityMultiplier, float widthMultiplier) {
        if (opacityMultiplier <= 0F) {
            return;
        }
        float[] opacities = new float[points.size()];
        Arrays.fill(opacities, opacityMultiplier);
        this.renderPolyline(poseStack, submitNodeCollector, points, ticks, partialTick, opacities, widthMultiplier);
    }

    public void renderPolyline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, List<Vec3> points, int ticks, float partialTick, float[] opacities, float widthMultiplier) {
        if (points.size() < 2) {
            return;
        }

        float time = ticks + partialTick;

        Matrix4f inverse = new Matrix4f(poseStack.last().pose()).invert();
        Vector3f cameraModel = inverse.transformPosition(new Vector3f(0F, 0F, 0F));
        Vec3 camera = new Vec3(cameraModel.x(), cameraModel.y(), cameraModel.z());

        Vec3[] sides = new Vec3[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Vec3 tangent = tangentAt(points, i);
            Vec3 view = camera.subtract(points.get(i));
            double viewLength = view.length();
            view = viewLength < 1.0E-5D ? new Vec3(0, 0, 1) : view.scale(1D / viewLength);
            Vec3 side = tangent.cross(view);
            double sideLength = side.length();
            sides[i] = sideLength < 1.0E-5D ? Vec3.ZERO : side.scale(1D / sideLength);
        }

        float baseHalfWidth = this.size.x * 0.5F * widthMultiplier;

        Color coreColor = computeColor(this.core, ticks, partialTick);
        if (this.core.opacity > 0F) {
            submitRibbon(poseStack, submitNodeCollector, this.core.renderType(), points, sides, baseHalfWidth * this.core.getPulseScale(time),
                    coreColor.getRed() / 255F, coreColor.getGreen() / 255F, coreColor.getBlue() / 255F,
                    this.core.opacity, opacities);
        }

        if (this.glow.opacity > 0F) {
            float pulse = this.glow.getPulseScale(time);
            Color glowColor = computeColor(this.glow, ticks, partialTick);
            float r = glowColor.getRed() / 255F;
            float g = glowColor.getGreen() / 255F;
            float b = glowColor.getBlue() / 255F;
            for (int i = 0; i < this.bloom + 1; i++) {
                float halfWidth = baseHalfWidth + i * 0.5F * 0.0625F * pulse;
                float alpha = (i == 0 ? 1F : 1F / i / 2F) * this.glow.opacity;
                submitRibbon(poseStack, submitNodeCollector, this.glow.renderType(), points, sides, halfWidth, r, g, b, alpha, opacities);
            }
        }
    }

    private static void submitRibbon(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, List<Vec3> points, Vec3[] sides, float halfWidth, float red, float green, float blue, float alpha, float[] opacities) {
        if (alpha <= 0F) {
            return;
        }
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            for (int i = 0; i < points.size() - 1; i++) {
                Vec3 p0 = points.get(i);
                Vec3 p1 = points.get(i + 1);
                Vec3 s0 = sides[i].scale(halfWidth);
                Vec3 s1 = sides[i + 1].scale(halfWidth);
                float a0 = alpha * opacities[i];
                float a1 = alpha * opacities[i + 1];
                vertexConsumer.addVertex(pose, (float) (p0.x + s0.x), (float) (p0.y + s0.y), (float) (p0.z + s0.z)).setColor(red, green, blue, a0).setLight(LightCoordsUtil.FULL_SKY);
                vertexConsumer.addVertex(pose, (float) (p1.x + s1.x), (float) (p1.y + s1.y), (float) (p1.z + s1.z)).setColor(red, green, blue, a1).setLight(LightCoordsUtil.FULL_SKY);
                vertexConsumer.addVertex(pose, (float) (p1.x - s1.x), (float) (p1.y - s1.y), (float) (p1.z - s1.z)).setColor(red, green, blue, a1).setLight(LightCoordsUtil.FULL_SKY);
                vertexConsumer.addVertex(pose, (float) (p0.x - s0.x), (float) (p0.y - s0.y), (float) (p0.z - s0.z)).setColor(red, green, blue, a0).setLight(LightCoordsUtil.FULL_SKY);
            }
        });
    }

    private static Vec3 tangentAt(List<Vec3> points, int i) {
        Vec3 tangent;
        if (i == 0) {
            tangent = points.get(1).subtract(points.get(0));
        } else if (i == points.size() - 1) {
            tangent = points.get(i).subtract(points.get(i - 1));
        } else {
            Vec3 incoming = points.get(i).subtract(points.get(i - 1));
            Vec3 outgoing = points.get(i + 1).subtract(points.get(i));
            double inLength = incoming.length();
            double outLength = outgoing.length();
            incoming = inLength < 1.0E-5D ? Vec3.ZERO : incoming.scale(1D / inLength);
            outgoing = outLength < 1.0E-5D ? Vec3.ZERO : outgoing.scale(1D / outLength);
            tangent = incoming.add(outgoing);
        }
        double length = tangent.length();
        return length < 1.0E-5D ? new Vec3(0, 1, 0) : tangent.scale(1D / length);
    }

    public static List<Vec3> partialPoints(List<Vec3> points, float progress) {
        int segments = points.size() - 1;
        if (segments < 1) {
            return List.of();
        }
        if (progress >= 1F) {
            return points;
        }
        float exact = segments * Math.max(0F, progress);
        int full = (int) Math.floor(exact);
        List<Vec3> result = new ArrayList<>();
        for (int i = 0; i <= Math.min(full, segments); i++) {
            result.add(points.get(i));
        }
        float fraction = exact - full;
        if (full < segments && fraction > 1.0E-3F) {
            result.add(points.get(full).lerp(points.get(full + 1), fraction));
        }
        return result;
    }

    private static Color computeColor(LaserPart part, int ticks, float partialTick) {
        if (part.rainbow > 0F) {
            int rate = Math.max((int) (25 * (1F - part.rainbow)), 1);
            int l = ticks / rate;
            int m = DyeColor.values().length;
            int n = l % m;
            int o = (l + 1) % m;
            float h = ((float) (ticks % rate) + partialTick) / rate;
            int p = ColorLerper.Type.SHEEP.getColor(DyeColor.byId(n));
            int q = ColorLerper.Type.SHEEP.getColor(DyeColor.byId(o));
            return new Color(ARGB.srgbLerp(h, p, q));
        } else {
            return part.color;
        }
    }

    public record LaserPart(Color color, float opacity, float rainbow, @Nullable Pulse pulse, BlendingMode blendingMode) {

        public static final LaserPart DEFAULT = new LaserPart(Color.WHITE, 1F, 0F, null, BlendingMode.ADDITIVE);

        public RenderType renderType() {
            return this.blendingMode.getRenderType();
        }

        private static final Codec<LaserPart> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PalladiumCodecs.COLOR_CODEC.optionalFieldOf("color", Color.WHITE).forGetter(LaserPart::color),
                ExtraCodecs.floatRange(0F, 1F).optionalFieldOf("opacity", 1F).forGetter(LaserPart::opacity),
                PalladiumCodecs.FLOAT_OR_BOOLEAN_CODEC.optionalFieldOf("rainbow", 0F).forGetter(LaserPart::rainbow),
                Pulse.CODEC.optionalFieldOf("pulse").forGetter(p -> Optional.ofNullable(p.pulse())),
                BlendingMode.CODEC.optionalFieldOf("blending_mode", BlendingMode.ADDITIVE).forGetter(LaserPart::blendingMode)
        ).apply(instance, (color, opacity, rainbow, pulse, blendingMode) -> new LaserPart(color, opacity, rainbow, pulse.orElse(null), blendingMode)));

        public static final Codec<LaserPart> CODEC = Codec.either(PalladiumCodecs.COLOR_CODEC, DIRECT_CODEC).xmap(colorLaserPartEither ->
                        colorLaserPartEither.map(color1 -> new LaserPart(color1, 1F, 0F, null, BlendingMode.ADDITIVE), laserPart -> laserPart),
                laserPart -> laserPart.opacity == 1F && laserPart.rainbow == 0F && laserPart.pulse == null && laserPart.blendingMode == BlendingMode.ADDITIVE ?
                        Either.left(laserPart.color()) :
                        Either.right(laserPart));

        public float getPulseScale(float time) {
            return this.pulse != null ? 1F + (this.pulse.scale() * Mth.sin(time * this.pulse.frequency())) : 1F;
        }

    }

    public record Pulse(float scale, float frequency) {

        public static final Codec<Pulse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("scale", 1F).forGetter(Pulse::scale),
                ExtraCodecs.floatRange(0F, 10F).optionalFieldOf("frequency", 1F).forGetter(Pulse::frequency)
        ).apply(instance, Pulse::new));

    }

    public enum BlendingMode implements StringRepresentable {
        ADDITIVE("additive", PalladiumRenderTypes.LASER),
        TRANSLUCENT("translucent", PalladiumRenderTypes.LASER_TRANSLUCENT);

        public static final Codec<BlendingMode> CODEC = StringRepresentable.fromEnum(BlendingMode::values);

        private final String name;
        private final RenderType renderType;

        BlendingMode(String name, RenderType renderType) {
            this.name = name;
            this.renderType = renderType;
        }

        public RenderType getRenderType() {
            return this.renderType;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
