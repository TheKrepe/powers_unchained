package com.thekrepe.powersunchained.web;

import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.registry.PUDataComponents;
import com.thekrepe.powersunchained.registry.PUItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Web fluid ammo: web-shooters hold up to {@link #SLOTS} web cartridges (put in like items into a bundle), every
 * shot uses one portion of web fluid from the first cartridge that still has some. Used-up cartridges stay inside
 * as empty cartridges until they are taken out or swapped for a full one. Players in creative mode shoot for free.
 */
public final class WebCartridges {

    public static final int SLOTS = 2;
    /** Shots in one full cartridge. */
    public static final int MAX_FLUID = 128;

    private WebCartridges() {
    }

    /** Web fluid of every loaded cartridge (0 = empty cartridge). */
    public static List<Integer> contents(ItemStack shooters) {
        return shooters.getOrDefault(PUDataComponents.WEB_CARTRIDGES.get(), List.of());
    }

    private static void set(ItemStack shooters, List<Integer> contents) {
        if (contents.isEmpty()) {
            shooters.remove(PUDataComponents.WEB_CARTRIDGES.get());
        } else {
            shooters.set(PUDataComponents.WEB_CARTRIDGES.get(), List.copyOf(contents));
        }
    }

    public static int totalFluid(ItemStack shooters) {
        return contents(shooters).stream().mapToInt(Integer::intValue).sum();
    }

    /** A (partly) filled web cartridge that can be loaded. */
    public static boolean isLoadable(ItemStack stack) {
        return stack.is(PUItems.WEB_CARTRIDGE.get()) && fluid(stack) > 0;
    }

    /** Web fluid of a cartridge stack (0 for an empty cartridge). */
    public static int fluid(ItemStack cartridge) {
        if (!cartridge.is(PUItems.WEB_CARTRIDGE.get())) {
            return 0;
        }
        return Mth.clamp(cartridge.getOrDefault(PUDataComponents.WEB_FLUID.get(), MAX_FLUID), 0, MAX_FLUID);
    }

    /** The cartridge item for a loaded slot. */
    public static ItemStack toStack(int fluid) {
        if (fluid <= 0) {
            return new ItemStack(PUItems.EMPTY_WEB_CARTRIDGE.get());
        }
        ItemStack stack = new ItemStack(PUItems.WEB_CARTRIDGE.get());
        if (fluid < MAX_FLUID) {
            stack.set(PUDataComponents.WEB_FLUID.get(), fluid);
        }
        return stack;
    }

    /** Loads one cartridge into a free slot. @return whether it was loaded (the caller removes it) */
    public static boolean insert(ItemStack shooters, ItemStack cartridge) {
        List<Integer> contents = contents(shooters);
        if (!isLoadable(cartridge) || contents.size() >= SLOTS) {
            return false;
        }
        List<Integer> next = new ArrayList<>(contents);
        next.add(fluid(cartridge));
        set(shooters, next);
        return true;
    }

    /**
     * Loads one cartridge, swapping out a used-up one if there is no free slot.
     *
     * @return {@code null} if it could not be loaded, else the swapped-out empty cartridge (or an empty stack)
     */
    public static @Nullable ItemStack insertOrSwap(ItemStack shooters, ItemStack cartridge) {
        if (insert(shooters, cartridge)) {
            return ItemStack.EMPTY;
        }
        if (!isLoadable(cartridge)) {
            return null;
        }
        List<Integer> next = new ArrayList<>(contents(shooters));
        int empty = next.indexOf(0);
        if (empty < 0) {
            return null;
        }
        next.set(empty, fluid(cartridge));
        set(shooters, next);
        return toStack(0);
    }

    /** Takes the last loaded cartridge out, or returns an empty stack. */
    public static ItemStack removeLast(ItemStack shooters) {
        List<Integer> contents = contents(shooters);
        if (contents.isEmpty()) {
            return ItemStack.EMPTY;
        }
        List<Integer> next = new ArrayList<>(contents);
        int fluid = next.remove(next.size() - 1);
        set(shooters, next);
        return toStack(fluid);
    }

    /** Uses one shot of web fluid. @return {@code false} if all cartridges are empty */
    public static boolean consume(ItemStack shooters) {
        List<Integer> next = new ArrayList<>(contents(shooters));
        for (int i = 0; i < next.size(); i++) {
            if (next.get(i) > 0) {
                next.set(i, next.get(i) - 1);
                set(shooters, next);
                return true;
            }
        }
        return false;
    }

    /** The web-shooters the player wears (live stack), or an empty stack. */
    public static ItemStack wornShooters(Player player) {
        return Services.GAMEPLAY.findAccessory(player, PUItems.WEB_SHOOTERS.get());
    }

    /** Uses one shot of web fluid from the worn web-shooters. @return {@code false} if there is none */
    public static boolean consumeWorn(Player player) {
        boolean[] used = {false};
        Services.GAMEPLAY.modifyAccessory(player, PUItems.WEB_SHOOTERS.get(), shooters -> used[0] = consume(shooters));
        return used[0];
    }

    /** Whether the player can fire a web now (has web fluid, or is in creative mode). */
    public static boolean hasFluid(Player player) {
        return player.getAbilities().instabuild || totalFluid(wornShooters(player)) > 0;
    }

    public static void playInsertSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    public static void playRemoveSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }
}
