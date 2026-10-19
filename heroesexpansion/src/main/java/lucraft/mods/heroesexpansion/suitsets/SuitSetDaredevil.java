package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.lucraftcore.superpowers.effects.Effect;
import lucraft.mods.lucraftcore.superpowers.effects.EffectConditionAlways;
import lucraft.mods.lucraftcore.superpowers.effects.EffectHUD;
import lucraft.mods.lucraftcore.superpowers.effects.EffectNameChange;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuitSetDaredevil extends HESuitSet {

    public List<Effect> effects = new ArrayList<>();
    public ItemArmor.ArmorMaterial material;

    public SuitSetDaredevil(String name, ItemArmor.ArmorMaterial material, ResourceLocation hudTexture) {
        super(name);
        this.material = material;

        EffectNameChange nameChange = new EffectNameChange();
        nameChange.conditions.add(new EffectConditionAlways());
        nameChange.name = new TextComponentString("???");
        this.effects.add(nameChange);

        EffectHUD hud = new EffectHUD();
        hud.texture = hudTexture;
        this.effects.add(hud);
    }

    @Override
    public List<String> getExtraDescription(ItemStack stack) {
        if (this == HESuitSet.DAREDEVIL_HOMEMADE)
            return Arrays.asList(StringHelper.translateToLocal("heroesexpansion.info.homemade"));
        return super.getExtraDescription(stack);
    }

    @Override
    public List<Effect> getEffects() {
        return this.effects;
    }

    @Override
    public ItemStack getRepairItem(ItemStack toRepair) {
        return new ItemStack(UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.RED));
    }

    @Override
    public ItemArmor.ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
        return this.material;
    }

}
