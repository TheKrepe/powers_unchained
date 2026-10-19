package lucraft.mods.heroes.speedsterheroes.client.book;

import java.util.ArrayList;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.items.ItemSpeedsterArmor;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.client.gui.book.BookChapter;
import lucraft.mods.lucraftcore.client.gui.book.BookPage;
import lucraft.mods.lucraftcore.extendedinventory.ExtendedPlayerInventory;
import lucraft.mods.lucraftcore.util.LCRenderHelper;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class BookPageSpeedster extends BookPage {

	public SpeedsterType type;

	public BookPageSpeedster(BookChapter parent, SpeedsterType type) {
		super(parent);
		this.type = type;
	}

	@Override
	public void renderLeftSide(Minecraft mc, int x, int y, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		mc.fontRendererObj.drawString(TextFormatting.UNDERLINE + type.getDisplayName(), x, y, 0);

		boolean unicode = mc.fontRendererObj.getUnicodeFlag();
		mc.fontRendererObj.setUnicodeFlag(true);

		if (type.hasExtraDescription(ItemStack.EMPTY, mc.player)) {
			List<String> desc = type.getExtraDescription(ItemStack.EMPTY, mc.player);
			for(int i = 0; i < desc.size(); i++) {
				mc.fontRendererObj.drawString(desc.get(i), x, y + 10 + i*8, 0);
			}
		}

		mc.fontRendererObj.drawString(TextFormatting.UNDERLINE + LucraftCoreUtil.translateToLocal("speedsterheroes.info.extraspeedlevel") + ":" + TextFormatting.RESET + " " + type.getExtraSpeedLevel(type, mc.player), x, y + 115, 0);
		TrailType trail = type.getTrailType();
		mc.fontRendererObj.drawString(TextFormatting.UNDERLINE + LucraftCoreUtil.translateToLocal("speedsterheroes.info.trail") + ":" + TextFormatting.RESET + " " + (trail != null ? trail.getDisplayName() : "/"), x, y + 125, 0);

		// ----------------------------------------------------------------------

		ItemStack heldItem = mc.player.getHeldItemMainhand();
		mc.player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
		ItemStack helmet = mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		mc.player.inventory.armorInventory.set(3, new ItemStack(type.getHelmet()));
		ItemStack chestplate = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		mc.player.inventory.armorInventory.set(2, new ItemStack(type.getChestplate()));
		ItemStack legs = mc.player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
		mc.player.inventory.armorInventory.set(2, new ItemStack(type.getLegs()));
		ItemStack boots = mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
		mc.player.inventory.armorInventory.set(0, new ItemStack(type.getBoots()));
		ItemStack necklace = mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_NECKLACE);
		mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().setInventorySlotContents(ExtendedPlayerInventory.SLOT_NECKLACE, ItemStack.EMPTY);
		ItemStack mantle = mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_MANTLE);
		mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().setInventorySlotContents(ExtendedPlayerInventory.SLOT_MANTLE, ItemStack.EMPTY);
		ItemStack wrist = mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_WRIST);
		mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().setInventorySlotContents(ExtendedPlayerInventory.SLOT_WRIST, ItemStack.EMPTY);

		GlStateManager.color(1, 1, 1);
		drawEntityOnScreen(x + 40, y + 105, 40, 0, 0, mc.player.ticksExisted, mc.player);

		mc.player.inventory.armorInventory.set(3, helmet);
		mc.player.inventory.armorInventory.set(2, chestplate);
		mc.player.inventory.armorInventory.set(1, legs);
		mc.player.inventory.armorInventory.set(0, boots);
		mc.player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, heldItem);
		mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().setInventorySlotContents(ExtendedPlayerInventory.SLOT_NECKLACE, necklace);
		mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().setInventorySlotContents(ExtendedPlayerInventory.SLOT_MANTLE, mantle);
		mc.player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().setInventorySlotContents(ExtendedPlayerInventory.SLOT_WRIST, wrist);

		// ----------------------------------------------------------------------

		mc.fontRendererObj.setUnicodeFlag(unicode);
		GlStateManager.popMatrix();
	}

	@Override
	public void postRenderLeftSide(Minecraft mc, int x, int y, int mouseX, int mouseY) {
		boolean unicode = mc.fontRendererObj.getUnicodeFlag();
		mc.fontRendererObj.setUnicodeFlag(true);

		if (!type.helmet.isEmpty()) {
			int posX = 85;
			int posY = 28;
			SpeedsterType itemType = ((ItemSpeedsterArmor) type.helmet.getItem()).getSpeedsterType();
			mc.getRenderItem().renderItemIntoGUI(itemType.helmet, x + posX, y + posY);

			if (mouseX >= x + posX && mouseX < x + posX + 16 && mouseY >= y + posY && mouseY < y + posY + 16) {
				List<String> list = new ArrayList<String>();
				list.add(TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + itemType.helmet.getDisplayName());
				if (itemType.hasExtraDescription(ItemStack.EMPTY, mc.player))
					list.addAll(itemType.getExtraDescription(ItemStack.EMPTY, mc.player));
				ItemStack repair = itemType.getRepairItem(itemType.helmet);
				if (!repair.isEmpty()) {
					list.add(LucraftCoreUtil.translateToLocal("speedsterheroes.info.repairingitem") + ":");
					list.add("  " + repair.getDisplayName());
				}
				LCRenderHelper.drawStringList(list, mouseX + 8, mouseY, true);
			}
		}

		if (!type.chestplate.isEmpty()) {
			int posX = 85;
			int posY = 49;
			SpeedsterType itemType = ((ItemSpeedsterArmor) type.chestplate.getItem()).getSpeedsterType();
			mc.getRenderItem().renderItemIntoGUI(itemType.chestplate, x + posX, y + posY);

			if (mouseX >= x + posX && mouseX < x + posX + 16 && mouseY >= y + posY && mouseY < y + posY + 16) {
				List<String> list = new ArrayList<String>();
				list.add(TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + itemType.chestplate.getDisplayName());
				if (itemType.hasExtraDescription(ItemStack.EMPTY, mc.player))
					list.addAll(itemType.getExtraDescription(ItemStack.EMPTY, mc.player));
				ItemStack repair = itemType.getRepairItem(itemType.chestplate);
				if (!repair.isEmpty()) {
					list.add(LucraftCoreUtil.translateToLocal("speedsterheroes.info.repairingitem") + ":");
					list.add("  " + repair.getDisplayName());
				}
				LCRenderHelper.drawStringList(list, mouseX + 8, mouseY, true);
			}
		}

		if (!type.legs.isEmpty()) {
			int posX = 85;
			int posY = 72;
			SpeedsterType itemType = ((ItemSpeedsterArmor) type.legs.getItem()).getSpeedsterType();
			mc.getRenderItem().renderItemIntoGUI(itemType.legs, x + posX, y + posY);

			if (mouseX >= x + posX && mouseX < x + posX + 16 && mouseY >= y + posY && mouseY < y + posY + 16) {
				List<String> list = new ArrayList<String>();
				list.add(TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + itemType.legs.getDisplayName());
				if (itemType.hasExtraDescription(ItemStack.EMPTY, mc.player))
					list.addAll(itemType.getExtraDescription(ItemStack.EMPTY, mc.player));
				ItemStack repair = itemType.getRepairItem(itemType.legs);
				if (!repair.isEmpty()) {
					list.add(LucraftCoreUtil.translateToLocal("speedsterheroes.info.repairingitem") + ":");
					list.add("  " + repair.getDisplayName());
				}
				LCRenderHelper.drawStringList(list, mouseX + 8, mouseY, true);
			}
		}

		if (!type.boots.isEmpty()) {
			int posX = 85;
			int posY = 93;
			SpeedsterType itemType = ((ItemSpeedsterArmor) type.boots.getItem()).getSpeedsterType();
			mc.getRenderItem().renderItemIntoGUI(itemType.boots, x + posX, y + posY);

			if (mouseX >= x + posX && mouseX < x + posX + 16 && mouseY >= y + posY && mouseY < y + posY + 16) {
				List<String> list = new ArrayList<String>();
				list.add(TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + itemType.boots.getDisplayName());
				if (itemType.hasExtraDescription(ItemStack.EMPTY, mc.player))
					list.addAll(itemType.getExtraDescription(ItemStack.EMPTY, mc.player));
				ItemStack repair = itemType.getRepairItem(itemType.boots);
				if (!repair.isEmpty()) {
					list.add(LucraftCoreUtil.translateToLocal("speedsterheroes.info.repairingitem") + ":");
					list.add("  " + repair.getDisplayName());
				}
				LCRenderHelper.drawStringList(list, mouseX + 8, mouseY, true);
			}
		}

		mc.fontRendererObj.setUnicodeFlag(unicode);
	}

	@Override
	public void renderRightSide(Minecraft mc, int x, int y, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		if (type.hasSymbol()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 45, y + 5, 0);
			GlStateManager.scale(2, 2, 2);
			mc.getRenderItem().renderItemIntoGUI(SHItems.getSymbolFromSpeedsterType(type, 1), 0, 0);
			GlStateManager.popMatrix();
		}

		boolean unicode = mc.fontRendererObj.getUnicodeFlag();
		mc.fontRendererObj.setUnicodeFlag(true);

		// Ability List
		List<Ability> list = type.addDefaultAbilities(null, new ArrayList<Ability>());
		int listRenderLength = 11 + list.size() * 10;

		// if(type.hasSymbol())
		// listRenderLength -= 32;

		GlStateManager.translate(0, 80 + (type.hasSymbol() ? 16 : 0) - listRenderLength / 2, 0);

		mc.fontRendererObj.drawString(TextFormatting.UNDERLINE + LucraftCoreUtil.translateToLocal("book.chapter.superpowers.abilities") + ":", x + 5, y, 0);
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Ability ab = list.get(i);
				mc.fontRendererObj.drawString("- " + ab.getDisplayName(), x + 10, y + 11 + i * 10, 0);
			}
		}

		mc.fontRendererObj.setUnicodeFlag(unicode);
		GlStateManager.popMatrix();
	}

	public void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, int progress, EntityLivingBase ent) {
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) posX, (float) posY, 50.0F);
		GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		float f = ent.renderYawOffset;
		float f1 = ent.rotationYaw;
		float f2 = ent.rotationPitch;
		float f3 = ent.prevRotationYawHead;
		float f4 = ent.rotationYawHead;
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
		ent.renderYawOffset = (float) Math.atan((double) (mouseX / 40.0F)) * 20.0F;
		ent.rotationYaw = (float) Math.atan((double) (mouseX / 40.0F)) * 40.0F;
		ent.rotationPitch = -((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F;
		ent.rotationYawHead = ent.rotationYaw;
		ent.prevRotationYawHead = ent.rotationYaw;
		GlStateManager.translate(0.0F, 0.0F, 0.0F);
		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180.0F);
		rendermanager.setRenderShadow(false);
		GlStateManager.rotate((progress + LucraftCoreClientUtil.renderTick) * 2, 0F, 1F, 0F);
		rendermanager.doRenderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
		rendermanager.setRenderShadow(true);
		ent.renderYawOffset = f;
		ent.rotationYaw = f1;
		ent.rotationPitch = f2;
		ent.prevRotationYawHead = f3;
		ent.rotationYawHead = f4;
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

}
