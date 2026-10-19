package lucraft.mods.lucraftcore.infinity.render;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import lucraft.mods.lucraftcore.infinity.items.InventoryInfinityGauntlet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemRendererInfinityGauntlet extends TileEntityItemStackRenderer {

    public static ModelInfinityGauntlet MODEL = new ModelInfinityGauntlet();
    public static final ResourceLocation TEXTURE = new ResourceLocation(LucraftCore.MODID, "textures/models/infinity_gauntlet.png");

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        ModelInfinityGauntlet MODEL = new ModelInfinityGauntlet();

        if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Fist")) {
            MODEL.index_0.rotateAngleX = -1;
            MODEL.thumb_0.rotateAngleY = (float) Math.toRadians(90);
            MODEL.middle_0.rotateAngleX = -1;
            MODEL.pinkie_0.rotateAngleX = -1;
            MODEL.ring_0.rotateAngleX = -1;

            MODEL.index_1.rotateAngleX = -0.6F;
            MODEL.thumb_1.rotateAngleX = -1.4F;
            MODEL.middle_1.rotateAngleX = -0.6F;
            MODEL.pinkie_1.rotateAngleX = -0.6F;
            MODEL.ring_1.rotateAngleX = -0.6F;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 1.8F, 0.5F);
        GlStateManager.scale(-1, -1, -1);
        GlStateManager.disableCull();
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        MODEL.renderModel(0.0625F);

        if (stack.hasTagCompound()) {
            InventoryInfinityGauntlet inv = new InventoryInfinityGauntlet(stack);
            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

            for (int i = 0; i < inv.getSizeInventory(); i++) {
                EnumInfinityStone type = InventoryInfinityGauntlet.SLOTS[i];
                ItemStack s = inv.getStackInSlot(i);

                if (!s.isEmpty()) {
                    GlStateManager.pushMatrix();
                    if (type == EnumInfinityStone.SOUL)
                        GlStateManager.translate(0.119F, 1.07F, -0.14F);
                    else if (type == EnumInfinityStone.REALITY)
                        GlStateManager.translate(0.044F, 1.07F, -0.14F);
                    else if (type == EnumInfinityStone.SPACE)
                        GlStateManager.translate(-0.0375F, 1.07F, -0.14F);
                    else if (type == EnumInfinityStone.POWER)
                        GlStateManager.translate(-0.118F, 1.07F, -0.14F);
                    else if (type == EnumInfinityStone.MIND)
                        GlStateManager.translate(0.006F, 1.164F, -0.14F);
                    else if (type == EnumInfinityStone.TIME) {
                        GlStateManager.translate(-0.14F, 1.095F, 0.062F);
                        GlStateManager.rotate(90F, 0, 1, 0);
                    }

                    GlStateManager.scale(0.35F, 0.35F, 0.35F);
                    GlStateManager.translate(-0.5F, -0.5F, -0.5F);

                    if (s.getItem().getTileEntityItemStackRenderer() != null)
                        s.getItem().getTileEntityItemStackRenderer().renderByItem(s, partialTicks);

                    GlStateManager.popMatrix();
                }
            }
        }

        GlStateManager.popMatrix();
    }

}
