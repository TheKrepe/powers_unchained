package lucraft.mods.heroesexpansion.client.render.layer;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityBlindness;
import lucraft.mods.heroesexpansion.abilities.AbilitySpiderSense;
import lucraft.mods.heroesexpansion.abilities.AbilityXray;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
public class LayerRendererHighlightEntity implements LayerRenderer<EntityLivingBase> {

    private static List<RenderLivingBase> entitiesWithLayer = new ArrayList<RenderLivingBase>();
    private static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<EntityLivingBase> e) {
        if (!entitiesWithLayer.contains(e.getRenderer())) {
            e.getRenderer().addLayer(new LayerRendererHighlightEntity(e.getRenderer()));
            entitiesWithLayer.add(e.getRenderer());
        }
    }

    // -------------------------------------------------------------------------------------------------------------

    public RenderLivingBase renderer;

    public LayerRendererHighlightEntity(RenderLivingBase renderLivingBase) {
        this.renderer = renderLivingBase;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn == mc.player) {
            return;
        }

        renderBlindness(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        renderSpiderSense(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        renderXrayVision(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
    }

    public void renderSpiderSense(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        SuitSet suitSet = SuitSet.getSuitSet(mc.player);

        if (suitSet == null || suitSet.getData() == null || !suitSet.getData().getBoolean("spider_sense"))
            return;

        for (AbilitySpiderSense spiderSense : Ability.getAbilitiesFromClass(Ability.getAbilities(mc.player), AbilitySpiderSense.class)) {
            if (spiderSense != null && spiderSense.isUnlocked() && spiderSense.getProgress(partialTicks) > 0F && spiderSense.isThreat(entitylivingbaseIn)) {
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.disableTexture2D();
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.color(1F, 1F, 1F, 0.5F * (1F - spiderSense.getProgress(partialTicks)));
                GlStateManager.blendFunc(770, 771);
                this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                GlStateManager.blendFunc(771, 770);
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
                return;
            }
        }
    }

    public void renderBlindness(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (AbilityBlindness.Renderer.renderBlindness()) {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.blendFunc(770, 771);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.blendFunc(771, 770);
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    public void renderXrayVision(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (AbilityXray.Renderer.xray) {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.color(1F, 1F, 1F, 0.5F);
            GlStateManager.blendFunc(770, 771);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.blendFunc(771, 770);
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
