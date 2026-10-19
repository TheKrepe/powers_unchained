package net.threetag.pymtech.util;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.threetag.pymtech.PymTech;
import org.lwjgl.opengl.GL11;

public class PTRenderTypes extends RenderType {

    public PTRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static final RenderType DISCO_TRAIL = makeType(PymTech.MODID + ":disco_trail", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true, true,
            State.getBuilder().texturing(TexturingState.ENTITY_GLINT_TEXTURING)
                    .transparency(TransparencyState.LIGHTNING_TRANSPARENCY)
                    .alpha(RenderState.DEFAULT_ALPHA)
                    .lightmap(RenderState.LIGHTMAP_ENABLED).build(false));

}
