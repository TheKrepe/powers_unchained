package lucraft.mods.heroes.speedsterheroes.client.models;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPSChest extends ModelBase {
	
	public ModelRenderer Part0;
	public ModelRenderer Part1;
	public ModelRenderer Part2;
	public ModelRenderer Part3;

	public ModelPSChest() {
		this.textureWidth = 64;
		this.textureHeight = 64;

		this.Part0 = new ModelRenderer(this, 0, 0);
		this.Part0.setRotationPoint(-6.0F, 20.0F, -6.0F);
		this.Part0.addBox(0.0F, 0.0F, 0.0F, 12, 4, 12);
		this.Part1 = new ModelRenderer(this, 0, 16);
		this.Part1.setRotationPoint(-6.0F, 20.3F, -6.0F);
		this.Part1.addBox(0.0F, -3.0F, 0.0F, 6, 3, 12);
		this.Part2 = new ModelRenderer(this, 0, 31);
		this.Part2.setRotationPoint(6.0F, 20.3F, -6.0F);
		this.Part2.addBox(-6.0F, -3.0F, 0.0F, 6, 3, 12);
		this.Part3 = new ModelRenderer(this, 46, 0);
		this.Part3.setRotationPoint(-2.0F, 18.0F, -2.0F);
		this.Part3.addBox(0.0F, 0.0F, 0.0F, 4, 3, 4);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
		this.Part0.render(scale);
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
		this.Part1.render(scale);
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
		this.Part2.render(scale);
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
		this.Part3.render(scale);
		GlStateManager.disableBlend();
	}
	
	public void render(float scale) {
		this.Part0.render(scale);
		this.Part1.render(scale);
		this.Part2.render(scale);
		
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		float lastBrightnessX = OpenGlHelper.lastBrightnessX;
		float lastBrightnessY = OpenGlHelper.lastBrightnessY;
		
		float brightness = 100F + (float) (1 + Math.sin(Minecraft.getMinecraft().player.ticksExisted / 20F)) * 70F;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness, brightness);
		this.Part3.render(scale);
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	public void setRotationAngles(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
