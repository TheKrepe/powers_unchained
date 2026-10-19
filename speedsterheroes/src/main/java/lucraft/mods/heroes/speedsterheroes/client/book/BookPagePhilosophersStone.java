package lucraft.mods.heroes.speedsterheroes.client.book;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.lucraftcore.client.gui.book.BookChapter;
import lucraft.mods.lucraftcore.client.gui.book.BookPage;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class BookPagePhilosophersStone extends BookPage {

	public ItemStack stone = new ItemStack(SHItems.philosophersStone);
	
	public BookPagePhilosophersStone(BookChapter parent) {
		super(parent);
	}

	@Override
	public void renderLeftSide(Minecraft mc, int x, int y, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		mc.fontRendererObj.drawString(TextFormatting.UNDERLINE + stone.getDisplayName(), x, y, 0);
		
		float scale = 4;
		GlStateManager.translate(x + 20, y + 30, 0);
		GlStateManager.scale(scale, scale, scale);
		mc.getRenderItem().renderItemIntoGUI(stone, 0, 0);
		GlStateManager.enableRescaleNormal();
		
		GlStateManager.popMatrix();
	}

	@Override
	public void renderRightSide(Minecraft mc, int x, int y, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		boolean unicode = mc.fontRendererObj.getUnicodeFlag();
		mc.fontRendererObj.setUnicodeFlag(true);
		mc.fontRendererObj.drawSplitString(LucraftCoreUtil.translateToLocal("book.ancientArtifacts.philosophersStone.desc"), x, y + 12, 120, 0);
		mc.fontRendererObj.setUnicodeFlag(unicode);
		
		GlStateManager.popMatrix();
	}

}
