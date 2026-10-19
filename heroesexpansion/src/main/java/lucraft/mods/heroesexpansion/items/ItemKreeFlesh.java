package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.potion.PotionEffect;

public class ItemKreeFlesh extends ItemFood {

    public ItemKreeFlesh(String name) {
        super(4, 0.1F, true);
        this.setTranslationKey(name);
        this.setRegistryName(StringHelper.unlocalizedToResourceName(name));
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.setPotionEffect(new PotionEffect(MobEffects.NAUSEA, 600, 0), 0.8F);
    }

}
