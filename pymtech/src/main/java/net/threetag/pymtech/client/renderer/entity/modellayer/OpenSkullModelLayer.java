package net.threetag.pymtech.client.renderer.entity.modellayer;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.threetag.pymtech.client.renderer.entity.model.BrainHeadModel;
import net.threetag.pymtech.client.renderer.entity.model.OpenSkullModel;
import net.threetag.threecore.client.renderer.entity.modellayer.IModelLayer;
import net.threetag.threecore.client.renderer.entity.modellayer.IModelLayerContext;
import net.threetag.threecore.client.renderer.entity.modellayer.ModelLayerManager;
import net.threetag.threecore.client.renderer.entity.modellayer.predicates.IModelLayerPredicate;
import net.threetag.threecore.util.RenderUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class OpenSkullModelLayer implements IModelLayer {

    public final List<IModelLayerPredicate> glowPredicates;
    public final List<IModelLayerPredicate> predicateList = Lists.newLinkedList();

    public OpenSkullModelLayer() {
        this.glowPredicates = Collections.emptyList();
    }

    public OpenSkullModelLayer(List<IModelLayerPredicate> glowPredicates) {
        this.glowPredicates = glowPredicates;
    }

    @Override
    public void render(IModelLayerContext context, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int packedLight, @Nullable IEntityRenderer<? extends Entity, ? extends EntityModel<?>> entityRenderer, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityRenderer != null && entityRenderer.getEntityModel() instanceof BipedModel<?> && context.getAsEntity() instanceof AbstractClientPlayerEntity) {
            matrixStack.push();
            ((BipedModel<?>) entityRenderer.getEntityModel()).bipedHead.translateRotate(matrixStack);
            ResourceLocation texture = ((AbstractClientPlayerEntity) context.getAsEntity()).getLocationSkin();
            OpenSkullModel.INSTANCE.render(matrixStack, renderTypeBuffer.getBuffer(ModelLayerManager.arePredicatesFulFilled(this.glowPredicates, context) ? RenderUtil.RenderTypes.getGlowing(texture) : RenderUtil.RenderTypes.getEntityTranslucentCull(texture)), packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
            BrainHeadModel.INSTANCE.render(matrixStack, renderTypeBuffer.getBuffer(ModelLayerManager.arePredicatesFulFilled(this.glowPredicates, context) ? RenderUtil.RenderTypes.getGlowing(BrainHeadModel.TEXTURE) : RenderUtil.RenderTypes.getEntityTranslucentCull(BrainHeadModel.TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
            matrixStack.pop();
        }
    }

    @Override
    public boolean isActive(IModelLayerContext context) {
        return ModelLayerManager.arePredicatesFulFilled(this.predicateList, context);
    }

    @Override
    public OpenSkullModelLayer addPredicate(IModelLayerPredicate predicate) {
        this.predicateList.add(predicate);
        return this;
    }

    @Override
    public void postRotationAnglesCallback(LivingRenderer<? extends LivingEntity, ? extends EntityModel> renderer, LivingEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entityIn instanceof AbstractClientPlayerEntity) {
            ((BipedModel) renderer.getEntityModel()).bipedHead.showModel = false;
            ((BipedModel) renderer.getEntityModel()).bipedHeadwear.showModel = false;
        }
    }

    public static OpenSkullModelLayer parse(JsonObject json) {
        List<IModelLayerPredicate> glowPredicates = Lists.newLinkedList();

        if (JSONUtils.hasField(json, "glow")) {
            JsonElement glowJson = json.get("glow");

            if (glowJson.isJsonPrimitive() && glowJson.getAsBoolean()) {
                glowPredicates.add((c) -> true);
            } else {
                JsonArray predicateArray = JSONUtils.getJsonArray(json, "glow");
                for (int i = 0; i < predicateArray.size(); i++) {
                    IModelLayerPredicate predicate = ModelLayerManager.parsePredicate(predicateArray.get(i).getAsJsonObject());
                    if (predicate != null)
                        glowPredicates.add(predicate);
                }
            }
        } else {
            glowPredicates.add((c) -> false);
        }

        return new OpenSkullModelLayer(glowPredicates);
    }
}
