package lucraft.mods.heroes.speedsterheroes.items;

import java.util.ArrayList;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSymbol extends Item {

	public static ArrayList<SpeedsterType> list = new ArrayList<SpeedsterType>();
	
	public ItemSymbol() {
		String name = "symbol";
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
		GameRegistry.register(this);
		
		String[] models = new String[list.size()];
		
		for(int i = 0; i < list.size(); i++) {
			models[i] = "symbol/" + list.get(i).getUnlocalizedName().toLowerCase();
			LucraftCore.proxy.registerModel(this, new LCModelEntry(i, "symbol/" + list.get(i).getUnlocalizedName().toLowerCase()));
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		int i = stack.getMetadata();
		if(list.get(i).hasExtraDescription(stack, playerIn))
			tooltip.addAll(list.get(i).getExtraDescription(stack, playerIn));
		
		if(list.get(i).canBeCraftedDescription(stack))
			tooltip.add(LucraftCoreUtil.translateToLocal("speedsterheroes.info.canbecrafted"));
		else
			tooltip.add(LucraftCoreUtil.translateToLocal("speedsterheroes.info.canbefoundindungeons"));
	}
	
	public SpeedsterType getSpeedsterType(ItemStack stack) {
		return list.get(stack.getMetadata());
	}
	
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		return super.getUnlocalizedName(stack) + "." + list.get(i).getUnlocalizedName();
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		int i = stack.getMetadata();
		if(i >= 0 && i < list.size())
			return list.get(i).getDisplayName() + LucraftCoreUtil.translateToLocal("speedsterheroes.info.symbolprefix");
		else
			return "null";
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < list.size(); ++i) {
			ItemStack stack = new ItemStack(itemIn, 1, i);
			SpeedsterType type = getSpeedsterType(stack);
			
			if(type.showInCreativeTab())
				subItems.add(stack);
		}
	}

}
