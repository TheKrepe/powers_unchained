package com.star_1mp.powersunchained.client.techtable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunchained.client.gadget.WebShootersModel;
import com.star_1mp.powersunchained.client.suitcreator.SuitHologram;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/**
 * Tech Table, Customization tab: a player-shaped hologram (Steve / Alex, no entity behind it) wearing the gadget, like
 * the Suit Creator's hologram. The gadget is drawn by {@link Layer} (added to the player renderers), which only draws on
 * render states with the id {@link #ID} + pose — real players get their web-shooters drawn by Curios.
 */
public final class GadgetHologram {

    /** Render state id of the hologram (far from real entity ids and from the suit hologram's). */
    public static final int ID = SuitHologram.ID_BASE + 1000;

    private static final boolean[] NO_SUIT = {false, false, false, false};

    private GadgetHologram() {
    }

    /** Draws the hologram in the rectangle, feet at {@code y1 - 2}. */
    public static void draw(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float scale, boolean slim, float yaw, float age,
            SuitHologram.Pose pose) {
        SuitHologram.draw(graphics, x0, y0, x1, y1, scale, null, NO_SUIT, slim, pose, yaw, age, ID + pose.ordinal());
    }

    /** Whether a render state is this hologram (id {@code ID + pose}). */
    public static boolean is(AvatarRenderState state) {
        int rel = state.id - ID;
        return rel >= 0 && rel < SuitHologram.Pose.values().length;
    }

    /** Draws the web-shooters on the hologram (player and mannequin renderers). */
    public static final class Layer extends RenderLayer<AvatarRenderState, PlayerModel> {

        public Layer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
            if (is(state)) {
                WebShootersModel.render(poseStack, collector, lightCoords, state);
            }
        }
    }
}
