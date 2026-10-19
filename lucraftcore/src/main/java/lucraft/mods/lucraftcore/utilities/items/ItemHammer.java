package lucraft.mods.lucraftcore.utilities.items;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemHammer extends ItemBase {

    public ItemHammer(String name) {
        super(name);
        this.setMaxStackSize(1);
        this.setCreativeTab(LucraftCore.CREATIVE_TAB);
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        super.onCreated(stack, worldIn, playerIn);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("Uses", 0);
        stack.setTagCompound(nbt);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        if (stack.getTagCompound() == null)
            stack.setTagCompound(new NBTTagCompound());

        NBTTagCompound nbt = stack.getTagCompound();
        int uses = nbt.getInteger("Uses") + 1;

        if (uses < 16) {
            nbt.setInteger("Uses", uses);
            ItemStack newS = stack.copy();
            newS.setTagCompound(nbt);
            return newS;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        if (stack.getTagCompound() == null)
            stack.setTagCompound(new NBTTagCompound());

        return stack.getTagCompound().getInteger("Uses") > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (stack.getTagCompound() == null)
            stack.setTagCompound(new NBTTagCompound());

        return stack.getTagCompound().getInteger("Uses") / 16D;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

}
