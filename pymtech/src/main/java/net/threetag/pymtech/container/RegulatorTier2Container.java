package net.threetag.pymtech.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.threetag.pymtech.ability.RegulatorAbility;

import static net.threetag.pymtech.container.RegulatorSlot.canFitRegulatorSlot;
import static net.threetag.pymtech.container.PymParticleVialSlot.canFitVialSlot;

public class RegulatorTier2Container extends AbstractAdvancedRegulatorContainer {

    public final PlayerInventory playerInventory;
    public final RegulatorAbility ability;

    public RegulatorTier2Container(int id, PlayerInventory playerInventory, RegulatorAbility ability) {
        super(PTContainerTypes.REGULATOR_TIER2, ability, id);
        this.playerInventory = playerInventory;
        this.ability = ability;

        this.addSlot(new RegulatorSlot(ability.itemHandler, 0, 120, 169));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 1, 7, 158));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 2, 7, 180));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 3, 28, 158));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 4, 28, 180));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 5, 212, 158));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 6, 212, 180));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 7, 233, 158));
        this.addSlot(new PymParticleVialSlot(ability.itemHandler, 8, 233, 180));

        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 48 + j * 18, 213));
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index > 8) {
                if (canFitRegulatorSlot(itemstack1)) {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (canFitVialSlot(itemstack1)) {
                    if (!this.mergeItemStack(itemstack1, 1, 9, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.mergeItemStack(itemstack1, 9, 18, false)) {
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
