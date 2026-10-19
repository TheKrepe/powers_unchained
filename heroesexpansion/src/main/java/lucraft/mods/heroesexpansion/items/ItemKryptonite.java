package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.potions.PotionKryptonitePoison;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityCondition;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionPotionWeakness;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemKryptonite extends ItemBase {

    public ItemKryptonite(String name) {
        super("kryptonite");
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityLivingBase) {
            giveKryptonitePoison((EntityLivingBase) entityIn, 10 * 20);
        }
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        int radius = 5;
        for (EntityLivingBase player : entityItem.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(entityItem.getPosition().add(radius, radius, radius), entityItem.getPosition().add(-radius, -radius, -radius))))
            giveKryptonitePoison(player, 5 * 20);
        return false;
    }

    public static void giveKryptonitePoison(EntityLivingBase player, int duration) {
        for (Ability ability : Ability.getAbilities(player)) {
            for (AbilityCondition condition : ability.getConditions()) {
                if (condition instanceof AbilityConditionPotionWeakness && ((AbilityConditionPotionWeakness) condition).potion == PotionKryptonitePoison.KRYPTONITE_POISON)
                    player.addPotionEffect(new PotionEffect(PotionKryptonitePoison.KRYPTONITE_POISON, duration));
            }
        }
    }

    public static class ItemBlockKryptonite extends ItemBlock {

        public ItemBlockKryptonite(Block block) {
            super(block);
        }

        @Override
        public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
            if (entityIn instanceof EntityLivingBase) {
                giveKryptonitePoison((EntityLivingBase) entityIn, 10 * 20);
            }
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem entityItem) {
            int radius = 10;
            for (EntityLivingBase player : entityItem.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(entityItem.getPosition().add(radius, radius, radius), entityItem.getPosition().add(-radius, -radius, -radius))))
                giveKryptonitePoison(player, 5 * 20);
            return false;
        }
    }

}
