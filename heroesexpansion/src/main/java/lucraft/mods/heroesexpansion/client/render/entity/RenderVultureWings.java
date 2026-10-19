package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelVultureWings;
import lucraft.mods.heroesexpansion.entities.EntityVultureWings;
import lucraft.mods.lucraftcore.superpowers.render.RenderSuperpowerLayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
public class RenderVultureWings extends RenderLiving<EntityVultureWings> {

    public static ModelVultureWings MODEL = new ModelVultureWings();
    public static ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/vulture_wings.png");

    public RenderVultureWings(RenderManager renderManager) {
        super(renderManager, MODEL, 0.5F);
    }

    @Override
    public void doRender(EntityVultureWings entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.getControllingPassenger() == null)
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected void preRenderCallback(EntityVultureWings entitylivingbaseIn, float partialTickTime) {
        float f = 2F;
        GlStateManager.scale(f, f, f);
        GlStateManager.translate(0, 0.7F, 0);
    }

    @Override
    protected void applyRotations(EntityVultureWings entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityVultureWings entity) {
        return TEXTURE;
    }

    @SubscribeEvent
    public static void onRenderLayer(RenderSuperpowerLayerEvent e) {
        if (e.getPlayer().getRidingEntity() != null && e.getPlayer().getRidingEntity() instanceof EntityVultureWings) {
            GlStateManager.pushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
            float f = 2F;
            e.getRenderPlayer().getMainModel().bipedBody.postRender(e.getScale());
            GlStateManager.scale(f, f, f);
            MODEL.render(e.getPlayer().getRidingEntity(), e.getLimbSwing(), e.getLimbSwingAmount(), e.getAgeInTicks(), e.getNetHeadYaw(), e.getHeadPitch(), e.getScale());
            GlStateManager.popMatrix();
        }
    }

}
