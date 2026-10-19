package lucraft.mods.heroesexpansion.util.items;

import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemOwner extends ItemBase {

    public ItemOwner(String name) {
        super(name);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        String owner = nbt.getString("Owner");

        if (!owner.equalsIgnoreCase(entityIn.getPersistentID().toString())) {
            nbt.setString("Owner", entityIn.getPersistentID().toString());
            stack.setTagCompound(nbt);
        }
    }
}
