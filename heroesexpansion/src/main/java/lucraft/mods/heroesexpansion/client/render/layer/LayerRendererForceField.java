package lucraft.mods.heroesexpansion.client.render.layer;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityForceField;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.render.SuperpowerRenderer;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.render.ModelCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static lucraft.mods.lucraftcore.superpowers.render.SuperpowerRenderLayer.WHITE_TEX;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
public class LayerRendererForceField implements LayerRenderer<EntityLivingBase> {

    private static List<RenderLivingBase> entitiesWithLayer = new ArrayList<RenderLivingBase>();
    private static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<EntityLivingBase> e) {
        if (!entitiesWithLayer.contains(e.getRenderer())) {
            e.getRenderer().addLayer(new LayerRendererForceField(e.getRenderer()));
            entitiesWithLayer.add(e.getRenderer());
        }
    }

    // -------------------------------------------------------------------------------------------------------------

    public RenderLivingBase renderer;

    public LayerRendererForceField(RenderLivingBase renderLivingBase) {
        this.renderer = renderLivingBase;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelBase model = getModel(entitylivingbaseIn);
        model.isChild = entitylivingbaseIn.isChild();
        model.setModelAttributes(renderer.getMainModel());

        mc.renderEngine.bindTexture(WHITE_TEX);
        for (AbilityForceField abilityForceField : Ability.getAbilitiesFromClass(Ability.getAbilities(entitylivingbaseIn), AbilityForceField.class)) {
            if (abilityForceField != null && abilityForceField.isUnlocked() && abilityForceField.isEnabled()) {
                GlStateManager.pushMatrix();
                LCRenderHelper.setLightmapTextureCoords(240, 240);
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.blendFunc(770, 1);
                Color color = abilityForceField.getDataManager().get(AbilityForceField.COLOR);
                float f = abilityForceField.getDataManager().get(AbilityForceField.OPACITY);
                float opacity = MathHelper.clamp(MathHelper.sin((entitylivingbaseIn.ticksExisted + partialTicks) / 10F) * 0.1F + 0.1F + f, 0.11F, 1F);
                GlStateManager.color((float) color.getRed() / 255F, (float) color.getGreen() / 255F, (float) color.getBlue() / 255F, opacity);
                SuperpowerRenderer.overrideSkin = false;
                model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                SuperpowerRenderer.overrideSkin = true;
                LCRenderHelper.restoreLightmapTextureCoords();
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    public ModelBase getModel(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            float size = 0.5F;
            String key = "player_" + (PlayerHelper.hasSmallArms((EntityPlayer) entity) ? "steve" : "alex") + "_" + size;
            if (ModelCache.getModel(key) == null)
                return ModelCache.storeModel(key, new ModelPlayer(size, PlayerHelper.hasSmallArms((EntityPlayer) entity)));
            else
                return ModelCache.getModel(key);
        } else {
            return this.renderer.getMainModel();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
