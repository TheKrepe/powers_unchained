package com.star_1mp.powersunchained.client.suit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunchained.item.SuitPieceItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Draws the suit sleeve on the player's own arm in first person. */
public final class SuitFirstPersonRenderer {

    private static @Nullable SuitModelSet wideModels;
    private static @Nullable SuitModelSet slimModels;

    private SuitFirstPersonRenderer() {
    }

    /** Re-bakes the models; called whenever entity renderers are (re)created on resource reload. */
    public static void bakeModels(EntityModelSet modelSet) {
        wideModels = SuitModelSet.bake(modelSet, false);
        slimModels = SuitModelSet.bake(modelSet, true);
    }

    /**
     * Draws the bare skin arm (without the skin's sleeve layer) and the suit arm over it.
     *
     * @return {@code true} if the arm was drawn here and vanilla must skip drawing it
     */
    public static boolean renderArm(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, Identifier skinTexture,
            ModelPart skinArm, HumanoidArm arm, @Nullable AvatarRenderState state) {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack chest = state != null ? state.chestEquipment : ItemStack.EMPTY;
        if (SuitPieceItem.get(chest, EquipmentSlot.CHEST) == null && player != null) {
            chest = player.getItemBySlot(EquipmentSlot.CHEST);
        }
        boolean slim;
        if (state != null) {
            slim = state.skin.model() == PlayerModelType.SLIM;
        } else if (player != null) {
            slim = player.getSkin().model() == PlayerModelType.SLIM;
        } else {
            return false;
        }

        SuitPieceItem piece = SuitPieceItem.get(chest, EquipmentSlot.CHEST);
        SuitModelSet models = slim ? slimModels : wideModels;
        if (piece == null || models == null) {
            return false;
        }

        boolean right = arm == HumanoidArm.RIGHT;
        float zRot = right ? 0.1F : -0.1F;

        // 1. The skin arm, without its outer sleeve layer (it would poke through the suit).
        skinArm.resetPose();
        skinArm.visible = true;
        String sleeveName = right ? "right_sleeve" : "left_sleeve";
        if (skinArm.hasChild(sleeveName)) {
            skinArm.getChild(sleeveName).visible = false;
        }
        skinArm.zRot = zRot;
        collector.submitModelPart(skinArm, poseStack, RenderTypes.entityTranslucent(skinTexture), lightCoords, OverlayTexture.NO_OVERLAY, null);

        // 2. The suit arm on top.
        SuitModel model = models.get(SuitModel.Part.CHEST);
        ModelPart suitArm = right ? model.rightArm : model.leftArm;
        ModelPart suitSleeve = right ? model.rightSleeve : model.leftSleeve;
        suitArm.resetPose();
        suitArm.visible = true;
        suitSleeve.visible = true;
        suitArm.zRot = zRot;
        Identifier texture = SuitRendering.texture(piece.getSuit(), SuitModel.Part.CHEST, slim);
        collector.submitModelPart(suitArm, poseStack, SuitRendering.renderType(texture, chest), lightCoords, OverlayTexture.NO_OVERLAY, null);
        return true;
    }
}
