package com.thekrepe.powersunchained.client.suit;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/**
 * A player-shaped model (same UV layout as a player skin) that only shows the body parts
 * covered by one suit piece. One instance per piece, because models are drawn deferred and
 * {@link #setupAnim} is re-run right before drawing.
 */
public class SuitModel extends PlayerModel {

    public enum Part {
        /** Head + hat layer. */
        HEAD,
        /** Body + jacket, both arms + sleeves. */
        CHEST,
        /** Both legs + pants layer. */
        LEGS,
        /** Both legs + pants layer, drawn slightly larger than {@link #LEGS}. */
        FEET
    }

    private final Part part;

    public SuitModel(ModelPart root, boolean slim, Part part) {
        super(root, slim);
        this.part = part;
    }

    public Part getPart() {
        return this.part;
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        // Copies the player's current pose (walking, sneaking, swimming, arm poses, ...).
        super.setupAnim(state);
        // PlayerModel also hides hat/jacket/... from the render state flags, which we turn off
        // for the player's skin — so the suit's own visibility is set explicitly afterwards.
        this.applyVisibility();
    }

    public void applyVisibility() {
        boolean head = this.part == Part.HEAD;
        boolean chest = this.part == Part.CHEST;
        boolean legs = this.part == Part.LEGS || this.part == Part.FEET;

        this.head.visible = head;
        this.hat.visible = head;
        this.body.visible = chest;
        this.jacket.visible = chest;
        this.rightArm.visible = chest;
        this.leftArm.visible = chest;
        this.rightSleeve.visible = chest;
        this.leftSleeve.visible = chest;
        this.rightLeg.visible = legs;
        this.leftLeg.visible = legs;
        this.rightPants.visible = legs;
        this.leftPants.visible = legs;
    }
}
