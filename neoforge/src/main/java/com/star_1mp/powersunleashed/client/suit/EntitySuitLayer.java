package com.star_1mp.powersunleashed.client.suit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.item.SuitPieceItem;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Draws suits on living renderers that use standard humanoid parts, or just a head. */
public final class EntitySuitLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

    private static final ContextKey<Equipment> SUIT = new ContextKey<>(Constants.id("suit_equipment"));
    private static final String HEAD = "head";
    private static final String BODY = "body";
    private static final String RIGHT_ARM = "right_arm";
    private static final String LEFT_ARM = "left_arm";
    private static final String RIGHT_LEG = "right_leg";
    private static final String LEFT_LEG = "left_leg";

    private record Equipment(ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet) {
    }

    private record Fit(float x, float y, float z, float width, float height, float depth) {
        void apply(PoseStack pose) {
            pose.translate(x / 16.0F, y / 16.0F, z / 16.0F);
            pose.scale(width, height, depth);
        }

        /** Fits the player UV mesh to the cubes of this model part in its local coordinates. */
        static Fit between(ModelPart original, ModelPart suitPart) {
            var bounds = new Bounds();
            original.visit(new PoseStack(), (pose, path, index, cube) -> {
                if (path.isEmpty()) {
                    bounds.include(cube);
                }
            });
            var suitBounds = new Bounds();
            suitPart.visit(new PoseStack(), (pose, path, index, cube) -> {
                if (path.isEmpty()) {
                    suitBounds.include(cube);
                }
            });
            float width = (bounds.maxX - bounds.minX) / (suitBounds.maxX - suitBounds.minX);
            float height = (bounds.maxY - bounds.minY) / (suitBounds.maxY - suitBounds.minY);
            float depth = (bounds.maxZ - bounds.minZ) / (suitBounds.maxZ - suitBounds.minZ);
            return new Fit(bounds.minX - suitBounds.minX * width, bounds.minY - suitBounds.minY * height,
                    bounds.minZ - suitBounds.minZ * depth, width, height, depth);
        }
    }

    private static final class Bounds {
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;

        void include(ModelPart.Cube cube) {
            // Ignore flat details such as ears or hats that extend from the main box.
            if (cube.minX < cube.maxX && cube.minY < cube.maxY && cube.minZ < cube.maxZ) {
                this.minX = Math.min(this.minX, cube.minX);
                this.minY = Math.min(this.minY, cube.minY);
                this.minZ = Math.min(this.minZ, cube.minZ);
                this.maxX = Math.max(this.maxX, cube.maxX);
                this.maxY = Math.max(this.maxY, cube.maxY);
                this.maxZ = Math.max(this.maxZ, cube.maxZ);
            }
        }
    }

    private final SuitModelSet meshes;
    private final Map<ModelPart, Fit> fits = new IdentityHashMap<>();

    public EntitySuitLayer(LivingEntityRenderer<?, S, M> renderer, SuitModelSet meshes) {
        super(renderer);
        this.meshes = meshes;
    }

    /** The collector captures the posed transforms. These meshes stay at their local origin. */
    public static SuitModelSet bake(EntityModelSet modelSet) {
        SuitModelSet meshes = SuitModelSet.bake(modelSet, false);
        for (SuitModel.Part part : SuitModel.Part.values()) {
            SuitModel mesh = meshes.get(part);
            mesh.head.loadPose(PartPose.ZERO);
            mesh.body.loadPose(PartPose.ZERO);
            mesh.rightArm.loadPose(PartPose.ZERO);
            mesh.leftArm.loadPose(PartPose.ZERO);
            mesh.rightLeg.loadPose(PartPose.ZERO);
            mesh.leftLeg.loadPose(PartPose.ZERO);
        }
        return meshes;
    }

    /** Extracts equipment vanilla's armor layer filters out, after the entity renderer has filled its state. */
    public static void extract(LivingEntity entity, LivingEntityRenderState state) {
        if (state instanceof AvatarRenderState || state instanceof ArmorStandRenderState) {
            return;
        }
        ItemStack head = worn(entity, EquipmentSlot.HEAD);
        ItemStack chest = worn(entity, EquipmentSlot.CHEST);
        ItemStack legs = worn(entity, EquipmentSlot.LEGS);
        ItemStack feet = worn(entity, EquipmentSlot.FEET);
        if (head.isEmpty() && chest.isEmpty() && legs.isEmpty() && feet.isEmpty()) {
            return;
        }
        state.setRenderData(SUIT, new Equipment(head, chest, legs, feet));
        if (!head.isEmpty() && Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity)
                instanceof LivingEntityRenderer<?, ?, ?> renderer) {
            ModelPart headModel = head(renderer.getModel());
            if (headModel != null && !headModel.isEmpty()) {
                state.headItem.clear();
            }
        }
    }

    private static ItemStack worn(LivingEntity entity, EquipmentSlot slot) {
        ItemStack stack = entity.getItemBySlot(slot);
        return SuitPieceItem.get(stack, slot) != null ? stack : ItemStack.EMPTY;
    }

    private static @Nullable ModelPart head(EntityModel<?> model) {
        if (model instanceof HeadedModel headed) {
            return headed.getHead();
        }
        return model.root().hasChild(HEAD) ? model.root().getChild(HEAD) : null;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        Equipment equipment = state.getRenderData(SUIT);
        if (equipment == null) {
            return;
        }
        M parent = this.getParentModel();
        ModelPart root = parent.root();
        if (!root.visible) {
            return;
        }
        pose.pushPose();
        root.translateAndRotate(pose);
        OrderedSubmitNodeCollector armor = collector.order(1);
        int outline = state.outlineColor;
        if (!equipment.chest().isEmpty()) {
            SuitModel mesh = this.meshes.get(SuitModel.Part.CHEST);
            RenderType type = renderType(equipment.chest(), SuitModel.Part.CHEST);
            draw(armor, pose, light, outline, type, child(root, BODY), mesh.body);
            draw(armor, pose, light, outline, type, child(root, RIGHT_ARM), mesh.rightArm);
            draw(armor, pose, light, outline, type, child(root, LEFT_ARM), mesh.leftArm);
        }
        this.drawLegs(armor, pose, light, outline, root, equipment.legs(), SuitModel.Part.LEGS);
        this.drawLegs(armor, pose, light, outline, root, equipment.feet(), SuitModel.Part.FEET);
        if (!equipment.head().isEmpty()) {
            ModelPart head = head(parent);
            SuitModel mesh = this.meshes.get(SuitModel.Part.HEAD);
            RenderType type = renderType(equipment.head(), SuitModel.Part.HEAD);
            if (head != null && head.visible && parent instanceof HeadedModel headed) {
                headed.translateToHead(pose);
                this.drawFitted(armor, pose, light, outline, type, head, mesh.head);
            } else if (head != null) {
                draw(armor, pose, light, outline, type, head, mesh.head);
            }
        }
        pose.popPose();
    }

    private void drawLegs(OrderedSubmitNodeCollector collector, PoseStack pose, int light, int outline, ModelPart root,
            ItemStack stack, SuitModel.Part part) {
        if (!stack.isEmpty()) {
            SuitModel mesh = this.meshes.get(part);
            RenderType type = renderType(stack, part);
            draw(collector, pose, light, outline, type, child(root, BODY), mesh.body);
            draw(collector, pose, light, outline, type, child(root, RIGHT_LEG), mesh.rightLeg);
            draw(collector, pose, light, outline, type, child(root, LEFT_LEG), mesh.leftLeg);
        }
    }

    private static RenderType renderType(ItemStack stack, SuitModel.Part part) {
        SuitPieceItem item = (SuitPieceItem) stack.getItem();
        return SuitRendering.renderType(SuitRendering.texture(item.getSuit(), part, false), stack);
    }

    private static @Nullable ModelPart child(ModelPart root, String name) {
        return root.hasChild(name) ? root.getChild(name) : null;
    }

    private void draw(OrderedSubmitNodeCollector collector, PoseStack pose, int light, int outline,
            RenderType type, @Nullable ModelPart posed, ModelPart mesh) {
        if (posed == null || !posed.visible || posed.isEmpty()) {
            return;
        }
        pose.pushPose();
        posed.translateAndRotate(pose);
        this.drawFitted(collector, pose, light, outline, type, posed, mesh);
        pose.popPose();
    }

    private void drawFitted(OrderedSubmitNodeCollector collector, PoseStack pose, int light, int outline,
            RenderType type, ModelPart wornPart, ModelPart mesh) {
        Fit fit = this.fits.computeIfAbsent(wornPart, part -> Fit.between(part, mesh));
        if (Float.isFinite(fit.width()) && Float.isFinite(fit.height()) && Float.isFinite(fit.depth())) {
            fit.apply(pose);
            collector.submitModelPart(mesh, pose, type, light, OverlayTexture.NO_OVERLAY, null, -1, outline);
        }
    }
}
