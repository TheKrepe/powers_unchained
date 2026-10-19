package lucraft.mods.heroes.speedsterheroes.potions;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionVelocity9 extends Potion {

	public PotionVelocity9() {
		super(false, 0xffd500);
		this.setPotionName("potion.velocity9");
//		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "91AEBD56-376B-4498-CAF6-2F7EA7E70635", 5D, 2);
	}
	
	@Override
	public boolean shouldRender(PotionEffect effect) {
		return true;
	}
	
	@Override
	public boolean hasStatusIcon() {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		if(effect.getPotion() == this) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			mc.getRenderItem().renderItemIntoGUI(new ItemStack(SHItems.velocity9), x + 8, y + 8);
			
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}
	
	@Override
	public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
		if(effect.getPotion() == this) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			mc.getRenderItem().renderItemIntoGUI(new ItemStack(SHItems.velocity9), x + 5, y + 4);
			
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}
	
	@Override
	public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
	}
	
}
