package com.thekrepe.powersunchained.web;

import com.thekrepe.powersunchained.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Kinds of web the web-shooters can fire. Order = order in the selection menu. */
public enum WebType {
    /** Hard web ball that damages what it hits. */
    IMPACT("impact_web", 12, 4.0F),
    /** Sticky web that sticks to the target and slows it down, no damage. */
    STICKY("sticky_web", 20, 0.0F);

    private final String name;
    private final int cooldown;
    private final float damage;

    WebType(String name, int cooldown, float damage) {
        this.name = name;
        this.cooldown = cooldown;
        this.damage = damage;
    }

    public String getSerializedName() {
        return this.name;
    }

    /** Ticks before the web-shooters can fire again. */
    public int cooldown() {
        return this.cooldown;
    }

    public float damage() {
        return this.damage;
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
