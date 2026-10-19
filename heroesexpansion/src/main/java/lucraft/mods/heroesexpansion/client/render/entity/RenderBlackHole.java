package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.entities.EntityBlackHole;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import javax.annotation.Nullable;

public class RenderBlackHole extends Render<EntityBlackHole> {

    public static int sphereIdOutside;
    public static int sphereIdInside;

    static {
        Sphere sphere = new Sphere();
        sphere.setDrawStyle(GLU.GLU_FILL);
        sphere.setNormals(GLU.GLU_SMOOTH);
        sphere.setOrientation(GLU.GLU_OUTSIDE);

        sphereIdOutside = GL11.glGenLists(1);
        GL11.glNewList(sphereIdOutside, GL11.GL_COMPILE);
        sphere.draw(0.5F, 32, 32);
        GL11.glEndList();

        sphere.setOrientation(GLU.GLU_INSIDE);
        sphereIdInside = GL11.glGenLists(1);
        GL11.glNewList(sphereIdInside, GL11.GL_COMPILE);
        sphere.draw(0.5F, 32, 32);
        GL11.glEndList();
    }

    public RenderBlackHole(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityBlackHole entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2D, z);
        renderBlackHole((entity.prevSize + (entity.getSize() - entity.prevSize) * partialTicks) / 100F);
        GlStateManager.popMatrix();
    }

    public static void renderBlackHole(float size) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(size, size, size);
        GlStateManager.disableTexture2D();

        GlStateManager.disableLighting();
        LCRenderHelper.setLightmapTextureCoords(240, 240);
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        GL11.glAlphaFunc(516, 0.003921569F);

        GlStateManager.color(0, 0, 0, 1);
        GL11.glCallList(sphereIdOutside);
        GL11.glCallList(sphereIdInside);

        GlStateManager.scale(1.2F, 1.2F, 1.2F);
        GlStateManager.color(0, 0, 1, 0.2F);
        GL11.glCallList(sphereIdOutside);
        GL11.glCallList(sphereIdInside);

        GlStateManager.disableBlend();
        LCRenderHelper.restoreLightmapTextureCoords();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityBlackHole entity) {
        return null;
    }
}
