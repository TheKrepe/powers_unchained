package lucraft.mods.heroes.speedsterheroes.items;

import java.util.List;

import lucraft.mods.heroes.speedsterheroes.entity.EntityRingDummy;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.ExtendedPlayerInventory;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.items.ItemBase;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ItemRing extends ItemBase implements IItemExtendedInventory {

	public ItemRing() {
		super("ring");

		LucraftCore.proxy.registerModel(this, new LCModelEntry(0, name));
		setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);

		this.setMaxStackSize(1);
	}

	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		super.onCreated(stack, worldIn, playerIn);
		stack.setTagCompound(new NBTTagCompound());
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		return spawnSuitEntity(playerIn, playerIn.getHeldItem(hand));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (!stack.hasTagCompound())
			return;

		SpeedsterType type = SpeedsterType.getSpeedsterTypeFromName(stack.getTagCompound().getString("SpeedsterType"));

		if (type != null) {
			tooltip.add(TextFormatting.BOLD + type.getDisplayName());

			if (type.hasExtraDescription(stack, playerIn))
				tooltip.addAll(type.getExtraDescription(stack, playerIn));
		}
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (SpeedsterType type : SpeedsterType.speedsterTypes) {
			if (type.showInCreativeTab()) {
				subItems.add(setArmorIn(new ItemStack(itemIn), new ItemStack(type.getHelmet()), new ItemStack(type.getChestplate()), new ItemStack(type.getLegs()), new ItemStack(type.getBoots())));
			}
		}
	}

	public static ItemStack setArmorIn(ItemStack ring, ItemStack helmet, ItemStack chestplate, ItemStack legs, ItemStack boots) {
		if (!ring.hasTagCompound())
			ring.setTagCompound(new NBTTagCompound());

		ItemStack[] armor = new ItemStack[] { helmet, chestplate, legs, boots };
		NBTTagList nbtList = new NBTTagList();
		SpeedsterType type = null;

		for (int i = 0; i < 4; i++) {
			ItemStack s = armor[i];
			if (!s.isEmpty()) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("Slot", i);
				s.writeToNBT(nbt);
				nbtList.appendTag(nbt);

				if (s.getItem() instanceof ItemSpeedsterArmor) {
					type = ((ItemSpeedsterArmor) s.getItem()).getSpeedsterType();
				}
			}
		}

		ring.getTagCompound().setTag("ItemInventory", nbtList);
		ring.getTagCompound().setString("SpeedsterType", type.getUnlocalizedName());

		return ring;
	}

	public static ItemStack[] getArmorInRing(ItemStack ring) {
		if (!ring.hasTagCompound())
			ring.setTagCompound(new NBTTagCompound());

		NBTTagList items = ring.getTagCompound().getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
		ItemStack[] armor = new ItemStack[4];

		for (int i = 0; i < 4; i++) {
			NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");

			if (slot >= 0 && slot < 4) {
				armor[slot] = new ItemStack(item);
			}
		}

		return armor;
	}

	@Override
	public ExtendedInventoryItemType getEIItemType(ItemStack stack) {
		return ExtendedInventoryItemType.WRIST;
	}

	@Override
	public void onPressedButton(ItemStack itemstack, EntityPlayer player) {
		spawnSuitEntity(player, itemstack);
		if(itemstack.getCount() == 0) {
			player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().setInventorySlotContents(ExtendedPlayerInventory.SLOT_WRIST, ItemStack.EMPTY);
		}
	}

	public ActionResult<ItemStack> spawnSuitEntity(EntityPlayer playerIn, ItemStack stack) {
		World worldIn = playerIn.world;
		ItemStack[] armor = getArmorInRing(stack);

		boolean hasStuffInIt = false;
		for (ItemStack stacks : armor) {
			if (!stacks.isEmpty())
				hasStuffInIt = true;
		}

		if (hasStuffInIt) {
			EntityRingDummy dummy = new EntityRingDummy(worldIn);
			Vec3d look = playerIn.getLookVec();
			dummy.setPosition(playerIn.posX, playerIn.posY, playerIn.posZ);
			dummy.dirX = (float) look.xCoord;
			dummy.dirZ = (float) look.zCoord;
			dummy.rotationPitch = playerIn.rotationPitch;
			dummy.rotationYaw = playerIn.rotationYaw;
			dummy.setItemStackToSlot(EntityEquipmentSlot.HEAD, armor[0]);
			dummy.setItemStackToSlot(EntityEquipmentSlot.CHEST, armor[1]);
			dummy.setItemStackToSlot(EntityEquipmentSlot.LEGS, armor[2]);
			dummy.setItemStackToSlot(EntityEquipmentSlot.FEET, armor[3]);
			if (!worldIn.isRemote)
				worldIn.spawnEntity(dummy);
			stack.shrink(1);
			playerIn.addStat(SHAchievements.suitRing);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}

}
