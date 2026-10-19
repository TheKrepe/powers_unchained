package net.threetag.pymtech.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.threetag.pymtech.entity.PymParticleDiskEntity;

public class PymParticleDiskRenderer<T extends PymParticleDiskEntity & IRendersAsItem> extends EntityRenderer<T> {

    private final ItemRenderer itemRenderer;

    public PymParticleDiskRenderer(EntityRendererManager rendererManager, ItemRenderer itemRenderer) {
        super(rendererManager);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();
        matrixStackIn.translate(0, 0.125D, 0);
        matrixStackIn.scale(0.25F, 0.25F, 0.25F);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw) - 90.0F));
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch)));
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees((entityIn.ticksExisted + partialTicks) * 100F));
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));

        this.itemRenderer.renderItem(((IRendersAsItem) entityIn).getItem(), ItemCameraTransforms.TransformType.NONE, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);

        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }
}
