package net.threetag.palladium.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.entity.layer.pack.CompoundPackRenderLayer;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayerManager;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderUtil {

    public static final WidgetSprites BIG_BUTTON_SPRITES = new WidgetSprites(Palladium.id("widget/big_button"), Palladium.id("widget/big_button_disabled"), Palladium.id("widget/big_button_highlighted"));
    public static final int FULL_WHITE = ARGB.white(1F);
    public static final int FULL_BLACK = ARGB.color(255, 0, 0, 0);
    public static final int DEFAULT_GRAY = ARGB.opaque(0x404040);
    public static final Color DEFAULT_GRAY_COLOR = new Color(ARGB.opaque(0x404040));

    private static TriState OVERRIDEN_STRING_SHADOW = TriState.DEFAULT;
    private static Integer OVERRIDEN_STRING_COLOR = null;

    public static void setItemInHumanoidRenderStateSlot(HumanoidRenderState state, EquipmentSlot slot, ItemStack stack) {
        ItemModelResolver itemModelResolver = Minecraft.getInstance().getItemModelResolver();

        if (slot == EquipmentSlot.MAINHAND) {
            var right = state.mainArm == HumanoidArm.RIGHT;

            if (right) {
                state.rightHandItemStack = stack;
                itemModelResolver.updateForTopItem(state.rightHandItemState, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, null, null, 0);
            } else {
                state.leftHandItemStack = stack;
                itemModelResolver.updateForTopItem(state.leftHandItemState, stack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, null, null, 0);
            }
        } else if (slot == EquipmentSlot.OFFHAND) {
            var right = state.mainArm != HumanoidArm.RIGHT;

            if (right) {
                state.rightHandItemStack = stack;
                itemModelResolver.updateForTopItem(state.rightHandItemState, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, null, null, 0);
            } else {
                state.leftHandItemStack = stack;
                itemModelResolver.updateForTopItem(state.leftHandItemState, stack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, null, null, 0);
            }
        } else if (slot == EquipmentSlot.HEAD) {
            state.headEquipment = stack;
            if (!HumanoidArmorLayer.shouldRender(stack, EquipmentSlot.HEAD)) {
                itemModelResolver.updateForTopItem(state.headItem, stack, ItemDisplayContext.HEAD, null, null, 0);
            }
        } else if (slot == EquipmentSlot.CHEST) {
            state.chestEquipment = stack;
        } else if (slot == EquipmentSlot.LEGS) {
            state.legsEquipment = stack;
        } else if (slot == EquipmentSlot.FEET) {
            state.feetEquipment = stack;
        }
    }

    public static void addItemRenderLayersInHumanoidRenderStateSlot(RegistryAccess registryAccess, HumanoidRenderState state, EquipmentSlot slot, ItemStack stack) {
        Map<PackRenderLayer<PackRenderLayer.State>, PackRenderLayer.State> renderLayers = new HashMap<>(state.getRenderDataOrDefault(PalladiumRenderStateKeys.RENDER_LAYERS, Collections.emptyMap()));
        var playerSlot = PlayerSlot.get(slot);
        var context = DataContext.create().with(DataContextKeys.ITEM, stack).with(DataContextKeys.SLOT, playerSlot);

        if (stack.has(PalladiumDataComponents.Items.RENDER_LAYERS)) {
            addRenderLayers(renderLayers, stack.get(PalladiumDataComponents.Items.RENDER_LAYERS).forSlot(playerSlot), context);
        }

        var appearance = MultiversalItemVariant.resolve(registryAccess, stack);
        if (appearance != null) {
            addRenderLayers(renderLayers, appearance.renderLayers().forSlot(playerSlot), context);
        }

        state.setRenderData(PalladiumRenderStateKeys.RENDER_LAYERS, renderLayers);

        // Remove Item on head rendering when render layers are active
        if (slot == EquipmentSlot.HEAD) {
            boolean hasHeadLayers = stack.has(PalladiumDataComponents.Items.RENDER_LAYERS)
                    || MultiversalItemVariant.hasRenderLayers(registryAccess, stack, playerSlot);
            if (!(stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock)
                    && hasHeadLayers) {
                state.wornHeadType = null;
                state.wornHeadProfile = null;
                state.headItem.clear();
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addRenderLayers(Map<PackRenderLayer<PackRenderLayer.State>, PackRenderLayer.State> renderLayers, List<Identifier> layers, DataContext context) {
        for (Identifier layerId : layers) {
            PackRenderLayer<PackRenderLayer.State> layer = (PackRenderLayer<PackRenderLayer.State>) PackRenderLayerManager.INSTANCE.get(layerId);

            if (layer != null) {
                if (layer instanceof CompoundPackRenderLayer com) {
                    for (PackRenderLayer comLayer : com.getLayers()) {
                        if (!renderLayers.containsKey(comLayer))
                            renderLayers.put(comLayer, comLayer.createExtractedState(context));
                    }
                } else if (!renderLayers.containsKey(layer)) {
                    renderLayers.put(layer, layer.createExtractedState(context));
                }
            }
        }
    }

    public static void faceVec(PoseStack poseStack, Vec3 src, Vec3 dst) {
        double x = dst.x - src.x;
        double y = dst.y - src.y;
        double z = dst.z - src.z;
        double diff = Mth.sqrt((float) (x * x + z * z));
        float yaw = (float) (Math.atan2(z, x) * 180 / Math.PI) - 90;
        float pitch = (float) -(Math.atan2(y, diff) * 180 / Math.PI);

        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
    }

    public static void submitFilledBox(PoseStack stack, RenderType renderType, SubmitNodeCollector submitNodeCollector, AABB box, float red, float green, float blue, float alpha, int combinedLightIn) {
        submitNodeCollector.submitCustomGeometry(stack, renderType, (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        });
    }

    public static void submitTexturedBox(PoseStack stack, RenderType renderType, SubmitNodeCollector submitNodeCollector, AABB box, int color, int combinedLightIn, int packedOverlay, float heightMultiplier) {
        submitNodeCollector.submitCustomGeometry(stack, renderType, (pose, vertexConsumer) -> {
            var yDiff = (float) (box.maxY - box.minY);
            float maxY = (float) (box.minY + yDiff * heightMultiplier);

            vertexConsumer.addVertex(pose, (float) box.minX, maxY, (float) box.minZ).setUv(0, 0).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.minX, maxY, (float) box.maxZ).setUv(0, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, maxY, (float) box.maxZ).setUv(1, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, maxY, (float) box.minZ).setUv(1, 0).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);

            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setUv(0, 0).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setUv(1, 0).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setUv(1, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setUv(0, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);

            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setUv(1, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.minX, maxY, (float) box.minZ).setUv(1, 1 - heightMultiplier).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, maxY, (float) box.minZ).setUv(0, 1 - heightMultiplier).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setUv(0, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);

            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setUv(0, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setUv(1, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, maxY, (float) box.maxZ).setUv(1, 1 - heightMultiplier).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.minX, maxY, (float) box.maxZ).setUv(0, 1 - heightMultiplier).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);

            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setUv(1, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, maxY, (float) box.minZ).setUv(1, 1 - heightMultiplier).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, maxY, (float) box.maxZ).setUv(0, 1 - heightMultiplier).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setUv(0, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);

            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setUv(0, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setUv(1, 1).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.minX, maxY, (float) box.maxZ).setUv(1, 1 - heightMultiplier).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vertexConsumer.addVertex(pose, (float) box.minX, maxY, (float) box.minZ).setUv(0, 1 - heightMultiplier).setColor(color).setOverlay(packedOverlay).setLight(combinedLightIn).setNormal(pose, 0.0F, 1.0F, 0.0F);
        });
    }

    public static void flashStringShadowOverride(boolean shadow) {
        OVERRIDEN_STRING_SHADOW = TriState.from(shadow);
    }

    public static boolean getStringShadowOverride(boolean original) {
        var override = OVERRIDEN_STRING_SHADOW.toBoolean(original);
        OVERRIDEN_STRING_SHADOW = TriState.DEFAULT;
        return override;
    }

    public static void flashStringColorOverride(int color) {
        OVERRIDEN_STRING_COLOR = color;
    }

    public static int getStringColorOverride(int original) {
        if (OVERRIDEN_STRING_COLOR != null) {
            int override = OVERRIDEN_STRING_COLOR;
            OVERRIDEN_STRING_COLOR = null;
            return override;
        } else {
            return original;
        }
    }
}
