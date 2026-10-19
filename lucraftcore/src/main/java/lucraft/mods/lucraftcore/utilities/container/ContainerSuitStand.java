package lucraft.mods.lucraftcore.utilities.container;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.slots.SlotEIItem;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntitySuitStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerSuitStand extends Container {

    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
    private final TileEntitySuitStand suitStand;

    public ContainerSuitStand(EntityPlayer player, TileEntitySuitStand suitStand) {
        this.suitStand = suitStand;

        for (int i = 0; i < 4; i++) {
            final int k_f = i;
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[i];
            this.addSlotToContainer(new Slot(suitStand, i, 89, 17 + i * 19) {
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    if (stack.isEmpty())
                        return false;
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, player);
                }

                @Override
                @SideOnly(Side.CLIENT)
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[3 - k_f];
                }
            });
        }

        for (int k = 0; k < 4; ++k) {
            final int k_f = k;
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(player.inventory, 36 + (3 - k), 8, 19 + k * 18) {
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    if (stack.isEmpty())
                        return false;
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, player);
                }

                @Override
                @SideOnly(Side.CLIENT)
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[3 - k_f];
                }
            });
        }

        // NECKLACE
        this.addSlotToContainer(new Slot(suitStand, 4, 89, 93) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) stack.getItem()).getEIItemType(stack) == IItemExtendedInventory.ExtendedInventoryItemType.NECKLACE;
            }
        });

        // MANTLE
        this.addSlotToContainer(new Slot(suitStand, 5, 107, 93) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) stack.getItem()).getEIItemType(stack) == IItemExtendedInventory.ExtendedInventoryItemType.MANTLE;
            }
        });

        // WRIST
        this.addSlotToContainer(new Slot(suitStand, 6, 125, 93) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() != null && stack.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) stack.getItem()).getEIItemType(stack) == IItemExtendedInventory.ExtendedInventoryItemType.WRIST;
            }
        });

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 124 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(player.inventory, k, 8 + k * 18, 182));
        }

        InventoryExtendedInventory inv = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();

        this.addSlotToContainer(new SlotEIItem(player, inv, InventoryExtendedInventory.SLOT_NECKLACE, 26, 93, IItemExtendedInventory.ExtendedInventoryItemType.NECKLACE));

        this.addSlotToContainer(new SlotEIItem(player, inv, InventoryExtendedInventory.SLOT_MANTLE, 44, 93, IItemExtendedInventory.ExtendedInventoryItemType.MANTLE));

        this.addSlotToContainer(new SlotEIItem(player, inv, InventoryExtendedInventory.SLOT_WRIST, 62, 93, IItemExtendedInventory.ExtendedInventoryItemType.WRIST));

    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return suitStand.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index >= 0 && index <= 3) {
                if (!this.mergeItemStack(itemstack1, 4, 8, false) && !this.mergeItemStack(itemstack1, 11, 47, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 8 && index <= 10) {
                if (!this.mergeItemStack(itemstack1, 47, 50, false) && !this.mergeItemStack(itemstack1, 11, 47, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 4 && index <= 7) {
                if (!this.mergeItemStack(itemstack1, 0, 4, false) && !this.mergeItemStack(itemstack1, 11, 47, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 47 && index <= 49) {
                if (!this.mergeItemStack(itemstack1, 8, 11, false) && !this.mergeItemStack(itemstack1, 11, 47, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 11 && index <= 46) {
                if (!this.mergeItemStack(itemstack1, 0, 11, false) && !this.mergeItemStack(itemstack1, 47, 50, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
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
