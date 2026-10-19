package lucraft.mods.heroesexpansion.client.render.layer;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityWebShoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
public class LayerRendererWebbing implements LayerRenderer<EntityLivingBase> {

    private static List<RenderLivingBase> entitiesWithLayer = new ArrayList<RenderLivingBase>();
    private static Minecraft mc = Minecraft.getMinecraft();
    public static ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/webbing.png");

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<EntityLivingBase> e) {
        if (!entitiesWithLayer.contains(e.getRenderer())) {
            e.getRenderer().addLayer(new LayerRendererWebbing(e.getRenderer()));
            entitiesWithLayer.add(e.getRenderer());
        }
    }

    // -------------------------------------------------------------------------------------------------------------

    public RenderLivingBase renderer;

    public LayerRendererWebbing(RenderLivingBase renderLivingBase) {
        this.renderer = renderLivingBase;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        for (Entity entity : entitylivingbaseIn.getPassengers()) {
            if (entity instanceof EntityWebShoot.EntityImpactWeb) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1F, 1F, 1F, 1F);
                GlStateManager.blendFunc(770, 771);
                Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
                this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                GlStateManager.blendFunc(771, 770);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                return;
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
