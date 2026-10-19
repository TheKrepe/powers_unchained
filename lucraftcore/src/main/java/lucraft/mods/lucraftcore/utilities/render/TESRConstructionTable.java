package lucraft.mods.lucraftcore.utilities.render;

import lucraft.mods.lucraftcore.utilities.blocks.TileEntityConstructionTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TESRConstructionTable extends TileEntitySpecialRenderer<TileEntityConstructionTable> {

    @Override
    public void render(TileEntityConstructionTable te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.getStackInSlot(0).isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + 0.95F, z + 0.4D);
            GlStateManager.rotate(90, 1, 0, 0);
            Minecraft.getMinecraft().getRenderItem().renderItem(te.getStackInSlot(0), TransformType.GROUND);
            GlStateManager.popMatrix();
        }
    }

}
