package com.star_1mp.powersunleashed.tech;

import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.web.WebShooting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Feats: things a player has to DO before some Tech Table modules can be unlocked (owner, like Fisk's suit
 * requirements). Stored in {@link TechProgress#feats()}, shown in the Upgrade tab as done / not done.
 */
public final class TechFeats {

    /** Struck by lightning while wearing web-shooters (Electro-Web). */
    public static final String LIGHTNING = "lightning_in_web_shooters";

    /**
     * Not a task (no toast, not in the command): the player has had a Radioactive Spider Eye, which reveals the cheaper
     * Isotope Capsule recipe on the Tech Table.
     */
    public static final String SEEN_RADIOACTIVE_EYE = "seen_radioactive_spider_eye";

    /** Every task (for the command). */
    public static final java.util.List<String> ALL = java.util.List.of(LIGHTNING);

    private TechFeats() {
    }

    public static Component title(String feat) {
        return Component.translatable("feat.powers_unleashed." + feat);
    }

    /** Loader hook: a lightning bolt hit a player. */
    public static void onStruckByLightning(ServerPlayer player) {
        if (WebShooting.hasWebShooters(player)) {
            complete(player, LIGHTNING);
        }
    }

    /** Server, every player tick: notices a radioactive spider eye in the inventory (every 2 s). */
    public static void playerTick(ServerPlayer player) {
        if (player.tickCount % 40 == 0 && !Services.GAMEPLAY.techProgress(player).hasFeat(SEEN_RADIOACTIVE_EYE)
                && player.getInventory().contains(new net.minecraft.world.item.ItemStack(
                        com.star_1mp.powersunleashed.registry.PUItems.RADIOACTIVE_SPIDER_EYE.get()))) {
            complete(player, SEEN_RADIOACTIVE_EYE);
        }
    }

    public static void complete(ServerPlayer player, String feat) {
        TechProgress progress = Services.GAMEPLAY.techProgress(player);
        if (progress.hasFeat(feat)) {
            return;
        }
        // owner: a toast (top right) instead of the green action bar line — the client sees the synced feat and shows it
        Services.GAMEPLAY.setTechProgress(player, progress.withFeat(feat));
    }
}
