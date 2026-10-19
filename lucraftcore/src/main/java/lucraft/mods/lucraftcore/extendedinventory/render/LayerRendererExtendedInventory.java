package lucraft.mods.lucraftcore.extendedinventory.render;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.render.ExtendedInventoryItemRendererRegistry.IItemExtendedInventoryRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class LayerRendererExtendedInventory implements LayerRenderer<EntityPlayer> {

    public RenderLivingBase<?> renderer;
    public static Minecraft mc = Minecraft.getMinecraft();

    public LayerRendererExtendedInventory(RenderLivingBase<?> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        GlStateManager.pushMatrix();

        render(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, false);

        GlStateManager.popMatrix();

        float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
        float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.rotate(yawOffset, 0, -1, 0);
        GlStateManager.rotate(yaw - 270, 0, 1, 0);
        GlStateManager.rotate(pitch, 0, 0, 1);

        render(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, true);

        GlStateManager.popMatrix();
    }

    private void render(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                        float scale, boolean head) {
        InventoryExtendedInventory inventory = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();

        for (int i = 0; i <= InventoryExtendedInventory.SLOT_RIGHT_WEAPON; i++) {
            ItemStack item = inventory.getStackInSlot(i);

            if (!item.isEmpty() && item.getItem() != null && item.getItem() instanceof IItemExtendedInventory) {
                IItemExtendedInventoryRenderer renderer = ExtendedInventoryItemRendererRegistry.getRenderer((IItemExtendedInventory) item.getItem());

                if (renderer != null)
                    renderer.render(player, this.renderer, item, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, head);
            } else if (!item.isEmpty() && item.getItem() != null && i > 2 && !head) {
                //                if (LCConfig.modules.advanced_combat) {
                //                    IAdvancedCombatCapability cap = entity.getCapability(CapabilityAdvancedCombat.ADVANCED_COMBAT_CAP, null);
                //                    if (cap != null && !cap.isCombatModeEnabled()) {
                //                        GlStateManager.pushMatrix();
                //                        GlStateManager.translate(i == 3 ? -0.35 : 0.2, 0.75, -0.1);
                //                        GlStateManager.rotate(90f, 1.0f, 0.0f, 0.0f);
                //                        GlStateManager.rotate(i == 3 ? 5f : -5f, 0.0f, 1.0f, 0.0f);
                //                        Minecraft.getMinecraft().getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
                //                        GlStateManager.popMatrix();
                //                    }
                //                }
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

}
