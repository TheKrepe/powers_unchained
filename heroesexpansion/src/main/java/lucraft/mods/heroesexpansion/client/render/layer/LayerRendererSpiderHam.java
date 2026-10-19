package lucraft.mods.heroesexpansion.client.render.layer;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.superpowers.HESuperpowers;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.ResourceLocation;

public class LayerRendererSpiderHam implements LayerRenderer<EntityPig> {

    public static final ResourceLocation SPIDER_HAM_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/spider_ham.png");

    public RenderLivingBase renderer;

    public LayerRendererSpiderHam(RenderLivingBase renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityPig entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (SuperpowerHandler.hasSuperpower(entitylivingbaseIn, HESuperpowers.SPIDER_POWERS)) {
            GlStateManager.pushMatrix();
            GlStateManager.color(1F, 1F, 1F, 1F);
            Minecraft.getMinecraft().renderEngine.bindTexture(SPIDER_HAM_TEXTURE);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
