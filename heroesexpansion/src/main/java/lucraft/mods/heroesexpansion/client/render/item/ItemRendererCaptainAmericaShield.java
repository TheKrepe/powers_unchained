package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.lucraftcore.extendedinventory.render.ExtendedInventoryItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class ItemRendererCaptainAmericaShield implements ExtendedInventoryItemRendererRegistry.IItemExtendedInventoryRenderer {

    @Override
    public void render(EntityPlayer player, RenderLivingBase<?> render, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean isHead) {
        if (isHead)
            return;

        GlStateManager.pushMatrix();

        if (player.isSneaking()) {
            GlStateManager.rotate(27, 1, 0, 0);
            GlStateManager.translate(0, 0.2F, 0);
        }

        float s = 0.9F;
        GlStateManager.scale(s, s, s);
        if (!player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty())
            GlStateManager.translate(0, 0, 0.1D);
        GlStateManager.translate(0, 0.5D, 0.2D);
        GlStateManager.rotate(90, 1, 0, 0);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

        GlStateManager.popMatrix();
    }

}
