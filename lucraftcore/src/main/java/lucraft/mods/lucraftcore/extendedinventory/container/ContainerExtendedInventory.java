package lucraft.mods.lucraftcore.extendedinventory.container;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory.ExtendedInventoryItemType;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.slots.SlotEIItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ContainerExtendedInventory extends Container {

    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

    public final InventoryExtendedInventory inventory2;
    private final EntityPlayer thePlayer;

    public ContainerExtendedInventory(EntityPlayer player) {
        this.thePlayer = player;
        InventoryPlayer inventoryPlayer = player.inventory;
        this.inventory2 = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlotToContainer(new Slot(inventoryPlayer, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i1, 8 + i1 * 18, 142));
        }

        for (int k = 0; k < 4; ++k) {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(inventoryPlayer, 36 + (3 - k), 45, 8 + k * 18) {
                /**
                 * Returns the maximum stack size for a given slot (usually the same as
                 * getInventoryStackLimit(), but 1 in the case of armor slots)
                 */
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                /**
                 * Check if the stack is allowed to be placed in this slot, used for armor slots
                 * as well as furnace fuel.
                 */
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, player);
                }

                /**
                 * Return whether this slot's stack can be taken from this slot.
                 */
                @Override
                public boolean canTakeStack(EntityPlayer playerIn) {
                    ItemStack itemstack = this.getStack();
                    return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
                }

                @Override
                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        this.addSlotToContainer(new Slot(inventoryPlayer, 40, 114, 62) {
            @Override
            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });

        this.addSlotToContainer(new SlotEIItem(player, inventory2, InventoryExtendedInventory.SLOT_NECKLACE, 114, 8, ExtendedInventoryItemType.NECKLACE));

        this.addSlotToContainer(new SlotEIItem(player, inventory2, InventoryExtendedInventory.SLOT_MANTLE, 114, 26, ExtendedInventoryItemType.MANTLE));

        this.addSlotToContainer(new SlotEIItem(player, inventory2, InventoryExtendedInventory.SLOT_WRIST, 114, 44, ExtendedInventoryItemType.WRIST));

        this.addSlotToContainer(new Slot(inventory2, InventoryExtendedInventory.SLOT_LEFT_WEAPON, 17, 35) {
            /**
             * Returns the maximum stack size for a given slot (usually the
             * same as getInventoryStackLimit(), but 1 in the case of armor
             * slots)
             */
            public int getSlotStackLimit() {
                return 1;
            }

            /**
             * Check if the stack is allowed to be placed in this slot, used
             * for armor slots as well as furnace fuel.
             */
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem().getAttributeModifiers(EntityEquipmentSlot.MAINHAND, stack).containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            }

            /**
             * Return whether this slot's stack can be taken from this slot.
             */
            public boolean canTakeStack(EntityPlayer playerIn) {
                ItemStack itemstack = this.getStack();
                return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
            }
        });

        this.addSlotToContainer(new Slot(inventory2, InventoryExtendedInventory.SLOT_RIGHT_WEAPON, 142, 35) {
            /**
             * Returns the maximum stack size for a given slot (usually the
             * same as getInventoryStackLimit(), but 1 in the case of armor
             * slots)
             */
            public int getSlotStackLimit() {
                return 1;
            }

            /**
             * Check if the stack is allowed to be placed in this slot, used
             * for armor slots as well as furnace fuel.
             */
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem().getAttributeModifiers(EntityEquipmentSlot.MAINHAND, stack).containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            }

            /**
             * Return whether this slot's stack can be taken from this slot.
             */
            public boolean canTakeStack(EntityPlayer playerIn) {
                ItemStack itemstack = this.getStack();
                return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
            }
        });
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    @Nullable
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);

            if (index >= 36 && index < 40) {
                if (!this.mergeItemStack(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) itemstack.getItem()).getEIItemType(itemstack) == ExtendedInventoryItemType.NECKLACE && !((Slot) this.inventorySlots.get(41)).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 41, 42, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) itemstack.getItem()).getEIItemType(itemstack) == ExtendedInventoryItemType.MANTLE && !((Slot) this.inventorySlots.get(42)).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 42, 43, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) itemstack.getItem()).getEIItemType(itemstack) == ExtendedInventoryItemType.WRIST && !((Slot) this.inventorySlots.get(43)).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 43, 44, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack.getItem().getAttributeModifiers(EntityEquipmentSlot.MAINHAND, itemstack).containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName()) && !((Slot) this.inventorySlots.get(44)).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 44, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack.getItem().getAttributeModifiers(EntityEquipmentSlot.MAINHAND, itemstack).containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName()) && !((Slot) this.inventorySlots.get(45)).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !((Slot) this.inventorySlots.get(39 - entityequipmentslot.getIndex())).getHasStack()) {
                int i = 3 - entityequipmentslot.getIndex();

                if (!this.mergeItemStack(itemstack1, 36 + i, 36 + i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (entityequipmentslot == EntityEquipmentSlot.OFFHAND && !((Slot) this.inventorySlots.get(40)).getHasStack()) {
                if (!this.mergeItemStack(itemstack1, 40, 41, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 0 && index < 27) {
                if (!this.mergeItemStack(itemstack1, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 27 && index < 36) {
                if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 26, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

}
