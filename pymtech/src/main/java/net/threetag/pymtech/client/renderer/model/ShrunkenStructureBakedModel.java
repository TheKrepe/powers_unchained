package net.threetag.pymtech.client.renderer.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ShrunkenStructureBakedModel implements IBakedModel {

    public IBakedModel defaultModel;
    public IBakedModel placeholderModel;

    public ShrunkenStructureBakedModel(IBakedModel defaultModel, IBakedModel placeholderModel) {
        this.defaultModel = defaultModel;
        this.placeholderModel = placeholderModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return defaultModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return defaultModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return defaultModel.isGui3d();
    }

    @Override
    public boolean isSideLit() {
        return this.defaultModel.isSideLit();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return defaultModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return defaultModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return defaultModel.getOverrides();
    }

    @Override
    public boolean doesHandlePerspectives() {
        return false;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
        if (cameraTransformType == ItemCameraTransforms.TransformType.GUI) {
            return this.placeholderModel.handlePerspective(cameraTransformType, mat);
        }
        return defaultModel.handlePerspective(cameraTransformType, mat);
    }
}
