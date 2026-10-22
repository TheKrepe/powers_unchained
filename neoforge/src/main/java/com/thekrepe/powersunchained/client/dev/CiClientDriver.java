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

    private static final String FULL_SUIT = "equipment:{head:{id:\"powers_unchained:spider_man_mask\"},chest:{id:\"powers_unchained:spider_man_chestplate\"},"
            + "legs:{id:\"powers_unchained:spider_man_pants\"},feet:{id:\"powers_unchained:spider_man_boots\"}}";
    private static final String MASK_AND_BOOTS = "equipment:{head:{id:\"powers_unchained:spider_man_mask\"},feet:{id:\"powers_unchained:spider_man_boots\"}}";
    private static final String SUIT_AND_PANTS = "equipment:{chest:{id:\"powers_unchained:spider_man_chestplate\"},legs:{id:\"powers_unchained:spider_man_pants\"}}";
    private static final String WIDE = "profile:{model:\"wide\",texture:\"minecraft:entity/player/wide/steve\"}";
    private static final String SLIM = "profile:{model:\"slim\",texture:\"minecraft:entity/player/slim/alex\"}";

    private static final String[] VARIANTS = {"spider_man", "spider_man_first_appearance", "spider_man_advanced_2",
            "spider_man_brand_new_day", "spider_man_raimi"};

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
        if (ticks >= 4080 && ticks <= 4220 && ticks % 3 == 0) {
            rawShot(ticks / 3 % 2);  // rapid fire from both wrists, third person
        }
        if (ticks >= 4240 && ticks <= 4380 && ticks % 5 == 0) {
            rawShot(0);  // right wrist only, first person
        }
        switch (ticks) {
            case 100 -> {
                run("time set 6000");
                run("weather clear");
                run("tp @p 0.5 ~ 0.5 0 0");
                run("item replace entity @p armor.head with powers_unchained:spider_man_mask");
                run("item replace entity @p armor.chest with powers_unchained:spider_man_chestplate");
                run("item replace entity @p armor.legs with powers_unchained:spider_man_pants");
                run("item replace entity @p armor.feet with powers_unchained:spider_man_boots");
                run("suit spider_man_raimi @p");
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
                // Armor stands: full suit with arms, mask + boots, suit + pants posed, small one with the full suit.
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                run("tp @p 40.5 ~ 0.5 0 15");
                armorStand(-2.4, FULL_SUIT + ",ShowArms:1b");
                armorStand(-0.8, MASK_AND_BOOTS);
                armorStand(0.8, SUIT_AND_PANTS + ",ShowArms:1b,Pose:{RightArm:[-60f,0f,0f],LeftLeg:[-20f,0f,0f],Head:[0f,25f,0f]}");
                armorStand(2.4, FULL_SUIT + ",ShowArms:1b,Small:1b");
            }
            case 1480 -> shot("05_armor_stands");
            case 1680 -> {
                // Suit Creator: front (facing the player), side and back.
                run("tp @p 80.5 ~ 0.5 0 30");
                run("execute at @p run setblock ~ ~ ~3 powers_unchained:suit_creator[facing=north]");
                run("execute at @p run setblock ~2 ~ ~3 powers_unchained:suit_creator[facing=west]");
                run("execute at @p run setblock ~-2 ~ ~3 powers_unchained:suit_creator[facing=south]");
            }
            case 1780 -> shot("06_suit_creators");
            case 1980 -> {
                // All fabrics + the Suit Creator item, shown big with item displays.
                run("tp @p 120.5 ~ 0.5 0 -8");
                String[] colors = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
                for (int i = 0; i < colors.length; i++) {
                    double x = 2.1 - (i % 8) * 0.6;
                    double y = 2.4 - (i / 8) * 0.7;
                    display(x, y, "powers_unchained:" + colors[i] + "_fabric");
                }
                display(0.0, 0.9, "powers_unchained:suit_creator");
            }
            case 2080 -> shot("07_fabrics_and_suit_creator_item");
            case 2280 -> {
                // All Spider-Man variants on mannequins (wide/slim alternating), facing the player.
                run("tp @p 160.5 ~ 0.5 0 10");
                for (int i = 0; i < VARIANTS.length; i++) {
                    summon(2.6 - i * 1.3, i % 2 == 0 ? WIDE : SLIM, fullSuit(VARIANTS[i]));
                }
            }
            case 2380 -> shot("08_suit_variants_front");
            case 2580 -> run("tp @p 160.5 ~ 10.5 180 10");
            case 2680 -> shot("09_suit_variants_back");
            case 2880 -> {
                // Item icons: one column per variant, rows mask / suit / pants / boots.
                run("tp @p 200.5 ~ 0.5 0 -8");
                String[] pieces = {"mask", "chestplate", "pants", "boots"};
                for (int i = 0; i < VARIANTS.length; i++) {
                    for (int j = 0; j < pieces.length; j++) {
                        display(1.6 - i * 0.8, 2.6 - j * 0.6, "powers_unchained:" + VARIANTS[i] + "_" + pieces[j]);
                    }
                }
            }
            case 2980 -> shot("10_suit_items");
            case 3180 -> {
                // Web-shooters in the Curios bracelet slot (Curios runs in dev/CI only), over the Raimi suit.
                run("tp @p 240.5 ~ 0.5 0 0");
                run("curios replace bracelet 0 @p with powers_unchained:web_shooters[powers_unchained:web_cartridges=[32,20]]");
                run("item replace entity @p weapon.mainhand with minecraft:air");
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            }
            case 3280 -> shot("11_web_shooters_third_person");
            case 3480 -> mc.options.setCameraType(CameraType.FIRST_PERSON);
            case 3580 -> shot("12_web_shooters_first_person");
            case 3780 -> {
                // Web shooting: villager target, a stone wall to the left, shots by the server-side ability code.
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                run("tp @p 280.5 ~ 0.5 0 0");
                run("execute at @p run summon minecraft:villager ~ ~ ~6 {NoAI:1b,Rotation:[180f,0f]}");
                run("execute at @p run summon minecraft:pig ~2 ~ ~6 {NoAI:1b}");
                run("execute at @p run fill ~3 ~ ~8 ~8 ~3 ~8 minecraft:stone");
            }
            case 3800 -> run("execute as @p at @s run tp @s ~ ~ ~ -25 5");
            case 3830, 3834, 3838, 3842 -> shootWeb(com.thekrepe.powersunchained.web.WebType.IMPACT);
            case 3846 -> run("execute as @p at @s run tp @s ~ ~ ~ -25 -10");
            case 3850 -> shootWeb(com.thekrepe.powersunchained.web.WebType.STICKY);
            case 3854 -> run("execute as @p at @s run tp @s ~ ~ ~ 0 0");
            case 3858, 3864 -> shootWeb(com.thekrepe.powersunchained.web.WebType.STICKY);
            case 3866 -> run("execute as @p at @s run tp @s ~ ~ ~ -8 10");
            case 3868 -> shot("13_webs_on_villager_and_wall");
            case 4030 -> {
                // the villager shoots sticky webs at the player
                net.minecraft.server.level.ServerPlayer player = serverPlayer();
                mc.getSingleplayerServer().execute(() -> {
                    net.minecraft.server.level.ServerLevel level = player.level();
                    level.getEntitiesOfClass(net.minecraft.world.entity.npc.villager.Villager.class, player.getBoundingBox().inflate(10))
                            .forEach(v -> {
                                level.addFreshEntity(new com.thekrepe.powersunchained.entity.web.StickyWeb(level, v, 0));
                                v.setXRot(25.0F);
                                level.addFreshEntity(new com.thekrepe.powersunchained.entity.web.StickyWeb(level, v, 1));
                            });
                });
                run("execute as @p at @s run tp @s ~ ~ ~ 0 60");
            }
            case 4036 -> shot("14_player_webbed_first_person");
            case 4080 -> {
                run("execute as @p at @s run tp @s ~ ~ ~ 0 -15");
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            }
            case 4086 -> shot("15_rapid_fire_both_arms_third_person");
            case 4240 -> mc.options.setCameraType(CameraType.FIRST_PERSON);
            case 4250 -> shot("16_web_aim_first_person");
            case 4400 -> com.thekrepe.powersunchained.client.web.WebShooterClient.debugState(true, false, com.thekrepe.powersunchained.web.WebType.IMPACT);
            case 4406 -> shot("17_web_mode_indicator");
            case 4450 -> com.thekrepe.powersunchained.client.web.WebShooterClient.debugState(true, true, com.thekrepe.powersunchained.web.WebType.STICKY);
            case 4470 -> shot("18_web_wheel");
            case 4500 -> {
                // sticky webs on zombies: one turned sideways (hit on its right side), one facing the player
                com.thekrepe.powersunchained.client.web.WebShooterClient.debugState(true, false, com.thekrepe.powersunchained.web.WebType.STICKY);
                run("kill @e[type=!player]");
                run("difficulty easy");  // hostile mobs vanish on peaceful
                run("tp @p 320.5 ~ 0.5 0 0");
                run("execute at @p run summon minecraft:husk ~ ~ ~4 {NoAI:1b,Rotation:[90f,0f]}");
                run("execute at @p run summon minecraft:husk ~3 ~ ~4 {NoAI:1b,Rotation:[180f,0f]}");
            }
            case 4510 -> run("execute as @p at @s run tp @s ~ ~ ~ 0 6");
            case 4514 -> shootWeb(com.thekrepe.powersunchained.web.WebType.STICKY);
            case 4516 -> run("execute as @p at @s run tp @s ~ ~ ~ 0 19");
            case 4520 -> shootWeb(com.thekrepe.powersunchained.web.WebType.STICKY);
            case 4522 -> run("execute as @p at @s run tp @s ~ ~ ~ -37 -1");
            case 4526 -> shootWeb(com.thekrepe.powersunchained.web.WebType.STICKY);
            case 4528 -> run("execute as @p at @s run tp @s ~ ~ ~ -37 15");
            case 4532 -> shootWeb(com.thekrepe.powersunchained.web.WebType.STICKY);
            case 4536 -> run("tp @p 317.5 ~ 2.5 -63 18");
            case 4540 -> shot("19_sticky_webs_on_zombies_and_ability_hud");
            case 4600 -> {
                // cocoon: five sticky webs on one husk
                run("kill @e[type=!player]");
                run("tp @p 360.5 ~ 0.5 0 10");
                run("execute at @p run summon minecraft:husk ~ ~ ~4 {NoAI:1b,Rotation:[180f,0f]}");
            }
            case 4610, 4612, 4614, 4616, 4618 -> rawWeb(com.thekrepe.powersunchained.web.WebType.STICKY, ticks % 4 == 0 ? 0 : 1);
            case 4619 -> run("tp @p 357.5 ~ 2.5 -63 12");
            case 4620 -> shot("20_cocoon");
            case 4700 -> {
                // two sticky webs on a husk's head
                run("kill @e[type=!player]");
                run("tp @p 380.5 ~ 0.5 0 -2");
                run("execute at @p run summon minecraft:husk ~ ~ ~4 {NoAI:1b,Rotation:[180f,0f]}");
            }
            case 4706 -> rawWeb(com.thekrepe.powersunchained.web.WebType.STICKY, 0);
            case 4708 -> run("execute as @p at @s run tp @s ~ ~ ~ 4 -4");
            case 4710 -> rawWeb(com.thekrepe.powersunchained.web.WebType.STICKY, 1);
            case 4713 -> run("tp @p 378.5 ~1 2.5 -45 15");
            case 4714 -> shot("21_head_webs");
            case 4780 -> {
                // the player wrapped up: first person overlay
                run("kill @e[type=!player]");
                net.minecraft.server.level.ServerPlayer player = serverPlayer();
                Minecraft.getInstance().getSingleplayerServer().execute(() ->
                        com.thekrepe.powersunchained.entity.web.WebCocoon.wrap(player));
            }
            case 4782 -> shot("22_cocoon_first_person");
            case 4860 -> {
                // web zip at a wall: the rope pulls the player (frozen mid-pull for the screenshot)
                run("kill @e[type=!player]");
                run("difficulty peaceful");
                run("tp @p 400.5 ~ 0.5 10 -18");
                run("execute at @p run fill ~-3 ~ ~14 ~3 ~8 ~14 minecraft:stone");
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            case 4870 -> {
                zipStart = mc.player.position();
                net.minecraft.server.level.ServerPlayer player = serverPlayer();
                Minecraft.getInstance().getSingleplayerServer().execute(() -> com.thekrepe.powersunchained.web.WebShooting.zip(player));
            }
            case 4875 -> run("tick freeze");
            case 4876 -> shot("23_web_zip_rope");
            case 4930 -> run("tick unfreeze");
            case 4945 -> Constants.LOG.info("[CI] web zip moved the player from {} to {}", zipStart, mc.player.position());
            case 4955 -> {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                com.thekrepe.powersunchained.client.ability.AbilityHudConfig cfg = com.thekrepe.powersunchained.client.ability.AbilityHudConfig.get();
                cfg.x = 0.55F;
                cfg.y = 0.15F;
                cfg.scale = 1.5F;
                cfg.textOpacity = 0.6F;
                mc.gui.setScreen(new com.thekrepe.powersunchained.client.ability.AbilityHudConfigScreen());
            }
            case 4957 -> shot("24_hud_settings_screen");
            case 5010 -> mc.gui.setScreen(null);
            case 5012 -> shot("25_hud_moved_in_game");
            case 5100 -> {
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

    private static String fullSuit(String suit) {
        return "equipment:{head:{id:\"powers_unchained:" + suit + "_mask\"},chest:{id:\"powers_unchained:" + suit + "_chestplate\"},"
                + "legs:{id:\"powers_unchained:" + suit + "_pants\"},feet:{id:\"powers_unchained:" + suit + "_boots\"}}";
    }

    private static net.minecraft.server.level.ServerPlayer serverPlayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    }

    private static net.minecraft.world.phys.Vec3 zipStart = net.minecraft.world.phys.Vec3.ZERO;

    /** Spawns a web of the given type from the given wrist, bypassing the ammo (screenshot tests). */
    private static void rawWeb(com.thekrepe.powersunchained.web.WebType type, int side) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> player.level().addFreshEntity(type.create(player.level(), player, side)));
    }

    /** Spawns an impact web from the given wrist, bypassing the ammo (screenshot tests). */
    private static void rawShot(int side) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> player.level().addFreshEntity(
                com.thekrepe.powersunchained.web.WebType.IMPACT.create(player.level(), player, side)));
    }

    private static void shootWeb(com.thekrepe.powersunchained.web.WebType type) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            com.thekrepe.powersunchained.web.WebShooting.shoot(player, type);
        });
        Constants.LOG.info("[CI] shot web {}", type);
    }

    private static void armorStand(double x, String nbt) {
        run("execute at @p run summon minecraft:armor_stand ~" + x + " ~ ~4 {Rotation:[180f,0f]," + nbt + "}");
    }

    private static void display(double x, double y, String item) {
        run("execute at @p run summon minecraft:item_display ~" + x + " ~" + y + " ~4 {item:{id:\"" + item + "\",count:1},"
                + "billboard:\"center\",transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],"
                + "translation:[0f,0f,0f],scale:[0.55f,0.55f,0.55f]}}");
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
