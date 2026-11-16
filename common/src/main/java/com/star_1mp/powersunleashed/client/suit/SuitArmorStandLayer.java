package com.star_1mp.powersunleashed.client.suit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunleashed.item.SuitPieceItem;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Draws suit pieces placed on an armor stand (normal and small), shaped like the player model. */
public class SuitArmorStandLayer extends RenderLayer<ArmorStandRenderState, ArmorStandArmorModel> {

    private final Map<SuitModel.Part, SuitArmorStandModel> models = new EnumMap<>(SuitModel.Part.class);
    private final Map<SuitModel.Part, SuitArmorStandModel> smallModels = new EnumMap<>(SuitModel.Part.class);

    public SuitArmorStandLayer(RenderLayerParent<ArmorStandRenderState, ArmorStandArmorModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        for (SuitModel.Part part : SuitModel.Part.values()) {
            var layer = switch (part) {
                case FEET -> SuitModelLayers.ARMOR_STAND_BOOTS;
                default -> SuitModelLayers.ARMOR_STAND;
            };
            var smallLayer = switch (part) {
                case FEET -> SuitModelLayers.ARMOR_STAND_SMALL_BOOTS;
                default -> SuitModelLayers.ARMOR_STAND_SMALL;
            };
            this.models.put(part, new SuitArmorStandModel(modelSet.bakeLayer(layer), part));
            this.smallModels.put(part, new SuitArmorStandModel(modelSet.bakeLayer(smallLayer), part));
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, ArmorStandRenderState state, float yRot, float xRot) {
        this.submitPiece(poseStack, collector, lightCoords, state, state.chestEquipment, EquipmentSlot.CHEST, SuitModel.Part.CHEST);
        this.submitPiece(poseStack, collector, lightCoords, state, state.legsEquipment, EquipmentSlot.LEGS, SuitModel.Part.LEGS);
        this.submitPiece(poseStack, collector, lightCoords, state, state.feetEquipment, EquipmentSlot.FEET, SuitModel.Part.FEET);
        this.submitPiece(poseStack, collector, lightCoords, state, state.headEquipment, EquipmentSlot.HEAD, SuitModel.Part.HEAD);
    }

    private void submitPiece(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, ArmorStandRenderState state,
            ItemStack stack, EquipmentSlot slot, SuitModel.Part part) {
        SuitPieceItem piece = SuitPieceItem.get(stack, slot);
        if (piece == null) {
            return;
        }
        Identifier texture = SuitRendering.texture(piece.getSuit(), part, false);
        SuitArmorStandModel model = (state.isSmall ? this.smallModels : this.models).get(part);
        collector.submitModel(model, state, poseStack, SuitRendering.renderType(texture, stack),
                lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    }
}
