package net.threetag.palladium.client.renderer.power;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.threetag.palladium.client.util.ModelUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VanillaMovementAnimator {

    private static final String HEAD = "head";
    private static final String BODY = "body";
    private static final String RIGHT_ARM = "right_arm";
    private static final String LEFT_ARM = "left_arm";
    private static final String RIGHT_LEG = "right_leg";
    private static final String LEFT_LEG = "left_leg";

    @Nullable
    private static ModelPart part(Model<?> model, String vanillaName, Map<String, String> boneMapping) {
        return ModelUtil.getPartFromModel(model, boneMapping.getOrDefault(vanillaName, vanillaName));
    }

    public static void apply(Model<?> model, EntityRenderState renderState, Map<String, String> boneMapping) {
        if (!(renderState instanceof LivingEntityRenderState state)) return;

        ModelPart head = part(model, HEAD, boneMapping);
        ModelPart body = part(model, BODY, boneMapping);
        ModelPart rightArm = part(model, RIGHT_ARM, boneMapping);
        ModelPart leftArm = part(model, LEFT_ARM, boneMapping);
        ModelPart rightLeg = part(model, RIGHT_LEG, boneMapping);
        ModelPart leftLeg = part(model, LEFT_LEG, boneMapping);

        // Mirrors vanilla setupAnim ordering exactly.

        // 1. Head rotation (look direction)
        applyHeadRotation(head, state);

        // 2. Walk cycle — arm and leg swing
        applyWalkAnimation(rightArm, leftArm, rightLeg, leftLeg, state);

        // 3. Passenger / riding pose
        if (state instanceof HumanoidRenderState h && h.isPassenger) {
            applyPassengerPose(rightArm, leftArm, rightLeg, leftLeg);
        }

        // 4. Arm item-holding poses (bow, crossbow, shield, spyglass, …)
        HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
        HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
        if (state instanceof ArmedEntityRenderState armed) {
            rightArmPose = armed.rightArmPose;
            leftArmPose = armed.leftArmPose;
            if (state instanceof HumanoidRenderState h) {
                applyArmPoses(head, rightArm, leftArm, h);
            }
            // 5. Attack swing
            setupAttackAnimation(head, body, rightArm, leftArm, armed, state.ageScale);
        }

        // 6. Crouching offsets
        if (state instanceof HumanoidRenderState h && h.isCrouching) {
            applyCrouching(head, body, rightArm, leftArm, rightLeg, leftLeg);
        }

        // 7. Idle arm bob (skipped while holding a spyglass, matching vanilla)
        if (rightArm != null && rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(rightArm, state.ageInTicks, 1.0F);
        }
        if (leftArm != null && leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(leftArm, state.ageInTicks, -1.0F);
        }

        // 8. Swimming stroke
        if (state instanceof HumanoidRenderState h && h.swimAmount > 0.0F) {
            applySwimming(rightArm, leftArm, rightLeg, leftLeg, state.walkAnimationPos, h);
        }
    }

    // -------------------------------------------------------------------------
    // Step implementations
    // -------------------------------------------------------------------------

    private static void applyHeadRotation(@Nullable ModelPart head, LivingEntityRenderState state) {
        if (head == null) return;
        head.xRot = state.xRot * (float) (Math.PI / 180.0);
        head.yRot = state.yRot * (float) (Math.PI / 180.0);
        if (state instanceof HumanoidRenderState h) {
            if (h.isFallFlying) {
                head.xRot = (float) (-Math.PI / 4);
            } else if (h.swimAmount > 0.0F) {
                head.xRot = Mth.rotLerpRad(h.swimAmount, head.xRot, (float) (-Math.PI / 4));
            }
        }
    }

    private static void applyWalkAnimation(
            @Nullable ModelPart rightArm, @Nullable ModelPart leftArm,
            @Nullable ModelPart rightLeg, @Nullable ModelPart leftLeg,
            LivingEntityRenderState state) {
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        float speedValue = (state instanceof HumanoidRenderState h) ? h.speedValue : 1.0F;

        if (rightArm != null) rightArm.xRot += Mth.cos(pos * 0.6662F + (float) Math.PI) * 2.0F * speed * 0.5F / speedValue;
        if (leftArm  != null) leftArm.xRot  += Mth.cos(pos * 0.6662F)                   * 2.0F * speed * 0.5F / speedValue;
        if (rightLeg != null) {
            rightLeg.xRot +=  Mth.cos(pos * 0.6662F)                   * 1.4F * speed / speedValue;
            rightLeg.yRot +=  0.005F;
            rightLeg.zRot +=  0.005F;
        }
        if (leftLeg != null) {
            leftLeg.xRot += Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.4F * speed / speedValue;
            leftLeg.yRot += -0.005F;
            leftLeg.zRot += -0.005F;
        }
    }

    private static void applyPassengerPose(
            @Nullable ModelPart rightArm, @Nullable ModelPart leftArm,
            @Nullable ModelPart rightLeg, @Nullable ModelPart leftLeg) {
        if (rightArm != null) rightArm.xRot += (float) (-Math.PI / 5);
        if (leftArm != null) leftArm.xRot += (float) (-Math.PI / 5);
        if (rightLeg != null) {
            rightLeg.xRot += -1.4137167F;
            rightLeg.yRot +=  (float) (Math.PI / 10);
            rightLeg.zRot +=  0.07853982F;
        }
        if (leftLeg != null) {
            leftLeg.xRot += -1.4137167F;
            leftLeg.yRot += -(float) (Math.PI / 10);
            leftLeg.zRot += -0.07853982F;
        }
    }

    private static void applyArmPoses(
            @Nullable ModelPart head, @Nullable ModelPart rightArm, @Nullable ModelPart leftArm,
            HumanoidRenderState humanoid) {
        boolean rightHanded = humanoid.mainArm == HumanoidArm.RIGHT;
        HumanoidModel.ArmPose rp = humanoid.rightArmPose;
        HumanoidModel.ArmPose lp = humanoid.leftArmPose;

        // Mirrors vanilla's "which arm goes first" logic
        if (humanoid.isUsingItem) {
            boolean mainHandUsed = humanoid.useItemHand == InteractionHand.MAIN_HAND;
            if (mainHandUsed == rightHanded) {
                poseRightArm(head, rightArm, leftArm, humanoid);
                if (!rp.affectsOffhandPose()) poseLeftArm(head, rightArm, leftArm, humanoid);
            } else {
                poseLeftArm(head, rightArm, leftArm, humanoid);
                if (!lp.affectsOffhandPose()) poseRightArm(head, rightArm, leftArm, humanoid);
            }
        } else {
            boolean twoHandedOffhand = rightHanded ? lp.isTwoHanded() : rp.isTwoHanded();
            if (rightHanded != twoHandedOffhand) {
                poseLeftArm(head, rightArm, leftArm, humanoid);
                if (!lp.affectsOffhandPose()) poseRightArm(head, rightArm, leftArm, humanoid);
            } else {
                poseRightArm(head, rightArm, leftArm, humanoid);
                if (!rp.affectsOffhandPose()) poseLeftArm(head, rightArm, leftArm, humanoid);
            }
        }
    }

    private static void poseRightArm(
            @Nullable ModelPart head, @Nullable ModelPart rightArm, @Nullable ModelPart leftArm,
            HumanoidRenderState humanoid) {
        if (rightArm == null) return;
        float hx = head != null ? head.xRot : 0.0F;
        float hy = head != null ? head.yRot : 0.0F;
        switch (humanoid.rightArmPose) {
            case EMPTY -> rightArm.yRot = 0.0F;
            case ITEM -> {
                rightArm.xRot = rightArm.xRot * 0.5F - (float) (Math.PI / 10);
                rightArm.yRot = 0.0F;
            }
            case BLOCK -> poseBlockingArm(head, rightArm, true);
            case BOW_AND_ARROW -> {
                rightArm.yRot = -0.1F + hy;
                rightArm.xRot = (float) (-Math.PI / 2) + hx;
                if (leftArm != null) {
                    leftArm.yRot = 0.1F + hy + 0.4F;
                    leftArm.xRot = (float) (-Math.PI / 2) + hx;
                }
            }
            case THROW_TRIDENT -> {
                rightArm.xRot = rightArm.xRot * 0.5F - (float) Math.PI;
                rightArm.yRot = 0.0F;
            }
            case CROSSBOW_CHARGE -> {
                if (leftArm != null)
                    AnimationUtils.animateCrossbowCharge(rightArm, leftArm, humanoid.maxCrossbowChargeDuration, humanoid.ticksUsingItem, true);
            }
            case CROSSBOW_HOLD -> {
                if (leftArm != null && head != null) AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, true);
            }
            case SPYGLASS -> {
                rightArm.xRot = Mth.clamp(hx - 1.9198622F - (humanoid.isCrouching ? (float) (Math.PI / 12) : 0.0F), -2.4F, 3.3F);
                rightArm.yRot = hy - (float) (Math.PI / 12);
            }
            case TOOT_HORN -> {
                rightArm.xRot = Mth.clamp(hx, -1.2F, 1.2F) - 1.4835298F;
                rightArm.yRot = hy - (float) (Math.PI / 6);
            }
            case BRUSH -> {
                rightArm.xRot = rightArm.xRot * 0.5F - (float) (Math.PI / 5);
                rightArm.yRot = 0.0F;
            }
            case SPEAR -> {
                if (head != null)
                    SpearAnimations.thirdPersonHandUse(rightArm, head, true, humanoid.getUseItemStackForArm(HumanoidArm.RIGHT), humanoid);
            }
            default -> {
            }
        }
    }

    private static void poseLeftArm(
            @Nullable ModelPart head, @Nullable ModelPart rightArm, @Nullable ModelPart leftArm,
            HumanoidRenderState humanoid) {
        if (leftArm == null) return;
        float hx = head != null ? head.xRot : 0.0F;
        float hy = head != null ? head.yRot : 0.0F;
        switch (humanoid.leftArmPose) {
            case EMPTY -> leftArm.yRot = 0.0F;
            case ITEM -> {
                leftArm.xRot = leftArm.xRot * 0.5F - (float) (Math.PI / 10);
                leftArm.yRot = 0.0F;
            }
            case BLOCK -> poseBlockingArm(head, leftArm, false);
            case BOW_AND_ARROW -> {
                leftArm.yRot = 0.1F + hy;
                leftArm.xRot = (float) (-Math.PI / 2) + hx;
                if (rightArm != null) {
                    rightArm.yRot = -0.1F + hy - 0.4F;
                    rightArm.xRot = (float) (-Math.PI / 2) + hx;
                }
            }
            case THROW_TRIDENT -> {
                leftArm.xRot = leftArm.xRot * 0.5F - (float) Math.PI;
                leftArm.yRot = 0.0F;
            }
            case CROSSBOW_CHARGE -> {
                if (rightArm != null)
                    AnimationUtils.animateCrossbowCharge(rightArm, leftArm, humanoid.maxCrossbowChargeDuration, humanoid.ticksUsingItem, false);
            }
            case CROSSBOW_HOLD -> {
                if (rightArm != null && head != null)
                    AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, false);
            }
            case SPYGLASS -> {
                leftArm.xRot = Mth.clamp(hx - 1.9198622F - (humanoid.isCrouching ? (float) (Math.PI / 12) : 0.0F), -2.4F, 3.3F);
                leftArm.yRot = hy + (float) (Math.PI / 12);
            }
            case TOOT_HORN -> {
                leftArm.xRot = Mth.clamp(hx, -1.2F, 1.2F) - 1.4835298F;
                leftArm.yRot = hy + (float) (Math.PI / 6);
            }
            case BRUSH -> {
                leftArm.xRot = leftArm.xRot * 0.5F - (float) (Math.PI / 5);
                leftArm.yRot = 0.0F;
            }
            case SPEAR -> {
                if (head != null)
                    SpearAnimations.thirdPersonHandUse(leftArm, head, false, humanoid.getUseItemStackForArm(HumanoidArm.LEFT), humanoid);
            }
            default -> {
            }
        }
    }

    private static void poseBlockingArm(@Nullable ModelPart head, ModelPart arm, boolean right) {
        float hx = head != null ? head.xRot : 0.0F;
        float hy = head != null ? head.yRot : 0.0F;
        arm.xRot = arm.xRot * 0.5F - 0.9424779F + Mth.clamp(hx, (float) (-Math.PI * 4.0 / 9.0), 0.43633232F);
        arm.yRot = (right ? -30.0F : 30.0F) * (float) (Math.PI / 180.0) + Mth.clamp(hy, (float) (-Math.PI / 6), (float) (Math.PI / 6));
    }

    private static void setupAttackAnimation(
            @Nullable ModelPart head, @Nullable ModelPart body,
            @Nullable ModelPart rightArm, @Nullable ModelPart leftArm,
            ArmedEntityRenderState armed, float ageScale) {
        float attackTime = armed.attackTime;
        if (attackTime <= 0.0F) return;

        float bodyYRot = Mth.sin(Mth.sqrt(attackTime) * (float) (Math.PI * 2)) * 0.2F;
        if (armed.attackArm == HumanoidArm.LEFT) bodyYRot *= -1.0F;

        if (body != null) body.yRot += bodyYRot;
        if (rightArm != null) {
            rightArm.yRot += bodyYRot;
        }
        if (leftArm != null) {
            leftArm.yRot += bodyYRot;
            leftArm.xRot += bodyYRot;
        }

        ModelPart attackArmPart = armed.attackArm == HumanoidArm.RIGHT ? rightArm : leftArm;
        if (attackArmPart != null) {
            switch (armed.swingAnimationType) {
                case WHACK -> {
                    float swing = Ease.outQuart(attackTime);
                    float aa = Mth.sin(swing * (float) Math.PI);
                    float bb = head != null ? Mth.sin(attackTime * (float) Math.PI) * -(head.xRot - 0.7F) * 0.75F : 0.0F;
                    attackArmPart.xRot -= aa * 1.2F + bb;
                    attackArmPart.yRot += bodyYRot * 2.0F;
                    attackArmPart.zRot += Mth.sin(attackTime * (float) Math.PI) * -0.4F;
                }
                default -> {
                }
            }
        }
    }

    private static void applyCrouching(
            @Nullable ModelPart head, @Nullable ModelPart body,
            @Nullable ModelPart rightArm, @Nullable ModelPart leftArm,
            @Nullable ModelPart rightLeg, @Nullable ModelPart leftLeg) {
        if (body     != null) body.xRot     += 0.5F;
        if (rightArm != null) rightArm.xRot += 0.4F;
        if (leftArm  != null) leftArm.xRot  += 0.4F;
    }

    private static void applySwimming(
            @Nullable ModelPart rightArm, @Nullable ModelPart leftArm,
            @Nullable ModelPart rightLeg, @Nullable ModelPart leftLeg,
            float animPos, HumanoidRenderState state) {
        float swimAmount = state.swimAmount;
        float swimPos = animPos % 26.0F;
        float rSwim = state.rightArmPose != HumanoidModel.ArmPose.SPEAR
                && (state.attackArm != HumanoidArm.RIGHT || !(state.attackTime > 0.0F)) ? swimAmount : 0.0F;
        float lSwim = state.leftArmPose != HumanoidModel.ArmPose.SPEAR
                && (state.attackArm != HumanoidArm.LEFT || !(state.attackTime > 0.0F)) ? swimAmount : 0.0F;

        if (!state.isUsingItem) {
            if (swimPos < 14.0F) {
                float q = quadraticArmUpdate(swimPos) / quadraticArmUpdate(14.0F);
                if (leftArm != null) {
                    leftArm.xRot = Mth.rotLerpRad(lSwim, leftArm.xRot, 0.0F);
                    leftArm.yRot = Mth.rotLerpRad(lSwim, leftArm.yRot, (float) Math.PI);
                    leftArm.zRot = Mth.rotLerpRad(lSwim, leftArm.zRot, (float) Math.PI + 1.8707964F * q);
                }
                if (rightArm != null) {
                    rightArm.xRot = Mth.lerp(rSwim, rightArm.xRot, 0.0F);
                    rightArm.yRot = Mth.lerp(rSwim, rightArm.yRot, (float) Math.PI);
                    rightArm.zRot = Mth.lerp(rSwim, rightArm.zRot, (float) Math.PI - 1.8707964F * q);
                }
            } else if (swimPos < 22.0F) {
                float t = (swimPos - 14.0F) / 8.0F;
                if (leftArm != null) {
                    leftArm.xRot = Mth.rotLerpRad(lSwim, leftArm.xRot, (float) (Math.PI / 2) * t);
                    leftArm.yRot = Mth.rotLerpRad(lSwim, leftArm.yRot, (float) Math.PI);
                    leftArm.zRot = Mth.rotLerpRad(lSwim, leftArm.zRot, 5.012389F - 1.8707964F * t);
                }
                if (rightArm != null) {
                    rightArm.xRot = Mth.lerp(rSwim, rightArm.xRot, (float) (Math.PI / 2) * t);
                    rightArm.yRot = Mth.lerp(rSwim, rightArm.yRot, (float) Math.PI);
                    rightArm.zRot = Mth.lerp(rSwim, rightArm.zRot, 1.2707963F + 1.8707964F * t);
                }
            } else if (swimPos < 26.0F) {
                float t = (swimPos - 22.0F) / 4.0F;
                if (leftArm != null) {
                    leftArm.xRot = Mth.rotLerpRad(lSwim, leftArm.xRot, (float) (Math.PI / 2) * (1.0F - t));
                    leftArm.yRot = Mth.rotLerpRad(lSwim, leftArm.yRot, (float) Math.PI);
                    leftArm.zRot = Mth.rotLerpRad(lSwim, leftArm.zRot, (float) Math.PI);
                }
                if (rightArm != null) {
                    rightArm.xRot = Mth.lerp(rSwim, rightArm.xRot, (float) (Math.PI / 2) * (1.0F - t));
                    rightArm.yRot = Mth.lerp(rSwim, rightArm.yRot, (float) Math.PI);
                    rightArm.zRot = Mth.lerp(rSwim, rightArm.zRot, (float) Math.PI);
                }
            }
        }

        if (rightLeg != null)
            rightLeg.xRot = Mth.lerp(swimAmount, rightLeg.xRot, 0.3F * Mth.cos(animPos * 0.33333334F));
        if (leftLeg != null)
            leftLeg.xRot = Mth.lerp(swimAmount, leftLeg.xRot, 0.3F * Mth.cos(animPos * 0.33333334F + (float) Math.PI));
    }

    private static float quadraticArmUpdate(float x) {
        return -65.0F * x + x * x;
    }
}
