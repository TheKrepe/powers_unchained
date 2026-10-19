package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.items.ItemRing;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.lucraftcore.abilities.AbilityAction;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilitySuitRing extends AbilityAction {

	public AbilitySuitRing(EntityPlayer player) {
		super(player);
	}
	
	private static final ItemStack iconRing = new ItemStack(SHItems.ringUpgrade);
	
	@Override
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
		mc.getRenderItem().renderItemIntoGUI(iconRing, x, y);
	}
	
	@Override
	public boolean isUnlocked() {
		return super.isUnlocked() && !player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() && LucraftCoreUtil.hasArmorThisUpgrade(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), SHItems.ringUpgrade);
	}
	
	@Override
	public void action() {
		ItemStack ring = new ItemStack(SHItems.ring);

		ItemRing.setArmorIn(ring, player.getItemStackFromSlot(EntityEquipmentSlot.HEAD), player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), player.getItemStackFromSlot(EntityEquipmentSlot.LEGS), player.getItemStackFromSlot(EntityEquipmentSlot.FEET));
		player.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
		player.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStack.EMPTY);
		player.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStack.EMPTY);
		player.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStack.EMPTY);

		LucraftCoreUtil.givePlayerItemStack(player, ring);
	}

}
