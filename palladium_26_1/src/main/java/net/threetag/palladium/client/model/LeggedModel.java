package net.threetag.palladium.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;

/**
 * The leg counterpart to {@link net.minecraft.client.model.ArmedModel}: lets a model expose where its legs are so
 * attachments (e.g. thrusters) can be anchored to a leg/foot regardless of the model's bone layout. {@code leg} uses
 * {@link HumanoidArm} purely as a left/right selector.
 */
public interface LeggedModel {

    ModelPart getLeg(HumanoidArm leg);

    void translateToLeg(LivingEntityRenderState state, HumanoidArm leg, PoseStack poseStack);
}
