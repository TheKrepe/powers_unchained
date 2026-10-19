package lucraft.mods.lucraftcore.util.items;

import lucraft.mods.lucraftcore.util.energy.EnergyStorageItem;
import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class ItemBaseEnergyStorage extends ItemBase {

    public int capacity;

    public ItemBaseEnergyStorage(String name, int capacity) {
        super(name);
        this.setMaxStackSize(1);
        this.capacity = capacity;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new EnergyItemCapabilityProvider(stack, capacity);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            tooltip.add(EnergyUtil.getFormattedEnergy(stack.getCapability(CapabilityEnergy.ENERGY, null)));
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (!stack.hasCapability(CapabilityEnergy.ENERGY, null))
            return 1D;
        double damage = 1D - ((double) stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored() / (double) stack.getCapability(CapabilityEnergy.ENERGY, null).getMaxEnergyStored());
        return damage;
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
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab))
            return;

        ItemStack empty = new ItemStack(this);
        ItemStack full = new ItemStack(this);
        full.getCapability(CapabilityEnergy.ENERGY, null).receiveEnergy(full.getCapability(CapabilityEnergy.ENERGY, null).getMaxEnergyStored(), false);

        items.add(empty);
        items.add(full);
    }

    public static class EnergyItemCapabilityProvider<NBTBase> implements ICapabilityProvider {

        final ItemStack stack;
        final IEnergyStorage energyCap;

        public EnergyItemCapabilityProvider(ItemStack stack, int capacity) {
            this.stack = stack;
            this.energyCap = new EnergyStorageItem(stack, capacity);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == CapabilityEnergy.ENERGY;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(energyCap) : null;
        }

    }

}
