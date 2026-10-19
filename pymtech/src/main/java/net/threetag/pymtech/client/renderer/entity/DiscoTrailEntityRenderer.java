package net.threetag.pymtech.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PreRenderCallbackBridge;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.client.renderer.entity.layers.DiscoTrailLayerRenderer;
import net.threetag.pymtech.entity.DiscoTrailEntity;
import net.threetag.pymtech.util.PTRenderTypes;

public class DiscoTrailEntityRenderer extends EntityRenderer<DiscoTrailEntity> {

    public static final ResourceLocation WHITE_TEXTURE = new ResourceLocation(PymTech.MODID, "textures/misc/white.png");

    public DiscoTrailEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(DiscoTrailEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entityIn.parent == null)
            return;

        if (entityIn.renderer == null) {
            if (!DiscoTrailLayerRenderer.TO_EDIT.containsKey(entityIn.parent.getUniqueID())) {
                DiscoTrailLayerRenderer.TO_EDIT.put(entityIn.parent.getUniqueID(), Lists.newArrayList(entityIn));
            } else if (DiscoTrailLayerRenderer.TO_EDIT.containsKey(entityIn.parent.getUniqueID()) && DiscoTrailLayerRenderer.TO_EDIT.get(entityIn.parent.getUniqueID()) != null && !DiscoTrailLayerRenderer.TO_EDIT.get(entityIn.parent.getUniqueID()).contains(entityIn)) {
                DiscoTrailLayerRenderer.TO_EDIT.get(entityIn.parent.getUniqueID()).add(entityIn);
            }

            return;
        }

        matrixStackIn.push();

        float alpha = 1F - ((float) entityIn.ticksExisted / (float) entityIn.lifeTime);
        PreRenderCallbackBridge.preRenderCallback(entityIn.renderer, entityIn.parent, matrixStackIn, partialTicks);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
        matrixStackIn.translate(0, -1.4F, 0);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entityIn.renderYawOffset));

        // TODO Re-add outlines/polygon mode
        IVertexBuilder builder = bufferIn.getBuffer(PTRenderTypes.DISCO_TRAIL);
//        RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        entityIn.renderer.getEntityModel().render(matrixStackIn, builder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, alpha / 1.4F);
//        RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

//        entityIn.renderer.getEntityModel().render(matrixStackIn, builder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, alpha / 2.4F);
;
        matrixStackIn.pop();

        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(DiscoTrailEntity entity) {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }
}
