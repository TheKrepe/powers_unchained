package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.registry.PUDataComponents;
import com.star_1mp.powersunchained.web.WebCartridges;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The modules installed in a pair of web-shooters (data component {@code powers_unchained:web_modules}) and what they do. */
public final class WebModules {

    public static final int MAX_INSTALLED = 5;
    public static final float RANGE_BONUS = 1.25F;
    public static final float RECHARGE_FACTOR = 0.7F;
    public static final int EXTRA_CARTRIDGES = 2;

    private WebModules() {
    }

    public static List<WebModule> installed(ItemStack shooters) {
        List<WebModule> list = new ArrayList<>();
        for (String id : shooters.getOrDefault(PUDataComponents.WEB_MODULES.get(), List.<String>of())) {
            WebModule m = WebModule.byId(id);
            if (m != null && !list.contains(m)) {
                list.add(m);
            }
        }
        return list;
    }

    public static boolean has(ItemStack shooters, WebModule module) {
        return shooters.getOrDefault(PUDataComponents.WEB_MODULES.get(), List.<String>of()).contains(module.id());
    }

    public static void set(ItemStack shooters, List<WebModule> modules) {
        if (modules.isEmpty()) {
            shooters.remove(PUDataComponents.WEB_MODULES.get());
        } else {
            shooters.set(PUDataComponents.WEB_MODULES.get(), modules.stream().map(WebModule::id).toList());
        }
    }

    /** Range multiplier of these web-shooters (web shots, zip, swing). */
    public static float range(ItemStack shooters) {
        return has(shooters, WebModule.TIGHT_SPRING) ? RANGE_BONUS : 1.0F;
    }

    /** Recharge time multiplier (web shot recharge, zip cooldown). */
    public static float recharge(ItemStack shooters) {
        return has(shooters, WebModule.FAST_FEED) ? RECHARGE_FACTOR : 1.0F;
    }

    /** How many cartridges fit. */
    public static int cartridgeSlots(ItemStack shooters) {
        return WebCartridges.SLOTS + (has(shooters, WebModule.SECOND_DRUM) ? EXTRA_CARTRIDGES : 0);
    }

    /** Of the web-shooters the entity wears (1 without). */
    public static float range(Entity entity) {
        return entity instanceof Player player ? range(WebCartridges.wornShooters(player)) : 1.0F;
    }

    public static float recharge(Entity entity) {
        return entity instanceof Player player ? recharge(WebCartridges.wornShooters(player)) : 1.0F;
    }

    public static boolean wornHas(Entity entity, WebModule module) {
        return entity instanceof Player player && has(WebCartridges.wornShooters(player), module);
    }
}
