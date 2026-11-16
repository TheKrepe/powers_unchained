package com.star_1mp.powersunleashed.registry;

import com.star_1mp.powersunleashed.menu.SuitCreatorMenu;
import com.star_1mp.powersunleashed.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;

/** Menu types of the mod. */
public final class PUMenus {

    public static final Supplier<MenuType<SuitCreatorMenu>> SUIT_CREATOR = Services.REGISTRY.registerMenu("suit_creator", SuitCreatorMenu::new);

    public static final Supplier<MenuType<com.star_1mp.powersunleashed.menu.TechTableMenu>> TECH_TABLE =
            Services.REGISTRY.registerMenu("tech_table", com.star_1mp.powersunleashed.menu.TechTableMenu::new);

    public static final Supplier<MenuType<com.star_1mp.powersunleashed.menu.IrradiatorMenu>> IRRADIATOR =
            Services.REGISTRY.registerMenu("irradiator", com.star_1mp.powersunleashed.menu.IrradiatorMenu::new);

    private PUMenus() {
    }

    public static void init() {
    }
}
