package net.threetag.palladium.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.beam.BeamManager;
import net.threetag.palladium.client.renderer.entity.state.GrappleHookRenderState;
import net.threetag.palladium.entity.GrappleHook;
import org.jetbrains.annotations.NotNull;

public class GrappleHookRenderer extends EntityRenderer<GrappleHook, GrappleHookRenderState> {

    public GrappleHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(GrappleHookRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(renderState, poseStack, nodeCollector, cameraRenderState);

        if (!(renderState.owner instanceof AbstractClientPlayer player) || renderState.beamRendererId == null) {
            return;
        }

        var beamRenderer = BeamManager.INSTANCE.get(renderState.beamRendererId);
        if (beamRenderer == null) {
            return;
        }

        var ownerPos = renderState.owner.getPosition(renderState.partialTick);
        var hookPos = new Vec3(renderState.x, renderState.y, renderState.z);
        var diff = ownerPos.subtract(hookPos);

        poseStack.pushPose();
        poseStack.translate(diff.x, diff.y, diff.z);
        beamRenderer.submitOnPlayer(player, ownerPos, hookPos, 1F, 1F, poseStack, nodeCollector, renderState.lightCoords, renderState.partialTick);
        poseStack.popPose();
    }

    @Override
    public @NotNull GrappleHookRenderState createRenderState() {
        return new GrappleHookRenderState();
    }

    @Override
    public void extractRenderState(GrappleHook entity, GrappleHookRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.owner = entity.getOwnerLiving();
        reusedState.beamRendererId = entity.getBeamRendererId();
        reusedState.partialTick = partialTick;

        if (reusedState.owner != null) {
            BlockPos ownerPos = reusedState.owner.blockPosition();
            int block = entity.level().getBrightness(LightLayer.BLOCK, ownerPos);
            int sky = entity.level().getBrightness(LightLayer.SKY, ownerPos);
            reusedState.lightCoords = (sky << 20) | (block << 4);
        }
    }

    @Override
    protected boolean affectedByCulling(@NotNull GrappleHook entity) {
        return false;
    }
}
