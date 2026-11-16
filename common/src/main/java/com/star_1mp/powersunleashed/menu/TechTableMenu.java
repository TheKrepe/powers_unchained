package com.star_1mp.powersunleashed.menu;

import com.star_1mp.powersunleashed.item.WebShootersItem;
import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.registry.PUBlocks;
import com.star_1mp.powersunleashed.registry.PUItems;
import com.star_1mp.powersunleashed.registry.PUMenus;
import com.star_1mp.powersunleashed.tech.GadgetSkins;
import com.star_1mp.powersunleashed.tech.TechLink;
import com.star_1mp.powersunleashed.tech.TechIngredient;
import com.star_1mp.powersunleashed.tech.TechProgress;
import com.star_1mp.powersunleashed.tech.TechRecipe;
import com.star_1mp.powersunleashed.tech.TechRecipes;
import com.star_1mp.powersunleashed.tech.WebModule;
import com.star_1mp.powersunleashed.tech.WebModules;
import com.star_1mp.powersunleashed.web.WebCartridges;
import java.util.ArrayList;
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
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Tech Table menu. Tabs (the screen decides which slots are on screen):
 * <ul>
 *   <li>Creation: a 3x3 crafting grid (blueprint) and the result slot; recipes from {@link TechRecipes};</li>
 *   <li>Upgrade: the gadget slot; modules are unlocked / installed / removed through {@link #clickMenuButton};</li>
 *   <li>Customization: the gadget slot (the same item, shown in another place) and the looks.</li>
 * </ul>
 * The grid and the gadget slot are emptied into the player's inventory when the menu closes.
 */
public class TechTableMenu extends AbstractContainerMenu {

    public static final int TAB_CREATE = 0;
    public static final int TAB_UPGRADE = 1;
    public static final int TAB_CUSTOMIZE = 2;

    /** Button ids. */
    public static final int PLACE_RECIPE = 100;
    public static final int UNLOCK_MODULE = 200;
    public static final int INSTALL_MODULE = 300;
    public static final int REMOVE_MODULE = 400;
    public static final int UNLOCK_SKIN = 500;
    public static final int SELECT_SKIN = 600;

    /** Item positions (inside the GUI). */
    public static final int INVENTORY_X = 141;
    public static final int INVENTORY_Y = 153;
    public static final int GRID_X = 168;
    public static final int GRID_Y = 52;
    public static final int RESULT_X = 349;
    public static final int RESULT_Y = 50;
    public static final int DEVICE_X = 213;
    public static final int DEVICE_Y = 80;
    public static final int SKIN_DEVICE_X = 284;  // right of the hologram controls (Steve | Alex | pose)
    public static final int SKIN_DEVICE_Y = 129;

    /** Slot indexes. */
    public static final int GRID_SLOT = 36;
    public static final int RESULT_SLOT = 45;
    public static final int DEVICE_SLOT = 46;
    public static final int SKIN_DEVICE_SLOT = 47;

    private final ContainerLevelAccess access;
    private final Player player;
    private final SimpleContainer grid = new SimpleContainer(9) {
        @Override
        public void setChanged() {
            super.setChanged();
            TechTableMenu.this.slotsChanged(this);
        }
    };
    private final SimpleContainer result = new SimpleContainer(1);
    private final SimpleContainer device = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            TechTableMenu.this.slotsChanged(this);
        }
    };

    /** 1 = the table is linked to a Suit Creator (touching / redstone line, see {@link TechLink}): Customization works. */
    private final DataSlot linked = DataSlot.standalone();
    private int linkCheck;
    private int linkVersion = -1;

    /** Client view: the open tab (inactive slots are neither drawn nor clickable). */
    public int tab = TAB_CREATE;

    public TechTableMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public TechTableMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(PUMenus.TECH_TABLE.get(), containerId);
        this.access = access;
        this.player = inventory.player;
        BooleanSupplier always = () -> true;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new ViewSlot(inventory, 9 + row * 9 + col, INVENTORY_X + col * 18, INVENTORY_Y + row * 18, always));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new ViewSlot(inventory, col, INVENTORY_X + col * 18, INVENTORY_Y + 58, always));
        }
        BooleanSupplier create = () -> this.tab == TAB_CREATE;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new ViewSlot(this.grid, row * 3 + col, GRID_X + col * 22, GRID_Y + row * 22, create));
            }
        }
        this.addSlot(new ResultSlot(this.result, 0, RESULT_X, RESULT_Y, create));
        this.addSlot(new DeviceSlot(this.device, DEVICE_X, DEVICE_Y, () -> this.tab == TAB_UPGRADE));
        this.addSlot(new DeviceSlot(this.device, SKIN_DEVICE_X, SKIN_DEVICE_Y, () -> this.tab == TAB_CUSTOMIZE && this.isLinked()));
        this.addDataSlot(this.linked);
        this.updateLink();
    }

    /** Whether the Customization tab works (the table is linked to a Suit Creator). */
    public boolean isLinked() {
        return this.linked.get() == 1;
    }

    /**
     * Server only: the client's menu has no level access, so it would set the slot to 0 — and the server, whose value
     * never changed, would not send it again (owner's bug: "no link" after taking the device out of the slot).
     */
    private void updateLink() {
        if (this.player instanceof ServerPlayer) {
            this.linked.set(this.access.evaluate(TechLink::isLinked, false) ? 1 : 0);
        }
    }

    @Override
    public void broadcastChanges() {
        // optimisation: the redstone-line search runs again only when blocks were placed / broken / blown up somewhere
        // (TechLink.version) — checked twice a second — or every 5 seconds for anything else (pistons, water...)
        if (++this.linkCheck % 10 == 0 && (this.linkVersion != TechLink.version() || this.linkCheck >= 100)) {
            this.linkCheck = 0;
            this.linkVersion = TechLink.version();
            this.updateLink();
        }
        super.broadcastChanges();
    }

    // ---- state ----

    public ItemStack device() {
        return this.device.getItem(0);
    }

    public List<ItemStack> gridItems() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            list.add(this.grid.getItem(i));
        }
        return list;
    }

    public static boolean isGadget(ItemStack stack) {
        return stack.getItem() instanceof WebShootersItem;
    }

    public TechProgress progress() {
        return Services.GAMEPLAY.techProgress(this.player);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.grid) {
            TechRecipe recipe = TechRecipes.find(this.gridItems());
            this.result.setItem(0, recipe != null ? recipe.resultStack() : ItemStack.EMPTY);
        }
    }

    // ---- buttons ----

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id <= TAB_CUSTOMIZE) {
            this.tab = id;  // the server needs it for shift-clicks
            return true;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (id >= SELECT_SKIN) {
            return this.selectSkin(serverPlayer, id - SELECT_SKIN);
        }
        if (id >= UNLOCK_SKIN) {
            return this.unlockSkin(serverPlayer, id - UNLOCK_SKIN);
        }
        if (id >= REMOVE_MODULE) {
            return this.removeModule(serverPlayer, module(id - REMOVE_MODULE));
        }
        if (id >= INSTALL_MODULE) {
            return this.installModule(serverPlayer, module(id - INSTALL_MODULE));
        }
        if (id >= UNLOCK_MODULE) {
            return this.unlockModule(serverPlayer, module(id - UNLOCK_MODULE));
        }
        if (id >= PLACE_RECIPE) {
            int index = id - PLACE_RECIPE;
            return index < TechRecipes.all().size() && this.placeRecipe(serverPlayer, TechRecipes.all().get(index));
        }
        return false;
    }

    private static @Nullable WebModule module(int ordinal) {
        return ordinal >= 0 && ordinal < WebModule.values().length ? WebModule.values()[ordinal] : null;
    }

    /** Like the recipe book: the grid goes back to the inventory, then one set of the ingredients goes in (if there is one). */
    private boolean placeRecipe(ServerPlayer player, TechRecipe recipe) {
        List<@Nullable TechIngredient> layout = recipe.layout();
        // enough of everything? (count what the grid holds too: it goes back first)
        List<ItemStack> pool = new ArrayList<>();
        for (ItemStack s : player.getInventory().getNonEquipmentItems()) {
            pool.add(s.copy());
        }
        for (int i = 0; i < 9; i++) {
            pool.add(this.grid.getItem(i).copy());
        }
        for (TechIngredient ing : layout) {
            if (ing != null && !takeFrom(pool, ing)) {
                return false;
            }
        }
        for (int i = 0; i < 9; i++) {
            ItemStack s = this.grid.removeItemNoUpdate(i);
            if (!s.isEmpty()) {
                player.getInventory().placeItemBackInInventory(s, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
        NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < 9; i++) {
            TechIngredient ing = layout.get(i);
            if (ing == null) {
                continue;
            }
            for (ItemStack s : items) {
                if (ing.matches(s)) {
                    this.grid.setItem(i, s.split(1));
                    break;
                }
            }
        }
        player.getInventory().setChanged();
        this.slotsChanged(this.grid);
        return true;
    }

    private static boolean takeFrom(List<ItemStack> pool, TechIngredient ing) {
        for (ItemStack s : pool) {
            if (ing.matches(s)) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    private boolean unlockModule(ServerPlayer player, @Nullable WebModule module) {
        TechProgress progress = this.progress();
        ItemStack gadget = this.device();
        // owner: unlocked on THIS pair of web-shooters only
        if (module == null || !isGadget(gadget) || WebModules.isUnlocked(gadget, module) || !module.requirementMet(progress)
                || !module.unlockCost().pay(player)) {
            return false;
        }
        WebModules.setUnlocked(gadget, module, true);
        this.device.setChanged();
        this.sound(player, SoundEvents.PLAYER_LEVELUP, 0.7F, 1.6F);
        return true;
    }

    private boolean installModule(ServerPlayer player, @Nullable WebModule module) {
        ItemStack gadget = this.device();
        if (module == null || !isGadget(gadget) || !WebModules.isUnlocked(gadget, module) || WebModules.has(gadget, module)) {
            return false;
        }
        List<WebModule> installed = new ArrayList<>(WebModules.installed(gadget));
        if (installed.size() >= WebModules.MAX_INSTALLED) {  // installing is free (owner)
            return false;
        }
        installed.add(module);
        WebModules.set(gadget, installed);
        this.device.setChanged();
        this.sound(player, SoundEvents.SMITHING_TABLE_USE, 0.8F, 1.3F);
        return true;
    }

    private boolean removeModule(ServerPlayer player, @Nullable WebModule module) {
        ItemStack gadget = this.device();
        if (module == null || !WebModules.has(gadget, module) || WebShootersItem.isCreative(gadget)) {
            return false;
        }
        List<WebModule> installed = new ArrayList<>(WebModules.installed(gadget));
        installed.remove(module);
        WebModules.set(gadget, installed);
        WebModules.setUnlocked(gadget, module, true);  // stays unlocked on this pair: put it back for free
        // fewer cartridge slots now: the extra cartridges come out
        while (WebCartridges.contents(gadget).size() > WebModules.cartridgeSlots(gadget)) {
            player.getInventory().placeItemBackInInventory(WebCartridges.removeLast(gadget), net.minecraft.util.Prediction.SERVER_ONLY);
        }
        this.device.setChanged();
        this.sound(player, SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.8F, 1.2F);
        return true;
    }

    private boolean unlockSkin(ServerPlayer player, int index) {
        if (!this.isLinked() || index < 0 || index >= GadgetSkins.WEB_SHOOTERS.size()) {
            return false;
        }
        GadgetSkins skin = GadgetSkins.WEB_SHOOTERS.get(index);
        ItemStack gadget = this.device();
        // owner: bought on THIS pair of web-shooters only; may also need a suit unlocked
        if (!isGadget(gadget) || skin.isUnlocked(gadget) || skin.cost() == null || !skin.requirementMet(player)
                || !skin.cost().pay(player)) {
            return false;
        }
        skin.unlock(gadget);
        this.device.setChanged();
        this.sound(player, SoundEvents.PLAYER_LEVELUP, 0.7F, 1.6F);
        return true;
    }

    private boolean selectSkin(ServerPlayer player, int index) {
        ItemStack gadget = this.device();
        if (!this.isLinked() || index < 0 || index >= GadgetSkins.WEB_SHOOTERS.size() || !isGadget(gadget)) {
            return false;
        }
        GadgetSkins skin = GadgetSkins.WEB_SHOOTERS.get(index);
        if (!skin.isUnlocked(gadget)) {
            return false;
        }
        GadgetSkins.set(gadget, skin.id());
        this.device.setChanged();
        this.sound(player, SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.8F, 1.2F);
        return true;
    }

    private void sound(Player player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.BLOCKS, volume, pitch);
    }

    // ---- crafting ----

    /** Takes one set of ingredients out of the grid (leaving bottles behind). */
    private void consumeGrid(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = this.grid.getItem(i);
            if (s.isEmpty()) {
                continue;
            }
            ItemStack rest = TechRecipe.remainder(s);
            s.shrink(1);
            if (!rest.isEmpty()) {
                if (s.isEmpty()) {
                    this.grid.setItem(i, rest);
                } else {
                    player.getInventory().placeItemBackInInventory(rest, net.minecraft.util.Prediction.SERVER_ONLY);
                }
            }
        }
        this.grid.setChanged();
    }

    // ---- container basics ----

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        if (index == RESULT_SLOT) {
            // craft as many as possible
            ItemStack first = slot.getItem().copy();
            int made = 0;
            while (slot.hasItem() && ItemStack.isSameItemSameComponents(slot.getItem(), first) && made < 64) {
                ItemStack out = slot.getItem().copy();
                if (!this.moveItemStackTo(out, 0, 36, true)) {
                    break;
                }
                slot.onTake(player, slot.getItem().copy());
                made++;
            }
            return made > 0 ? first : ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index >= GRID_SLOT) {
            if (!this.moveItemStackTo(stack, 0, 36, true)) {
                return ItemStack.EMPTY;
            }
        } else if ((this.tab == TAB_UPGRADE || this.tab == TAB_CUSTOMIZE && this.isLinked()) && isGadget(stack) && this.device().isEmpty()) {
            if (!this.moveItemStackTo(stack, DEVICE_SLOT, DEVICE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (this.tab == TAB_CREATE && this.moveItemStackTo(stack, GRID_SLOT, GRID_SLOT + 9, false)) {
            // into the grid
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
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.grid);
            this.clearContainer(player, this.device);
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, PUBlocks.TECH_TABLE.get());
    }

    /** A slot that is only there while its tab is open. */
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

    private static final class DeviceSlot extends ViewSlot {
        /** Empty device slot icon (a cog: any device goes here, for now only web-shooters). */
        static final net.minecraft.resources.Identifier ICON = com.star_1mp.powersunleashed.Constants.id("tech_table/device_slot");

        DeviceSlot(Container container, int x, int y, BooleanSupplier visible) {
            super(container, 0, x, y, visible);
        }

        @Override
        public net.minecraft.resources.Identifier getNoItemIcon() {
            return ICON;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isGadget(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private final class ResultSlot extends ViewSlot {
        ResultSlot(Container container, int slot, int x, int y, BooleanSupplier visible) {
            super(container, slot, x, y, visible);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            TechTableMenu.this.consumeGrid(player);
            super.onTake(player, stack);
        }
    }
}
