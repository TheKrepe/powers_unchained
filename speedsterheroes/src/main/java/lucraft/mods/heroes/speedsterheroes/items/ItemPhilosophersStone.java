package lucraft.mods.heroes.speedsterheroes.items;

import java.util.Random;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.lucraftcore.items.ItemBase;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class ItemPhilosophersStone extends ItemBase {

	public ItemPhilosophersStone() {
		super("philosophersStone");
		this.setAutomaticModelName(name);
		setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
		this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemstack = playerIn.getHeldItem(hand);
		
		if(LucraftCoreUtil.hasNoArmorOn(playerIn)) {
			SuperpowerHandler.setSuperpower(playerIn, SpeedsterHeroes.speedforce);
			playerIn.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(SHItems.savitarHelmet));
			playerIn.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SHItems.savitarChestplate));
			playerIn.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SHItems.savitarLegs));
			playerIn.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SHItems.savitarBoots));
			itemstack.shrink(1);
			
			Random rand = new Random();
			for(int i = 0; i < 200; i++) {
				float halfWidth = playerIn.width / 2F;
				worldIn.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, playerIn.posX - halfWidth + rand.nextFloat() * playerIn.width, playerIn.posY + rand.nextFloat() * playerIn.height, playerIn.posZ - halfWidth + rand.nextFloat() * playerIn.width, (rand.nextFloat() - 0.5F) * 2F, (rand.nextFloat() - 0.5F) * 2F, (rand.nextFloat() - 0.5F) * 2F);
				worldIn.spawnParticle(EnumParticleTypes.PORTAL, playerIn.posX - halfWidth + rand.nextFloat() * playerIn.width, playerIn.posY + rand.nextFloat() * playerIn.height, playerIn.posZ - halfWidth + rand.nextFloat() * playerIn.width, (rand.nextFloat() - 0.5F) * 2F, (rand.nextFloat() - 0.5F) * 2F, (rand.nextFloat() - 0.5F) * 2F);
				worldIn.spawnParticle(EnumParticleTypes.SPELL, playerIn.posX - halfWidth + rand.nextFloat() * playerIn.width, playerIn.posY + rand.nextFloat() * playerIn.height, playerIn.posZ - halfWidth + rand.nextFloat() * playerIn.width, (rand.nextFloat() - 0.5F) * 2F, (rand.nextFloat() - 0.5F) * 2F, (rand.nextFloat() - 0.5F) * 2F);
			}
			
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		}
		
		return super.onItemRightClick(worldIn, playerIn, hand);
	}

}
