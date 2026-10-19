package net.threetag.pymtech.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.threetag.pymtech.client.renderer.item.ShrunkenStructureItemRenderer;
import net.threetag.pymtech.entity.ShrunkenStructureEntity;
import net.threetag.pymtech.item.ShrunkenStructureItem;
import net.threetag.pymtech.storage.ShrunkenStructureData;

public class ShrunkenStructureRenderer<T extends ShrunkenStructureEntity> extends EntityRenderer<T> {

    public ShrunkenStructureRenderer(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        ShrunkenStructureData shrunkenStructure = ShrunkenStructureItem.get(entityIn.world, entityIn.getItem());
        matrixStackIn.push();

        float scale = 1F;
        if (shrunkenStructure != null) {
            scale = 1F / Math.max(shrunkenStructure.getSize().getX(), Math.max(shrunkenStructure.getSize().getY(), shrunkenStructure.getSize().getZ())) * 2F;
        }
        ShrunkenStructureItemRenderer.render(shrunkenStructure, matrixStackIn, entityIn.getItem().getOrCreateTag().getInt("ShrunkenStructure"), scale * 3);
        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
        return null;
    }

}
