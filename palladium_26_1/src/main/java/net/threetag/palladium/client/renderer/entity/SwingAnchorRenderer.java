package net.threetag.palladium.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.beam.Beam;
import net.threetag.palladium.client.beam.BeamManager;
import net.threetag.palladium.client.renderer.entity.state.SwingAnchorRenderState;
import net.threetag.palladium.entity.SwingAnchor;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.EntityScaleUtil;
import org.jetbrains.annotations.NotNull;

public class SwingAnchorRenderer extends EntityRenderer<SwingAnchor, SwingAnchorRenderState> {

    private static final double SWEEP_HEIGHT = 35;
    private static final int SWEEP_SEGMENTS = 12;
    private static final double SKY_EXTENSION = 120;

    public SwingAnchorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(SwingAnchorRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(renderState, poseStack, nodeCollector, cameraRenderState);

        if (renderState.owner instanceof AbstractClientPlayer player) {
            poseStack.pushPose();
            var ownerPos = renderState.owner.getPosition(renderState.partialTick);
            var anchorPos = new Vec3(renderState.x, renderState.y, renderState.z);
            var diff = ownerPos.subtract(anchorPos);
            poseStack.translate(diff.x, diff.y, diff.z);
            var beamRenderer = BeamManager.INSTANCE.get(renderState.beamRendererId);

            if (beamRenderer != null) {
                var sizeMultiplier = new Vec2(
                        EntityScaleUtil.getInstance().getModelWidthScale(player, renderState.partialTick),
                        EntityScaleUtil.getInstance().getModelHeightScale(player, renderState.partialTick)
                );
                var context = DataContext.forEntity(player);

                if (renderState.despawning) {
                    float fall = 1F - renderState.opacity;
                    fall = fall * fall;

                    for (Beam beam : beamRenderer.beams()) {
                        var releasePos = renderState.beamAnchors.get(beam);

                        if (releasePos == null) {
                            continue;
                        }

                        Vec3 fromAnchor = releasePos.subtract(anchorPos);
                        double length = fromAnchor.length();
                        if (length < 0.1) {
                            continue;
                        }

                        Vec3 down = fromAnchor.scale(1 / length);
                        Vec3 fallingEnd = anchorPos.add(slerpDirection(down, new Vec3(0, -1, 0), fall).scale(length));

                        beam.submit(context, fallingEnd.subtract(ownerPos),
                                anchorPos.subtract(ownerPos), sizeMultiplier, 1F, renderState.opacity, poseStack, nodeCollector,
                                renderState.lightCoords, player.tickCount, renderState.partialTick);

                        if (!renderState.blockAnchor) {
                            Vec3 sweepDir = slerpDirection(down.scale(-1), new Vec3(0, 1, 0), fall);
                            this.submitSkySweep(context, beam, anchorPos, sweepDir, ownerPos, sizeMultiplier,
                                    renderState.opacity, poseStack, nodeCollector, renderState.lightCoords, player.tickCount, renderState.partialTick);
                        }
                    }
                } else {
                    beamRenderer.submitOnPlayer(player, ownerPos,
                            anchorPos, renderState.shootProgress, renderState.opacity, poseStack, nodeCollector,
                            renderState.lightCoords, renderState.partialTick);

                    if (renderState.shootProgress >= 1F && !renderState.blockAnchor) {
                        Vec3 ropeToAnchor = anchorPos.subtract(ownerPos);
                        Vec3 sweepDir = ropeToAnchor.lengthSqr() > 0.01 ? ropeToAnchor.normalize() : new Vec3(0, 1, 0);

                        for (Beam beam : beamRenderer.beams()) {
                            this.submitSkySweep(context, beam, anchorPos, sweepDir, ownerPos, sizeMultiplier,
                                    renderState.opacity, poseStack, nodeCollector, renderState.lightCoords, player.tickCount, renderState.partialTick);
                        }
                    }
                }
            }

            poseStack.popPose();
        }
    }

    private void submitSkySweep(DataContext context, Beam beam, Vec3 anchorPos, Vec3 startDir, Vec3 ownerPos, Vec2 sizeMultiplier,
                                float opacity, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, int ageInTicks, float partialTick) {
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 point = anchorPos;
        double segmentLength = SWEEP_HEIGHT / SWEEP_SEGMENTS;

        for (int i = 0; i < SWEEP_SEGMENTS; i++) {
            Vec3 dir = slerpDirection(startDir, up, (float) i / (SWEEP_SEGMENTS - 1));
            Vec3 next = point.add(dir.scale(segmentLength));
            beam.submit(context, point.subtract(ownerPos), next.subtract(ownerPos), sizeMultiplier, 1F,
                    opacity, poseStack, nodeCollector, light, ageInTicks, partialTick);
            point = next;
        }

        beam.submit(context, point.subtract(ownerPos), point.subtract(ownerPos).add(0, SKY_EXTENSION, 0), sizeMultiplier, 1F,
                opacity, poseStack, nodeCollector, light, ageInTicks, partialTick);
    }

    private static Vec3 slerpDirection(Vec3 from, Vec3 to, float t) {
        double dot = Mth.clamp(from.dot(to), -1.0, 1.0);
        double angle = Math.acos(dot);

        if (angle < 1.0E-3) {
            return to;
        }

        double sin = Math.sin(angle);
        return from.scale(Math.sin((1 - t) * angle) / sin)
                .add(to.scale(Math.sin(t * angle) / sin))
                .normalize();
    }

    @Override
    public @NotNull SwingAnchorRenderState createRenderState() {
        return new SwingAnchorRenderState();
    }

    @Override
    public void extractRenderState(SwingAnchor entity, SwingAnchorRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.owner = entity.getOwner();
        reusedState.beamRendererId = entity.getBeamRendererId();
        reusedState.partialTick = partialTick;
        reusedState.opacity = entity.getOpacity(partialTick);
        reusedState.shootProgress = entity.getShootProgress(partialTick);
        reusedState.despawning = entity.isMarkedToDespawn();
        reusedState.blockAnchor = entity.isBlockAnchor();
        reusedState.beamAnchors = entity.beamAnchorCache;

        if (reusedState.owner != null) {
            BlockPos ownerPos = reusedState.owner.blockPosition();
            int block = entity.level().getBrightness(LightLayer.BLOCK, ownerPos);
            int sky = entity.level().getBrightness(LightLayer.SKY, ownerPos);
            reusedState.lightCoords = (sky << 20) | (block << 4);
        }

        if (entity.isMarkedToDespawn() && entity.beamAnchorCache.isEmpty() && entity.getOwner() instanceof AbstractClientPlayer player) {
            var beamRenderer = BeamManager.INSTANCE.get(entity.getBeamRendererId());

            if (beamRenderer != null) {
                entity.beamAnchorCache.putAll(beamRenderer.getAnchorPositions(player, partialTick));
            }
        }
    }

    @Override
    protected boolean affectedByCulling(@NotNull SwingAnchor entity) {
        return false;
    }
}
