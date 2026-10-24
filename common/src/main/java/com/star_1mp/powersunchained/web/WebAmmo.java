package com.star_1mp.powersunchained.web;

import net.minecraft.util.Mth;

/**
 * Shots left per web type, recharging one at a time (port of Fisk's equipment uses / cooldown). The server keeps
 * one per player to validate shots, the client one for the HUD; both run the same rules.
 */
public final class WebAmmo {

    private final int[] uses = new int[WebType.values().length];
    private final int[] cooldown = new int[WebType.values().length];
    private final int[] prevCooldown = new int[WebType.values().length];

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
                this.cooldown[i] = type.recharge();
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
            this.cooldown[i] = type.recharge();
        }
    }

    public int uses(WebType type) {
        return this.uses[type.ordinal()];
    }

    /** 0..1 progress of the shot that is recharging right now (1 = just started). */
    public float recharge(WebType type, float partialTick) {
        int i = type.ordinal();
        return Mth.lerp(partialTick, (float) this.prevCooldown[i], (float) this.cooldown[i]) / type.recharge();
    }
}
