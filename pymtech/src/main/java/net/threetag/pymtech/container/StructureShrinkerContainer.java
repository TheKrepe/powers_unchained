package net.threetag.pymtech.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.threetag.pymtech.tileentity.StructureShrinkerTileEntity;

public class StructureShrinkerContainer extends Container {

    public final PlayerInventory inventoryPlayer;
    public final StructureShrinkerTileEntity tileEntity;
    private final IIntArray intArray;

    public StructureShrinkerContainer(int id, PlayerInventory inventoryPlayer) {
        this(id, inventoryPlayer, new StructureShrinkerTileEntity(), new IntArray(8));
    }

    public StructureShrinkerContainer(int id, PlayerInventory inventoryPlayer, StructureShrinkerTileEntity tileEntity) {
        this(id, inventoryPlayer, tileEntity, new IntArray(8));
    }

    public StructureShrinkerContainer(int id, PlayerInventory inventoryPlayer, StructureShrinkerTileEntity tileEntity, IIntArray intArray) {
        super(PTContainerTypes.STRUCTURE_SHRINKER, id);
        this.inventoryPlayer = inventoryPlayer;
        this.tileEntity = tileEntity;
        this.intArray = intArray;
        assertIntArraySize(intArray, 8);
        this.trackIntArray(this.intArray);

        this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            this.addSlot(new SlotItemHandler(itemHandler, 0, 8, 61));
            this.addSlot(new SlotItemHandler(itemHandler, 1, 130, 17));
            this.addSlot(new SlotItemHandler(itemHandler, 2, 130, 61));
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 92 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventoryPlayer, k, 8 + k * 18, 150));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.tileEntity.isUsableByPlayer(playerIn);
    }

    @OnlyIn(Dist.CLIENT)
    public float getEnergyPercentage() {
        return (float) this.getEnergyStored() / (float) this.getMaxEnergyStored();
    }

    @OnlyIn(Dist.CLIENT)
    public int getEnergyStored() {
        return this.intArray.get(0);
    }

    @OnlyIn(Dist.CLIENT)
    public int getMaxEnergyStored() {
        return this.intArray.get(1);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getSelectionStart() {
        return new BlockPos(this.intArray.get(2), this.intArray.get(3), this.intArray.get(4)).add(this.tileEntity.getPos());
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getSelectionEnd() {
        return new BlockPos(this.intArray.get(5), this.intArray.get(6), this.intArray.get(7)).add(this.tileEntity.getPos());
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack1 = slot.getStack();
            stack = stack1.copy();
            if (index <= 2) {
                if (!this.mergeItemStack(stack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(stack1, stack);
            } else {
                if (stack1.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
                    if (!this.mergeItemStack(stack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stack1.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()) {
                    if (!this.mergeItemStack(stack1, 1, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 30) {
                    if (!this.mergeItemStack(stack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 39 && !this.mergeItemStack(stack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack1.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack1);
        }

        return super.transferStackInSlot(player, index);
    }

}
