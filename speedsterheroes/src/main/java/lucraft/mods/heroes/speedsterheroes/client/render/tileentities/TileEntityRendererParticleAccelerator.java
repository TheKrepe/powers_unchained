package lucraft.mods.heroes.speedsterheroes.client.render.tileentities;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.blocks.BlockParticleAccelerator;
import lucraft.mods.heroes.speedsterheroes.client.models.ModelParticleAccelerator;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityParticleAccelerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityRendererParticleAccelerator extends TileEntitySpecialRenderer<TileEntityParticleAccelerator> {

	public ModelParticleAccelerator model;
	public ResourceLocation texture;
	
	public static ResourceLocation[] progressTextures;
	public static ResourceLocation[] waterBucketsTextures;

	public TileEntityRendererParticleAccelerator() {
		this.model = new ModelParticleAccelerator();
		this.texture = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/models/particleAccelerator/model.png");
		
		progressTextures = new ResourceLocation[8];
		waterBucketsTextures = new ResourceLocation[8];
		
		for(int i = 0; i < 8; i++) {
			progressTextures[i] = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/models/particleAccelerator/progress/" + i + ".png");
			waterBucketsTextures[i] = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/models/particleAccelerator/water/" + i + ".png");
		}
	}

	@Override
	public void renderTileEntityAt(TileEntityParticleAccelerator te, double x, double y, double z, float partialTicks, int destroyStage) {
		if (te.getWorld().isAirBlock(te.getPos()))
			return;

		BlockPos blockpos = te.getPos();
		World world = te.getWorld();
		IBlockState iblockstate = world.getBlockState(blockpos);

		GlStateManager.pushMatrix();

		if (!te.isEnabled) {
			
			RenderHelper.disableStandardItemLighting();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.enableBlend();
			GlStateManager.disableCull();

			if (Minecraft.isAmbientOcclusionEnabled()) {
				GlStateManager.shadeModel(7425);
			} else {
				GlStateManager.shadeModel(7424);
			}
			
			GlStateManager.translate(0.5D, 0, 0.5D);
			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.translate((float) x, (float) y, (float) z);
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer worldrenderer = tessellator.getBuffer();
			worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
			int i = blockpos.getX();
			int j = blockpos.getY();
			int k = blockpos.getZ();
			worldrenderer.setTranslation((double) ((float) (-i) - 0.5F), (double) (-j), (double) ((float) (-k) - 0.5F));
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(iblockstate);
			blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, iblockstate, blockpos, worldrenderer, false);
			worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();
		} else {
			GlStateManager.translate(x + 0.5D, y + 1.5D, z + 0.5D);
			GL11.glRotatef(180, 0F, 0F, 1F);

			float lastBrightnessX = OpenGlHelper.lastBrightnessX;
			float lastBrightnessY = OpenGlHelper.lastBrightnessY;

			EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockParticleAccelerator.FACING);
			switch (facing) {
			case SOUTH:
				GlStateManager.rotate(180, 0, 1, 0);
				break;
			case WEST:
				GlStateManager.rotate(270, 0, 1, 0);
				break;
			case EAST:
				GlStateManager.rotate(90, 0, 1, 0);
				break;
			default:
				break;
			}
			GlStateManager.translate(0, 0, 0.8D);

			int l = te.getWorld().getCombinedLight(new BlockPos(x, y, z), 0);
			int i1 = l % 65536;
			int j1 = l / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) i1, (float) j1);
			
			this.bindTexture(texture);
			model.renderModel(0.0625F);
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			
			this.bindTexture(waterBucketsTextures[te.waterBuckets]);
			model.renderModel(0.0625F);
			
			this.bindTexture(progressTextures[te.progress / 20 / 3]);
			model.renderModel(0.0625F);
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		}

		GlStateManager.popMatrix();

	}

}
