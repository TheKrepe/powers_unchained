package com.star_1mp.powersunleashed.web;

import net.minecraft.util.Mth;

/**
 * Shots left per web type, recharging one at a time (port of Fisk's equipment uses / cooldown). The server keeps
 * one per player to validate shots, the client one for the HUD; both run the same rules.
 */
public final class WebAmmo {

    private final int[] uses = new int[WebType.values().length];
    private final int[] cooldown = new int[WebType.values().length];
    private final int[] prevCooldown = new int[WebType.values().length];

    /** Recharge time multiplier of the web-shooters in use (Fast Feed module). */
    private float factor = 1.0F;

    public void setFactor(float factor) {
        this.factor = factor;
    }

    private int rechargeTicks(WebType type) {
        return Math.max(1, Math.round(type.recharge() * this.factor));
    }

    public WebAmmo() {
        for (WebType type : WebType.values()) {
            this.uses[type.ordinal()] = type.maxUses();
        }
    }

    public void tick() {
        for (WebType type : WebType.values()) {
            int i = type.ordinal();
            this.prevCooldown[i] = this.cooldown[i];
            if (this.cooldown[i] > 0 && this.uses[i] < type.maxUses() && --this.cooldown[i] == 0
                    && ++this.uses[i] < type.maxUses()) {
                this.cooldown[i] = this.rechargeTicks(type);
            }
        }
    }

    public boolean canUse(WebType type) {
        return this.uses[type.ordinal()] > 0;
    }

    public void use(WebType type) {
        int i = type.ordinal();
        this.uses[i]--;
        if (this.cooldown[i] == 0) {
            this.cooldown[i] = this.rechargeTicks(type);
        }
    }

    public int uses(WebType type) {
        return this.uses[type.ordinal()];
    }

    /** 0..1 progress of the shot that is recharging right now (1 = just started). */
    public float recharge(WebType type, float partialTick) {
        int i = type.ordinal();
        return Mth.lerp(partialTick, (float) this.prevCooldown[i], (float) this.cooldown[i]) / this.rechargeTicks(type);
    }
}
