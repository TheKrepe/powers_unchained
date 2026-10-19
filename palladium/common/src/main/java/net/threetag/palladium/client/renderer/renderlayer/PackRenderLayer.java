package net.threetag.palladium.client.renderer.renderlayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.client.dynamictexture.DynamicModelLayerLocation;
import net.threetag.palladium.client.dynamictexture.DynamicTexture;
import net.threetag.palladium.client.dynamictexture.DynamicTextureManager;
import net.threetag.palladium.client.model.ExtraAnimatedModel;
import net.threetag.palladium.client.renderer.DynamicColor;
import net.threetag.palladium.condition.Condition;
import net.threetag.palladium.condition.ConditionEnvironment;
import net.threetag.palladium.condition.ConditionSerializer;
import net.threetag.palladium.condition.FalseCondition;
import net.threetag.palladium.util.SkinTypedValue;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.json.GsonUtil;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PackRenderLayer extends AbstractPackRenderLayer {

    private final SkinTypedValue<ModelTypes.Model> modelLookup;
    private final SkinTypedValue<ModelCache> model;
    private final SkinTypedValue<DynamicTexture> texture;
    private final RenderTypeFunction renderType;
    private final DynamicColor tint;
    private final List<Condition> enchantmentGlint;
    public final boolean renderFullModelInFirstPerson;
    public final Vector3f firstPersonOffset;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PackRenderLayer(SkinTypedValue<ModelTypes.Model> model, SkinTypedValue<DynamicModelLayerLocation> modelLayerLocation, SkinTypedValue<DynamicTexture> texture, RenderTypeFunction renderType, DynamicColor tint, List<Condition> enchantmentGlint, boolean renderFullModelInFirstPerson, Vector3f firstPersonOffset) {
        this.modelLookup = model;
        this.model = new SkinTypedValue(new ModelCache(modelLayerLocation.normal()), new ModelCache(modelLayerLocation.slim()));
        this.texture = texture;
        this.renderType = renderType;
        this.tint = tint;
        this.enchantmentGlint = enchantmentGlint;
        this.renderFullModelInFirstPerson = renderFullModelInFirstPerson;
        this.firstPersonOffset = firstPersonOffset;
    }

    @Override
    public void render(DataContext context, PoseStack poseStack, MultiBufferSource bufferSource, EntityModel<Entity> parentModel, int packedLight, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = context.getEntity();
        if (IPackRenderLayer.conditionsFulfilled(entity, this.conditions, this.thirdPersonConditions) && this.modelLookup.get(entity).fitsEntity(entity, parentModel)) {
            EntityModel entityModel = this.model.get(entity).getModel(context, this.modelLookup.get(entity));

            if (entityModel instanceof HumanoidModel entityHumanoidModel && parentModel instanceof HumanoidModel parentHumanoid) {
                IPackRenderLayer.copyModelProperties(entity, parentHumanoid, entityHumanoidModel);
            } else {
                entityModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
                entityModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            }

            if (entityModel instanceof ExtraAnimatedModel extra) {
                extra.extraAnimations(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks);
            }

            VertexConsumer vertexConsumer = this.renderType.createVertexConsumer(bufferSource, this.texture.get(entity).getTexture(context), context.getItem().hasFoil() || IPackRenderLayer.conditionsFulfilled(entity, this.enchantmentGlint));

            var tint = Color.WHITE;

            if (this.tint != null) {
                tint = this.tint.getColor(context);
            }

            entityModel.renderToBuffer(
                    poseStack,
                    vertexConsumer,
                    this.renderType.getPackedLight(packedLight),
                    OverlayTexture.NO_OVERLAY,
                    tint.getRed() / 255F,
                    tint.getGreen() / 255F,
                    tint.getBlue() / 255F,
                    tint.getAlpha() / 255F
            );
        }
    }

    @Override
    public void renderArm(DataContext context, HumanoidArm arm, PlayerRenderer playerRenderer, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        var player = context.getPlayer();
        if (player != null && IPackRenderLayer.conditionsFulfilled(player, this.conditions, this.firstPersonConditions) && this.modelLookup.get(player).fitsEntity(player, playerRenderer.getModel())) {
            if (this.renderFullModelInFirstPerson && arm != player.getMainArm()) {
                return;
            }

            EntityModel<?> entityModel = this.model.get(player).getModel(context, this.modelLookup.get(player));
            var partialTick = Minecraft.getInstance().getFrameTime();
            VertexConsumer vertexConsumer = this.renderType.createVertexConsumer(bufferSource, this.texture.get(player).getTexture(context), context.getItem().hasFoil() || IPackRenderLayer.conditionsFulfilled(player, this.enchantmentGlint));
            var tint = Color.WHITE;
            if (this.tint != null) {
                tint = this.tint.getColor(context);
            }

            poseStack.pushPose();
            if (this.renderFullModelInFirstPerson) {
                poseStack.last().pose().identity();
                poseStack.last().normal().identity();
                float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
                poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
                poseStack.translate(0, -player.getEyeHeight(), 0);
                float scale = player.getEyeHeight() / 1.75F;
                poseStack.scale(scale, scale, scale);
                if (this.firstPersonOffset != null) {
                    poseStack.translate(this.firstPersonOffset.x, this.firstPersonOffset.y, this.firstPersonOffset.z);
                }

                if (entityModel instanceof HumanoidModel humanoidModel) {
                    playerRenderer.getModel().copyPropertiesTo(humanoidModel);
                }

                entityModel.renderToBuffer(
                        poseStack,
                        vertexConsumer,
                        this.renderType.getPackedLight(packedLight),
                        OverlayTexture.NO_OVERLAY,
                        tint.getRed() / 255F,
                        tint.getGreen() / 255F,
                        tint.getBlue() / 255F,
                        tint.getAlpha() / 255F
                );
            } else if (entityModel instanceof HumanoidModel humanoidModel) {
                playerRenderer.getModel().copyPropertiesTo(humanoidModel);

                humanoidModel.attackTime = 0.0F;
                humanoidModel.crouching = false;
                humanoidModel.swimAmount = 0.0F;

                if (arm == HumanoidArm.RIGHT) {
                    humanoidModel.rightArm.xRot = 0.0F;
                    humanoidModel.rightArm.render(
                            poseStack,
                            vertexConsumer,
                            this.renderType.getPackedLight(packedLight),
                            OverlayTexture.NO_OVERLAY,
                            tint.getRed() / 255F,
                            tint.getGreen() / 255F,
                            tint.getBlue() / 255F,
                            tint.getAlpha() / 255F);
                } else {
                    humanoidModel.leftArm.xRot = 0.0F;
                    humanoidModel.leftArm.render(
                            poseStack,
                            vertexConsumer,
                            this.renderType.getPackedLight(packedLight),
                            OverlayTexture.NO_OVERLAY,
                            tint.getRed() / 255F,
                            tint.getGreen() / 255F,
                            tint.getBlue() / 255F,
                            tint.getAlpha() / 255F);
                }
            }
            poseStack.popPose();
        }
    }

    @Override
    public void createSnapshot(DataContext context, EntityModel<Entity> parentModel, Consumer<Snapshot> consumer) {
        var entity = context.getEntity();

        if (IPackRenderLayer.conditionsFulfilled(entity, this.conditions, this.firstPersonConditions) && this.modelLookup.get(entity).fitsEntity(entity, parentModel)) {
            EntityModel<?> entityModel = this.model.get(entity).getModel(context, this.modelLookup.get(entity));
            var texture = this.texture.get(entity).getTexture(context);
            consumer.accept(new Snapshot(entityModel, texture));
        }
    }

    public static PackRenderLayer parse(JsonObject json) {
        var renderType = PackRenderLayerManager.getRenderType(new ResourceLocation(GsonHelper.getAsString(json, "render_type", "solid")));

        SkinTypedValue<ModelTypes.Model> model;
        String modelTypeKey = "model_type";

        if (!json.has(modelTypeKey) && json.has("model")) {
            AddonPackLog.warning("Deprecated use of 'model' in render layer. Please switch to 'model_type'!");
            modelTypeKey = "model";
        }

        if (GsonHelper.isValidNode(json, modelTypeKey)) {
            model = SkinTypedValue.fromJSON(json.get(modelTypeKey), jsonElement -> {
                ResourceLocation modelId = new ResourceLocation(jsonElement.getAsString());
                ModelTypes.Model m = ModelTypes.get(modelId);

                if (m == null) {
                    throw new JsonParseException("Unknown model type '" + modelId + "'");
                }

                return m;
            });
        } else {
            model = new SkinTypedValue<>(ModelTypes.HUMANOID);
        }

        if (renderType == null) {
            throw new JsonParseException("Unknown render type '" + new ResourceLocation(GsonHelper.getAsString(json, "render_type", "solid")) + "'");
        }

        List<Condition> enchantmentGlint = json.has("enchantment_glint") ? ConditionSerializer.listFromJSON(json.get("enchantment_glint"), ConditionEnvironment.ASSETS) : Collections.singletonList(new FalseCondition());

        return new PackRenderLayer(
                model,
                SkinTypedValue.fromJSON(json.get("model_layer"), DynamicModelLayerLocation::fromJson),
                SkinTypedValue.fromJSON(json.get("texture"), DynamicTextureManager::fromJson),
                renderType,
                DynamicColor.getFromJson(json, "tint", null),
                enchantmentGlint,
                GsonHelper.getAsBoolean(json, "render_full_model_in_first_person", false),
                GsonUtil.getAsVector3f(json, "first_person_offset", null)
        );
    }

    public static class ModelCache {

        private final DynamicModelLayerLocation modelLayerLocation;
        private final Map<ModelLayerLocation, EntityModel<?>> models = new HashMap<>();

        public ModelCache(DynamicModelLayerLocation modelLayerLocation) {
            this.modelLayerLocation = modelLayerLocation;
        }

        public EntityModel<?> getModel(DataContext context, ModelTypes.Model modelType) {
            var modelLayer = this.modelLayerLocation.getModelLayer(context);

            if (this.models.containsKey(modelLayer)) {
                return this.models.get(modelLayer);
            } else {
                var model = modelType.getModel(Minecraft.getInstance().getEntityModels().bakeLayer(modelLayer));
                this.models.put(modelLayer, model);
                return model;
            }
        }
    }

}
