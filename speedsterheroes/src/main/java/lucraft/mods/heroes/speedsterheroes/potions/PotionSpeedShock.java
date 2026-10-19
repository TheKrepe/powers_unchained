package lucraft.mods.heroes.speedsterheroes.potions;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionSpeedShock extends Potion {

	public PotionSpeedShock() {
		super(true, 0xfb9f05);
		this.setPotionName("potion.speedShock");
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
	public void performEffect(EntityLivingBase entityLivingBaseIn, int p_76394_2_) {
		if (entityLivingBaseIn.getHealth() > 1.0F) {
			entityLivingBaseIn.attackEntityFrom(DamageSource.MAGIC, 3.0F);
		}
	}

	@Override
	public boolean isReady(int p_76397_1_, int p_76397_2_) {
        int j = 25 >> p_76397_2_;
        return j > 0 ? p_76397_1_ % j == 0 : true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		if (effect.getPotion() == this) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			mc.getRenderItem().renderItemIntoGUI(new ItemStack(SHItems.iconItem, 1, 10), x + 8, y + 8);

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

}
