package com.star_1mp.powersunchained.irradiation;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** The Irradiator's numbers (owner's spec). */
public final class Irradiation {

    /** 150 seconds. */
    public static final int DURATION = 150 * 20;
    /** Cave spider -> Radioactive Spider (else it dies, vanilla loot). */
    public static final float SPIDER_CHANCE = 0.70F;
    /** A player survives (5 min of poison) or dies. */
    public static final float PLAYER_SURVIVE = 0.75F;
    /** Any other mob survives (5 min of poison) or dies. */
    public static final float MOB_SURVIVE = 0.70F;
    /** Items (spider eye -> radioactive spider eye), else the item burns up. */
    public static final float ITEM_CHANCE = 0.70F;
    public static final int SURVIVOR_POISON_TICKS = 5 * 60 * 20;

    public static final int KIND_NONE = 0;
    public static final int KIND_CAVE_SPIDER = 1;
    public static final int KIND_PLAYER = 2;
    public static final int KIND_MOB = 3;

    private Irradiation() {
    }

    public static int kind(LivingEntity entity) {
        if (entity.getType() == EntityTypes.CAVE_SPIDER) {
            return KIND_CAVE_SPIDER;
        }
        return entity instanceof Player ? KIND_PLAYER : KIND_MOB;
    }

    /** Success chance in percent for the screen / JEI. */
    public static int chancePercent(int kind) {
        return Math.round(100.0F * switch (kind) {
            case KIND_CAVE_SPIDER -> SPIDER_CHANCE;
            case KIND_PLAYER -> PLAYER_SURVIVE;
            case KIND_MOB -> MOB_SURVIVE;
            default -> ITEM_CHANCE;
        });
    }
}
