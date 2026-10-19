package lucraft.mods.heroes.speedsterheroes.client.render.tileentities;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.blocks.BlockParticleAccelerator;
import lucraft.mods.heroes.speedsterheroes.blocks.SHBlocks;
import lucraft.mods.heroes.speedsterheroes.client.models.ModelPSChest;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityPhilosophersStoneChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileEntityRendererPhilosophersStoneChest extends TileEntitySpecialRenderer<TileEntityPhilosophersStoneChest> {

	public static ModelPSChest model = new ModelPSChest();
	public static ResourceLocation TEX = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/models/psChest.png");
	
	@Override
	public void renderTileEntityAt(TileEntityPhilosophersStoneChest te, double x, double y, double z, float partialTicks, int destroyStage) {
		if(te.getWorld().getBlockState(te.getPos()) == null || te.getWorld().getBlockState(te.getPos()).getBlock() != SHBlocks.philosophersStoneChest)
			return;
		
		GlStateManager.pushMatrix();
		
		this.bindTexture(TEX);
		GlStateManager.translate(x + 0.5D, y + 1.5D, z + 0.5D);
		GL11.glRotatef(180, 0F, 0F, 1F);
		
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
		
		float progress = te.prevTimer / 20F + (te.timer / 20F - te.prevTimer / 20F) * partialTicks;
		model.Part1.rotateAngleZ = progress * -1.3F;
		model.Part2.rotateAngleZ = progress * 1.3F;
		
		model.Part3.showModel = !te.getStackInSlot(0).isEmpty();
		
		model.render(0.0625F);
		
		GlStateManager.popMatrix();
	}
	
}
