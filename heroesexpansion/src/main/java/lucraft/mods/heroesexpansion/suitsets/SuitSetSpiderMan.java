package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.heroesexpansion.abilities.AbilityWebWings;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.effects.Effect;
import lucraft.mods.lucraftcore.superpowers.effects.EffectConditionAlways;
import lucraft.mods.lucraftcore.superpowers.effects.EffectNameChange;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuitSetSpiderMan extends HESuitSet {

    public NBTTagCompound data = new NBTTagCompound();
    public List<Effect> effects = new ArrayList<>();
    public ItemArmor.ArmorMaterial material;

    public SuitSetSpiderMan(String name, ItemArmor.ArmorMaterial material) {
        super(name);
        this.data.setBoolean("spider_sense", true);
        this.material = material;

        EffectNameChange nameChange = new EffectNameChange();
        nameChange.conditions.add(new EffectConditionAlways());
        nameChange.name = new TextComponentString("???");
        this.effects.add(nameChange);
    }

    @Override
    public List<String> getExtraDescription(ItemStack stack) {
        if (this == HESuitSet.SPIDER_MAN_HOMEMADE)
            return Arrays.asList(StringHelper.translateToLocal("heroesexpansion.info.homemade"));
        return super.getExtraDescription(stack);
    }

    @Override
    public NBTTagCompound getData() {
        return data;
    }

    @Override
    public List<Effect> getEffects() {
        return this.effects;
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public ItemStack getRepairItem(ItemStack toRepair) {
        return new ItemStack(UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.RED));
    }

    @Override
    public ItemArmor.ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
        return this.material;
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        if (this == HESuitSet.SPIDER_MAN)
            abilities.put("web_wings", new AbilityWebWings(entity));
        return super.addDefaultAbilities(entity, abilities, context);
    }
}
