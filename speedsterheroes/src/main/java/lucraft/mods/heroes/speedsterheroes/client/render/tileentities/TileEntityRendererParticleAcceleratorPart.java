package lucraft.mods.heroes.speedsterheroes.client.render.tileentities;

import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityParticleAcceleratorPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityRendererParticleAcceleratorPart extends TileEntitySpecialRenderer<TileEntityParticleAcceleratorPart> {

	@Override
	public void renderTileEntityAt(TileEntityParticleAcceleratorPart te, double x, double y, double z, float partialTicks, int destroyStage) {
		if (te.getWorld().isAirBlock(te.getPos()) || te.isConnected)
			return;
		
		BlockPos blockpos = te.getPos();
		World world = te.getWorld();
		IBlockState iblockstate = te.getWorld().getBlockState(te.getPos());
		
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5D, 0, 0.5D);
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.translate((float)x, (float)y, (float)z);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled())
        {
            GlStateManager.shadeModel(7425);
        }
        else
        {
            GlStateManager.shadeModel(7424);
        }
        
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();
        worldrenderer.setTranslation((double)((float)(-i) - 0.5F), (double)(-j), (double)((float)(-k) - 0.5F));
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(iblockstate);
        blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, iblockstate, blockpos, worldrenderer, false);
        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
	}
	
}
