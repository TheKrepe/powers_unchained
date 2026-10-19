package net.threetag.pymtech.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.entity.DiscoTrailEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = PymTech.MODID, value = Dist.CLIENT)
public class DiscoTrailLayerRenderer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {

    public static Map<UUID, List<DiscoTrailEntity>> TO_EDIT = Maps.newHashMap();
    private static ArrayList<Class<? extends LivingEntity>> entitiesWithLayer = new ArrayList<>();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderEntityPre(RenderLivingEvent.Pre e) {
        if (!entitiesWithLayer.contains(e.getEntity().getClass())) {
            e.getRenderer().addLayer(new DiscoTrailLayerRenderer(e.getRenderer()));
            entitiesWithLayer.add(e.getEntity().getClass());
        }
    }

    public DiscoTrailLayerRenderer(IEntityRenderer<T, M> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (TO_EDIT.containsKey(entityIn.getUniqueID())) {
            List<DiscoTrailEntity> list = TO_EDIT.get(entityIn.getUniqueID());
            if (list == null || list.isEmpty() || !(this.entityRenderer instanceof LivingRenderer)) {
                TO_EDIT.remove(entityIn.getUniqueID());
                return;
            }
            list.forEach(e -> {
                e.renderer = (LivingRenderer) this.entityRenderer;
                e.limbSwing = limbSwing;
                e.limbSwingAmount = limbSwingAmount;
                e.partialTicks = partialTicks;
                e.ageInTicks = ageInTicks;
                e.netHeadYaw = netHeadYaw;
                e.headPitch = headPitch;
                e.renderYawOffset = entityIn.renderYawOffset;
            });
            TO_EDIT.remove(entityIn.getUniqueID());
        }
    }
}
