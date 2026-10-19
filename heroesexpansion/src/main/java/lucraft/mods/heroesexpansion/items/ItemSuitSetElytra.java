package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.events.PlayerUseGadgetEvent;
import lucraft.mods.heroesexpansion.util.items.IElytra;
import lucraft.mods.lucraftcore.superpowers.suitsets.ItemSuitSetArmor;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import lucraft.mods.lucraftcore.util.items.ItemBaseEnergyStorage;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.List;

public class ItemSuitSetElytra extends ItemSuitSetArmor implements IElytra {

    public ItemSuitSetElytra(SuitSet suitSet, EntityEquipmentSlot armorSlot) {
        super(suitSet, armorSlot);
    }

    @Override
    public boolean canUseElytra(EntityPlayer player, ItemStack stack) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerUseGadgetEvent(player, stack)))
            return false;

        boolean b = SuitSet.getSuitSet(player) == this.getSuitSet();

        if (getSuitSet().getData() != null && getSuitSet().getData().getBoolean("use_energy")) {
            return b && stack.hasCapability(CapabilityEnergy.ENERGY, null) && stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored() >= 50;
        }

        return b;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            tooltip.add(EnergyUtil.getFormattedEnergy(stack.getCapability(CapabilityEnergy.ENERGY, null)));
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab))
            return;

        ItemStack empty = new ItemStack(this);
        ItemStack full = new ItemStack(this);

        if (full.hasCapability(CapabilityEnergy.ENERGY, null)) {
            full.getCapability(CapabilityEnergy.ENERGY, null).receiveEnergy(full.getCapability(CapabilityEnergy.ENERGY, null).getMaxEnergyStored(), false);

            items.add(empty);
            items.add(full);
        } else {
            super.getSubItems(tab, items);
        }
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        super.onArmorTick(world, player, itemStack);

        if (!world.isRemote && player.isElytraFlying()) {
            if (itemStack.hasCapability(CapabilityEnergy.ENERGY, null) && itemStack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored() >= 50) {
                itemStack.getCapability(CapabilityEnergy.ENERGY, null).extractEnergy(50, false);
            }

            if (getSuitSet().getData() != null && getSuitSet().getData().getBoolean("use_durability") && player.ticksExisted % 100 == 0) {
                itemStack.damageItem(1, player);
            }
        }

    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        if (getSuitSet().getData() != null && getSuitSet().getData().getBoolean("use_energy"))
            return new ItemBaseEnergyStorage.EnergyItemCapabilityProvider(stack, 1000000);
        else
            return super.initCapabilities(stack, nbt);
    }

}
