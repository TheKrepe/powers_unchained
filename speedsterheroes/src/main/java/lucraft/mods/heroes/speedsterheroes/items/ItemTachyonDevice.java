package lucraft.mods.heroes.speedsterheroes.items;

import java.util.List;

import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars.SpeedLevelBar;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.ExtendedPlayerInventory;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.util.IArmorUpgrade;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTachyonDevice extends ItemSHBase implements IArmorUpgrade, IItemExtendedInventory {

	private TachyonDeviceType type;
	
	public ItemTachyonDevice(TachyonDeviceType type) {
		super(type.getName());
		this.type = type;
		
		SHItems.items.add(this);
	}

	public TachyonDeviceType getTachyonDeviceType() {
		return type;
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if(stack.hasTagCompound()) {
			tooltip.add(stack.getTagCompound().getInteger("Charge") + "/" + getTachyonDeviceType().getMaxCharge());
		}
	}
	
	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		stack.setTagCompound(new NBTTagCompound());
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if(!stack.hasTagCompound())
			return 1D;
		return 1D - (stack.getTagCompound().getInteger("Charge") / (double)getTachyonDeviceType().getMaxCharge());
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean isDamaged(ItemStack stack) {
		return true;
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		ItemStack stack1 = new ItemStack(itemIn);
		stack1.setTagCompound(new NBTTagCompound());
		ItemStack stack2 = new ItemStack(itemIn);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("Charge", getTachyonDeviceType().getMaxCharge());
		stack2.setTagCompound(nbt);
		
		subItems.add(stack1);
		subItems.add(stack2);
	}
	
	@Override
	public boolean canBeApplied(int armorSlot, ItemStack suit, ItemStack upgrade) {
		
		if(upgrade.getItem() instanceof ItemTachyonDevice) {
			return SpeedsterHeroesUtil.getTachyonDeviceFromArmor(suit).isEmpty();
		}
		
		return false;
	}
	
	@Override
	public ExtendedInventoryItemType getEIItemType(ItemStack stack) {
		return ExtendedInventoryItemType.MANTLE;
	}
	
	@Override
	public void onWornTick(ItemStack stack, EntityPlayer player) {
		if(SpeedsterHeroesUtil.hasTachyonDevice(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST))) {
			LucraftCoreUtil.givePlayerItemStack(player, stack);
			player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().setInventorySlotContents(ExtendedPlayerInventory.SLOT_MANTLE, ItemStack.EMPTY);
		}
	}
	
	@Override
	public boolean canEquip(ItemStack itemstack, EntityPlayer player) {
		return !SpeedsterHeroesUtil.hasTachyonDevice(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
	}
	
	public static enum TachyonDeviceType {
		
		PROTOTYPE("tachyonPrototype", 3),
		DEVICE("tachyonDevice", 5),
		SMALL_DEVICE("smallTachyonDevice", 4);
		
		private String name;
		private int speedLevels;
		
		private TachyonDeviceType(String name, int speedLevels) {
			this.name = name;
			this.speedLevels = speedLevels;
		}
		
		public String getName() {
			return name;
		}
		
		public int getSpeedLevels() {
			return speedLevels;
		}
		
		public int getMaxCharge() {
			return getSpeedLevels() * 1000;
		}
		
		@SideOnly(Side.CLIENT)
		public ModelBase getModel() {
			return this == PROTOTYPE ? SHRenderer.TACHYON_PROTOTYPE_MODEL : (this == TachyonDeviceType.DEVICE ? SHRenderer.TACHYON_DEVICE_MODEL : null);
		}
		
		@SideOnly(Side.CLIENT)
		public void doModelTranslations(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			if(this == PROTOTYPE) {
				SpeedsterType type = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() ? null : player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemSpeedsterArmor ? ((ItemSpeedsterArmor) player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()).getSpeedsterType() : null;
				GlStateManager.translate(0.005F, 0.25F + (type != null ? type.getTachyonDeviceModelTranslation(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, null) * 0.7F : 0), -0.2F);
				GlStateManager.scale(0.25F, 0.25F, 0.25F);
			} else if(this == DEVICE) {
				SpeedsterType type = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() ? null : player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemSpeedsterArmor ? ((ItemSpeedsterArmor) player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()).getSpeedsterType() : null;
				GlStateManager.translate(0, 0.25F + (type != null ? type.getTachyonDeviceModelTranslation(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, null) * 0.7F : 0), -0.2F);
				GlStateManager.scale(0.2F, 0.2F, 0.2F);
			}
		}
		
		@SideOnly(Side.CLIENT)
		public ResourceLocation getTexture() {
			return this == PROTOTYPE ? SHRenderer.TACHYON_PROTOTYPE_TEX : (this == TachyonDeviceType.DEVICE ? SHRenderer.TACHYON_DEVICE_TEX : null);
		}
		
		public SpeedLevelBar getSpeedLevelBar() {
			return this == PROTOTYPE ? SpeedLevelBar.tachyonPrototypeSpeed : (this == DEVICE ? SpeedLevelBar.tachyonDeviceSpeed : SpeedLevelBar.smallTachyonDeviceSpeed);
		}
		
	}

}
