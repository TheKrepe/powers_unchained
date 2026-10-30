package com.star_1mp.powersunchained.tech;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A list of materials taken from the player's inventory (main + hotbar); creative players pay nothing. */
public record TechCost(List<Entry> entries) {

    public record Entry(Supplier<? extends Item> item, int count) {
        public ItemStack stack() {
            return new ItemStack(this.item.get(), this.count);
        }
    }

    public static TechCost of(Object... itemsAndCounts) {
        List<Entry> list = new ArrayList<>();
        for (int i = 0; i < itemsAndCounts.length; i += 2) {
            @SuppressWarnings("unchecked")
            Supplier<? extends Item> item = (Supplier<? extends Item>) itemsAndCounts[i];
            list.add(new Entry(item, (Integer) itemsAndCounts[i + 1]));
        }
        return new TechCost(List.copyOf(list));
    }

    public static int count(Inventory inventory, Item item) {
        int n = 0;
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            if (stack.is(item)) {
                n += stack.getCount();
            }
        }
        return n;
    }

    public boolean canPay(Player player) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        for (Entry e : this.entries) {
            if (count(player.getInventory(), e.item().get()) < e.count()) {
                return false;
            }
        }
        return true;
    }

    /** Takes the materials. @return {@code false} (and takes nothing) if the player doesn't have them */
    public boolean pay(Player player) {
        if (!this.canPay(player)) {
            return false;
        }
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        Inventory inventory = player.getInventory();
        NonNullList<ItemStack> items = inventory.getNonEquipmentItems();
        for (Entry e : this.entries) {
            int amount = e.count();
            for (int i = 0; i < items.size() && amount > 0; i++) {
                ItemStack stack = items.get(i);
                if (stack.is(e.item().get())) {
                    int n = Math.min(amount, stack.getCount());
                    stack.shrink(n);
                    amount -= n;
                }
            }
        }
        inventory.setChanged();
        return true;
    }
}
