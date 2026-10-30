package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.Constants;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Upgrade modules for the web-shooters (Tech Table, Upgrade tab). A module is unlocked once per player for
 * {@link #unlockCost} and installed into a pair of web-shooters for {@link #installCost} (every time); up to
 * {@link WebModules#MAX_INSTALLED} per web-shooters, each module at most once.
 */
public enum WebModule {
    /** Range +25%: web shots fly faster / further, web zip and swing ropes reach further. */
    TIGHT_SPRING("tight_spring",
            TechCost.of(sup(Items.IRON_INGOT), 6, sup(Items.COPPER_INGOT), 4, sup(Items.REDSTONE), 4),
            TechCost.of(sup(Items.IRON_INGOT), 1, sup(Items.COPPER_INGOT), 1)),
    /** Recharge -30%: web shots come back faster, web zip is ready sooner. */
    FAST_FEED("fast_feed",
            TechCost.of(sup(Items.REDSTONE), 8, sup(Items.GOLD_INGOT), 2, sup(Items.COPPER_INGOT), 4),
            TechCost.of(sup(Items.REDSTONE), 2, sup(Items.COPPER_INGOT), 1)),
    /** +2 cartridge slots (2 -> 4). */
    SECOND_DRUM("second_drum",
            TechCost.of(sup(Items.IRON_INGOT), 8, sup(Items.GLASS), 4, sup(Items.SLIME_BALL), 4),
            TechCost.of(sup(Items.IRON_INGOT), 2, sup(Items.GLASS), 1)),
    /** A new web type: Electro-Web (see {@code ElectroWeb}). */
    ELECTRO_WEB("electro_web",
            TechCost.of(sup(Items.COPPER_INGOT), 12, sup(Items.REDSTONE), 8, sup(Items.GLOWSTONE_DUST), 6, sup(Items.LIGHTNING_ROD), 1),
            TechCost.of(sup(Items.COPPER_INGOT), 2, sup(Items.GLOWSTONE_DUST), 2));

    private final String id;
    private final TechCost unlockCost;
    private final TechCost installCost;

    WebModule(String id, TechCost unlockCost, TechCost installCost) {
        this.id = id;
        this.unlockCost = unlockCost;
        this.installCost = installCost;
    }

    private static Supplier<Item> sup(Item item) {
        return () -> item;
    }

    public String id() {
        return this.id;
    }

    public TechCost unlockCost() {
        return this.unlockCost;
    }

    public TechCost installCost() {
        return this.installCost;
    }

    public Component displayName() {
        return Component.translatable("module." + Constants.MOD_ID + "." + this.id);
    }

    public Component description() {
        return Component.translatable("module." + Constants.MOD_ID + "." + this.id + ".desc");
    }

    /** GUI sprite (16x16). */
    public Identifier sprite() {
        return Constants.id("tech_table/module/" + this.id);
    }

    public static @Nullable WebModule byId(String id) {
        for (WebModule m : values()) {
            if (m.id.equals(id)) {
                return m;
            }
        }
        return null;
    }

    public static List<WebModule> all() {
        return List.of(values());
    }
}
