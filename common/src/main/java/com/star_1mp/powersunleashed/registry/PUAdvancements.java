package com.star_1mp.powersunleashed.registry;

import com.star_1mp.powersunleashed.Constants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

/**
 * Advancements given from code ({@code data/powers_unleashed/advancement/<path>.json}, one impossible criterion
 * "done"): Образец получен -> Опасный эксперимент -> Великая сила… (under the vanilla Adventure tab).
 */
public final class PUAdvancements {

    /** A cave spider stands on an Irradiator (players within 16 blocks). */
    public static final String SAMPLE = "irradiation/sample";
    /** An irradiation was started (the one who pressed the button / players near a redstone start). */
    public static final String EXPERIMENT = "irradiation/experiment";
    /** Got the Spider-Man power from the Radioactive Spider's bite. */
    public static final String GREAT_POWER = "irradiation/great_power";

    private PUAdvancements() {
    }

    public static void award(ServerPlayer player, String path) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(Constants.id(path));
        if (holder != null) {
            player.getAdvancements().award(holder, "done");
        }
    }
}
