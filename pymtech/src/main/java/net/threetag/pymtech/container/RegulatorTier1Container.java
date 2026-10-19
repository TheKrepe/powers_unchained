package net.threetag.pymtech.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.threetag.pymtech.ability.RegulatorAbility;

import static net.threetag.pymtech.container.RegulatorSlot.canFitRegulatorSlot;
import static net.threetag.pymtech.container.PymParticleVialSlot.canFitVialSlot;

public class RegulatorTier1Container extends Container {

    public final PlayerInventory playerInventory;

    public RegulatorTier1Container(int id, PlayerInventory playerInventory, RegulatorAbility ability) {
        super(PTContainerTypes.REGULATOR_TIER1, id);
        this.playerInventory = playerInventory;

        this.addSlot(new RegulatorSlot(ability.itemHandler, 0, 92, 22));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 1, 7, 11));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 2, 7, 33));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 3, 177, 11));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 4, 177, 33));

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 20 + j1 * 18, 74 + l * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 20 + j * 18, 132));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index > 4) {
                if (canFitRegulatorSlot(itemstack1)) {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (canFitVialSlot(itemstack1)) {
                    if (!this.mergeItemStack(itemstack1, 1, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 5 && index < 32) {
                    if (!this.mergeItemStack(itemstack1, 32, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 32 && index < 41 && !this.mergeItemStack(itemstack1, 5, 32, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 5, 41, false)) {
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
