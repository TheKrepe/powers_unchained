package lucraft.mods.lucraftcore.sizechanging.render;

import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SizeChangingRenderer {

    public static Map<Class<? extends EntityLivingBase>, Float> SHADOW_SIZES = new HashMap<>();
    private static ArrayList<Class<? extends EntityLivingBase>> entitiesWithLayer = new ArrayList<>();

    @SubscribeEvent
    public void renderEntityPre(RenderLivingEvent.Pre e) {
        if (!e.getEntity().hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null))
            return;

        float scale = e.getEntity().getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getRenderSize(e.getPartialRenderTick());

        if (scale <= 0)
            return;

        if (!SHADOW_SIZES.containsKey(e.getEntity().getClass()))
            SHADOW_SIZES.put(e.getEntity().getClass(), e.getRenderer().shadowSize);

        e.getRenderer().shadowSize = SHADOW_SIZES.get(e.getEntity().getClass()) * scale;

        GlStateManager.pushMatrix();

        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate((e.getX() / scale) - e.getX(), (e.getY() / scale) - e.getY(), (e.getZ() / scale) - e.getZ());
        if (e.getEntity().isSneaking()) {
            GlStateManager.translate(0, 0.125F / scale, 0);
            GlStateManager.translate(0, -0.125F, 0);
        }
        double theamountofshit = 5D;

        if (!entitiesWithLayer.contains(e.getEntity().getClass())) {
            e.getRenderer().addLayer(new LayerSizeChanging(e.getRenderer()));
            entitiesWithLayer.add(e.getEntity().getClass());
        }

    }

    @SubscribeEvent
    public void renderEntityPost(RenderLivingEvent.Post e) {
        if (!e.getEntity().hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null))
            return;
        if (e.getEntity().getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getRenderSize(e.getPartialRenderTick()) <= 0)
            return;
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void renderEntityNamePre(RenderLivingEvent.Specials.Pre e) {
        if (!e.getEntity().hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null))
            return;
        float scale = e.getEntity().getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getRenderSize(e.getPartialRenderTick());

        if (scale <= 0)
            return;

        GlStateManager.pushMatrix();

        boolean flag = e.getEntity().isSneaking();
        float vanillaOffset = e.getEntity().height + 0.5F - (flag ? 0.25F : 0.0F);

        GlStateManager.translate(0, -vanillaOffset, 0);

        float adjustedOffset = (e.getEntity().height / scale) + (0.5F) - (flag ? 0.25F : 0F);

        GlStateManager.translate(0, adjustedOffset, 0);
    }

    @SubscribeEvent
    public void renderEntityNamePost(RenderLivingEvent.Specials.Post e) {
        if (!e.getEntity().hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null))
            return;
        if (e.getEntity().getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getRenderSize(e.getPartialRenderTick()) <= 0)
            return;

        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void setRotationAngles(RenderModelEvent.SetRotationAngels e) {
        if (e.getEntity().hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null)) {
            float size = e.getEntity().getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getRenderSize(LCRenderHelper.renderTick);

            if (size <= 0F)
                return;

            float d = size < 1F ? (0.5F / size) : (1F / size);
            e.limbSwing *= d;
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void setupCamera(EntityViewRenderEvent.CameraSetup e) {
        if (!e.getEntity().hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null))
            return;

        float scale = e.getEntity().getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getRenderSize((float) e.getRenderPartialTicks());

        if (scale <= 0)
            return;

        float f = 1F / scale;
        GlStateManager.scale(f, f, f);

        if (!(e.getEntity() instanceof EntityLivingBase && ((EntityLivingBase) e.getEntity()).isPlayerSleeping()) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && e.getEntity().hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null)) {
            GlStateManager.translate(0, 0, -0.05F);
            GlStateManager.translate(0, 0, (scale * 0.05F));
        }
    }
}
