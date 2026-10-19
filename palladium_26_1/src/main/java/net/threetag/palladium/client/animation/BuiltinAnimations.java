package net.threetag.palladium.client.animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.client.util.ModelUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BuiltinAnimations {

    public static void setupAnim(HumanoidModel<?> model, HumanoidRenderState state) {
        var aim = state.getRenderDataOrDefault(PalladiumRenderStateKeys.AIM, new Float[]{0F, 0F});
        var hidden = state.getRenderDataOrDefault(PalladiumRenderStateKeys.HIDDEN_MODEL_PARTS, Collections.emptySet());
        var shrinkOverlay = state.getRenderDataOrDefault(PalladiumRenderStateKeys.SHRINK_OVERLAY, Collections.emptyMap());

        aim(model, aim);
        hiddenModelParts(model, hidden);
        shrinkOverlay(model, shrinkOverlay);
    }

    public static void aim(HumanoidModel<?> model, Float[] aim) {
        float left = aim[0];
        float right = aim[1];

        if (left > 0F) {
            model.leftArm.xRot = Mth.lerp(left, model.leftArm.xRot, (float) (model.head.xRot - Math.PI / 2F));
            model.leftArm.yRot = Mth.lerp(left, model.leftArm.yRot, model.head.yRot);
            model.leftArm.zRot = Mth.lerp(left, model.leftArm.zRot, model.head.zRot);
        }

        if (right > 0F) {
            model.rightArm.xRot = Mth.lerp(right, model.rightArm.xRot, (float) (model.head.xRot - Math.PI / 2F));
            model.rightArm.yRot = Mth.lerp(right, model.rightArm.yRot, model.head.yRot);
            model.rightArm.zRot = Mth.lerp(right, model.rightArm.zRot, model.head.zRot);
        }
    }

    public static void hiddenModelParts(HumanoidModel<?> model, Set<String> hidden) {
        for (String name : hidden) {
            var part = ModelUtil.getPartFromModel(model, name);

            if (part != null) {
                part.visible = false;
            }
        }
    }

    public static void shrinkOverlay(HumanoidModel<?> model, Map<PlayerModelPart, Float> parts) {
        if (parts.isEmpty()) {
            return;
        }

        scaleDown(model.hat, parts.getOrDefault(PlayerModelPart.HAT, 0F));

        if (model instanceof PlayerModel playerModel) {
            scaleDown(playerModel.jacket, parts.getOrDefault(PlayerModelPart.JACKET, 0F));
            scaleDown(playerModel.rightSleeve, parts.getOrDefault(PlayerModelPart.RIGHT_SLEEVE, 0F));
            scaleDown(playerModel.leftSleeve, parts.getOrDefault(PlayerModelPart.LEFT_SLEEVE, 0F));
            scaleDown(playerModel.rightPants, parts.getOrDefault(PlayerModelPart.RIGHT_PANTS_LEG, 0F));
            scaleDown(playerModel.leftPants, parts.getOrDefault(PlayerModelPart.LEFT_PANTS_LEG, 0F));
        }
    }

    private static void scaleDown(ModelPart target, float progress) {
        if (progress > 0F) {
            target.xScale = Mth.lerp(progress, target.xScale, 0.9F);
            target.yScale = Mth.lerp(progress, target.yScale, 0.9F);
            target.zScale = Mth.lerp(progress, target.zScale, 0.9F);
        }
    }

    public static void applyFirstPersonSleeveShrink(ModelPart sleeve, float progress) {
        float scale = Mth.lerp(progress, 1.0F, 0.9F);
        sleeve.xScale = scale;
        sleeve.yScale = scale;
        sleeve.zScale = scale;
    }

}
