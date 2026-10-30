package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.web.WebShooting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Feats: things a player has to DO before some Tech Table modules can be unlocked (owner, like Fisk's suit
 * requirements). Stored in {@link TechProgress#feats()}, shown in the Upgrade tab as done / not done.
 */
public final class TechFeats {

    /** Struck by lightning while wearing web-shooters (Electro-Web). */
    public static final String LIGHTNING = "lightning_in_web_shooters";

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
        Services.GAMEPLAY.setTechProgress(player, progress.withFeat(feat));
        player.sendSystemMessage(Component.translatable("feat.powers_unchained.done", title(feat)).withStyle(ChatFormatting.GREEN), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.4F);
    }
}
