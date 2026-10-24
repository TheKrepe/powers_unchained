package com.star_1mp.powersunchained.client.suit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunchained.item.SuitPieceItem;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Draws worn suit pieces on top of the player's skin. Added to every player and mannequin renderer. */
public class SuitLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final SuitModelSet models;
    private final boolean slim;

    public SuitLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, EntityModelSet modelSet, boolean slim) {
        super(renderer);
        this.models = SuitModelSet.bake(modelSet, slim);
        this.slim = slim;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        this.submitPiece(poseStack, collector, lightCoords, state, state.chestEquipment, EquipmentSlot.CHEST, SuitModel.Part.CHEST);
        this.submitPiece(poseStack, collector, lightCoords, state, state.legsEquipment, EquipmentSlot.LEGS, SuitModel.Part.LEGS);
        this.submitPiece(poseStack, collector, lightCoords, state, state.feetEquipment, EquipmentSlot.FEET, SuitModel.Part.FEET);
        this.submitPiece(poseStack, collector, lightCoords, state, state.headEquipment, EquipmentSlot.HEAD, SuitModel.Part.HEAD);
    }

    private void submitPiece(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state,
            ItemStack stack, EquipmentSlot slot, SuitModel.Part part) {
        SuitPieceItem piece = SuitPieceItem.get(stack, slot);
        if (piece == null) {
            return;
        }
        Identifier texture = SuitRendering.texture(piece.getSuit(), part, this.slim);
        collector.submitModel(this.models.get(part), state, poseStack, SuitRendering.renderType(texture, stack),
                lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    }
}
