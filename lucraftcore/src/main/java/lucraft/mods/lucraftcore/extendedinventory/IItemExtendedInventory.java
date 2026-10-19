package lucraft.mods.lucraftcore.extendedinventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IItemExtendedInventory {

    ExtendedInventoryItemType getEIItemType(ItemStack stack);

    default void onWornTick(ItemStack stack, EntityPlayer player) {

    }

    default void onEquipped(ItemStack itemstack, EntityPlayer player) {

    }

    default void onUnequipped(ItemStack itemstack, EntityPlayer player) {

    }

    default boolean useButton(ItemStack stack, EntityPlayer player) {
        return false;
    }

    default String getAbilityBarDescription(ItemStack stack, EntityPlayer player) {
        return "";
    }

    default void onPressedButton(ItemStack itemstack, EntityPlayer player, boolean pressed) {

    }

    default boolean canEquip(ItemStack itemstack, EntityPlayer player) {
        return true;
    }

    default boolean canUnequip(ItemStack itemstack, EntityPlayer player) {
        return true;
    }

    public static enum ExtendedInventoryItemType {

        NECKLACE, MANTLE, WRIST;

    }

}
