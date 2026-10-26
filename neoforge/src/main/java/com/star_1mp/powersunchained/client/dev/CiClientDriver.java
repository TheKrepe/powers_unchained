package com.star_1mp.powersunchained.client.dev;

import com.star_1mp.powersunchained.Constants;
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
            "spider_man_brand_new_day", "spider_man_raimi", "spider_man_marvel_midnight"};

    /** Dev: {@code -Dpowers_unchained.ci.start=N} skips the scenes before tick N. */
    private static final int START = Integer.getInteger("powers_unchained.ci.start", 0);
    /** When skipping, still wait ~15 s for the world to finish loading. */
    private static int ticks = START > 0 ? START - 300 : -1;

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
        if (START > 0 && ticks < START) {
            return;
        }
        if (ticks >= 4080 && ticks <= 4220 && ticks % 3 == 0) {
            rawShot(ticks / 3 % 2);  // rapid fire from both wrists, third person
        }
        if (ticks >= 5300 && ticks <= 5420 && ticks % 3 == 0) {
            rawShot(ticks / 3 % 2);  // rapid fire from both wrists, first person: the off arm shows up
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
                run("suit give spider_man_raimi @p");
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
            case 3830, 3834, 3838, 3842 -> shootWeb(com.star_1mp.powersunchained.web.WebType.IMPACT);
            case 3846 -> run("execute as @p at @s run tp @s ~ ~ ~ -25 -10");
            case 3850 -> shootWeb(com.star_1mp.powersunchained.web.WebType.STICKY);
            case 3854 -> run("execute as @p at @s run tp @s ~ ~ ~ 0 0");
            case 3858, 3864 -> shootWeb(com.star_1mp.powersunchained.web.WebType.STICKY);
            case 3866 -> run("execute as @p at @s run tp @s ~ ~ ~ -8 10");
            case 3868 -> shot("13_webs_on_villager_and_wall");
            case 4030 -> {
                // the villager shoots sticky webs at the player
                net.minecraft.server.level.ServerPlayer player = serverPlayer();
                mc.getSingleplayerServer().execute(() -> {
                    net.minecraft.server.level.ServerLevel level = player.level();
                    level.getEntitiesOfClass(net.minecraft.world.entity.npc.villager.Villager.class, player.getBoundingBox().inflate(10))
                            .forEach(v -> {
                                level.addFreshEntity(new com.star_1mp.powersunchained.entity.web.StickyWeb(level, v, 0));
                                v.setXRot(25.0F);
                                level.addFreshEntity(new com.star_1mp.powersunchained.entity.web.StickyWeb(level, v, 1));
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
            case 4400 -> com.star_1mp.powersunchained.client.web.WebShooterClient.debugState(true, false, com.star_1mp.powersunchained.web.WebType.IMPACT);
            case 4406 -> shot("17_web_mode_indicator");
            case 4450 -> com.star_1mp.powersunchained.client.web.WebShooterClient.debugState(true, true, com.star_1mp.powersunchained.web.WebType.STICKY);
            case 4470 -> shot("18_web_wheel");
            case 4500 -> {
                // sticky webs on zombies: one turned sideways (hit on its right side), one facing the player
                com.star_1mp.powersunchained.client.web.WebShooterClient.debugState(true, false, com.star_1mp.powersunchained.web.WebType.STICKY);
                run("kill @e[type=!player]");
                run("difficulty easy");  // hostile mobs vanish on peaceful
                run("tp @p 320.5 ~ 0.5 0 0");
                run("execute at @p run summon minecraft:husk ~ ~ ~4 {NoAI:1b,Rotation:[90f,0f]}");
                run("execute at @p run summon minecraft:husk ~3 ~ ~4 {NoAI:1b,Rotation:[180f,0f]}");
            }
            case 4510 -> run("execute as @p at @s run tp @s ~ ~ ~ 0 6");
            case 4514 -> shootWeb(com.star_1mp.powersunchained.web.WebType.STICKY);
            case 4516 -> run("execute as @p at @s run tp @s ~ ~ ~ 0 19");
            case 4520 -> shootWeb(com.star_1mp.powersunchained.web.WebType.STICKY);
            case 4522 -> run("execute as @p at @s run tp @s ~ ~ ~ -37 -1");
            case 4526 -> shootWeb(com.star_1mp.powersunchained.web.WebType.STICKY);
            case 4528 -> run("execute as @p at @s run tp @s ~ ~ ~ -37 15");
            case 4532 -> shootWeb(com.star_1mp.powersunchained.web.WebType.STICKY);
            case 4536 -> run("tp @p 317.5 ~ 2.5 -63 18");
            case 4540 -> shot("19_sticky_webs_on_zombies_and_ability_hud");
            case 4600 -> {
                // cocoon: five sticky webs on one husk
                run("kill @e[type=!player]");
                run("tp @p 360.5 ~ 0.5 0 10");
                run("execute at @p run summon minecraft:husk ~ ~ ~4 {NoAI:1b,Rotation:[180f,0f]}");
            }
            case 4610, 4612, 4614, 4616, 4618 -> rawWeb(com.star_1mp.powersunchained.web.WebType.STICKY, ticks % 4 == 0 ? 0 : 1);
            case 4619 -> run("tp @p 357.5 ~ 2.5 -63 12");
            case 4620 -> shot("20_cocoon");
            case 4700 -> {
                // two sticky webs on a husk's head
                run("kill @e[type=!player]");
                run("tp @p 380.5 ~ 0.5 0 -2");
                run("execute at @p run summon minecraft:husk ~ ~ ~4 {NoAI:1b,Rotation:[180f,0f]}");
            }
            case 4706 -> rawWeb(com.star_1mp.powersunchained.web.WebType.STICKY, 0);
            case 4708 -> run("execute as @p at @s run tp @s ~ ~ ~ 4 -4");
            case 4710 -> rawWeb(com.star_1mp.powersunchained.web.WebType.STICKY, 1);
            case 4713 -> run("tp @p 378.5 ~1 2.5 -45 15");
            case 4714 -> shot("21_head_webs");
            case 4780 -> {
                // the player wrapped up: first person overlay
                run("kill @e[type=!player]");
                net.minecraft.server.level.ServerPlayer player = serverPlayer();
                Minecraft.getInstance().getSingleplayerServer().execute(() ->
                        com.star_1mp.powersunchained.entity.web.WebCocoon.wrap(player));
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
                Minecraft.getInstance().getSingleplayerServer().execute(() -> com.star_1mp.powersunchained.web.WebShooting.zip(player));
            }
            case 4875 -> run("tick freeze");
            case 4876 -> shot("23_web_zip_rope");
            case 4930 -> run("tick unfreeze");
            case 4945 -> Constants.LOG.info("[CI] web zip moved the player from {} to {}", zipStart, mc.player.position());
            case 4955 -> {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                com.star_1mp.powersunchained.client.ability.AbilityHudConfig cfg = com.star_1mp.powersunchained.client.ability.AbilityHudConfig.get();
                cfg.x = 0.55F;
                cfg.y = 0.15F;
                cfg.scale = 1.5F;
                cfg.textOpacity = 0.6F;
                mc.gui.setScreen(new com.star_1mp.powersunchained.client.ability.AbilityHudConfigScreen());
            }
            case 4957 -> shot("24_hud_settings_screen");
            case 5010 -> mc.gui.setScreen(null);
            case 5012 -> shot("25_hud_moved_in_game");
            case 5040 -> {
                // web zip poses on mannequins seen from the side
                run("kill @e[type=!player]");
                run("tp @p 500.5 ~ 0.5 0 5");
                for (int i = 0; i < 6; i++) {
                    run("execute at @p run summon minecraft:mannequin ~" + (-5 + 2 * i) + " ~ ~6 {Rotation:[-90f,0f],immovable:1b}");
                }
            }
            case 5110 -> zipPoses(new int[][] {{0, 0, 0}, {1, 3, 0}, {1, 14, 0}, {1, 8, 1}, {1, 8, 2}, {4, 50, 0}});
            case 5112 -> shot("26_zip_poses_shoot_pulls_landing");
            case 5160 -> zipPoses(new int[][] {{2, 45, 0}, {2, 45, 1}, {2, 45, 2}, {2, 45, 3}, {2, 45, 4}, {2, 45, 5}});
            case 5162 -> shot("27_zip_poses_flips");
            case 5210 -> zipPoses(new int[][] {{2, 45, 6}, {2, 45, 7}, {3, 0, 0}, {3, 0, 1}, {3, 0, 2}, {4, 25, 0}});
            case 5212 -> shot("27b_zip_poses_flips_dives_landing");
            case 5240 -> zipPoses(new int[][] {{5, 0, 0}, {5, 0, 1}, {5, 0, 2}, {6, 0, 0}, {6, 0, 1}, {4, 50, 1}});
            case 5242 -> shot("29_swing_poses_hang_shoot_crouch");
            case 5280 -> zipPoses(new int[][] {{4, 50, 2}, {5, 0, 0, 1}, {5, 0, 1, 1}, {5, 0, 2, 1}, {4, 25, 1}, {4, 25, 2}});
            case 5282 -> shot("29b_swing_poses_rising_landings");
            case 5330 -> {
                run("kill @e[type=!player]");
                run("execute as @p at @s run tp @s ~ ~ ~ 0 -5");
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
            case 5346 -> shot("28_rapid_fire_both_arms_first_person");
            case 5400 -> {
                // web swinging for real: a stone ceiling ahead, the player in the air shoots a rope at it and swings
                // (slowed down to 4 ticks per second so the screenshots catch the moments)
                run("kill @e[type=!player]");
                run("item replace entity @p weapon.mainhand with minecraft:air");
                run("tp @p 600.5 ~ 0.5 0 0");
                run("execute at @p run fill ~-8 ~20 ~6 ~8 ~21 ~22 minecraft:stone");
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                com.star_1mp.powersunchained.client.web.WebSwingClient.debugState(true);
            }
            case 5448 -> run("tick rate 4");
            case 5450 -> run("execute as @p at @s run tp @s ~ ~8 ~ 0 -48");
            case 5451 -> {
                // hold the use key like a player: shoots the rope and keeps swinging
                zipStart = mc.player.position();
                mc.options.keyUse.setDown(true);
            }
            case 5452 -> shot("30_swing_rope_shot");
            case 5459 -> shot("30b_swing_hanging_back");
            case 5461 -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            case 5465 -> shot("30c_swing_hanging_front");
            case 5470 -> {
                Constants.LOG.info("[CI] swinging moved the player from {} to {} (motion {}), hanging {}", zipStart, mc.player.position(),
                        mc.player.getDeltaMovement(), com.star_1mp.powersunchained.client.web.WebZipAnimation.isHanging(mc.player.getId()));
                mc.options.keyUse.setDown(false);  // let go: release with a launch
            }
            case 5472 -> shot("31_swing_release_trick");
            case 5475 -> shot("31b_swing_release_trick");
            case 5490 -> run("tick rate 20");
            case 5540 -> Constants.LOG.info("[CI] after the swing: at {}, health {}, on ground {}", mc.player.position(), mc.player.getHealth(),
                    mc.player.onGround());
            case 5560 -> {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                run("tick rate 2");
            }
            case 5563 -> run("tp @p 600.5 -52 0.5 0 -48");
            case 5564 -> {
                net.minecraft.server.level.ServerPlayer player = serverPlayer();
                Minecraft.getInstance().getSingleplayerServer().execute(() -> {
                    player.level().addFreshEntity(new com.star_1mp.powersunchained.entity.web.WebSwingLine(player.level(), player, 0));
                    com.star_1mp.powersunchained.web.WebShooting.announceShot(player, 0);
                });
            }
            case 5572 -> {
                shot("32_swing_first_person");
                Constants.LOG.info("[CI] first person hang rotation {}, lines {}", com.star_1mp.powersunchained.client.web.WebZipAnimation.firstPersonAnchorView(0.0F),
                        mc.level.getEntitiesOfClass(com.star_1mp.powersunchained.entity.web.WebSwingLine.class, mc.player.getBoundingBox().inflate(64.0)).size());
            }
            case 5575 -> shot("32b_swing_first_person");
            case 5578 -> shot("32c_swing_first_person");
            case 5581 -> shot("32d_swing_first_person");
            case 5590 -> {
                run("tick rate 20");
                Constants.LOG.info("[CI] first person swing: hanging {}", com.star_1mp.powersunchained.client.web.WebZipAnimation.isHanging(mc.player.getId()));
            }
            case 5640 -> {
                // the Tech Table: front, side and top
                run("kill @e[type=!player]");
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                run("tp @p 700.5 -60 0.5 180 30");
                run("setblock 699 -60 -3 powers_unchained:tech_table[facing=south]");
                run("setblock 701 -60 -3 powers_unchained:tech_table[facing=east]");
                run("setblock 700 -60 -5 powers_unchained:tech_table[facing=south]");
            }
            case 5680 -> shot("33_tech_table");
            // ---- Suit Creator menu ----
            case 5760 -> {
                run("kill @e[type=!player]");
                run("time set 6000");
                run("gamemode survival @p");
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                run("tp @p 760.5 -60 0.5 180 20");
                run("setblock 760 -60 -2 powers_unchained:suit_creator[facing=south]");
                run("clear @p");
                run("suit lock all @p");
                run("suit unlock spider_man @p");
                run("suit unlock spider_man_raimi @p");
                run("suit unlock spider_man_marvel_midnight @p");
                run("xp set @p 25 levels");
                run("give @p powers_unchained:red_fabric 20");
                run("give @p powers_unchained:blue_fabric 10");
                run("give @p powers_unchained:black_fabric 4");
                run("give @p minecraft:string 3");
            }
            case 5800 -> openSuitCreator();
            case 5830 -> creator(0, "spider_man", 4, false, 0, false);
            case 5870 -> shot("34_sc_catalog");
            case 5950 -> creator(0, "spider_man", 0, false, 1, true);
            case 5990 -> shot("35_sc_catalog_mask_alex_run");
            case 6070 -> creator(0, "spider_man", 4, true, 0, false);
            case 6110 -> shot("36_sc_tags_popup");
            case 6190 -> creator(0, "spider_man_advanced_2", 4, false, 0, false);
            case 6230 -> shot("37_sc_locked_unlock_for_xp");
            case 6260 -> {
                // a real mouse click (SDL left button) on the Scanner tab, then on the Dossier tab
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.suitcreator.SuitCreatorScreen screen) {
                    screen.debugClickTab(2);
                    int afterFirst = com.star_1mp.powersunchained.client.suitcreator.SuitCreatorPrefs.tab;
                    screen.debugClickTab(0);
                    Constants.LOG.info("[CI] clicked tabs: scanner -> tab {}, catalog -> tab {}", afterFirst,
                            com.star_1mp.powersunchained.client.suitcreator.SuitCreatorPrefs.tab);
                }
            }
            case 6280 -> shot("37b_sc_clicked_catalog_tab");
            case 6310 -> creator(0, "spider_man_marvel_midnight", 4, false, 0, false);
            case 6350 -> shot("38_sc_catalog_midnight");
            case 6430 -> {
                creator(2, "spider_man", 4, false, 0, false);
                scanSlot("powers_unchained:spider_man_first_appearance", 1);
            }
            case 6450 -> shot("39_sc_scanning");
            case 6680 -> shot("40_sc_unlocked");
            case 6820 -> {
                creator(0, "spider_man_raimi", 0, false, 0, false);
                run("tick rate 2");
            }
            case 6822 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.suitcreator.SuitCreatorScreen screen) {
                    screen.debugSew();
                }
            }
            case 6826 -> shot("42_sc_sewing");
            case 6840 -> run("tick rate 20");
            case 6880 -> shot("43_sc_sewn");
            case 6960 -> {
                mc.player.closeContainer();
                run("gamemode creative @p");
            }
            case 7000 -> {
                Constants.LOG.info("[CI] done");
                mc.stop();
            }
            default -> {
            }
        }
    }

    /** Opens the Suit Creator next to the player (server side, like right-clicking the block). */
    private static void openSuitCreator() {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(760, -60, -2);
            if (!player.level().getBlockState(pos).is(com.star_1mp.powersunchained.registry.PUBlocks.SUIT_CREATOR.get())) {
                // the setblock command may have run before the chunk was loaded
                player.level().setBlockAndUpdate(pos, com.star_1mp.powersunchained.registry.PUBlocks.SUIT_CREATOR.get().defaultBlockState());
            }
            Constants.LOG.info("[CI] opening the Suit Creator at {} (player at {})", pos, player.position());
            player.openMenu(player.level().getBlockState(pos).getMenuProvider(player.level(), pos));
        });
    }

    /** Sets up the open Suit Creator screen: tab, suit, blueprint piece, tags popup, pose, Alex. */
    private static void creator(int tab, String suit, int piece, boolean tags, int pose, boolean slim) {
        if (Minecraft.getInstance().gui.screen() instanceof com.star_1mp.powersunchained.client.suitcreator.SuitCreatorScreen screen) {
            screen.debugSetup(tab, suit, piece, tags, com.star_1mp.powersunchained.client.suitcreator.SuitHologram.Pose.values()[pose], slim);
        } else {
            Constants.LOG.warn("[CI] Suit Creator screen is not open: {}", Minecraft.getInstance().gui.screen());
        }
    }

    /** Puts a worn-out piece of a suit into the scanner slot (server side). */
    private static void scanSlot(String suit, int count) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            if (player.containerMenu instanceof com.star_1mp.powersunchained.menu.SuitCreatorMenu menu) {
                com.star_1mp.powersunchained.suit.Suit s = com.star_1mp.powersunchained.suit.Suits.byName(suit.substring(suit.indexOf(':') + 1));
                net.minecraft.world.item.ItemStack stack = com.star_1mp.powersunchained.registry.PUItems.piece(s,
                        net.minecraft.world.item.equipment.ArmorType.HELMET).get().getDefaultInstance();
                stack.setCount(count);
                stack.setDamageValue(stack.getMaxDamage() - 5);  // durability doesn't matter
                menu.getSlot(com.star_1mp.powersunchained.menu.SuitCreatorMenu.SCAN_SLOT).set(stack);
            }
        });
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

    /** Poses the mannequins (left to right) with {phase, value (ticks / percent), variant}. */
    private static void zipPoses(int[][] poses) {
        Minecraft mc = Minecraft.getInstance();
        java.util.List<net.minecraft.world.entity.Entity> mannequins = new java.util.ArrayList<>();
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath().equals("mannequin")) {
                mannequins.add(e);
            }
        }
        mannequins.sort(java.util.Comparator.comparingDouble(e -> -e.getX()));
        for (int i = 0; i < Math.min(poses.length, mannequins.size()); i++) {
            int[] p = poses[i];
            float value = p[0] == 2 || p[0] == 4 ? p[1] / 100.0F : p[1];
            net.minecraft.world.phys.Vec3 motion = p[0] == 3 ? new net.minecraft.world.phys.Vec3(0.3, -1.2, 0.0)
                    : p[0] == 5 ? new net.minecraft.world.phys.Vec3(0.8, p.length > 3 ? 0.5 : -0.5, 0.0)
                    : p[0] == 4 ? net.minecraft.world.phys.Vec3.ZERO : new net.minecraft.world.phys.Vec3(0.9, 0.3, 0.0);
            com.star_1mp.powersunchained.client.web.WebZipAnimation.debugPose(mannequins.get(i).getId(), p[0], value, p[2], motion);
        }
        Constants.LOG.info("[CI] posed {} mannequins", mannequins.size());
    }

    private static net.minecraft.world.phys.Vec3 zipStart = net.minecraft.world.phys.Vec3.ZERO;

    /** Spawns a web of the given type from the given wrist, bypassing the ammo (screenshot tests). */
    private static void rawWeb(com.star_1mp.powersunchained.web.WebType type, int side) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            player.level().addFreshEntity(type.create(player.level(), player, side));
            com.star_1mp.powersunchained.web.WebShooting.announceShot(player, side);
        });
    }

    /** Spawns an impact web from the given wrist, bypassing the ammo (screenshot tests). */
    private static void rawShot(int side) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            player.level().addFreshEntity(com.star_1mp.powersunchained.web.WebType.IMPACT.create(player.level(), player, side));
            com.star_1mp.powersunchained.web.WebShooting.announceShot(player, side);
        });
    }

    private static void shootWeb(com.star_1mp.powersunchained.web.WebType type) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            com.star_1mp.powersunchained.web.WebShooting.shoot(player, type);
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
