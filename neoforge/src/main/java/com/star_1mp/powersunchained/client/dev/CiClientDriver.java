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
                run("give @p powers_unchained:white_fabric 2");
                run("give @p minecraft:tinted_glass 1");
                run("give @p minecraft:iron_ingot 1");
            }
            case 5800 -> openSuitCreator();
            case 5830 -> {
                com.star_1mp.powersunchained.client.suitcreator.SuitCreatorPrefs.FAVORITES.add("spider_man_raimi");
                creator(0, "spider_man", 4, false, 0, false);
            }
            case 5870 -> shot("34_sc_catalog");
            case 5900 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.suitcreator.SuitCreatorScreen screen) {
                    screen.debugScrollMaterials(-3.0);  // wheel down over the materials
                }
            }
            case 5925 -> shot("34b_sc_materials_scrolled");
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
            // ---- superpowers ----
            case 7040 -> {
                run("kill @e[type=!player]");
                run("time set 6000");
                run("gamemode creative @p");
                run("clear @p");
                run("superpower add spiderman @p");
                run("tp @p 800.5 -60 0.5 180 0");
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
            case 7060 -> {
                // after the teleport: the chunks are loaded now
                run("fill 798 -60 -3 802 -56 -3 minecraft:stone_bricks");
                run("fill 818 -57 -4 824 -57 2 minecraft:stone_bricks");
                run("item replace entity @p armor.head with powers_unchained:spider_man_mask");
                run("item replace entity @p armor.chest with powers_unchained:spider_man_chestplate");
                run("item replace entity @p armor.legs with powers_unchained:spider_man_pants");
                run("item replace entity @p armor.feet with powers_unchained:spider_man_boots");
            }
            case 7080 -> {
                Constants.LOG.info("[CI] powers: {}", com.star_1mp.powersunchained.power.Superpowers.data(mc.player));
                com.star_1mp.powersunchained.client.power.AbilityWheel.debugOpen(true, 0);
            }
            case 7110 -> shot("44_ability_wheel");
            case 7150 -> com.star_1mp.powersunchained.client.power.AbilityWheel.toggle(com.star_1mp.powersunchained.power.PowerAbility.NIGHT_VISION);
            case 7155 -> com.star_1mp.powersunchained.client.power.AbilityWheel.debugOpen(true, 1);
            case 7166 -> shot("44b_ability_wheel_night_vision_on");
            case 7194 -> {
                com.star_1mp.powersunchained.client.power.AbilityWheel.debugOpen(false, -1);
                run("tp @p 800.5 -60 -1.69 180 10");
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                run("tick rate 5");  // slow motion for the whole climbing part (screenshots lag ~1 s)
            }
            // walk into the wall: clings after a few ticks, W climbs up
            case 7200 -> mc.options.keyUp.setDown(true);
            case 7212 -> climbLog("pressing into the wall");
            case 7214 -> shot("45_climb_wall_up");
            case 7218 -> {
                mc.player.setYRot(-90.0F);  // look along the wall: W crawls sideways
                mc.player.setXRot(10.0F);
            }
            case 7224 -> shot("46_climb_wall_sideways");
            case 7226 -> climbLog("sideways");
            // round the wall's end (outer corner), the camera turning along
            case 7244 -> shot("47_climb_outer_corner");
            case 7246 -> climbLog("outer corner");
            case 7256 -> shot("47b_climb_around");
            case 7258 -> climbLog("around");
            case 7262 -> mc.options.keyUp.setDown(false);
            // up to the top: over it (mantle)
            case 7266 -> run("tp @p 800.5 -58 -1.69 180 -30");
            case 7270 -> com.star_1mp.powersunchained.client.power.WallClimbClient.debugAttach(mc.player,
                    com.star_1mp.powersunchained.power.WallClimbing.MODE_WALL, net.minecraft.core.Direction.NORTH);
            case 7271 -> {
                // look away from the wall, the camera in front: the climber's back, head up; S climbs up
                mc.player.setYRot(0.0F);
                mc.player.setXRot(20.0F);
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                mc.options.keyDown.setDown(true);
            }
            case 7276 -> shot("47c_climb_up_from_behind");
            case 7284 -> climbLog("near the top");
            case 7286 -> shot("48_climb_mantle");
            case 7300 -> {
                climbLog("mantle");
                mc.options.keyDown.setDown(false);
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            case 7310 -> climbLog("after the mantle");
            // ceiling: cling, walk along it, over its edge onto the outer face, up and over the top
            case 7336 -> {
                run("gamemode survival @p");
                run("tp @p 821.5 -60 -1 -90 10");
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            // jump holding jump: clings to the ceiling (3 blocks up), W walks along it
            case 7340 -> mc.options.keyJump.setDown(true);
            case 7346 -> {
                climbLog("jumped to the ceiling");
                mc.options.keyUp.setDown(true);
            }
            case 7349 -> mc.options.keyJump.setDown(false);
            case 7352 -> climbLog("ceiling");
            case 7354 -> shot("49_climb_ceiling");
            case 7370 -> climbLog("ceiling edge");
            case 7372 -> shot("49b_climb_ceiling_edge");
            case 7380 -> shot("49c_climb_ceiling_edge");
            case 7384 -> climbLog("outer face");
            case 7400 -> climbLog("over the platform");
            case 7402 -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            case 7406 -> shot("49d_climb_ceiling_front");
            case 7410 -> {
                mc.options.keyUp.setDown(false);
                climbLog("end");
                run("tick rate 20");
            }
            // dodge roll (strafe left: seen from the side) and an air flip
            case 7440 -> {
                run("tp @p 840.5 -60 0.5 180 15");
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            case 7450 -> {
                mc.options.keyLeft.setDown(true);
                run("tick rate 4");
            }
            case 7452 -> rollKey();
            case 7454 -> {
                rollLog();
                com.star_1mp.powersunchained.client.power.RollAnimation.debugHold(0.3F);
            }
            case 7456 -> shot("50_roll");
            case 7466 -> com.star_1mp.powersunchained.client.power.RollAnimation.debugHold(0.55F);
            case 7468 -> shot("50b_roll");
            case 7478 -> {
                com.star_1mp.powersunchained.client.power.RollAnimation.debugHold(-1.0F);
                mc.options.keyLeft.setDown(false);
                run("tick rate 20");
            }
            case 7470 -> Constants.LOG.info("[CI] after the roll: {}", mc.player.position());
            case 7484 -> mc.options.keyJump.setDown(true);
            case 7486 -> {
                mc.options.keyJump.setDown(false);
                run("tick rate 4");
            }
            case 7490 -> rollKey();
            case 7492 -> {
                rollLog();
                com.star_1mp.powersunchained.client.power.RollAnimation.debugHold(0.35F);
            }
            case 7494 -> shot("51_air_flip");
            case 7504 -> com.star_1mp.powersunchained.client.power.RollAnimation.debugHold(0.6F);
            case 7506 -> shot("51b_air_flip");
            case 7516 -> {
                com.star_1mp.powersunchained.client.power.RollAnimation.debugHold(-1.0F);
                run("tick rate 20");
            }
            // spider-sense: a zombie close by
            case 7530 -> {
                run("difficulty easy");
                run("gamemode survival @p");
                run("tp @p 860.5 -60 0.5 180 5");
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
            case 7540 -> run("summon minecraft:zombie 861.5 -60 -2.5");
            case 7544 -> {
                com.star_1mp.powersunchained.client.power.SpiderSense.debugForce(true);
                run("tick rate 3");
            }
            case 7546 -> shot("52_spider_sense");
            case 7547 -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            case 7549 -> com.star_1mp.powersunchained.client.power.SpiderSenseLayer.burst(mc.player.getId());
            case 7551 -> shot("52b_spider_sense_bolts");
            case 7558 -> {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                com.star_1mp.powersunchained.client.power.SpiderSense.debugForce(false);
                run("tick rate 20");
            }
            // climbing poses on mannequins: wall (head up), wall (crawling sideways), ceiling
            case 7560 -> {
                run("kill @e[type=!player]");
                run("gamemode creative @p");
                run("tp @p 880.5 -58.5 5.8 172 8");
            }
            case 7570 -> {
                run("fill 873 -60 -3 884 -56 -3 minecraft:stone_bricks");
                run("fill 886 -57 -5 889 -57 -1 minecraft:stone_bricks");
                summonAt(874.4, -59.0, -1.69, "{NoGravity:1b,immovable:1b,Rotation:[90f,0f]," + WIDE + "," + fullSuit("spider_man") + "}");
                summonAt(876.5, -59.0, -1.69, "{NoGravity:1b,immovable:1b,Rotation:[0f,0f]," + WIDE + "," + fullSuit("spider_man") + "}");
                summonAt(878.6, -59.0, -1.69, "{NoGravity:1b,immovable:1b," + WIDE + "," + fullSuit("spider_man") + "}");
                summonAt(880.5, -59.0, -1.69, "{NoGravity:1b,immovable:1b," + WIDE + "," + fullSuit("spider_man") + "}");
                summonAt(883.0, -59.0, -1.69, "{NoGravity:1b,immovable:1b," + WIDE + "," + fullSuit("spider_man") + "}");
                summonAt(887.5, -58.8, -3.0, "{NoGravity:1b,immovable:1b," + WIDE + "," + fullSuit("spider_man") + "}");
                // upside down on the web (rappel pose)
                summonAt(893.5, -58.0, -2.5, "{NoGravity:1b,immovable:1b,pose:\"crouching\",Rotation:[0f,0f]," + WIDE + "," + fullSuit("spider_man") + "}");
            }
            case 7580 -> {
                java.util.List<net.minecraft.world.entity.Entity> mannequins = new java.util.ArrayList<>();
                for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
                    if (net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath().equals("mannequin")) {
                        mannequins.add(e);
                    }
                }
                mannequins.sort(java.util.Comparator.comparingDouble(net.minecraft.world.entity.Entity::getX));
                if (!mannequins.isEmpty() && mannequins.getLast().getX() > 892.0) {
                    com.star_1mp.powersunchained.client.web.WebRappel.set(mannequins.removeLast().getId(), true);
                }
                Constants.LOG.info("[CI] climbing mannequins: {}", mannequins.size());
                net.minecraft.core.Direction north = net.minecraft.core.Direction.NORTH;
                int wall = com.star_1mp.powersunchained.power.WallClimbing.MODE_WALL;
                if (mannequins.size() >= 6) {
                    // looking along the wall, still: perched side-on
                    com.star_1mp.powersunchained.client.power.ClimbState.debugSet(mannequins.remove(0).getId(), wall, north, null);
                    // looking away from the wall (no fixed heading): back to it
                    com.star_1mp.powersunchained.client.power.ClimbState.debugSet(mannequins.get(0).getId(), wall, north, null);
                    com.star_1mp.powersunchained.client.power.ClimbState.debugSet(mannequins.get(1).getId(),
                            com.star_1mp.powersunchained.power.WallClimbing.MODE_MANTLE, north, new net.minecraft.world.phys.Vec3(0, 1, 0));
                    com.star_1mp.powersunchained.client.power.ClimbState.debugSet(mannequins.get(2).getId(), wall, north,
                            new net.minecraft.world.phys.Vec3(0, 1, 0));
                    com.star_1mp.powersunchained.client.power.ClimbState.debugSet(mannequins.get(3).getId(), wall, north,
                            new net.minecraft.world.phys.Vec3(1, 0, 0));
                    com.star_1mp.powersunchained.client.power.ClimbState.debugSet(mannequins.get(4).getId(),
                            com.star_1mp.powersunchained.power.WallClimbing.MODE_CEILING, north, new net.minecraft.world.phys.Vec3(1, 0, 0));
                }
            }
            case 7596 -> {
                for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
                    if (net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath().equals("mannequin")) {
                        com.star_1mp.powersunchained.client.power.SpiderSenseLayer.burst(e.getId());
                    }
                }
                run("tick rate 4");
            }
            case 7598 -> shot("54_climb_poses");
            case 7606 -> run("tick rate 20");
            case 7608 -> {
                run("tp @p 872.0 -58.6 0.6 235 6");  // close-up of the side perch, from the front
                run("tick rate 4");
            }
            case 7612 -> shot("54c_perch_close");
            case 7618 -> run("tick rate 20");
            case 7620 -> run("tp @p 892.5 -59 1.5 120 8");
            case 7630 -> shot("54b_climb_ceiling_pose");
            case 7640 -> {
                run("tp @p 893.5 -58.3 2.2 180 0");
                run("tick rate 4");
            }
            case 7644 -> shot("54d_web_rappel_pose");
            case 7652 -> run("tick rate 20");
            case 7670 -> {
                run("kill @e[type=!player]");
                run("time set 18000");
                run("tp @p 860.5 -60 0.5 180 5");
            }
            case 7700 -> shot("53_night_vision");
            // ability keys: Spider-Man alone (roll 1, wheel 2), then with web-shooters (1-3) + Spider-Man (4-5)
            case 7760 -> {
                run("time set 6000");
                run("tp @p 860.5 -60 0.5 180 5");
            }
            case 7790 -> shot("55_hud_spider_keys");
            case 7800 -> run("curios replace bracelet 0 @p with powers_unchained:web_shooters[powers_unchained:web_cartridges=[32,20]]");
            case 7840 -> {
                Constants.LOG.info("[CI] ability keys: shot {} roll {} wheel {}",
                        keyName(com.star_1mp.powersunchained.client.ability.AbilitySlots.WEB_SHOT),
                        keyName(com.star_1mp.powersunchained.client.ability.AbilitySlots.ROLL),
                        keyName(com.star_1mp.powersunchained.client.ability.AbilitySlots.ABILITY_WHEEL));
                shot("56_hud_web_shooters_and_spider");
            }
            case 7868 -> mc.gui.setScreen(new com.star_1mp.powersunchained.client.ability.ModSettingsScreen(null));
            case 7870 -> shot("57a_mod_settings");
            case 7900 -> {
                mc.gui.setScreen(new com.star_1mp.powersunchained.client.ability.AbilitySettingsScreen(null));
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.ability.AbilitySettingsScreen screen) {
                    // own set: roll on key 1, web shot on key 3, nothing on key 5; then pick key 2
                    screen.debugChoose(0, com.star_1mp.powersunchained.client.ability.AbilitySlots.ROLL);
                    screen.debugChoose(2, com.star_1mp.powersunchained.client.ability.AbilitySlots.WEB_SHOT);
                    screen.debugChoose(4, com.star_1mp.powersunchained.client.ability.AbilitySlots.NONE);
                    screen.debugSelect(1);
                }
            }
            case 7901 -> run("tick rate 2");
            case 7902 -> shot("57_ability_settings");
            case 7905 -> run("tick rate 20");
            case 7935 -> mc.gui.setScreen(null);
            case 7945 -> shot("57b_hud_own_set");
            case 7975 -> {
                com.star_1mp.powersunchained.client.ability.AbilitySlots.resetChoices();
                mc.gui.setScreen(null);
            }
            // a 1x1 pillar: crawling round it, the body wraps round every edge
            case 7980 -> {
                run("gamemode survival @p");
                run("tp @p 900.5 -60 -1.69 180 10");
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            case 7990 -> run("fill 900 -60 -3 900 -54 -3 minecraft:stone_bricks");
            case 8000 -> {
                run("tp @p 900.5 -59 -1.69 180 10");
                run("tick rate 5");
            }
            case 8004 -> com.star_1mp.powersunchained.client.power.WallClimbClient.debugAttach(mc.player,
                    com.star_1mp.powersunchained.power.WallClimbing.MODE_WALL, net.minecraft.core.Direction.NORTH);
            case 8006 -> {
                mc.player.setYRot(-90.0F);
                mc.player.setXRot(0.0F);
                mc.options.keyUp.setDown(true);
            }
            case 8012 -> shot("58_pillar_edge");
            case 8020 -> climbLog("pillar 1");
            case 8022 -> shot("58b_pillar_round");
            case 8034 -> climbLog("pillar 2");
            case 8036 -> shot("58c_pillar_round");
            case 8050 -> climbLog("pillar 3");
            case 8052 -> shot("58d_pillar_round");
            case 8064 -> {
                mc.options.keyUp.setDown(false);
                climbLog("pillar end");
            }
            case 8066 -> rollKey();  // a flip off the pillar
            case 8069 -> shot("59_wall_flip");
            case 8072 -> {
                rollLog();
                climbLog("after the flip");
            }
            case 8080 -> run("tick rate 20");
            // recipe viewers: the Suit Creator's blueprints in JEI / REI (whichever the CI client runs)
            case 8100 -> {
                run("gamemode creative @p");
                run("suit unlock spider_man @p");
            }
            case 8110 -> recipeViewer(null);
            case 8111 -> run("tick rate 2");
            case 8130 -> shot("60_recipe_viewer_suit_creator");
            case 8135 -> run("tick rate 20");
            case 8140 -> {
                mc.gui.setScreen(null);
                recipeViewer(com.star_1mp.powersunchained.registry.PUItems.piece(com.star_1mp.powersunchained.suit.Suits.SPIDER_MAN_RAIMI,
                        net.minecraft.world.item.equipment.ArmorType.HELMET).get().getDefaultInstance());
            }
            case 8141 -> run("tick rate 2");
            case 8160 -> shot("61_recipe_viewer_raimi_mask");
            case 8165 -> run("tick rate 20");
            case 8180 -> mc.gui.setScreen(null);
            case 8185 -> {
                try {
                    java.nio.file.Path f = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("jei/recipe-category-sort-order.ini");
                    if (java.nio.file.Files.exists(f)) {
                        Constants.LOG.info("[CI] JEI category order: {}", java.nio.file.Files.readAllLines(f).subList(0, Math.min(6,
                                java.nio.file.Files.readAllLines(f).size())));
                    }
                } catch (java.io.IOException e) {
                    Constants.LOG.warn("[CI] could not read JEI's order", e);
                }
            }
            // Tech Table: craft web-shooters, unlock + install modules, the look hologram; then the Electro-Web
            case 8200 -> {
                run("gamemode survival @p");
                run("clear @p");
                run("tp @p 770.5 -60 0.5 180 30");
                for (String item : new String[]{"iron_ingot 64", "dispenser 2", "sticky_piston 2", "lever 2", "glass 16",
                        "copper_ingot 64", "redstone 64", "gold_ingot 16", "glowstone_dust 32", "amethyst_shard 8", "slime_ball 16",
                        "powers_unchained:empty_web_cartridge 4", "white_dye 4", "red_dye 4"}) {
                    run("give @p minecraft:" + item.replace("minecraft:powers_unchained:", "powers_unchained:"));
                }
                run("give @p powers_unchained:empty_web_cartridge 4");
            }
            case 8210 -> openTechTable();
            case 8220 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    screen.debugTab(com.star_1mp.powersunchained.menu.TechTableMenu.TAB_CREATE);
                    screen.debugSelectRecipe("web_shooters");
                }
            }
            case 8225 -> run("tick rate 2");
            case 8226 -> shot("62_tech_table_create");
            case 8238 -> run("tick rate 20");
            case 8243 -> logTechTable("before craft");
            case 8245 -> serverQuickMove(com.star_1mp.powersunchained.menu.TechTableMenu.RESULT_SLOT);
            case 8248 -> logTechTable("after craft");
            case 8253 -> logTechTable("after moving the web-shooters");
            case 8249 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    screen.debugTab(com.star_1mp.powersunchained.menu.TechTableMenu.TAB_UPGRADE);
                }
            }
            case 8251 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    for (int i = 0; i < 36; i++) {
                        if (mc.player.containerMenu.slots.get(i).getItem().is(com.star_1mp.powersunchained.registry.PUItems.WEB_SHOOTERS.get())) {
                            int slot = i;
                            net.minecraft.server.level.ServerPlayer sp = serverPlayer();
                            mc.getSingleplayerServer().execute(() -> {
                                if (sp.containerMenu instanceof com.star_1mp.powersunchained.menu.TechTableMenu menu) {
                                    menu.tab = com.star_1mp.powersunchained.menu.TechTableMenu.TAB_UPGRADE;
                                }
                            });
                            serverQuickMove(slot);
                            break;
                        }
                    }
                }
            }
            case 8255 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    screen.debugButton(com.star_1mp.powersunchained.menu.TechTableMenu.UNLOCK_MODULE + com.star_1mp.powersunchained.tech.WebModule.TIGHT_SPRING.ordinal());
                    screen.debugButton(com.star_1mp.powersunchained.menu.TechTableMenu.UNLOCK_MODULE + com.star_1mp.powersunchained.tech.WebModule.ELECTRO_WEB.ordinal());
                    screen.debugButton(com.star_1mp.powersunchained.menu.TechTableMenu.UNLOCK_MODULE + com.star_1mp.powersunchained.tech.WebModule.SECOND_DRUM.ordinal());
                }
            }
            case 8260 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    screen.debugButton(com.star_1mp.powersunchained.menu.TechTableMenu.INSTALL_MODULE + com.star_1mp.powersunchained.tech.WebModule.TIGHT_SPRING.ordinal());
                    screen.debugSelectModule(com.star_1mp.powersunchained.tech.WebModule.ELECTRO_WEB);
                }
            }
            case 8265 -> run("tick rate 2");
            case 8266 -> shot("63_tech_table_upgrade");
            case 8278 -> run("tick rate 20");
            case 8285 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    screen.debugButton(com.star_1mp.powersunchained.menu.TechTableMenu.INSTALL_MODULE + com.star_1mp.powersunchained.tech.WebModule.ELECTRO_WEB.ordinal());
                    screen.debugTab(com.star_1mp.powersunchained.menu.TechTableMenu.TAB_CUSTOMIZE);
                }
            }
            case 8290 -> run("tick rate 2");
            case 8291 -> shot("64_tech_table_customize");
            case 8303 -> run("tick rate 20");
            // owner: Customization only works next to / wired to a Suit Creator -> put one right next to the table
            case 8305 -> run("setblock 771 -60 -2 powers_unchained:suit_creator");
            case 8315 -> {
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    screen.debugHologram(true, true);
                    screen.debugButton(com.star_1mp.powersunchained.menu.TechTableMenu.UNLOCK_SKIN + 1);
                    screen.debugButton(com.star_1mp.powersunchained.menu.TechTableMenu.SELECT_SKIN + 1);
                    screen.debugSelectSkin(1);
                }
                Constants.LOG.info("[CI] tech table linked: {}", mc.player.containerMenu instanceof com.star_1mp.powersunchained.menu.TechTableMenu menu && menu.isLinked());
            }
            case 8318 -> run("tick rate 2");
            case 8319 -> shot("64b_tech_table_customize_linked");
            case 8331 -> run("tick rate 20");
            // no device: empty list / right panel, cog icon in the slot
            case 8335 -> {
                setTechDevice(net.minecraft.world.item.ItemStack.EMPTY);
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    screen.debugTab(com.star_1mp.powersunchained.menu.TechTableMenu.TAB_UPGRADE);
                }
            }
            case 8338 -> Constants.LOG.info("[CI] linked after taking the device out: {}",
                    mc.player.containerMenu instanceof com.star_1mp.powersunchained.menu.TechTableMenu menu && menu.isLinked());
            case 8340 -> run("tick rate 2");
            case 8341 -> shot("63b_tech_table_upgrade_empty");
            case 8353 -> run("tick rate 20");
            // creative web-shooters: every module
            case 8356 -> {
                setTechDevice(com.star_1mp.powersunchained.item.WebShootersItem.creative());
                if (mc.gui.screen() instanceof com.star_1mp.powersunchained.client.techtable.TechTableScreen screen) {
                    screen.debugSelectModule(com.star_1mp.powersunchained.tech.WebModule.FAST_FEED);
                }
            }
            case 8360 -> run("tick rate 2");
            case 8361 -> shot("63c_tech_table_upgrade_creative");
            case 8373 -> run("tick rate 20");
            case 8376 -> {
                Constants.LOG.info("[CI] creative modules: {}", com.star_1mp.powersunchained.tech.WebModules.installed(
                        com.star_1mp.powersunchained.item.WebShootersItem.creative()));
                run("techtable module lock all @p");
            }
            case 8378 -> {
                Constants.LOG.info("[CI] modules after lock all: {}", com.star_1mp.powersunchained.platform.Services.GAMEPLAY.techProgress(mc.player));
                run("techtable module unlock fast_feed @p");
            }
            case 8380 -> Constants.LOG.info("[CI] modules after unlock fast_feed: {}", com.star_1mp.powersunchained.platform.Services.GAMEPLAY.techProgress(mc.player));
            case 8382 -> {
                Constants.LOG.info("[CI] tech table device: {}", mc.player.containerMenu instanceof com.star_1mp.powersunchained.menu.TechTableMenu menu ? menu.device().getComponents() : "-");
                mc.gui.setScreen(null);
            }
            // a redstone line links a table to a Suit Creator (dust on the ground, a repeater), a gap breaks it
            case 8385 -> {
                net.minecraft.server.level.ServerPlayer sp = serverPlayer();
                mc.getSingleplayerServer().execute(() -> {
                    net.minecraft.world.level.Level level = sp.level();
                    net.minecraft.core.BlockPos table = new net.minecraft.core.BlockPos(800, -60, 10);
                    level.setBlockAndUpdate(table, com.star_1mp.powersunchained.registry.PUBlocks.TECH_TABLE.get().defaultBlockState());
                    for (int x = 801; x <= 806; x++) {
                        level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x, -60, 10), x == 803
                                ? net.minecraft.world.level.block.Blocks.REPEATER.defaultBlockState()
                                : net.minecraft.world.level.block.Blocks.REDSTONE_WIRE.defaultBlockState());
                    }
                    level.setBlockAndUpdate(new net.minecraft.core.BlockPos(807, -60, 10), com.star_1mp.powersunchained.registry.PUBlocks.SUIT_CREATOR.get().defaultBlockState());
                    boolean wired = com.star_1mp.powersunchained.tech.TechLink.isLinked(level, table);
                    level.setBlockAndUpdate(new net.minecraft.core.BlockPos(805, -60, 10), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                    boolean broken = com.star_1mp.powersunchained.tech.TechLink.isLinked(level, table);
                    Constants.LOG.info("[CI] redstone link: wired {} broken {}", wired, broken);
                });
            }
            // Electro-Web at two zombies
            case 8400 -> {
                run("gamemode creative @p");
                run("clear @p");  // web shots need an empty main hand
                run("tp @p 790.5 -60 0.5 0 0");
                run("curios replace bracelet 0 @p with powers_unchained:web_shooters[powers_unchained:web_modules=[\"electro_web\",\"tight_spring\"],powers_unchained:web_cartridges=[128,128]]");
                run("difficulty easy");
                run("time set night");
                run("execute at @p run summon minecraft:zombie ~ ~ ~7 {NoAI:1b,Rotation:[180f,0f]}");
                run("execute at @p run summon minecraft:zombie ~2 ~ ~8 {NoAI:1b,Rotation:[180f,0f]}");
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
            case 8404 -> run("techtable module unlock fast_feed @p");
            case 8406 -> Constants.LOG.info("[CI] worn shooters after the command: {}",
                    com.star_1mp.powersunchained.web.WebCartridges.wornShooters(mc.player).get(com.star_1mp.powersunchained.registry.PUDataComponents.UNLOCKED_MODULES.get()));
            case 8410 -> {
                run("item replace entity @p weapon.mainhand with minecraft:air");  // web shots need an empty main hand
                com.star_1mp.powersunchained.client.web.WebShooterClient.debugState(true, false, com.star_1mp.powersunchained.web.WebType.ELECTRO);
            }
            case 8415 -> run("tick rate 2");
            case 8416 -> shootWeb(com.star_1mp.powersunchained.web.WebType.ELECTRO);
            case 8418 -> shot("65_electro_web");
            case 8426 -> {
                net.minecraft.server.level.ServerPlayer sp = serverPlayer();
                mc.getSingleplayerServer().execute(() -> {
                    for (net.minecraft.world.entity.Mob z : sp.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                            sp.getBoundingBox().inflate(12))) {
                        Constants.LOG.info("[CI] electro zombie hp {} effects {}", z.getHealth(), z.getActiveEffects());
                    }
                });
            }
            case 8430 -> run("tick rate 20");
            // Electro-Web condition: lightning while wearing web-shooters
            case 8432 -> {
                Constants.LOG.info("[CI] feats before lightning: {}", com.star_1mp.powersunchained.platform.Services.GAMEPLAY.techProgress(mc.player));
                run("execute at @p run summon minecraft:lightning_bolt ~ ~ ~");
            }
            case 8437 -> Constants.LOG.info("[CI] feats after lightning: {}", com.star_1mp.powersunchained.platform.Services.GAMEPLAY.techProgress(mc.player));
            // recipe viewers: the Tech Table category
            case 8440 -> {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                net.neoforged.fml.ModList mods = net.neoforged.fml.ModList.get();
                try {
                    if (mods.isLoaded("jei")) {
                        com.star_1mp.powersunchained.compat.jei.PUJeiPlugin.debugShowTechTable();
                    } else if (mods.isLoaded("roughlyenoughitems")) {
                        com.star_1mp.powersunchained.compat.rei.PUReiPlugin.debugShowTechTable();
                    }
                } catch (RuntimeException e) {
                    Constants.LOG.error("[CI] recipe viewer failed", e);
                }
            }
            case 8441 -> run("tick rate 2");
            case 8460 -> shot("66_recipe_viewer_tech_table");
            case 8465 -> run("tick rate 20");
            case 8470 -> {
                mc.gui.setScreen(null);
                Constants.LOG.info("[CI] done");
                mc.stop();
            }
            default -> {
            }
        }
    }

    /** Opens the recipe viewer the CI client runs (JEI or REI) on the Suit Creator, or on an item's recipes. */
    private static void recipeViewer(net.minecraft.world.item.@org.jspecify.annotations.Nullable ItemStack output) {
        net.neoforged.fml.ModList mods = net.neoforged.fml.ModList.get();
        Constants.LOG.info("[CI] recipe viewer: jei {} rei {}", mods.isLoaded("jei"), mods.isLoaded("roughlyenoughitems"));
        try {
            if (mods.isLoaded("jei")) {
                com.star_1mp.powersunchained.compat.jei.PUJeiPlugin.debugShow(output);
            } else if (mods.isLoaded("roughlyenoughitems")) {
                com.star_1mp.powersunchained.compat.rei.PUReiPlugin.debugShow();
            }
        } catch (RuntimeException e) {
            Constants.LOG.error("[CI] recipe viewer failed", e);
        }
    }

    private static void logTechTable(String when) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            if (player.containerMenu instanceof com.star_1mp.powersunchained.menu.TechTableMenu menu) {
                Constants.LOG.info("[CI] tech table {}: grid {} result {} device {} client tab {}", when, menu.gridItems(),
                        menu.slots.get(com.star_1mp.powersunchained.menu.TechTableMenu.RESULT_SLOT).getItem(), menu.device(), menu.tab);
            } else {
                Constants.LOG.info("[CI] tech table {}: menu {}", when, player.containerMenu);
            }
        });
    }

    /** Shift-click on a slot of the open menu, done by the server (synced back). */
    private static void serverQuickMove(int slot) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            player.containerMenu.quickMoveStack(player, slot);
            player.containerMenu.broadcastChanges();
        });
    }

    /** Puts a stack into the open Tech Table's device slot (server side). */
    private static void setTechDevice(net.minecraft.world.item.ItemStack stack) {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            if (player.containerMenu instanceof com.star_1mp.powersunchained.menu.TechTableMenu menu) {
                menu.getSlot(com.star_1mp.powersunchained.menu.TechTableMenu.DEVICE_SLOT).set(stack);
                menu.broadcastChanges();
            }
        });
    }

    /** Opens the Tech Table next to the player (server side, like right-clicking the block). */
    private static void openTechTable() {
        net.minecraft.server.level.ServerPlayer player = serverPlayer();
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(770, -60, -2);
            if (!player.level().getBlockState(pos).is(com.star_1mp.powersunchained.registry.PUBlocks.TECH_TABLE.get())) {
                player.level().setBlockAndUpdate(pos, com.star_1mp.powersunchained.registry.PUBlocks.TECH_TABLE.get().defaultBlockState());
            }
            player.openMenu(player.level().getBlockState(pos).getMenuProvider(player.level(), pos));
        });
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

    private static void summonAt(double x, double y, double z, String nbt) {
        run("summon minecraft:mannequin " + x + " " + y + " " + z + " " + nbt);
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

    private static String keyName(String ability) {
        net.minecraft.client.KeyMapping key = com.star_1mp.powersunchained.client.ability.AbilitySlots.key(ability);
        return key != null ? key.getName() : "-";
    }

    /** Presses the key the roll is on (ability key 1 without web-shooters). */
    private static void rollKey() {
        net.minecraft.client.KeyMapping key = com.star_1mp.powersunchained.client.ability.AbilitySlots.key(
                com.star_1mp.powersunchained.client.ability.AbilitySlots.ROLL);
        Constants.LOG.info("[CI] roll key: {}", key != null ? key.getName() : null);
        if (key != null) {
            net.minecraft.client.KeyMapping.click(key.getKey());
        }
    }

    private static void rollLog() {
        Minecraft mc = Minecraft.getInstance();
        Constants.LOG.info("[CI] roll: rolling {} animated {} on ground {} at {}", com.star_1mp.powersunchained.client.power.RollClient.isRolling(),
                com.star_1mp.powersunchained.client.power.RollAnimation.isRolling(mc.player.getId()), mc.player.onGround(), mc.player.position());
    }

    private static void climbLog(String what) {
        Minecraft mc = Minecraft.getInstance();
        Constants.LOG.info("[CI] climb {}: mode {} wall {} at {} yaw {}", what, com.star_1mp.powersunchained.client.power.WallClimbClient.mode(),
                com.star_1mp.powersunchained.client.power.WallClimbClient.wall(), mc.player.position(), mc.player.getYRot());
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
