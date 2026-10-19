package lucraft.mods.lucraftcore.utilities.container;

import lucraft.mods.lucraftcore.utilities.blocks.TileEntityConstructionTable;
import lucraft.mods.lucraftcore.utilities.items.ItemInstruction;
import lucraft.mods.lucraftcore.utilities.recipes.InstructionRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerConstructionTable extends Container {

    public final TileEntityConstructionTable tileTable;
    public final EntityPlayer player;

    public ContainerConstructionTable(EntityPlayer player, TileEntityConstructionTable tableInventory) {
        this.tileTable = tableInventory;
        this.player = player;
        InventoryPlayer playerInv = player.inventory;
        this.addSlotToContainer(new Slot(tableInventory, 0, 11, 34) {

            @Override
            public int getSlotStackLimit() {
                return 1;
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ItemInstruction;
            }

        });

        this.addSlotToContainer(new Slot(tableInventory, 1, 149, 34) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack getStack() {
                if (tileTable.selectedRecipe < 0 || tileTable.selectedRecipe >= tileTable.getInstructionRecipes().size())
                    return ItemStack.EMPTY;
                InstructionRecipe recipe = tileTable.getInstructionRecipes().get(tileTable.selectedRecipe);
                if (recipe != null && tileTable.hasPlayerAllRequiredItems(playerInv, recipe)) {
                    return recipe.getOutput().copy();
                }
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
                tileTable.craftItem(playerInv);
                player.inventory.addItemStackToInventory(ItemStack.EMPTY);
                return super.onTake(thePlayer, stack);
            }

            @Override
            public void putStack(ItemStack stack) {
            }

            @Override
            public void onSlotChanged() {
            }

            @Override
            public ItemStack decrStackSize(int amount) {
                return getStack();
            }

        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.tileTable.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 1) {
                if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 0) {
                if (!itemstack1.isEmpty() && itemstack1.getItem() instanceof ItemInstruction) {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 2 && index < 29) {
                    if (!this.mergeItemStack(itemstack1, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 29 && index < 38 && !this.mergeItemStack(itemstack1, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 2, 38, false)) {
                return ItemStack.EMPTY;
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
