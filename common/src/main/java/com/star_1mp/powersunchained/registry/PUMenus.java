package com.star_1mp.powersunchained.registry;

import com.star_1mp.powersunchained.menu.SuitCreatorMenu;
import com.star_1mp.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;

/** Menu types of the mod. */
public final class PUMenus {

    public static final Supplier<MenuType<SuitCreatorMenu>> SUIT_CREATOR = Services.REGISTRY.registerMenu("suit_creator", SuitCreatorMenu::new);

    public static final Supplier<MenuType<com.star_1mp.powersunchained.menu.TechTableMenu>> TECH_TABLE =
            Services.REGISTRY.registerMenu("tech_table", com.star_1mp.powersunchained.menu.TechTableMenu::new);

    private PUMenus() {
    }

    public static void init() {
    }
}
