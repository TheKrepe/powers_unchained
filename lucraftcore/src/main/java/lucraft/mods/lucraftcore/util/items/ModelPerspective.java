package lucraft.mods.lucraftcore.util.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

public class ModelPerspective implements IBakedModel {

    public IBakedModel defaultModel;
    public Pair<ItemCameraTransforms.TransformType, ? extends IBakedModel>[] variants;

    public ModelPerspective(IBakedModel defaultModel, Pair<ItemCameraTransforms.TransformType, ? extends IBakedModel>... variants) {
        this.defaultModel = defaultModel;
        this.variants = variants;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
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
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        for (Pair<ItemCameraTransforms.TransformType, ? extends IBakedModel> pairs : this.variants) {
            if (pairs.getLeft() == cameraTransformType) {
                return pairs.getRight().handlePerspective(cameraTransformType);
            }
        }
        return defaultModel.handlePerspective(cameraTransformType);
    }
}
