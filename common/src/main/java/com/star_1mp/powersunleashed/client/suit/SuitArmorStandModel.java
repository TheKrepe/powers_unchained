package com.star_1mp.powersunleashed.client.suit;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;

/**
 * Suit piece model for armor stands: player-skin UV layout (with the outer layer parts) but the part pivots of the
 * vanilla armor stand armor model, and posed from the armor stand's poses like vanilla armor.
 * One instance per piece (see {@link SuitModel}).
 */
public class SuitArmorStandModel extends ArmorStandArmorModel {

    private final SuitModel.Part part;

    public SuitArmorStandModel(ModelPart root, SuitModel.Part part) {
        super(root);
        this.part = part;
        this.applyVisibility();
    }

    /** Wide (4 px arms) player mesh moved onto the armor stand pivots (head y=1, legs y=11). */
    public static MeshDefinition createMesh(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation outer = g.extend(0.25F);

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, g),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        head.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, g.extend(0.5F)),
                PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, g),
                PartPose.ZERO);
        body.addOrReplaceChild("jacket",
                CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, outer),
                PartPose.ZERO);

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("right_sleeve",
                CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, outer),
                PartPose.ZERO);
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("left_sleeve",
                CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, outer),
                PartPose.ZERO);

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(-1.9F, 11.0F, 0.0F));
        rightLeg.addOrReplaceChild("right_pants",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, outer),
                PartPose.ZERO);
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                PartPose.offset(1.9F, 11.0F, 0.0F));
        leftLeg.addOrReplaceChild("left_pants",
                CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, outer),
                PartPose.ZERO);
        return mesh;
    }

    @Override
    public void setupAnim(ArmorStandRenderState state) {
        super.setupAnim(state);
        this.applyVisibility();
    }

    /** Outer-layer parts are children of the base parts, so hiding a base part hides its overlay too. */
    private void applyVisibility() {
        boolean head = this.part == SuitModel.Part.HEAD;
        boolean chest = this.part == SuitModel.Part.CHEST;
        boolean legs = this.part == SuitModel.Part.LEGS || this.part == SuitModel.Part.FEET;
        this.head.visible = head;
        this.hat.visible = head;
        this.body.visible = chest || legs;  // pants may paint the torso (belt)
        this.rightArm.visible = chest;
        this.leftArm.visible = chest;
        this.rightLeg.visible = legs;
        this.leftLeg.visible = legs;
    }
}
