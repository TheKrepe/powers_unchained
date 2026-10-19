package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.lucraftcore.superpowers.effects.Effect;
import lucraft.mods.lucraftcore.superpowers.effects.EffectConditionNot;
import lucraft.mods.lucraftcore.superpowers.effects.EffectConditionOpenArmor;
import lucraft.mods.lucraftcore.superpowers.effects.EffectNameChange;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.RegistryEvent.Register;

import java.util.ArrayList;
import java.util.List;

public class SuitSetArrow extends HESuitSet {

    public NBTTagCompound data = new NBTTagCompound();
    public List<Effect> effects = new ArrayList<>();

    public SuitSetArrow(String name) {
        super(name);
        this.data.setFloat("bow_fov", 30F);

        EffectNameChange nameChange = new EffectNameChange();
        EffectConditionNot not = new EffectConditionNot();
        EffectConditionOpenArmor openArmor = new EffectConditionOpenArmor();
        openArmor.slot = EntityEquipmentSlot.CHEST;
        not.conditions.add(openArmor);
        nameChange.conditions.add(not);
        nameChange.name = new TextComponentString("???");
        this.effects.add(nameChange);
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.CHEST;
    }

    @Override
    public void registerItems(Register<Item> e) {
        e.getRegistry().register(chestplate = createItem(this, EntityEquipmentSlot.CHEST));
        e.getRegistry().register(legs = createItem(this, EntityEquipmentSlot.LEGS));
        e.getRegistry().register(boots = createItem(this, EntityEquipmentSlot.FEET));
    }

    @Override
    public ItemStack getRepairItem(ItemStack toRepair) {
        return new ItemStack(UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.GREEN));
    }

    @Override
    public NBTTagCompound getData() {
        return this.data;
    }

    @Override
    public List<Effect> getEffects() {
        return this.effects;
    }
}
