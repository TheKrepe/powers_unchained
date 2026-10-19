package net.threetag.palladium.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.HumanoidArm;
import net.threetag.palladium.client.renderer.power.PowerRenderer;
import net.threetag.palladium.client.util.ModelUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PowerRendererModel extends Model<Unit> implements ArmedModel<LivingEntityRenderState>, HeadedModel, LeggedModel {

    public static final String HEAD = "head";
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    public static final String RIGHT_LEG = "right_leg";
    public static final String LEFT_LEG = "left_leg";

    private final Map<String, String> boneMapping;
    private final PowerRenderer.ItemBones itemLocations;

    private final ModelPart head;
    private @Nullable List<ModelPart> rightArmPath;
    private @Nullable List<ModelPart> leftArmPath;
    private @Nullable List<ModelPart> rightLegPath;
    private @Nullable List<ModelPart> leftLegPath;
    private @Nullable List<ModelPart> headPath;

    public PowerRendererModel(ModelPart root, Map<String, String> boneMapping, PowerRenderer.ItemBones itemLocations) {
        super(root, RenderTypes::entityTranslucent);
        this.boneMapping = boneMapping;
        this.itemLocations = itemLocations;
        this.head = resolvePart(this.headBoneName());
    }

    @Override
    public void setupAnim(Unit state) {
        // nothing
    }

    @Override
    public void translateToHand(LivingEntityRenderState state, HumanoidArm arm, PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        for (ModelPart part : arm == HumanoidArm.RIGHT ? this.rightArmPath() : this.leftArmPath()) {
            part.translateAndRotate(poseStack);
        }
    }

    @Override
    public ModelPart getLeg(HumanoidArm leg) {
        var parts = (leg == HumanoidArm.LEFT ? this.leftLegPath : this.rightLegPath);
        return parts != null ? parts.getLast() : null;
    }

    @Override
    public void translateToLeg(LivingEntityRenderState state, HumanoidArm leg, PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        for (ModelPart part : leg == HumanoidArm.RIGHT ? this.rightLegPath() : this.leftLegPath()) {
            part.translateAndRotate(poseStack);
        }
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void translateToHead(PoseStack poseStack) {
        for (ModelPart part : this.headPath()) {
            part.translateAndRotate(poseStack);
        }
    }

    private String headBoneName() {
        return this.itemLocations.head();
    }

    public String armBoneName(HumanoidArm arm) {
        String mapped = this.itemLocations != null ? (arm == HumanoidArm.RIGHT ? this.itemLocations.rightArm() : this.itemLocations.leftArm()) : null;
        if (mapped != null) {
            return mapped;
        }
        return arm == HumanoidArm.RIGHT ? RIGHT_ARM : LEFT_ARM;
    }

    private String legBoneName(HumanoidArm leg) {
        String vanilla = leg == HumanoidArm.RIGHT ? RIGHT_LEG : LEFT_LEG;
        return this.boneMapping.getOrDefault(vanilla, vanilla);
    }

    private List<ModelPart> rightArmPath() {
        if (this.rightArmPath == null) {
            this.rightArmPath = ModelUtil.resolvePath(this, this.armBoneName(HumanoidArm.RIGHT));
        }
        return this.rightArmPath;
    }

    private List<ModelPart> leftArmPath() {
        if (this.leftArmPath == null) {
            this.leftArmPath = ModelUtil.resolvePath(this, this.armBoneName(HumanoidArm.LEFT));
        }
        return this.leftArmPath;
    }

    private List<ModelPart> rightLegPath() {
        if (this.rightLegPath == null) {
            this.rightLegPath = ModelUtil.resolvePath(this, this.legBoneName(HumanoidArm.RIGHT));
        }
        return this.rightLegPath;
    }

    private List<ModelPart> leftLegPath() {
        if (this.leftLegPath == null) {
            this.leftLegPath = ModelUtil.resolvePath(this, this.legBoneName(HumanoidArm.LEFT));
        }
        return this.leftLegPath;
    }

    private List<ModelPart> headPath() {
        if (this.headPath == null) {
            this.headPath = ModelUtil.resolvePath(this, this.headBoneName());
        }
        return this.headPath;
    }

    private ModelPart resolvePart(String boneName) {
        List<ModelPart> path = ModelUtil.resolvePath(this, boneName);
        return path.isEmpty() ? this.root() : path.getLast();
    }

}
