package lucraft.mods.lucraftcore.sizechanging.render;

import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.capabilities.ISizeChanging;
import lucraft.mods.lucraftcore.sizechanging.entities.EntitySizeChanging;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public class LayerSizeChanging implements LayerRenderer<EntityLivingBase> {

    public RenderLivingBase<EntityLivingBase> renderer;
    public static Minecraft mc = Minecraft.getMinecraft();

    public LayerSizeChanging(RenderLivingBase<EntityLivingBase> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null)) {
            ISizeChanging data = entitylivingbaseIn.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null);

            for (EntitySizeChanging entity : data.getEntities()) {
                if (entity.renderer == null) {
                    entity.limbSwing = limbSwing;
                    entity.limbSwingAmount = limbSwingAmount;
                    entity.partialTicks = partialTicks;
                    entity.ageInTicks = ageInTicks;
                    entity.netHeadYaw = netHeadYaw;
                    entity.headPitch = headPitch;
                    entity.scale = scale;
                    entity.renderer = renderer;
                }
            }

            data.getSizeChanger().render(entitylivingbaseIn, renderer, data.getEntities(), limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
