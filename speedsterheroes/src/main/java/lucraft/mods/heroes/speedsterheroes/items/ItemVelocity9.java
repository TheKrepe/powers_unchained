package lucraft.mods.heroes.speedsterheroes.items;

import java.util.ArrayList;

import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.lucraftcore.items.LCItems;
import lucraft.mods.lucraftcore.network.MessageSyncPotionEffects;
import lucraft.mods.lucraftcore.network.PacketDispatcher;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemVelocity9 extends ItemSHBase {

	public ItemVelocity9() {
		super("velocity9");
		this.setFull3D();
		this.setMaxStackSize(1);
		
		SHItems.items.add(this);
	}

	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase playerIn) {
		return stack;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {

		if (entityLiving instanceof EntityPlayer && timeLeft <= 71980) {
			EntityPlayer player = (EntityPlayer) entityLiving;

			PotionEffect effect = new PotionEffect(SpeedsterHeroesProxy.velocity9, 5 * 60 * 20);
			effect.setCurativeItems(new ArrayList<ItemStack>());
			player.addPotionEffect(effect);
			PacketDispatcher.sendToAll(new MessageSyncPotionEffects(entityLiving));
			player.addStat(SHAchievements.velocity9);
			if (!player.capabilities.isCreativeMode) {
				player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(LCItems.syringe));
			}
		}
	}

}
