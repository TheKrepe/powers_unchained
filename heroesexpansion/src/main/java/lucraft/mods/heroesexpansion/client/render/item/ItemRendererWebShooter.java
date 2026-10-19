package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.lucraftcore.extendedinventory.render.ExtendedInventoryItemRendererRegistry;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemRendererWebShooter implements ExtendedInventoryItemRendererRegistry.IItemExtendedInventoryRenderer {

    public ResourceLocation normal;
    public ResourceLocation smallArms;

    public ItemRendererWebShooter(ResourceLocation normal, ResourceLocation smallArms) {
        this.normal = normal;
        this.smallArms = smallArms;
    }

    @Override
    public void render(EntityPlayer player, RenderLivingBase<?> render, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean isHead) {
        if (isHead)
            return;

        GlStateManager.pushMatrix();
        float s = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() ? 0.25F : 0.3F;
        ModelPlayer model = new ModelPlayer(s, PlayerHelper.hasSmallArms(player));
        model.setModelAttributes(render.getMainModel());
        ModelBase.copyModelAngles(model.bipedRightArm, model.bipedRightArmwear);
        Minecraft.getMinecraft().renderEngine.bindTexture(PlayerHelper.hasSmallArms(player) ? smallArms : normal);
        model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 0.0625F);
        GlStateManager.popMatrix();
    }

}
