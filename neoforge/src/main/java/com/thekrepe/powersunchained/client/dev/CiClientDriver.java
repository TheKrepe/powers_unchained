package com.thekrepe.powersunchained.client.dev;

import com.thekrepe.powersunchained.Constants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Automated visual test used only by the CI "client test" workflow. Completely inactive unless the
 * JVM is started with {@code -Dpowers_unchained.ci=true}. Logs {@code [CI-SHOT] name} lines; the workflow takes a
 * screenshot of the virtual display whenever such a line appears.
 */
public final class CiClientDriver {

    private static final boolean CLIENT = Boolean.getBoolean("powers_unchained.ci");

    private static final String FULL_SUIT = "equipment:{head:{id:\"powers_unchained:spider_man_mask\"},chest:{id:\"powers_unchained:spider_man_suit\"},"
            + "legs:{id:\"powers_unchained:spider_man_pants\"},feet:{id:\"powers_unchained:spider_man_boots\"}}";
    private static final String MASK_AND_BOOTS = "equipment:{head:{id:\"powers_unchained:spider_man_mask\"},feet:{id:\"powers_unchained:spider_man_boots\"}}";
    private static final String SUIT_AND_PANTS = "equipment:{chest:{id:\"powers_unchained:spider_man_suit\"},legs:{id:\"powers_unchained:spider_man_pants\"}}";
    private static final String WIDE = "profile:{model:\"wide\",texture:\"minecraft:entity/player/wide/steve\"}";
    private static final String SLIM = "profile:{model:\"slim\",texture:\"minecraft:entity/player/slim/alex\"}";

    private static int ticks = -1;

    private CiClientDriver() {
    }

    public static void register() {
        if (CLIENT) {
            Constants.LOG.warn("[CI] Client test driver enabled");
            NeoForge.EVENT_BUS.addListener(CiClientDriver::onClientTick);
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) {
            return;
        }
        ticks++;
        switch (ticks) {
            case 100 -> {
                run("time set 6000");
                run("weather clear");
                run("tp @p 0.5 ~ 0.5 0 0");
                run("item replace entity @p armor.head with powers_unchained:spider_man_mask");
                run("item replace entity @p armor.chest with powers_unchained:spider_man_suit");
                run("item replace entity @p armor.legs with powers_unchained:spider_man_pants");
                run("item replace entity @p armor.feet with powers_unchained:spider_man_boots");
            }
            // Each scene: set up, wait ~5 s for it to render, log the shot marker, then hold ~10 s so the
            // workflow (which polls the log) captures the screen before the next scene starts.
            case 160 -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            case 260 -> shot("01_player_front");
            case 460 -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            case 560 -> shot("02_player_back");
            case 760 -> {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                summon(-2.4, WIDE, FULL_SUIT);
                summon(-0.8, SLIM, FULL_SUIT);
                summon(0.8, WIDE, MASK_AND_BOOTS);
                summon(2.4, SLIM, SUIT_AND_PANTS);
            }
            case 880 -> shot("03_mannequins_and_hand");
            case 1080 -> {
                run("item replace entity @p armor.chest with minecraft:air");
                run("item replace entity @p armor.legs with minecraft:air");
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            }
            case 1180 -> shot("04_player_mask_and_boots_only");
            case 1380 -> {
                Constants.LOG.info("[CI] done");
                mc.stop();
            }
            default -> {
            }
        }
    }

    private static void summon(double x, String profile, String equipment) {
        run("execute at @p run summon minecraft:mannequin ~" + x + " ~ ~5 {Rotation:[180f,0f],immovable:1b," + profile + "," + equipment + "}");
    }

    private static void shot(String name) {
        Constants.LOG.info("[CI-SHOT] {}", name);
    }

    private static void run(String command) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command));
        }
    }
}
