package net.threetag.palladium.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.model.PowerRendererModel;
import net.threetag.palladium.client.renderer.entity.layer.pack.ClientEntityRenderLayers;
import net.threetag.palladium.client.renderer.entity.layer.pack.EntityRenderLayers;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.client.renderer.power.ClientPowerRendererHandler;
import net.threetag.palladium.client.renderer.power.PowerRendererLayer;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.ability.HideModelPartAbility;
import net.threetag.palladium.util.PlayerUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class ModelUtil {

    public static final String RIGHT_ARM_PART_NAME = "right_arm";
    public static final String LEFT_ARM_PART_NAME = "left_arm";
    private static final Matrix4f[] FIRST_PERSON_ARM_ANCHOR = new Matrix4f[HumanoidArm.values().length];

    public static void captureFirstPersonArmAnchor(HumanoidArm arm, Matrix4f viewSpaceAnchor) {
        FIRST_PERSON_ARM_ANCHOR[arm.ordinal()] = new Matrix4f(viewSpaceAnchor);
    }

    public static void copyProperties(ModelPart from, ModelPart to) {
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }

    public static void copyPropertiesWithoutPivots(ModelPart from, ModelPart to) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }

    public static ModelPart getPartFromModel(Model<?> model, String name) {
        if (name.equals("root") || name.equals("*")) {
            return model.root();
        } else if (name.contains(".")) {
            String[] split = name.split("\\.");
            ModelPart part = model.root().getChild(split[0]);
            for (int i = 1; i < split.length; i++) {
                part = part.getChild(split[i]);
            }
            return part;
        } else {
            return model.root().getChild(name);
        }
    }

    public static void translateRotateFullPath(Model<?> model, String boneName, PoseStack poseStack) {
        var path = resolvePath(model, boneName);
        for (ModelPart modelPart : path) {
            modelPart.translateAndRotate(poseStack);
        }
    }

    public static List<ModelPart> resolvePath(Model<?> model, @Nullable String boneName) {
        if (boneName == null) {
            return List.of();
        }

        if (boneName.contains(".")) {
            List<ModelPart> path = new ArrayList<>();
            ModelPart part = model.root();
            for (String segment : boneName.split("\\.")) {
                if (!part.hasChild(segment)) {
                    return List.of();
                }
                part = part.getChild(segment);
                path.add(part);
            }
            return path;
        }

        List<ModelPart> path = new ArrayList<>();
        if (findPath(model.root(), boneName, path)) {
            return path;
        }
        return List.of();
    }

    public static boolean findPath(ModelPart current, String target, List<ModelPart> path) {
        for (Map.Entry<String, ModelPart> entry : current.children.entrySet()) {
            path.add(entry.getValue());
            if (entry.getKey().equals(target) || findPath(entry.getValue(), target, path)) {
                return true;
            }
            path.removeLast();
        }
        return false;
    }

    public static @Nullable ModelPart findPart(Model<?> model, String name) {
        List<ModelPart> path = resolvePath(model, name);
        return path.isEmpty() ? null : path.getLast();
    }

    public static boolean isWholeModel(String partName) {
        return partName.equals("root") || partName.equals("*");
    }

    public static Set<String> getHiddenModelPartNames(LivingEntity entity) {
        return getHiddenModelPartNames(entity, PerspectiveAwareConditions.Perspective.THIRD_PERSON);
    }

    public static Set<String> getHiddenModelPartNames(LivingEntity entity, PerspectiveAwareConditions.Perspective perspective) {
        Set<String> parts = new HashSet<>();

        for (AbilityInstance<HideModelPartAbility> instance : AbilityUtil.getEnabledInstances(entity, AbilitySerializers.HIDE_MODEL_PART.get())) {
            parts.addAll(instance.getAbility().modelParts);
        }

        for (Map.Entry<PackRenderLayer<PackRenderLayer.State>, PackRenderLayer.State> e : ((ClientEntityRenderLayers) EntityRenderLayers.get(entity, PalladiumEntityDataTypes.RENDER_LAYERS.get())).getLayerStates().entrySet()) {
            if (e.getKey().shouldRender(e.getValue(), perspective)) {
                parts.addAll(e.getKey().getProperties().hiddenModelParts());
            }
        }

        return parts;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Matrix4f getInWorldModelPartMatrix(String part, AbstractClientPlayer player, float partialTicks) {
        var poseStack = new PoseStack();
        EntityRenderer<?, ?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);

        if (!(entityRenderer instanceof AvatarRenderer renderer)
                || !(renderer.createRenderState(player, partialTicks) instanceof AvatarRenderState state)) {
            return poseStack.last().pose();
        }

        // Use the overriding power model as the reference when one is replacing the player model.
        var prHandler = ClientPowerRendererHandler.get(player);
        PowerRendererModel powerModel = prHandler != null && prHandler.getVisibleModelRendererPowerId() != null
                ? prHandler.getOrBuildModel(PlayerUtil.hasSmallArms(player)) : null;
        Model<?> model = powerModel != null ? powerModel : renderer.getModel();

        boolean root = part.equals("root") || part.equals("*");
        List<ModelPart> path = root ? List.of() : resolvePath(model, part);
        if (!root && path.isEmpty()) {
            return poseStack.last().pose();
        }

        if (powerModel == null) {
            var typedModel = (Model<AvatarRenderState>) model;
            typedModel.setupAnim(state);
        }

        if (state.hasPose(Pose.SLEEPING)) {
            Direction direction = state.bedOrientation;
            if (direction != null) {
                float f = state.eyeHeight - 0.1F;
                poseStack.translate((float) (-direction.getStepX()) * f, 0.0F, (float) (-direction.getStepZ()) * f);
            }
        }

        float g = state.scale;
        poseStack.scale(g, g, g);
        renderer.setupRotations(state, poseStack, state.bodyRot, g);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        PowerRendererLayer.applyBodyBone(poseStack, state);
        renderer.scale(state, poseStack);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        model.root().translateAndRotate(poseStack);
        for (ModelPart modelPart : path) {
            modelPart.translateAndRotate(poseStack);
        }

        return poseStack.last().pose();
    }

    public static WorldAnchor getWorldAnchor(String part, AbstractClientPlayer player, float partialTicks) {
        var mc = Minecraft.getInstance();
        if (player == mc.player
                && mc.options.getCameraType() == CameraType.FIRST_PERSON
                && (part.equals(RIGHT_ARM_PART_NAME) || part.equals(LEFT_ARM_PART_NAME))) {
            HumanoidArm arm = part.equals(RIGHT_ARM_PART_NAME) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            return new WorldAnchor(getInWorldFirstPersonArmMatrix(arm), mc.gameRenderer.getMainCamera().position());
        }
        return new WorldAnchor(getInWorldModelPartMatrix(part, player, partialTicks), player.getPosition(partialTicks));
    }

    public static Vec3 getInWorldPosition(String part, Vector3f offset, AbstractClientPlayer player, float partialTicks) {
        return getWorldAnchor(part, player, partialTicks).transform(offset);
    }

    public static Vec3 getInWorldFirstPersonPosition(HumanoidArm arm, Vector3f offset) {
        Vector3f vec = getInWorldFirstPersonArmMatrix(arm).transformPosition(new Vector3f(offset));
        return Minecraft.getInstance().gameRenderer.getMainCamera().position().add(vec.x, vec.y, vec.z);
    }

    public record WorldAnchor(Matrix4f matrix, Vec3 origin) {
        public Vec3 transform(Vector3f localOffset) {
            Vector3f vec = this.matrix.transformPosition(new Vector3f(localOffset));
            return this.origin.add(vec.x, vec.y, vec.z);
        }
    }

    public static Matrix4f getInWorldFirstPersonArmMatrix(HumanoidArm arm) {
        var poseStack = new PoseStack();
        var mc = Minecraft.getInstance();
        var player = mc.player;
        Matrix4f anchor = FIRST_PERSON_ARM_ANCHOR[arm.ordinal()];

        if (anchor == null || player == null
                || !(mc.getEntityRenderDispatcher().getRenderer(player) instanceof LivingEntityRenderer<?, ?, ?> renderer)) {
            return poseStack.last().pose();
        }

        boolean rightArm = arm == HumanoidArm.RIGHT;
        var modelPart = getPartFromModel(renderer.getModel(), rightArm ? RIGHT_ARM_PART_NAME : LEFT_ARM_PART_NAME);
        if (modelPart == null) {
            return poseStack.last().pose();
        }

        poseStack.last().pose().mul(anchor);
        modelPart.resetPose();
        modelPart.zRot = rightArm ? 0.1F : -0.1F;
        modelPart.translateAndRotate(poseStack);

        return poseStack.last().pose();
    }

}
