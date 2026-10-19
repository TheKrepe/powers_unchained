package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelQuiver;
import lucraft.mods.heroesexpansion.items.InventoryQuiver;
import lucraft.mods.heroesexpansion.items.ItemHEArrow;
import lucraft.mods.heroesexpansion.items.ItemQuiver;
import lucraft.mods.lucraftcore.extendedinventory.render.ExtendedInventoryItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemRendererQuiver implements ExtendedInventoryItemRendererRegistry.IItemExtendedInventoryRenderer {

    public static ModelQuiver MODEL = new ModelQuiver();
    public static ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/quiver.png");
    public static ResourceLocation TEXTURE_OVERLAY = new ResourceLocation(HeroesExpansion.MODID, "textures/models/quiver_overlay.png");

    @Override
    public void render(EntityPlayer player, RenderLivingBase<?> render, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean isHead) {
        if (isHead)
            return;

        GlStateManager.pushMatrix();

        if (player.isSneaking()) {
            GlStateManager.translate(0, 0.2F, 0);
        }

        ((ModelBiped) render.getMainModel()).bipedBody.postRender(0.0625F);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        MODEL.render(0.0625F);

        String hex = Integer.toHexString(((ItemQuiver) stack.getItem()).getColor(stack));
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        GlStateManager.color(r / 255F, g / 255F, b / 255F);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_OVERLAY);
        MODEL.render(0.0625F);

        InventoryQuiver inv = new InventoryQuiver(stack);

        GlStateManager.translate(0, -1, 0);
        GlStateManager.rotate(160, 0, 0, 1);
        GlStateManager.disableLighting();

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            GlStateManager.pushMatrix();
            ItemStack arrow = inv.getStackInSlot(i);

            if (i == 0)
                GlStateManager.translate(0.45F, -0.8F, 0.2F);
            else if (i == 1)
                GlStateManager.translate(0.5F, -0.8F, 0.2F);
            else if (i == 2)
                GlStateManager.translate(0.45F, -0.8F, 0.25F);
            else if (i == 3)
                GlStateManager.translate(0.5F, -0.8F, 0.25F);
            else if (i == 4)
                GlStateManager.translate(0.475F, -0.8F, 0.225F);

            if (!arrow.isEmpty()) {
                if (arrow.getItem() instanceof ItemHEArrow) {
                    ItemHEArrow arrowItem = (ItemHEArrow) arrow.getItem();
                    arrowItem.type.renderArrow(null, true, false, arrowItem.getColor(arrow), Minecraft.getMinecraft());
                } else {
                    RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
                    GlStateManager.rotate(90, 1, 0, 0);
                    GlStateManager.translate(0, 0, 0.4F);
                    Entity arrowEntity = ((ItemArrow) Items.ARROW).createArrow(player.world, arrow, player);
                    rendermanager.renderEntity(arrowEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
                }
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

}