package lucraft.mods.heroesexpansion.enchantments;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentWorthiness extends Enchantment {

    protected EnchantmentWorthiness(Rarity rarityIn, EnumEnchantmentType typeIn, EntityEquipmentSlot[] slots) {
        super(rarityIn, typeIn, slots);
        this.setName("worthiness");
        this.setRegistryName(HeroesExpansion.MODID, "worthiness");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

}
