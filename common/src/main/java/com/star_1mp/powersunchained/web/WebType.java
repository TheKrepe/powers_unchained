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
    },
    /** Electro-Web (Tech Table module): a charged impact web that shocks, stuns and arcs to mobs nearby (see ElectroWeb). */
    ELECTRO("electro_web", 5, 60) {
        @Override
        public AbstractWebProjectile create(Level level, LivingEntity shooter, int side) {
            return new com.star_1mp.powersunchained.entity.web.ElectroWeb(level, shooter, side);
        }

        @Override
        public boolean isAvailable(net.minecraft.world.entity.player.Player player) {
            return com.star_1mp.powersunchained.tech.WebModules.wornHas(player, com.star_1mp.powersunchained.tech.WebModule.ELECTRO_WEB);
        }

        @Override
        public int fluidCost() {
            return 2;
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

    /** Whether the player's web-shooters can fire this type (some need a module). */
    public boolean isAvailable(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    /** Portions of web fluid one shot uses. */
    public int fluidCost() {
        return 1;
    }

    /** The types the player can fire, in menu order. */
    public static java.util.List<WebType> available(net.minecraft.world.entity.player.@org.jspecify.annotations.Nullable Player player) {
        java.util.List<WebType> list = new java.util.ArrayList<>();
        for (WebType t : values()) {
            if (player == null ? t != ELECTRO : t.isAvailable(player)) {
                list.add(t);
            }
        }
        return list;
    }

    /** The next available type after {@code current} ({@code dir} = +1 / -1). */
    public static WebType next(net.minecraft.world.entity.player.@org.jspecify.annotations.Nullable Player player, WebType current, int dir) {
        java.util.List<WebType> list = available(player);
        int i = list.indexOf(current);
        return list.get(Math.floorMod((i < 0 ? 0 : i) + dir, list.size()));
    }

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
