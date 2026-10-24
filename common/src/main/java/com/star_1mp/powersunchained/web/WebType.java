package com.star_1mp.powersunchained.web;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.entity.web.AbstractWebProjectile;
import com.star_1mp.powersunchained.entity.web.ImpactWeb;
import com.star_1mp.powersunchained.entity.web.StickyWeb;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Kinds of web the web-shooters can fire (order = order in the selection menu). Like Fisk's equipment, every type
 * has a few shots that recharge one by one.
 */
public enum WebType {
    /** Hard web ball that damages what it hits. */
    IMPACT("impact_web", 10, 40) {
        @Override
        public AbstractWebProjectile create(Level level, LivingEntity shooter, int side) {
            return new ImpactWeb(level, shooter, side);
        }
    },
    /** Sticky web that sticks to the target and slows it down, no damage. */
    STICKY("sticky_web", 10, 40) {
        @Override
        public AbstractWebProjectile create(Level level, LivingEntity shooter, int side) {
            return new StickyWeb(level, shooter, side);
        }
    };

    private final String name;
    private final int maxUses;
    private final int recharge;

    WebType(String name, int maxUses, int recharge) {
        this.name = name;
        this.maxUses = maxUses;
        this.recharge = recharge;
    }

    public abstract AbstractWebProjectile create(Level level, LivingEntity shooter, int side);

    public String getSerializedName() {
        return this.name;
    }

    /** Shots available when fully loaded. */
    public int maxUses() {
        return this.maxUses;
    }

    /** Ticks to get one shot back. */
    public int recharge() {
        return this.recharge;
    }

    public Component displayName() {
        return Component.translatable("web_type." + Constants.MOD_ID + "." + this.name);
    }

    /** Menu icon (32x32). */
    public Identifier icon() {
        return Constants.id("textures/gui/web/" + this.name + ".png");
    }

    public static WebType byId(int id) {
        WebType[] values = values();
        return values[Math.floorMod(id, values.length)];
    }
}
