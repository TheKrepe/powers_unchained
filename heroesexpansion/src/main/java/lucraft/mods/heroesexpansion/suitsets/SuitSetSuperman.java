package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.RegistryEvent.Register;

public class SuitSetSuperman extends HESuitSet {

    public NBTTagCompound data = new NBTTagCompound();

    public SuitSetSuperman(String name) {
        super(name);
        this.data.setFloat("solar_energy_multiplier", 2);
    }

    @Override
    public void registerItems(Register<Item> e) {
        e.getRegistry().register(chestplate = createItem(this, EntityEquipmentSlot.CHEST));
        e.getRegistry().register(legs = createItem(this, EntityEquipmentSlot.LEGS));
        e.getRegistry().register(boots = createItem(this, EntityEquipmentSlot.FEET));
    }

    @Override
    public ItemStack getRepairItem(ItemStack toRepair) {
        return new ItemStack(UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.BLUE));
    }

    @Override
    public NBTTagCompound getData() {
        return this.data;
    }
}
