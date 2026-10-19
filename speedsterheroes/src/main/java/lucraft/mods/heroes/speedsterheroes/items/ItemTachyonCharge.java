package lucraft.mods.heroes.speedsterheroes.items;

import java.util.List;

import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.util.SHNBTTags;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.items.ItemBase;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class ItemTachyonCharge extends ItemBase {

	public static final int maxProgress = 5000;
	
	public ItemTachyonCharge() {
		super("tachyonCharge");
		String name = "tachyonCharge";
		for(int i = 0; i < 6; i++) {
			LucraftCore.proxy.registerModel(this, new LCModelEntry(i, name + "_" + i));
		}
		setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
		
		SHItems.items.add(this);
	}
	
	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		super.onCreated(stack, worldIn, playerIn);
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(SHNBTTags.progress, 0);
		stack.setTagCompound(new NBTTagCompound());
	}
	
	@Override
	public int getMetadata(int damage) {
		return super.getMetadata(damage);
	}
	
	@Override
	public int getMetadata(ItemStack stack) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
			
		int progress = stack.getTagCompound().getInteger(SHNBTTags.progress);
		
		if(progress < 1000)
			return 0;
		else if(progress < 2000)
			return 1;
		else if(progress < 3000)
			return 2;
		else if(progress < 4000)
			return 3;
		else if(progress < 5000)
			return 4;
		else
			return 5;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return stack.getTagCompound().getInteger(SHNBTTags.progress) / maxProgress;
	}
	
	@Override
	public boolean isDamaged(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		tooltip.add(stack.getTagCompound().getInteger(SHNBTTags.progress) + "/" + maxProgress);
		tooltip.add(LucraftCoreUtil.translateToLocal("speedsterheroes.info.tachyoncharge"));
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		ItemStack stack1 = new ItemStack(itemIn, 1, 0);
		stack1.setTagCompound(new NBTTagCompound());
		ItemStack stack2 = new ItemStack(itemIn, 1, 5);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(SHNBTTags.progress, maxProgress);
		stack2.setTagCompound(nbt);
		
		subItems.add(stack1);
		subItems.add(stack2);
	}

}
