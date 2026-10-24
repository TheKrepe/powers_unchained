package com.star_1mp.powersunchained.suit;

import com.star_1mp.powersunchained.item.SuitFragmentItem;
import com.star_1mp.powersunchained.item.SuitPieceItem;
import com.star_1mp.powersunchained.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Unlocking suits: by scanning (fragments or a piece of the suit) or for experience levels. */
public final class SuitUnlocks {

    private SuitUnlocks() {
    }

    public static SuitProgress progress(Player player) {
        return Services.GAMEPLAY.suitProgress(player);
    }

    public static boolean isUnlocked(Player player, Suit suit) {
        return progress(player).isUnlocked(suit);
    }

    public static void unlock(ServerPlayer player, Suit suit) {
        Services.GAMEPLAY.setSuitProgress(player, progress(player).unlock(suit));
    }

    public static void lock(ServerPlayer player, Suit suit) {
        Services.GAMEPLAY.setSuitProgress(player, progress(player).lock(suit));
    }

    /** Adds a scanned fragment; unlocks the suit when enough were scanned. @return whether it got unlocked */
    public static boolean addFragment(ServerPlayer player, Suit suit) {
        SuitProgress progress = progress(player);
        int count = progress.fragments(suit) + 1;
        if (count >= SuitCatalog.info(suit).fragments()) {
            unlock(player, suit);
            return true;
        }
        Services.GAMEPLAY.setSuitProgress(player, progress.withFragments(suit, count));
        return false;
    }

    /** Unlocks a suit for experience levels (free in creative). @return whether it worked */
    public static boolean buyWithExperience(ServerPlayer player, Suit suit) {
        if (isUnlocked(player, suit)) {
            return false;
        }
        int cost = SuitCatalog.info(suit).xpCost();
        if (!player.hasInfiniteMaterials()) {
            if (player.experienceLevel < cost) {
                return false;
            }
            player.giveExperienceLevels(-cost);
        }
        unlock(player, suit);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    /** The suit a scanned item reveals: a fragment's suit or the suit of a suit piece; {@code null} if none. */
    public static @Nullable Suit scanTarget(ItemStack stack) {
        if (stack.getItem() instanceof SuitPieceItem piece) {
            return piece.getSuit();
        }
        return SuitFragmentItem.suit(stack);
    }
}
