package com.star_1mp.powersunchained.menu;

import com.star_1mp.powersunchained.item.SuitFragmentItem;
import com.star_1mp.powersunchained.registry.PUBlocks;
import com.star_1mp.powersunchained.registry.PUItems;
import com.star_1mp.powersunchained.registry.PUMenus;
import com.star_1mp.powersunchained.suit.Suit;
import com.star_1mp.powersunchained.suit.SuitCatalog;
import com.star_1mp.powersunchained.suit.SuitInfo;
import com.star_1mp.powersunchained.suit.SuitUnlocks;
import com.star_1mp.powersunchained.suit.Suits;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Suit Creator menu: the player's inventory (shown on the Catalog and Scanner tabs) and the scanner slot (Scanner
 * tab). Sewing and unlocking for experience go through {@link #clickMenuButton}; scanning runs on the server once
 * per tick while the menu is open.
 */
public class SuitCreatorMenu extends AbstractContainerMenu {

    /** Ticks to scan one item. */
    public static final int SCAN_TICKS = 60;
    /** Button ids: sew = suit index * 8 + piece (0-3, {@link SuitCatalog#ALL} = all); unlock = {@link #UNLOCK_BUTTON} + suit index. */
    public static final int UNLOCK_BUTTON = 4000;

    public static final int STATE_EMPTY = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_KNOWN = 2;
    public static final int STATE_INVALID = 3;

    /** Item position of the top left inventory slot / the scan slot (screen coordinates inside the GUI). */
    public static final int INVENTORY_X = 141;
    public static final int INVENTORY_Y = 153;
    public static final int SCAN_X = 206;
    public static final int SCAN_Y = 41;
    public static final int SCAN_SLOT = 36;

    private static final ArmorType[] PIECES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};

    private final ContainerLevelAccess access;
    private final Player player;
    private final SimpleContainer scan = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            SuitCreatorMenu.this.slotsChanged(this);
        }
    };
    private final DataSlot scanProgress = DataSlot.standalone();
    private final DataSlot scanState = DataSlot.standalone();
    private long lastScanTick = -1;

    /** Client view: which parts of the menu are on screen (inactive slots are neither drawn nor clickable). */
    public boolean inventoryVisible = true;
    public boolean scannerVisible = false;

    public SuitCreatorMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public SuitCreatorMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(PUMenus.SUIT_CREATOR.get(), containerId);
        this.access = access;
        this.player = inventory.player;
        BooleanSupplier inv = () -> this.inventoryVisible;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new ViewSlot(inventory, 9 + row * 9 + col, INVENTORY_X + col * 18, INVENTORY_Y + row * 18, inv));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new ViewSlot(inventory, col, INVENTORY_X + col * 18, INVENTORY_Y + 58, inv));
        }
        this.addSlot(new ViewSlot(this.scan, 0, SCAN_X, SCAN_Y, () -> this.scannerVisible) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isScannable(stack);
            }
        });
        this.addDataSlot(this.scanProgress);
        this.addDataSlot(this.scanState);
    }

    public static boolean isScannable(ItemStack stack) {
        return SuitUnlocks.scanTarget(stack) != null;
    }

    public int scanProgress() {
        return this.scanProgress.get();
    }

    public int scanState() {
        return this.scanState.get();
    }

    public ItemStack scanItem() {
        return this.scan.getItem(0);
    }

    // ---- scanning (server) ----

    @Override
    public void broadcastChanges() {
        if (this.player instanceof ServerPlayer serverPlayer) {
            long now = serverPlayer.level().getGameTime();
            if (now != this.lastScanTick) {
                this.lastScanTick = now;
                this.tickScan(serverPlayer);
            }
        }
        super.broadcastChanges();
    }

    private void tickScan(ServerPlayer player) {
        ItemStack stack = this.scan.getItem(0);
        Suit target = SuitUnlocks.scanTarget(stack);
        if (target == null) {
            this.scanProgress.set(0);
            this.scanState.set(stack.isEmpty() ? STATE_EMPTY : STATE_INVALID);
            return;
        }
        if (SuitUnlocks.isUnlocked(player, target)) {
            this.scanProgress.set(0);
            this.scanState.set(STATE_KNOWN);
            return;
        }
        this.scanState.set(STATE_SCANNING);
        int progress = this.scanProgress.get() + 1;
        if (progress % 20 == 1) {
            this.sound(player, SoundEvents.BEACON_AMBIENT, 0.6F, 1.8F);
        }
        if (progress < SCAN_TICKS) {
            this.scanProgress.set(progress);
            return;
        }
        this.scanProgress.set(0);
        boolean unlocked;
        if (stack.getItem() instanceof SuitFragmentItem) {
            stack.shrink(1);
            this.scan.setChanged();
            unlocked = SuitUnlocks.addFragment(player, target);
        } else {
            // a piece of the suit itself: the whole pattern can be read from it, the piece stays
            SuitUnlocks.unlock(player, target);
            unlocked = true;
        }
        this.sound(player, unlocked ? SoundEvents.PLAYER_LEVELUP : SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.8F, unlocked ? 1.4F : 1.0F);
    }

    private void sound(Player player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.BLOCKS, volume, pitch);
    }

    // ---- buttons: sew, unlock ----

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (id >= UNLOCK_BUTTON) {
            int index = id - UNLOCK_BUTTON;
            return index < Suits.ALL.size() && SuitUnlocks.buyWithExperience(serverPlayer, Suits.ALL.get(index));
        }
        int index = id / 8;
        int piece = id % 8;
        if (index < 0 || index >= Suits.ALL.size() || piece > SuitCatalog.ALL) {
            return false;
        }
        Suit suit = Suits.ALL.get(index);
        if (!SuitUnlocks.isUnlocked(player, suit)) {
            return false;
        }
        List<SuitInfo.Material> cost = SuitCatalog.cost(suit, piece);
        if (!player.hasInfiniteMaterials()) {
            if (!hasMaterials(player.getInventory(), cost)) {
                return false;
            }
            for (SuitInfo.Material m : cost) {
                take(player.getInventory(), m.item().get(), m.count());
            }
        }
        for (int p = 0; p < 4; p++) {
            if (piece == SuitCatalog.ALL || piece == p) {
                ItemStack result = PUItems.piece(suit, PIECES[p]).get().getDefaultInstance();
                if (!player.getInventory().add(result) || !result.isEmpty()) {
                    net.minecraft.world.entity.item.ItemEntity drop = player.createItemStackToDrop(result, false, false);
                    if (drop != null) {
                        player.level().addFreshEntity(drop);
                    }
                }
            }
        }
        this.sound(player, SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
        this.sound(player, SoundEvents.UI_LOOM_TAKE_RESULT, 1.0F, 1.0F);
        return true;
    }

    /** How many of {@code item} the inventory holds (main + hotbar). */
    public static int count(Inventory inventory, Item item) {
        int n = 0;
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            if (stack.is(item)) {
                n += stack.getCount();
            }
        }
        return n;
    }

    public static boolean hasMaterials(Inventory inventory, List<SuitInfo.Material> cost) {
        for (SuitInfo.Material m : cost) {
            if (count(inventory, m.item().get()) < m.count()) {
                return false;
            }
        }
        return true;
    }

    private static void take(Inventory inventory, Item item, int amount) {
        NonNullList<ItemStack> items = inventory.getNonEquipmentItems();
        for (int i = 0; i < items.size() && amount > 0; i++) {
            ItemStack stack = items.get(i);
            if (stack.is(item)) {
                int n = Math.min(amount, stack.getCount());
                stack.shrink(n);
                amount -= n;
            }
        }
        inventory.setChanged();
    }

    // ---- container basics ----

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == SCAN_SLOT) {
            if (!this.moveItemStackTo(stack, 0, 36, true)) {
                return ItemStack.EMPTY;
            }
        } else if (this.scannerVisible && isScannable(stack) && this.moveItemStackTo(stack, SCAN_SLOT, SCAN_SLOT + 1, false)) {
            // moved into the scanner
        } else if (index < 27) {
            if (!this.moveItemStackTo(stack, 27, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 27, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.scan));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, PUBlocks.SUIT_CREATOR.get());
    }

    /** A slot that is only there while its part of the screen is visible. */
    private static class ViewSlot extends Slot {
        private final BooleanSupplier visible;

        ViewSlot(Container container, int slot, int x, int y, BooleanSupplier visible) {
            super(container, slot, x, y);
            this.visible = visible;
        }

        @Override
        public boolean isActive() {
            return this.visible.getAsBoolean();
        }
    }
}
