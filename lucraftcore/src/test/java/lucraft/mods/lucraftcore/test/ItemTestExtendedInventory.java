package lucraft.mods.lucraftcore.test;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityFirePunch;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityFlight;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityWaterBreathing;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.IAbilityProvider;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemTestExtendedInventory extends ItemBase implements IItemExtendedInventory, IAbilityProvider {

    public ItemTestExtendedInventory() {
        super("test");
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack != newStack;
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("water_breathing", new AbilityWaterBreathing(entity));
        abilities.put("flight", new AbilityFlight(entity).setDataValue(AbilityFlight.SPEED, 0.1F).setDataValue(AbilityFlight.SPRINT_SPEED, 0.2F).setDataValue(AbilityFlight.ROTATE_ARMS, true));
        abilities.put("fire_punch", new AbilityFirePunch(entity).setDataValue(AbilityFirePunch.DURATION, 50));
        abilities.put("test", new AbilityTest(entity).setMaxCooldown(20));
        return abilities;
    }

    @Override
    public ExtendedInventoryItemType getEIItemType(ItemStack stack) {
        return ExtendedInventoryItemType.NECKLACE;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityPlayer player) {

    }

    @Override
    public void onEquipped(ItemStack itemstack, EntityPlayer player) {
        System.out.println("EQUIP");
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityPlayer player) {
        System.out.println("UNEQUIP");
    }

    @Override
    public void onPressedButton(ItemStack itemstack, EntityPlayer player, boolean pressed) {
        System.out.println("BUTTON");
    }

    @Override
    public boolean canEquip(ItemStack itemstack, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack itemstack, EntityPlayer player) {
        return true;
    }

}
