package com.thekrepe.powersunchained.client.suitcreator;

import com.thekrepe.powersunchained.item.SuitFragmentItem;
import com.thekrepe.powersunchained.registry.PUItems;
import com.thekrepe.powersunchained.suit.Suit;
import com.thekrepe.powersunchained.suit.SuitUnlocks;
import com.thekrepe.powersunchained.suit.Suits;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.ArmorType;
import org.jspecify.annotations.Nullable;

/** Client side of suit unlocking: toasts for newly unlocked suits, and what the Suit Creator shows as "just unlocked". */
public final class SuitCreatorClient {

    private static @Nullable Player lastPlayer;
    private static Set<Identifier> known = new HashSet<>();
    private static @Nullable Suit lastUnlocked;
    private static long lastUnlockedAt;
    private static long ticks;

    private SuitCreatorClient() {
    }

    public static void init() {
        SuitFragmentItem.localPlayer = () -> Minecraft.getInstance().player;
    }

    /** Client tick: compares the synced progress with the last one and toasts new suits. */
    public static void tick() {
        ticks++;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            lastPlayer = null;
            return;
        }
        Set<Identifier> now = SuitUnlocks.progress(player).unlocked();
        if (player != lastPlayer) {
            // new world / respawn: take the current state as known
            lastPlayer = player;
            known = new HashSet<>(now);
            return;
        }
        for (Identifier id : now) {
            if (known.add(id)) {
                Suit suit = Suits.ALL.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
                if (suit != null) {
                    lastUnlocked = suit;
                    lastUnlockedAt = ticks;
                    mc.gui.toastManager().addToast(new SuitUnlockToast(PUItems.piece(suit, ArmorType.HELMET).get().getDefaultInstance(), displayName(suit)));
                }
            }
        }
        known.retainAll(now);
    }

    /** Full name of a suit, e.g. "Человек-паук: Рэйми". */
    public static Component displayName(Suit suit) {
        return Component.translatable("screen.powers_unchained.suit_creator.full_name",
                Component.translatable("suit_group.powers_unchained." + com.thekrepe.powersunchained.suit.SuitCatalog.info(suit).group()),
                Component.translatable(Suits.variantNameKey(suit)));
    }

    /** The suit unlocked within the last {@code withinTicks} ticks, or {@code null}. */
    public static @Nullable Suit recentlyUnlocked(int withinTicks) {
        return lastUnlocked != null && ticks - lastUnlockedAt <= withinTicks ? lastUnlocked : null;
    }

    public static long ticks() {
        return ticks;
    }
}
