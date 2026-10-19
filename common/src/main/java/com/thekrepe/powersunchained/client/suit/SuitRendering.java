package com.thekrepe.powersunchained.client.suit;

import com.thekrepe.powersunchained.item.SuitPieceItem;
import com.thekrepe.powersunchained.suit.Suit;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Shared helpers for drawing suits. */
public final class SuitRendering {

    private SuitRendering() {
    }

    /**
     * Hides the player skin's outer (second) layer under the worn suit pieces, like Palladium:
     * mask → hat layer, suit → jacket + sleeves, pants/boots → pants layer.
     * Called after vanilla has filled the render state.
     */
    public static void hideCoveredSkinLayers(AvatarRenderState state) {
        if (SuitPieceItem.get(state.headEquipment, EquipmentSlot.HEAD) != null) {
            state.showHat = false;
        }
        if (SuitPieceItem.get(state.chestEquipment, EquipmentSlot.CHEST) != null) {
            state.showJacket = false;
            state.showLeftSleeve = false;
            state.showRightSleeve = false;
        }
        if (SuitPieceItem.get(state.legsEquipment, EquipmentSlot.LEGS) != null
                || SuitPieceItem.get(state.feetEquipment, EquipmentSlot.FEET) != null) {
            state.showLeftPants = false;
            state.showRightPants = false;
        }
    }

    public static Identifier texture(Suit suit, SuitModel.Part part, boolean slim) {
        if (part == SuitModel.Part.FEET && suit.bootsTexture() != null) {
            return suit.bootsTexture();
        }
        return suit.texture(slim);
    }

    public static RenderType renderType(Identifier texture, ItemStack stack) {
        return stack.hasFoil() ? RenderTypes.armorCutoutNoCullGlint(texture) : RenderTypes.armorCutoutNoCull(texture);
    }
}
