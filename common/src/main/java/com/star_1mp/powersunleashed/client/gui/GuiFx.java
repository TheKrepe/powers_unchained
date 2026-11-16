package com.star_1mp.powersunleashed.client.gui;

import com.star_1mp.powersunleashed.Constants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Shared GUI drawing helpers of our machine screens (Suit Creator, Tech Table, powers menu, JEI / REI art). */
public final class GuiFx {

    /** 16x2 tile: a clear row, then a faint blue-grey line ({@code 0x163C5A6E}); mcmeta scaling = tile. */
    private static final Identifier SCANLINES = Constants.id("ui/scanlines");

    private GuiFx() {
    }

    /**
     * The monitors' scan lines (a faint line on every 2nd row, from {@code y + 2}) inside the frame of a {@code w x h}
     * monitor at {@code x, y}. Optimisation: ONE tiled sprite instead of one {@code fill} per line (a tall monitor used
     * to cost 100 draw calls a frame).
     */
    public static void scanlines(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (w > 2 && h > 3) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, SCANLINES, x + 1, y + 1, w - 2, h - 2);
        }
    }

    /**
     * A live entity in the GUI box {@code x0,y0 .. x1,y1}, turned by {@code yawDeg}, {@code size} pixels per block (like
     * the inventory doll, without following the mouse).
     */
    public static void entity(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int size, float yawDeg,
            net.minecraft.world.entity.LivingEntity entity) {
        net.minecraft.client.renderer.entity.EntityRenderer<? super net.minecraft.world.entity.LivingEntity, ?> renderer =
                net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        net.minecraft.client.renderer.entity.state.EntityRenderState state = renderer.createRenderState(entity, 1.0F);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        if (state instanceof net.minecraft.client.renderer.entity.state.LivingEntityRenderState living) {
            living.bodyRot = 180.0F + yawDeg;
            living.yRot = 0.0F;
            living.xRot = 0.0F;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1.0F;
        }
        org.joml.Quaternionf rotation = new org.joml.Quaternionf().rotateZ((float) Math.PI);
        org.joml.Vector3f translation = new org.joml.Vector3f(0.0F, state.boundingBoxHeight / 2.0F + 0.0625F, 0.0F);
        g.entity(state, size, translation, rotation, null, x0, y0, x1, y1);
    }
}
