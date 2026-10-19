package lucraft.mods.lucraftcore.infinity.items;

import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class ItemInfinityStone extends ItemIndestructible {

    public abstract EnumInfinityStone getType();

    public abstract boolean isContainer();

    public int getEnergyPerTick(ItemStack stack) {
        return 100000;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    public void onGauntletTick(Entity entityIn, World world, ItemStack stack) {
    }

    public Ability.AbilityMap addStoneAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        return abilities;
    }

    public static boolean hasStone(EntityLivingBase entity, Ability.EnumAbilityContext context, EnumInfinityStone stone) {
        if (context == Ability.EnumAbilityContext.MAIN_HAND || context == Ability.EnumAbilityContext.OFF_HAND) {
            ItemStack stack = context == Ability.EnumAbilityContext.MAIN_HAND ? entity.getHeldItemMainhand() : entity.getHeldItemOffhand();

            if (!stack.isEmpty() && stack.getItem() instanceof ItemInfinityGauntlet) {
                InventoryInfinityGauntlet inv = new InventoryInfinityGauntlet(stack);

                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack stack1 = inv.getStackInSlot(i);

                    if (!stack1.isEmpty() && stack1.getItem() instanceof ItemInfinityStone && ((ItemInfinityStone) stack1.getItem()).getType() == stone) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}