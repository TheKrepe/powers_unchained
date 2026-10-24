package com.star_1mp.powersunchained.client.suit;

import com.star_1mp.powersunchained.item.SuitPieceItem;
import com.star_1mp.powersunchained.suit.Suit;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
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
        boolean[] worn = extractEquipment(entity, state);
        if (worn[0]) {
            state.showHat = false;
        }
        if (worn[1]) {
            state.showJacket = false;
            state.showLeftSleeve = false;
            state.showRightSleeve = false;
        }
        if (worn[2] || worn[3]) {
            state.showLeftPants = false;
            state.showRightPants = false;
        }
    }

    /**
     * Copies worn suit pieces into a humanoid render state (players, mannequins, armor stands) and stops the
     * mask from being drawn as a flat item on the head.
     *
     * @return which pieces are worn: head, chest, legs, feet
     */
    public static boolean[] extractEquipment(LivingEntity entity, HumanoidRenderState state) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = entity.getItemBySlot(EquipmentSlot.FEET);
        boolean[] worn = {
                SuitPieceItem.get(head, EquipmentSlot.HEAD) != null,
                SuitPieceItem.get(chest, EquipmentSlot.CHEST) != null,
                SuitPieceItem.get(legs, EquipmentSlot.LEGS) != null,
                SuitPieceItem.get(feet, EquipmentSlot.FEET) != null};
        if (worn[0]) {
            state.headEquipment = head;
            state.headItem.clear();
        }
        if (worn[1]) {
            state.chestEquipment = chest;
        }
        if (worn[2]) {
            state.legsEquipment = legs;
        }
        if (worn[3]) {
            state.feetEquipment = feet;
        }
        return worn;
    }

    public static Identifier texture(Suit suit, SuitModel.Part part, boolean slim) {
        return switch (part) {
            case HEAD -> suit.maskTexture();
            case CHEST -> slim ? suit.chestplateSlimTexture() : suit.chestplateTexture();
            case LEGS -> suit.pantsTexture();
            case FEET -> suit.bootsTexture();
        };
    }

    public static RenderType renderType(Identifier texture, ItemStack stack) {
        return stack.hasFoil() ? RenderTypes.armorCutoutNoCullGlint(texture) : RenderTypes.armorCutoutNoCull(texture);
    }
}
