package com.thekrepe.powersunchained.client.suit;

import com.thekrepe.powersunchained.item.SuitPieceItem;
import com.thekrepe.powersunchained.suit.Suit;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Shared helpers for drawing suits. */
public final class SuitRendering {

    private SuitRendering() {
    }

    /**
     * Fills the render state with the worn suit pieces and hides what they cover. Must run after vanilla
     * has extracted the state (NeoForge: render state modifier).
     * <ul>
     *   <li>Vanilla only copies equipment that renders as regular armor into the state, so suit pieces
     *       are copied from the entity's slots here.</li>
     *   <li>Hides the player skin's outer (second) layer under the worn pieces, like Palladium:
     *       mask → hat layer, suit → jacket + sleeves, pants/boots → pants layer.</li>
     *   <li>Stops vanilla from drawing the mask as a flat item on the head (it does that for any head
     *       item that isn't regular armor).</li>
     * </ul>
     */
    public static void extract(LivingEntity entity, AvatarRenderState state) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = entity.getItemBySlot(EquipmentSlot.FEET);
        boolean hasHead = SuitPieceItem.get(head, EquipmentSlot.HEAD) != null;
        boolean hasChest = SuitPieceItem.get(chest, EquipmentSlot.CHEST) != null;
        boolean hasLegs = SuitPieceItem.get(legs, EquipmentSlot.LEGS) != null;
        boolean hasFeet = SuitPieceItem.get(feet, EquipmentSlot.FEET) != null;

        if (hasHead) {
            state.headEquipment = head;
            state.headItem.clear();
            state.showHat = false;
        }
        if (hasChest) {
            state.chestEquipment = chest;
            state.showJacket = false;
            state.showLeftSleeve = false;
            state.showRightSleeve = false;
        }
        if (hasLegs) {
            state.legsEquipment = legs;
        }
        if (hasFeet) {
            state.feetEquipment = feet;
        }
        if (hasLegs || hasFeet) {
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
