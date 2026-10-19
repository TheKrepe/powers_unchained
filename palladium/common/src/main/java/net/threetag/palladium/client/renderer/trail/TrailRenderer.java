package net.threetag.palladium.client.renderer.trail;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.renderer.DynamicColor;
import net.threetag.palladium.client.renderer.entity.TrailSegmentEntityRenderer;
import net.threetag.palladium.entity.PalladiumEntityExtension;
import net.threetag.palladium.entity.TrailSegmentEntity;
import net.threetag.palladium.util.SizeUtil;

import java.util.ArrayList;

public abstract class TrailRenderer<T extends TrailRenderer.SegmentCache> {

    @Environment(EnvType.CLIENT)
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TrailSegmentEntityRenderer trailRenderer, Entity livingEntity, TrailSegmentEntity<T> segment, float partialTick, float entityYaw) {

    }

    public SegmentCache createCache() {
        return new SegmentCache();
    }

    public DynamicColor getColor() {
        return DynamicColor.WHITE;
    }

    public abstract float getSpacing();

    public abstract int getLifetime();

    public abstract boolean requiresMovement();

    @Environment(EnvType.CLIENT)
    public static void renderTrailsForEntity(Entity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!(entity instanceof PalladiumEntityExtension ext)) return;

        float widthScale = SizeUtil.getInstance().getModelWidthScale(entity, partialTicks);
        float heightScale = SizeUtil.getInstance().getModelHeightScale(entity, partialTicks);

        if (widthScale == 0F || heightScale == 0F) {
            return;
        }

        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        poseStack.pushPose();
        poseStack.scale(1F / widthScale, 1F / heightScale, 1F / widthScale);

        for (var segments : new ArrayList<>(ext.palladium$getTrailHandler().getTrails().values())) {
            for (TrailSegmentEntity<?> segment : new ArrayList<>(segments)) {
                if (!segment.isAlive()) continue;

                var renderer = dispatcher.getRenderer(segment);
                if (renderer instanceof TrailSegmentEntityRenderer segmentRenderer) {
                    Vec3 offset = segment.position().subtract(entity.getPosition(partialTicks));
                    poseStack.pushPose();
                    poseStack.translate(offset.x, offset.y, offset.z);
                    segmentRenderer.render(segment, segment.getYRot(), partialTicks, poseStack, buffer, packedLight);
                    poseStack.popPose();
                }
            }
        }

        poseStack.popPose();
    }

    public static class SegmentCache {

    }

}
