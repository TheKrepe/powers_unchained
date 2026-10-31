package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.web.WebShooting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Feats: things a player has to DO before some Tech Table modules can be unlocked (owner, like Fisk's suit
 * requirements). Stored in {@link TechProgress#feats()}, shown in the Upgrade tab as done / not done.
 */
public final class TechFeats {

    /** Struck by lightning while wearing web-shooters (Electro-Web). */
    public static final String LIGHTNING = "lightning_in_web_shooters";

    /** Every task (for the command). */
    public static final java.util.List<String> ALL = java.util.List.of(LIGHTNING);

    private TechFeats() {
    }

    public static Component title(String feat) {
        return Component.translatable("feat.powers_unchained." + feat);
    }

    /** Loader hook: a lightning bolt hit a player. */
    public static void onStruckByLightning(ServerPlayer player) {
        if (WebShooting.hasWebShooters(player)) {
            complete(player, LIGHTNING);
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
