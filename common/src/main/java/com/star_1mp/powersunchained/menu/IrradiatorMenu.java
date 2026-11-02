package com.star_1mp.powersunchained.menu;

import com.star_1mp.powersunchained.block.entity.IrradiatorBlockEntity;
import com.star_1mp.powersunchained.irradiation.IrradiationRecipes;
import com.star_1mp.powersunchained.registry.PUBlocks;
import com.star_1mp.powersunchained.registry.PUItems;
import com.star_1mp.powersunchained.registry.PUMenus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Irradiator menu: capsule, sample and result slots + the player's inventory; button 0 = start. */
public class IrradiatorMenu extends AbstractContainerMenu {

    public static final int BUTTON_START = 0;

    // slot positions (match client/irradiation/IrradiatorScreen)
    // owner's variant A: the capsule in the neck of the fuel-rod flask, sample / result in two petri dishes
    public static final int FUEL_X = 186;
    public static final int FUEL_Y = 34;
    public static final int INPUT_X = 222;
    public static final int INPUT_Y = 50;
    public static final int OUTPUT_X = 222;
    public static final int OUTPUT_Y = 98;
    public static final int INVENTORY_X = 49;
    public static final int INVENTORY_Y = 152;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public IrradiatorMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(IrradiatorBlockEntity.SLOTS), new SimpleContainerData(IrradiatorBlockEntity.DATA_SIZE),
                ContainerLevelAccess.NULL);
    }

    public IrradiatorMenu(int containerId, Inventory inventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(PUMenus.IRRADIATOR.get(), containerId);
        checkContainerSize(container, IrradiatorBlockEntity.SLOTS);
        this.container = container;
        this.data = data;
        this.access = access;
        this.addSlot(new Slot(container, IrradiatorBlockEntity.SLOT_FUEL, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(PUItems.ISOTOPE_CAPSULE.get());
            }
        });
        this.addSlot(new Slot(container, IrradiatorBlockEntity.SLOT_INPUT, INPUT_X, INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return IrradiationRecipes.find(stack) != null;
            }
        });
        this.addSlot(new Slot(container, IrradiatorBlockEntity.SLOT_OUTPUT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, 9 + row * 9 + col, INVENTORY_X + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, INVENTORY_X + col * 18, INVENTORY_Y + 58));
        }
        this.addDataSlots(data);
    }

    public int data(int index) {
        return this.data.get(index);
    }

    public int progress() {
        return this.data.get(IrradiatorBlockEntity.DATA_PROGRESS);
    }

    public boolean running() {
        return this.data.get(IrradiatorBlockEntity.DATA_RUNNING) == 1;
    }

    public int mode() {
        return this.data.get(IrradiatorBlockEntity.DATA_MODE);
    }

    /** Entity id of the creature on the block (-1 none). */
    public int targetId() {
        int lo = this.data.get(IrradiatorBlockEntity.DATA_TARGET_LO) & 0xFFFF;
        int hi = this.data.get(IrradiatorBlockEntity.DATA_TARGET_HI) & 0xFFFF;
        return (short) hi == -1 && lo == 0xFFFF ? -1 : (hi << 16) | lo;
    }

    public int targetKind() {
        return this.data.get(IrradiatorBlockEntity.DATA_KIND);
    }

    public ItemStack slotItem(int slot) {
        return this.container.getItem(slot);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_START && player instanceof ServerPlayer serverPlayer) {
            return this.access.evaluate((level, pos) -> level instanceof ServerLevel server
                    && level.getBlockEntity(pos) instanceof IrradiatorBlockEntity be && be.start(server, serverPlayer), false);
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int machineEnd = IrradiatorBlockEntity.SLOTS;
        int end = this.slots.size();
        if (index < machineEnd) {
            if (!this.moveItemStackTo(stack, machineEnd, end, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(PUItems.ISOTOPE_CAPSULE.get())) {
            if (!this.moveItemStackTo(stack, IrradiatorBlockEntity.SLOT_FUEL, IrradiatorBlockEntity.SLOT_FUEL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (IrradiationRecipes.find(stack) != null) {
            if (!this.moveItemStackTo(stack, IrradiatorBlockEntity.SLOT_INPUT, IrradiatorBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, PUBlocks.IRRADIATOR.get());
    }
}
